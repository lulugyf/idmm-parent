package com.sitech.crmpd.idmm.ble.store;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;

import com.sitech.crmpd.idmm.ble.MsgIndex;


/**
 * 消息索引 row-object mapper
 */
@Configuration
public class IndexRowMapper implements RowMapper<MsgIndex> {

	/**
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public MsgIndex mapRow(ResultSet rs, int rowNum) throws SQLException {
		// select idmm_msg_id, dst_cli_id, dst_topic_id, group_id, priority, 
		//consumer_resend, create_time, broker_id, req_time, commit_code, 
		//commit_time, next_topic_id, next_client_id, src_topic_id,
		// commit_desc, expire_time
		//  from msgidx_part_%d
		MsgIndex i = new MsgIndex();
		i.setMsgid(rs.getString(1));
		i.setGroupid(rs.getString(4));
		i.setPriority(rs.getInt(5));
		i.setGetTime(rs.getLong(9));
		i.setCreateTime(rs.getLong(7));
		i.setRetry(rs.getInt(6));
		i.setNextTopic(rs.getString(12));
		i.setNextClient(rs.getString(13));
		i.setProduceClient(rs.getString(14));
		i.setSrcTopic(rs.getString(15));
		i.setCommitDesc(rs.getString(16));
		//i.setExpireTime(rs.getLong(17));
		return i;
	}

}
