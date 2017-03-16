package com.sitech.crmpd.idmm2.broker.repository;

import org.springframework.data.repository.CrudRepository;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年9月11日 下午3:28:13
 */
public interface TargetMessageRepository extends CrudRepository<Message, MessageId> {

}
