package com.sitech.crmpd.idmm.ble.store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Component;

import com.sitech.crmpd.idmm.ble.BLEConfig;
import com.sitech.crmpd.idmm.ble.MsgIndex;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;

/**
 * 启动的时候加载存储中的消息索引到内存中
 *
 * @author guanyf
 *
 */
// @Transactional
@Component("IndexRepositoryImpl")
public class IndexRepositoryImpl {
	private static final Logger log = LoggerFactory.getLogger(IndexRepositoryImpl.class);

	@Autowired
	protected JdbcTemplate jdbcIndex; // 索引库

	@Resource
	private IndexRowMapper indexRowMapper;

	@Value("${indextable.count}")
	private int indextablecount; // 索引分表的数量

	@Value("${ble.default.priority}")
	private int default_priority;

	@Value("${jdbc.driverClassName}")
	private String _driverclass;

	@Resource
	private BLEConfig blecfg;

	// 索引操作相关sql语句
	// @Resource
	// private String sqlQueryIndex; //查询索引分表数据
	@Resource
	private Map<String, String> create_table_sqls;

	@Resource
	protected Map<String, String> idx_op_sqls;

	public List<MsgIndex> load(String dst_cli_id, String dst_topic_id) {
		Object[] par = new Object[] { dst_cli_id, dst_topic_id };
		final LinkedList<MsgIndex> arr = new LinkedList<MsgIndex>();
		// 遍历所有分表， 获取索引数据
		final List<MsgIndex> ret = null;
		for (int i = 0; i < indextablecount; i++) {
			final String sql = idx_op_sqls.get("sqlQueryIndex").replace("%d", String.valueOf(i));
			try {
				loadOneTable(sql, par, i, arr);
			} catch (final org.springframework.jdbc.BadSqlGrammarException ex) {
				final String e = ex.getMessage().toLowerCase();
				if (e.indexOf(" exist") > 0) {
					// 貌似表不存在， 创建分表
					jdbcIndex.update(create_table_sqls.get("sqlCreateIndexTable").replace("%d",
							String.valueOf(i)));
					jdbcIndex.update(create_table_sqls.get("sqlCreateIndexTableHis").replace("%d",
							String.valueOf(i)));
					log.info("create index table for {}", i);
				} else {
					log.error("load index into memory failed", ex);
				}
				continue;
			}
		}
		Collections.sort(arr); // 按createTime排序

		// 加载ble不在线期间broker保存到 ble_not_found 表中的数据， 这个数据要在 temp_list 之前， 但在 上面数据的后面
		// DONE: 另外还需要处理的是加载完后及时删除， 但这个数据可能多个队列共享（表中只有目标主题，可能有多个订阅者）
		// DONE: 还有一个需要注意的是， 这部分数据是需要在加载期间入表的，需要单独标注出来
		final List<MsgIndex> arr1 = new LinkedList<MsgIndex>();
		final MyRowCallback r = new MyRowCallback(arr1);
		try {
			final String sql = idx_op_sqls.get("sqlQryBleNotFound");
			par = new Object[]{dst_topic_id};
			if(_driverclass.indexOf("mysql") >= 0) {
				int limit = 0;
				while (true) {
					r.rows = 0;
					jdbcIndex.query(sql + " limit " + limit + ",500", par, r);
					if (r.rows <= 0) {
						break;
					}
					limit += 500;
				}
			}else{
				jdbcIndex.query(sql, par, r);
			}
			Collections.sort(arr1); // 排序

			// 这里直接入索引表
			for (final MsgIndex mi : arr1) {
				addIndex(mi, dst_cli_id, dst_topic_id, "ble_not_found");
				blecfg.checkOrders((Message) mi.getData(), mi, dst_topic_id, dst_cli_id);  // 消费顺序的处理
			}
			log.warn("load ble_not_found,  topic:{}, client:{} count:{}", dst_topic_id, dst_cli_id,
					arr1.size());
		} catch (final org.springframework.jdbc.BadSqlGrammarException ex) {
			// 表不存在就建表
			jdbcIndex.update(create_table_sqls.get("ble_not_found"));
		}

		arr.addAll(arr1);

		return arr;
	}

	final class MyRowCallback implements RowCallbackHandler {
		protected int rows = 0;
		private List<MsgIndex> arr;

