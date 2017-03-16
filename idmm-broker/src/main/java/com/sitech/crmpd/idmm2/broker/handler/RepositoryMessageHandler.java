package com.sitech.crmpd.idmm2.broker.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.sitech.crmpd.idmm2.broker.repository.MessageRepository;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年4月1日 上午10:56:04
 */
public abstract class RepositoryMessageHandler extends BaseMessageHandler {

	@Autowired
	MessageRepository messageRepository;
	@Value("${jdbc.expectTimeInMs:100}")
	int jdbcExpectTime;
}
