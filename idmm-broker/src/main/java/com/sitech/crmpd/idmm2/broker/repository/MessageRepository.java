package com.sitech.crmpd.idmm2.broker.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageId;

/**
 * @author heihuwudi@gmail.com</br> Created By: 2015年3月29日 下午1:59:11
 */
public interface MessageRepository extends PagingAndSortingRepository<Message, MessageId> {

	public <S extends Message> S saveDelay(S entity);

}