		MyRowCallback(List<MsgIndex> arr) {
			this.arr = arr;
		}

		private int findTableId(String msgid) {
			final int p1 = msgid.lastIndexOf("::");
			if (p1 <= 0) {
				return -1;
			}
			final int p2 = msgid.lastIndexOf("::", p1 - 1);
			if (p2 <= 0) {
				return -1;
			}
			try {
				return Integer.parseInt(msgid.substring(p2 + 2, p1));
			} catch (final Exception ex) {}
			return -1;
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			rows++;
			final MsgIndex mi = new MsgIndex();
			// select msg_id, dest_topic_id, properties, op_time from ble_not_found where
			// dest_topic_id=?
			mi.setMsgid(rs.getString(1));
			mi.setCreateTime(rs.getDate(4).getTime());
			mi.setTblid(findTableId(rs.getString(1)));

			final Message msg = Message.createSimple(rs.getString(3));

			String groupid = null;
			if (msg.existProperty(PropertyOption.GROUP)) {
				groupid = msg.getStringProperty(PropertyOption.GROUP);
			}
			int priority = default_priority;
			if (msg.existProperty(PropertyOption.PRIORITY)) {
				priority = msg.getIntegerProperty(PropertyOption.PRIORITY);
			}
			final String cli_id = msg.getStringProperty(PropertyOption.CLIENT_ID);
			String src_topic_id = null;
			if (msg.existProperty(PropertyOption.TOPIC)) {
				src_topic_id = msg.getStringProperty(PropertyOption.TOPIC);
			}
			long effective_time = 0L;
			if (msg.existProperty(PropertyOption.EFFECTIVE_TIME)) {
				effective_time = msg.getLongProperty(PropertyOption.EFFECTIVE_TIME);
				// 改为 负数
				effective_time = 0 - effective_time;
			}
			long expire_time = 0L;
			if (msg.existProperty(PropertyOption.EXPIRE_TIME)) {
				expire_time = msg.getLongProperty(PropertyOption.EXPIRE_TIME);
			}

			if (groupid == null) {
				groupid = "[groupid]" + priority;
			}
			mi.setGroupid(groupid);
			mi.setPriority(priority);
			mi.setProduceClient(cli_id);
			mi.setSrcTopic(src_topic_id);
			mi.setGetTime(effective_time);
			mi.setExpireTime(expire_time);

			mi.setData(msg); // 保存这个， 用于处理消费顺序的逻辑

			arr.add(mi);
		}
	}

	/**
	 * 因为mysql代理的一次查询记录数限制， 使用limit 来循环读取
	 *
	 * @param sql
	 * @param par
	 * @param tableid
	 * @param arr
	 */
	private void loadOneTable(String sql, Object[] par, int tableid, List<MsgIndex> arr) {
		List<MsgIndex> ret = null;
		if(_driverclass.indexOf("mysql") >= 0) {
			int limit = 0;
			while (true) {
				ret = jdbcIndex.query(sql + " limit " + limit + ",500", par, indexRowMapper);
				if (ret.size() == 0) {
					break;
				}
				limit += 500;
				for (final MsgIndex mi : ret) {
					mi.setTblid(tableid);
				}
				arr.addAll(ret);
			}
		}else{
			ret = jdbcIndex.query(sql, par, indexRowMapper);
			for (final MsgIndex mi : ret) {
				mi.setTblid(tableid);
			}
			arr.addAll(ret);
		}
	}

	private AtomicInteger qcount = new AtomicInteger(0);
	private ConcurrentHashMap<String, Integer> topics = new ConcurrentHashMap<String, Integer>();

	public void beginLoad(String dst_cli_id, String dst_topic_id) {
		final int c = qcount.incrementAndGet();
		topics.put(dst_topic_id, c);
	}

	public void finishLoad(String dst_cli_id, String dst_topic_id) {
		final int c = qcount.decrementAndGet();
		if (c == 0) { // 这个判断貌似不能完全保证过程的可靠
			final String sql = idx_op_sqls.get("sqlDelBleNotFound");
			for (final String topic : topics.keySet()) {
				final int ret = jdbcIndex.update(sql, new Object[] { topic });
				log.warn("delete ble_not_found, topic[{}] topic, rows:{}", topic, ret);
			}
		}
	}

