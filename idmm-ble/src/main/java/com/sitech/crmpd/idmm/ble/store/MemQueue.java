package com.sitech.crmpd.idmm.ble.store;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sitech.crmpd.idmm.ble.MsgIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by guanyf on 2016/4/26.
 *
 * 从 PrioQueue.java 中分拆出来， 只处理内存数据结构， 而 PrioQueue 则负责与netty db线程之间的交互

 * 排序规则说明：
 * 1. 消息必须要有优先级, 基本原则是：先进先出； 优先级大的先出； 同一groupid下的消息按最大优先级使用同一优先级
 * 2. 可选的groupid， 如果有groupid， 则按同一groupid下最大优先级处理， 并按进入队列的时间顺序先进先出，同一groupid下的消息，只能有一个在途
 * 3. 队列划分依据为 clientid + topicid, 一个队列只给一个clientid消费
 * 4. clientid对应配置上最大并行数量nmax， 当未确认消息的数量超过nmax时， 则不再提供新的消息
 * 5. 消费确认后才移除消息， 超时未确认则恢复为未发送状态， 重发次数+1


 * 启动时的异步恢复内存数据过程说明：
 * 1. 启动是 loading_flag 为true
 * 2.1 为每个queue启动一个线程从存储中恢复数据到内存
 * 2.2 get操作直接返回无消息
 * 2.3 add操作检查loading_flag == true 则在 synchronized(temp_list){...} 中把收到的消息保存到temp_list 中并保存都存储中
 * 3 恢复线程处理完存储中的数据后， 获取到 temp_list 的锁， 并把其中数据全部保存到优先队列中
 * 4 恢复线程修改 loading_flag = false
 */
public class MemQueue implements LoadCallback{
    private static final Logger log = LoggerFactory.getLogger(MemQueue.class);

    protected String dst_cli_id;
    protected String dst_topic_id;
    private String tags; //用于表示一个队列  dst_topic_id + "." + dst_cli_id
    private int maxretry = 10;
    private int errcount = 0;
    private long totalCount = 0;
    private int min_timeout = 6000;
    private int max_timeout = 60000;


    // 是否正在启动恢复过程中
    protected boolean loading_flag = true;
    private ArrayList<MsgIndex> temp_list = new ArrayList<MsgIndex>();

    //保存在途消息的groupid锁定， 确保同一groupid下消息的消费时间顺序, key为groupid
    private HashMap<String, Long> grouplocks = new HashMap<String, Long>();

    //按groupid 存放的消息体, 没有groupid的消息， 则使用[nogroup]+prio 作为groupid
    private HashMap<String, LinkedList<MsgIndex>> messages = new HashMap<String, LinkedList<MsgIndex>>();

    // 按msgid 保存的消息索引， 可以直接通过msgid找到对应的消息， 用于消费确认、取消、删除消息时使用
    private HashMap<String, MsgIndex> messageids = new HashMap<String, MsgIndex>();

    private HashMap<String, MsgIndex> lockingMessages = new HashMap<String, MsgIndex>();

    // groupid 锁定时间， 单位秒
    private int lockTimeout = 60 * 1000;

    private int nmax; //最大并发数

    private Store store = null;



    /////////////////////////////////////////////////
    /////////////////////////////////////////////////

    public long getTotalCount() { return totalCount; }

    public MemQueue(String dst_cli_id, String dst_topic_id, int nConcurrents){
        this.dst_cli_id = dst_cli_id;
        this.dst_topic_id = dst_topic_id;
        this.tags = dst_topic_id + "." + dst_cli_id;
        if(nConcurrents > 0)
            this.nmax = nConcurrents;
        else
            nmax = 10;
    }
    protected MemQueue() {}

