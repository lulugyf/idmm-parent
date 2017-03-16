package com.sitech.crmpd.idmm.ble.store;

import com.sitech.crmpd.idmm.ble.MsgIndex;

/*

void q.delete(messageid);
MsgIndex mi = q.rollback(messageid, 0L);
q.add(MsgIndex mi);//保存到内存
MsgIndex mi = q.ack(msgid);
q.rollback(msgid, 0);
q.fail(msgid, "00", commit_desc); //q.rollback(msgid);
q.delay(msgid, msg.getLongPropertyValue(PropertyOption.RETRY_AFTER));
mi = q.get(broker_id, process_time);
 */
final class OPReq {
	public static enum Type{
		ADD,
		GET,
		ACK,
		ROLLBACK,
		DELETE,
		FAIL,
		DELAY
	}
	
	volatile public MsgIndex mi;
	volatile public String msgid;
	volatile public long tm;
	volatile public String desc;
	volatile public String brokerid;
	volatile public Type tp;

	public String toString() {
		return tp + " " + (mi!=null ? mi.getMsgid():msgid);
	}
	
	protected OPReq(Type t){
		tp = t;
	}
	
	public static OPReq GET(String bkid,  long process_time){
		OPReq r = new OPReq(Type.GET);
		r.brokerid = bkid;
		r.tm = process_time;
		return r;
	}
	public static OPReq ADD(MsgIndex mi){
		OPReq r = new OPReq( Type.ADD);
		r.mi = mi;
		r.msgid = mi.getMsgid();
		return r;
	}
	public static OPReq ACK(String msgid){
		OPReq r = new OPReq( Type.ACK);
		r.msgid = msgid;
		return r;
	}
	
	public static OPReq ROLLBACK(String msgid, long tm){
		OPReq r = new OPReq( Type.ROLLBACK);
		r.msgid = msgid;
		r.tm = tm;
		return r;		
	}
	public static OPReq DELETE(String msgid){
		OPReq r = new OPReq( Type.DELETE);
		r.msgid = msgid;
		return r;		
	}
	public static OPReq FAIL(String msgid, String code, String desc){
		OPReq r = new OPReq( Type.FAIL);
		r.msgid = msgid;
		r.desc = desc;
		return r;		
	}
	public static OPReq DELAY(String msgid, long tm){
		OPReq r = new OPReq( Type.DELAY);
		r.msgid = msgid;
		r.tm = tm;
		return r;		
	}

}