	public void createTables() {
		// 尝试创建分表， 分历史表和错误表
		for (int i = 0; i < indextablecount; i++) {
			try {
				jdbcIndex.update(String.format(create_table_sqls.get("sqlCreateIndexTable"), i));
			} catch (final Throwable e) {}

			try {
				jdbcIndex.update(String.format(create_table_sqls.get("sqlCreateIndexTableHis"), i));
			} catch (final Throwable e) {}
		}
		try {
			jdbcIndex.update(create_table_sqls.get("sqlCreateIndexTableErr"));
		} catch (final Throwable e) {

		}
	}

	/**
	 * 从messageid 中拆出索引分表数字（分表由broker生成messageid时确定） 便于维护时查找数据
	 *
	 * @param msgid
	 * @return
	 */
	private int findTableId(String msgid){
		int p1 = msgid.lastIndexOf("::");
		if(p1 <= 0) return -1;
		int p2 = msgid.lastIndexOf("::", p1-1);
		if(p2 <=0) return -1;
		try{
			return Integer.parseInt(msgid.substring(p2+2, p1));
		}catch(Exception ex){
		}
		return -1;
	}

	public boolean addIndex(MsgIndex mi, String dst_cli_id, String dst_topic_id, String broker_id) {
		mi.setTblid(findTableId(mi.getMsgid()));
		if (mi.getTblid() == -1) {
			final int hash = mi.getMsgid().hashCode();
			final int tableid = Math.abs(hash) % indextablecount;
			mi.setTblid(tableid);
		}
		final String sql = String.format(idx_op_sqls.get("sqlInsertIndex"), mi.getTblid());

		// idmm_msg_id, dst_cli_id, dst_topic_id, group_id, priority,
		// consumer_resend, create_time, broker_id, req_time, commit_code,
		// commit_time, next_topic_id, next_client_id, produce_cli_id, src_topic_id,
		// commit_desc
		try {
			final int ret = jdbcIndex.update(sql, mi.getMsgid(), dst_cli_id, dst_topic_id,
					mi.getGroupid(), mi.getPriority(), mi.getRetry(), mi.getCreateTime(), "", 0,
					"", 0, mi.getNextTopic(), mi.getNextClient(), mi.getProduceClient(),
					mi.getSrcTopic(), mi.getCommitDesc()); // mi.getExpireTime());
			if (ret != 1) {
				log.error("addIndex failed , return {}, messageid: {}", ret, mi.getMsgid());
			}
			// log.debug("new index for {} {} {} return {}", mi.getMsgid(), dst_cli_id,
			// dst_topic_id, ret);
		} catch (final Throwable ex) {
			log.error("addIndex failed", ex);
			return false;
		}
		return true;
	}

	public boolean removeIndex(String msgid, int tableid, long create_time, String dst_cli_id,
			String dst_topic_id) {
		final String sql = String.format(idx_op_sqls.get("sqlDelIndex"), tableid);
		try {
			final int ret = jdbcIndex.update(sql, msgid, dst_cli_id, dst_topic_id, create_time);
			if (ret != 1) {
				log.error("removeIndex failed , return {}, messageid: {}", ret, msgid);
				return false;
			}
		} catch (final Throwable ex) {
			log.error("removeIndex failed", ex);
			return false;
		}
		return true;
	}

	public boolean insertHis(String msgid, int tableid, long create_time, String dst_cli_id,
			String dst_topic_id) {
		final String sql = String.format(idx_op_sqls.get("sqlInsertHis"), tableid, tableid);
		try {
			final int ret = jdbcIndex.update(sql, System.currentTimeMillis(), msgid, dst_cli_id,
					dst_topic_id, create_time);
			if (ret != 1) {
				log.error("insertHis failed , return {}, messageid: {}", ret, msgid);
				return false;
			}
		} catch (final Throwable ex) {
			log.error("insertHis failed", ex);
			return false;
		}
		return true;
	}