    public void setStore(Store s) {
        store = s;
        if(s == null) return;
        try {
            store.restore(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMaxRetry(int r) { this.maxretry = r; }
    public void setLockTimeout(int t){
        this.lockTimeout = t;
    }

    public void setMaxSending(int nmax) {
        this.nmax = nmax;
    }
    public int   getMaxSending(){ return nmax; }
    public void setTimeouts(int min_timeout, int max_timeout) {
        this.min_timeout = min_timeout*1000;
        this.max_timeout = max_timeout*1000;
    }
    public int   getMinProcessTime() { return min_timeout / 1000; }
    public int   getMaxProcessTime() { return max_timeout / 1000; }


    //使用treeset来保存优先级
    private TreeSet<PrioItem> plist = new TreeSet<PrioItem>(new Comparator<PrioItem>(){
        @Override
        public int compare(PrioItem o1, PrioItem o2) {
            if(o1.prio > o2.prio)
                return 1;
            else if(o1.prio < o2.prio)
                return -1;
            return 0;
        }
    });

    // 优先级队列中的元素， 同一优先级下多个groupid
    static final class PrioItem extends LinkedList<String> {
        int prio;
        PrioItem(int p){
            prio = p;
        }
    }

    public String getTopic() {
        return dst_topic_id;
    }
    public String getClient() {
        return dst_cli_id;
    }

    private String errmsg = null;
    public String err(){
        return errmsg;
    }

    boolean delay(String msgid, long delay){
        if(loading_flag)
            return false;
        MsgIndex mi = null;

        mi = messageids.get(msgid);
        if(mi == null)
            return false;
        delay = System.currentTimeMillis()+delay*1000;
        mi.setGetTime(delay);

        return true;
    }


    protected MsgIndex mi(String msgid) {
        return messageids.get(msgid);
    }

    /**
     * 解除消息的锁定状态， 从两个锁定容器(组锁定， 消息锁定）中删除， 但不删除消息本身的
     * @param mi
     */
    final private void _removeLock(MsgIndex mi){
        String groupid = mi.getGroupid();
        String msgid = mi.getMsgid();
        if(lockingMessages.containsKey(msgid)){
            lockingMessages.remove(msgid);
        }
        if(!groupid.startsWith("[group]") && grouplocks.containsKey(groupid)){
            grouplocks.remove(groupid);
        }
    }


    //消费者取消息， 根据 gorupid 进行锁定
    JournalOP get(String brokerid, long process_time){
        errmsg = null;
        if(loading_flag){ // 启动恢复过程中不提供消费处理
            errmsg = "init loading...";
            return null;
        }
        if(process_time < min_timeout)
            process_time = min_timeout;
        else if(process_time > max_timeout)
            process_time = max_timeout;

        MsgIndex out_mi = null;
        long out_tm = 0L;

        long time_now = System.currentTimeMillis();

        if(plist.size() == 0){
            errmsg = "zero msg";
            return null;
        }
        if(process_time < 0L)
            process_time = lockTimeout;

        OUTER_LOOP:
        for(Iterator<PrioItem> iter = plist.descendingIterator(); iter.hasNext(); ){
            PrioItem pi = iter.next();
            if(pi.size() == 0){
                iter.remove();
                continue;
            }

            for(ListIterator<String> iter1=pi.listIterator(); iter1.hasNext();){
                String groupid = iter1.next();
                boolean hasGroup = !groupid.startsWith("[groupid]");

                // [groupid] 开头的没带group，则锁定时间不能在group上， 而是在每个消息上
                if(grouplocks.containsKey(groupid) && time_now < grouplocks.get(groupid) && hasGroup){
                    errmsg = "locking groupid:" +groupid;
                    continue; //如果有锁定， 则忽略, 锁定带有超时 lockTimeout, 超出这个时间则锁定失效
                }

                LinkedList<MsgIndex> lm = messages.get(groupid);
                if(lm == null || lm.size() == 0){
                    iter1.remove();
                    messages.remove(groupid);
                    errmsg = "empty group: " +groupid;
                    continue;
                }

                for(ListIterator<MsgIndex> iter2=lm.listIterator(); iter2.hasNext();){
                    MsgIndex om = iter2.next();
                    if(om.isMarkRemove()){
                        iter2.remove(); //lm.remove(j--); //先处理标记删除的
                        continue;
                    }
                    if(om.getRetry() >= maxretry){ // 删除超过重试次数的
                        //log.error("message {} too ,many retries, move to err, ", om.getMsgid());
                        errmsg = "message "+om.getMsgid()+" too ,many retries, move to err, ";
                        fail(om.getMsgid(), "11", "too many retries"); //这里的问题是进不去存储， 所以实际上放不到err 表中
                        if(store != null) {
                            JournalOP op = JournalOP.fail(om, "11", "too many retries");
                            setTblID(op, om);
                            store.put(op); //直接放到存储里了， 没法走存储操作线程， 通道一次只能传递一个存储操作
                        }
                        iter2.remove();
                        continue;
                    }
                    if(om.getExpireTime() > 0 && om.getExpireTime() < time_now){ //消息超出有效期的， 则直接删除
                        errmsg = "message "+om.getMsgid() + " expired";
                        log.warn("message {} expired {} ms", om.getMsgid(), time_now-om.getExpireTime());
                        fail(om.getMsgid(), "12", "expired");
                        if(store != null) {
                            JournalOP op = JournalOP.fail(om, "12", "expired");
                            setTblID(op, om);
                            store.put(op); //直接放到存储里了， 没法走存储操作线程， 通道一次只能传递一个存储操作
                        }
                        iter2.remove();
                        continue;
                    }
                    long getTime = om.getGetTime();
                    if(getTime == -1 ){ //////// 锁定消息
                        errmsg = "req_time is -1 "+om.getMsgid() + " hasGroup:"+hasGroup;
                        if(hasGroup)
                            break; //在同一组下面如果有锁定的话， 不会在 grouplocks 中出现。 这里用break忽略同组的后续消息
                        else
                            continue;
                    }
                    if(getTime > 0 && time_now < getTime){
                        errmsg = "msg: "+om.getMsgid()+" tblid: "+om.getTblid()+" is locking";
                        continue; // 已经取走并未超时
                    }

                    if(getTime < -1){ //这个消息设定了生效时间， 其值为 0-effective_time
                        if(-getTime > time_now)
                            continue; // 未到生效时间
                    }

                    if(lockingMessages.containsKey(om.getMsgid()))
                        _removeLock(om);
                    if(lockingMessages.size() >= nmax){
                        for(String mid: lockingMessages.keySet()){ //当前为非锁定的消息， 找一个锁定到期的消息并解开
                            MsgIndex mi1 = lockingMessages.get(mid);
                            if(mi1.getGetTime() <= time_now){
                                _removeLock(mi1);
                                mi1.setGetTime(0);
                                break;
                            }
                        }
                        if(lockingMessages.size() >= nmax){ //没有可解锁的到期消息， 返回空
                            errmsg = "exceed max on-road message,  sendt:"+lockingMessages.size() + " nmax:"+nmax;
                            return null;
                        }
                    }
                    getTime = time_now+process_time;
                    om.setGetTime(getTime); //设定消息取走时间
                    if(!groupid.startsWith("[groupid]"))
                        grouplocks.put(groupid, getTime); //添加groupid锁定
                    om.setRetry(om.getRetry()+1);
                    out_mi = om;
                    out_tm = getTime;
                    lockingMessages.put(out_mi.getMsgid(), out_mi);
                    break OUTER_LOOP;

                }
            }
        }

        if(out_mi != null){
            JournalOP op = JournalOP.get(out_mi.getMsgid(), out_tm, brokerid);
            op.maxwait = out_mi.getGetTime();
            op.retry = out_mi.getRetry();
            op.mi = out_mi;
            setTblID(op, out_mi);
            return op;
        }

        return null;
    }

    boolean ack(String messageid){
        if(loading_flag)
            return false;
        MsgIndex m = messageids.get(messageid);
        if(m == null){
            System.out.printf("=====msg %s not found, size: %d\n", messageid, messageids.size());
            return false;
        }
        return __ack(m);
    }

    private boolean __ack(MsgIndex m){
        String groupid = m.getGroupid();

        m.markRemove();

        //然后解除groupid的锁定
        if(!groupid.startsWith("[groupid]")){
            grouplocks.remove(groupid);
        }
        messageids.remove(m.getMsgid());
        _removeLock(m);
//        System.out.println("====why2");
        return true;
    }

    boolean rollback(String messageid, long maxwait){
        if(loading_flag)
            return false;
        MsgIndex m = null;

        m = messageids.get(messageid);
        if(m == null)
            return false;
        String groupid = m.getGroupid();
        if(!groupid.startsWith("[groupid]"))
            grouplocks.remove(groupid);

        _removeLock(m);

        m.setGetTime(maxwait);
        return true;
    }

    boolean fail(String messageid, String rcode, String desc){
        if(loading_flag)
            return false ;
        MsgIndex m = null;

        m = messageids.get(messageid);
        if(m == null)
            return false ;
        errcount ++;

        _removeLock(m);

        m.markRemove();
        messageids.remove(messageid);

        return true;
    }

    /**
     * 数据保存到持久存储中
     * @param op
     */
    public void setTblID(JournalOP op, MsgIndex mi) {
        if(mi != null){
            op.create_time = mi.getCreateTime();
            op.tableid = mi.getTblid();
        }
    }

    public boolean exists(String msgid) {
        return messageids.containsKey(msgid);
    }

    boolean delete(String messageid){
        if(loading_flag)
            return false;
        MsgIndex m = messageids.get(messageid);
        if(m == null)
            return false;
        m.markRemove();

        messageids.remove(m.getMsgid());
        _removeLock(m);
        return true;
    }

    /**
     * 启动时从存储中恢复内存的过程完成后， 调用本函数， 把过程中收到的消息再次加入队列
     */
    public void finishLoading(List<MsgIndex> arr){
        if(arr != null){
            for(MsgIndex m: arr){
                __add(m);
            }
//			log.info("queue T:{} C:{}  {} idx loaded from store", this.dst_topic_id, this.dst_cli_id, arr.size());
        }
        synchronized(temp_list){
            for(MsgIndex m: temp_list){
                __add(m);
            }
            temp_list.clear();
            loading_flag = false;
        }
//		log.info("queue T:{} C:{}  {} idx loaded", this.dst_topic_id, this.dst_cli_id, size());
    }

    /**
     *  生产者向队列中加入消息索引
     * @param o 消息索引
     * @return
     */
    boolean add(MsgIndex o){
        if(loading_flag){ //启动恢复过程中， 收到的消息先存到临时链表中
            synchronized(temp_list){
                if(loading_flag){ //得到锁后仍然要检查一次 loading_flag 的原因是避免在 finishLoading()之后进入
                    temp_list.add(o);
                    return true; //op; //在启动恢复数据期间， 仍然可以接受生产数据，也必须同步保存到存储中
                }
            }
        }
        return __add(o);
    }

    //	protected final synchronized List<MsgIndex> __add(MsgIndex o){
    protected final boolean __add(MsgIndex o){
        if(messageids.containsKey(o.getMsgid())){
            System.out.println("__add dulplicate:"+o.getMsgid());
            return false; //剔重
        }
        if(o.getGroupid() == null)
            o.setGroupid("[groupid]"+o.getPriority());

        long t2 = o.getGetTime();
        long t1 = System.currentTimeMillis();
        if( t2  > t1){
            lockingMessages.put(o.getMsgid(), o);
        }

        PrioItem pi1 = new PrioItem(o.getPriority());
        PrioItem pi = plist.ceiling(pi1);
        if(pi == null || pi.prio != pi1.prio){
            pi = pi1;
            plist.add(pi);
        }
        String groupid = o.getGroupid();
        if( !groupid.startsWith("[groupid]") && o.getGetTime() > 0){
            //恢复消息的锁定时间
            grouplocks.put(groupid, o.getGetTime());
        }

        if( !(pi.size() > 0 && groupid.equals(pi.getLast())))
            pi.add(groupid); //如果最后一个groupid 相同，则不加
        LinkedList<MsgIndex> lm = messages.get(groupid);
        if(lm == null){
            lm = new LinkedList<MsgIndex>();
            messages.put(groupid, lm);
        }
        messageids.put(o.getMsgid(), o);
        lm.add(o);
        totalCount ++;
        return true;
    }

    public int size(){
        return messageids.size();
    }
    public int errCount(){
        return errcount;
    }
    public int sending(){
        return lockingMessages.size();
    }


    public JSONObject lockdetail() {
        JSONObject jo = new JSONObject();
        long t1 = System.currentTimeMillis();

        JSONArray lg = new JSONArray();
        for (String groupid: grouplocks.keySet()){
            JSONObject jx = new JSONObject();
            jx.put("groupid", groupid);
            jx.put("locktime", grouplocks.get(groupid)-t1);
            lg.add(jx);
        }
        jo.put("groups", lg);

        JSONArray lm = new JSONArray();
        for(String msgid: lockingMessages.keySet()){
            JSONObject jx = new JSONObject();
            jx.put("msgid",	msgid);
            jx.put("locktime", lockingMessages.get(msgid).getGetTime()-t1);
            lm.add(jx);
        }
        jo.put("messages", lm);

        jo.put("sending", lockingMessages.size());
        jo.put("size", size());
        jo.put("total", totalCount);
        jo.put("status", loading_flag?"loading":"ready");
        return jo;
    }



    ///////////////////////////////////
    // for test only
    //////////////////////////////////
    static void base_test1() throws Exception{
        String topic_id = "topic";
        String cli_id   = "cli";
        MemQueue q = new MemQueue(cli_id,  topic_id,  20);
        q.loading_flag = false;

        //压力测试
        int COUNT = 2000000;
        int GroupCOUNT = 200;

//		int[] pp = new int[]{1, 2, 3, 4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20}; //优先级表
        int[] pp = new int[]{1};

        // 分组表
        String[] gp = new String[GroupCOUNT];
        int seed = 10000;
        for(int i=0; i<GroupCOUNT; i++){
            gp[i] = String.valueOf(seed+i);
        }

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        long S = System.currentTimeMillis();

        MsgIndex r = null;
        JournalOP j = null;

        long t1 = System.currentTimeMillis();
        int choice = 0;
        int cp = 0;
        int cc = 0;
        for(int i=0; i<COUNT; i++){
            choice = Math.abs(rand.nextInt(2));
            if(choice == 1){
                MsgIndex x = new MsgIndex();
                x.setMsgid(String.format("%d.%d", S, i));
                x.setPriority(pp[rand.nextInt(pp.length)]);
                x.setGroupid(gp[rand.nextInt(gp.length)]);
                x.setData(x.getGroupid() + "-" + x.getPriority());
                q.add(x);
                cp ++;
            }else{
                j = q.get("bk", 60000);
                if(j == null){
                    System.out.printf("==%s\n",  q.errmsg);
                    continue;
                }
                r = j.mi;
                q.ack(r.getMsgid());
                cc ++;
            }
        }

//		System.out.printf("size:%d\n", q.size());

//		int c1 = COUNT;
        long t2 = System.currentTimeMillis();

        //printStatus();
        System.out.printf("count:%d spd of produce:%f  time:%d\n", cp, cp*1000.0/(t2-t1), t2-t1);
        System.out.printf("count:%d spd of consume:%f  time:%d\n", cc, cc*1000.0/(t2-t1), t2-t1);
//
//		int c = 0;
//		while(true){
//			r = q.get("bk");
//			if(r == null){
//				System.out.printf("==%s\n",  q.errmsg);
//				break;
//			}
//			q.ack(r.getMsgid());
//			c++;
//		}
//		t2 = System.currentTimeMillis();
//		System.out.printf("count:%d spd:%f time:%d\n", c, c*1000.0/(t2-t1), t2-t1);
//
//		store.close();

        //log.debug("1 {}, 2 {}", pc1, pc2);
        //log.debug("3 {}, 4 {}", pc3, pc4);

    }

    static void base_test2() throws Exception{
        String topic_id = "topic";
        String cli_id   = "cli";
        MemQueue q = new MemQueue(cli_id,  topic_id,  20);
        q.loading_flag = false;

        long t1 = System.currentTimeMillis();
        for(int i=0; i<2000000; i++){
            MsgIndex mi = new MsgIndex();
            mi.setMsgid("msgid-"+i);
            mi.setGroupid("grp-"+i%10);
            mi.setPriority(i%20);
            q.add(mi);
        }

        int c = 0;
        while(true){
            JournalOP op = q.get("brk", 60000);
            if (op == null)
                break;
            c++;
            MsgIndex mi = op.mi;
            q.ack(mi.getMsgid());
        }
        long t2 = System.currentTimeMillis();
        System.out.printf("==%d  %d %d",  c, t2-t1,  c*1000/(t2-t1));
    }

    public static void main(String[] args) throws Exception{
        base_test2();
    }

}
