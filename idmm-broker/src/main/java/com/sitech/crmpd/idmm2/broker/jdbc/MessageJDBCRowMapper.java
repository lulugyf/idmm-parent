package com.sitech.crmpd.idmm2.broker.jdbc;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.google.common.io.ByteStreams;
import com.sitech.crmpd.idmm2.client.api.Message;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午9:28:04
 */
@Component
public class MessageJDBCRowMapper implements RowMapper<Message> {

	/**
	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
	 */
	@Override
	public Message mapRow(ResultSet rs, int rowNum) throws SQLException {
		try {
			final byte[] content = ByteStreams.toByteArray(rs.getBlob("content").getBinaryStream());
			return Message.create(rs.getString("properties"), content);
		} catch (final IOException e) {
			throw new SQLException(e);
		}
	}

}