	public boolean insertErr(MsgIndex mi, String dst_cli_id, String dst_topic_id, String commit_code,
			String commit_desc) {
		final String sql = idx_op_sqls.get("sqlInsertErr");

		try {
			final int ret = jdbcIndex.update(sql, mi.getMsgid(), dst_cli_id, dst_topic_id,
					mi.getGroupid(), mi.getPriority(), mi.getRetry(), mi.getCreateTime(),
					mi.getBroker_id(), mi.getGetTime(), commit_code, System.currentTimeMillis(),
					mi.getNextTopic(), mi.getNextClient(), mi.getProduceClient(), mi.getSrcTopic(),
					commit_desc);
			if (ret != 1) {
				log.error("insertErr failed , return {}, messageid: {}", ret, mi.getMsgid());
				return false;
			}
		} catch (final Throwable ex) {
			log.error("insertErr failed", ex);
			return false;
		}
		return true;
	}

	// 回滚索引， 只是把锁定时间置0
	public boolean delayIndex(String msgid, int tableid, long create_time, String dst_cli_id,
			String dst_topic_id, String result_code, long delay) {
		try {
			final int ret = jdbcIndex.update(
					String.format(idx_op_sqls.get("sqlRollbackIndex"), tableid), delay, msgid,
					dst_cli_id, dst_topic_id, create_time);
			if (ret != 1) {
				log.error("delayIndex failed , return {}, messageid: {}", ret, msgid);
				return false;
			}
		} catch (final Throwable ex) {
			log.error("delayIndex failed", ex);
			return false;
		}
		return true;
	}

	public boolean commitIndex(String msgid, int tableid, long create_time, String dst_cli_id,
			String dst_topic_id) {
		final String sql = String.format(idx_op_sqls.get("sqlDelIndex"), tableid);
		try {
			final int ret = jdbcIndex.update(sql, msgid, dst_cli_id, dst_topic_id, create_time);
			if (ret != 1) {
				log.error("commitIndex failed , return {}, messageid: {}", ret, msgid);
				return false;
			}
		} catch (final Throwable ex) {
			log.error("commit index failed", ex);
			return false;
		}
		return true;
	}

	public boolean updateIndex(String msgid, int tableid, long create_time, String dst_cli_id,
			String dst_topic_id, String broker_id, long gettime, long retry) {
		final String sql = String.format(idx_op_sqls.get("sqlUpdateIndex"), tableid);

		// if(broker_id != null && broker_id.length() > 8)
		// broker_id = broker_id.substring(0, 8);
		try {
			final int ret = jdbcIndex.update(sql, gettime, retry, broker_id, msgid, dst_cli_id,
					dst_topic_id, create_time);
			if (ret != 1) {
				log.error("updateIndex failed , return {}, messageid: {}", ret, msgid);
				return false;
			}
		} catch (final Throwable ex) {
			log.error("updateIndex failed", ex);
			return false;
		}
		return true;
	}

	// @Resource
	// private String sqlInsertHisAll; // 索引记录全插历史表
	// @Resource
	// private String sqlDelIndexAll; //删除全部索引记录

	/* 队列消费空的情况下， 清理索引表数据 */
	public void clearIndexTables(String dst_cli_id, String dst_topic_id) {
		String sql = null;
		int ret;
		for (int tableid = 0; tableid < indextablecount; tableid++) {
			sql = String.format(idx_op_sqls.get("sqlInsertHisAll"), tableid, tableid);
			try {
				ret = jdbcIndex.update(sql, dst_cli_id, dst_topic_id);
				if (ret > 0) {
					log.warn("clean index table, client:{} topic: {} tableid: {} records: {}",
							new Object[] { dst_cli_id, dst_topic_id, tableid, ret });
				}
				sql = String.format(idx_op_sqls.get("sqlDelIndexAll"), tableid);
				ret = jdbcIndex.update(sql, dst_cli_id, dst_topic_id);
			} catch (final Throwable ex) {
				log.error("updateIndex failed", ex);
			}
		}
	}

	protected boolean updateCommitTime(String dst_cli_id, String dst_topic_id, JournalOP op) {
		final String sql = String.format(idx_op_sqls.get("sqlUpdateCommitTime"), op.tableid);
		try {
			int ret = -1;

			for (int i = 0; i < 1; i++) {
				ret = jdbcIndex.update(sql, System.currentTimeMillis(), op.msgid, dst_cli_id,
						dst_topic_id, op.create_time);
				if (ret > 0) {
					break;
				}
				log.error("updateIndex failed , return {}, messageid: {}", ret, op.msgid);
				// Thread.sleep(20L);
			}
		} catch (final Throwable ex) {
			log.error("updateCommitTime failed", ex);
			return false;
		}
		return true;
	}
}
