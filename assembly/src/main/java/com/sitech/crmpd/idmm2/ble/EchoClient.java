/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.sitech.crmpd.idmm2.ble;

import java.util.Set;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.google.common.collect.Sets;
import com.sitech.crmpd.idmm2.client.api.FrameMessage;
import com.sitech.crmpd.idmm2.client.api.Message;
import com.sitech.crmpd.idmm2.client.api.MessageType;
import com.sitech.crmpd.idmm2.client.api.PropertyOption;
import com.sitech.crmpd.idmm2.client.api.PullCode;
import com.sitech.crmpd.idmm2.client.api.ResultCode;
import com.sitech.crmpd.idmm2.transport.FrameCodeC;

/**
 * Sends one message when a connection is open and echoes back any received data
 * to the server. Simply put, the echo client initiates the ping-pong traffic
 * between the echo client and server by sending the first message to the
 * server.
 */
public final class EchoClient {
	
	protected static String ble_ip = "127.0.0.1";
	protected static int ble_port = 5678;
	
	private static String clientid = "30000005";
	private static String target_topic = "20000003";

	public static void main(String[] args) throws Exception {
//		consumer(args);
//		producer(args);
		
		Set<Object> s = Sets.newConcurrentHashSet();
		s.add(null);
	}
	
	public static void consumer(String[] args) throws Exception {
		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LoggingHandler(LogLevel.TRACE));
							p.addLast(new FrameCodeC());
							p.addLast(new ConsumerClientHandler(clientid, target_topic));
						}
					});

			// Start the client.
			ChannelFuture f = b.connect(ble_ip, ble_port).sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}
	

	public static void producer(String[] args) throws Exception {
		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							ChannelPipeline p = ch.pipeline();
							p.addLast(new LoggingHandler(LogLevel.TRACE));
							p.addLast(new FrameCodeC());
							p.addLast(new ProducerClientHandler(clientid, target_topic));
						}
					});

			// Start the client.
			ChannelFuture f = b.connect(ble_ip, ble_port).sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}

}

class ConsumerClientHandler extends ChannelInboundHandlerAdapter {
	private String clientid;
	private String target_topic;
	
	public ConsumerClientHandler(String clientid, String target_topic){
		this.clientid = clientid;
		this.target_topic = target_topic;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		/*Message message = Message.create("hello ble".getBytes());
		message.setProperty(PropertyOption.ADDRESS, new String[] { "172.21.2.53:5678" });
		message.setProperty(PropertyOption.COMMIT_TIME,	System.currentTimeMillis());
		message.setProperty(PropertyOption.COMPRESS, true);
		message.setProperty(PropertyOption.PRIORITY, 1);
		message.setProperty(PropertyOption.CUSTOM_SERIAL, "C0"); */
		
		Message msg = Message.create();
		
		// PULL
		msg.setProperty(PropertyOption.CLIENT_ID, clientid);
		msg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		ctx.writeAndFlush(new FrameMessage(MessageType.PULL, msg));

	}

	private int msgct = 0;
	StringBuffer b = new StringBuffer();
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) {
		FrameMessage frameMessage = (FrameMessage) message;
		MessageType mtype = frameMessage.getType();
		System.out.println(mtype);
		Message msg = frameMessage.getMessage();
		System.out.println(msg.getPropertiesAsString());
		System.out.println(msg.getContentAsString());
		
		
		ResultCode rcode = ResultCode.valueOf( msg.getProperty(PropertyOption.RESULT_CODE) );
		
		Message rmsg = Message.create();
		//  测试消费的正常流程， 连接后发pull， 返回消息后commit，commit成功后再发pull，返回no-more-message 则终止
		if(rcode == ResultCode.NO_MORE_MESSAGE){
			System.out.printf("====:%s\n", b.toString());
			return;
		}else if(rcode == ResultCode.OK && msg.existProperty(PropertyOption.MESSAGE_ID)){
			String msgid = msg.getStringProperty(PropertyOption.MESSAGE_ID);
			b.append(msgid).append(' ');
			// COMMIT,  取到消息后则直接commit
			rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);
			rmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			rmsg.setProperty(PropertyOption.PULL_CODE, PullCode.COMMIT);
			rmsg.setProperty(PropertyOption.MESSAGE_ID, msg.getStringProperty(PropertyOption.MESSAGE_ID));
			ctx.writeAndFlush(new FrameMessage(MessageType.PULL, rmsg));
		}else if(rcode == ResultCode.OK){
			// PULL，  commit成功后返回ok， 则继续pull
			rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);
			rmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			ctx.writeAndFlush(new FrameMessage(MessageType.PULL, rmsg));			
		}
		
		/*
		// 测试并发数
		if(rcode == ResultCode.NO_MORE_MESSAGE){
			System.out.printf("=======%d\n", msgct);
			return;
		}else if(rcode == ResultCode.OK && msg.existProperty(PropertyOption.MESSAGE_ID)){
			// COMMIT,  取到消息后则直接commit
			rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);
			rmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
//			rmsg.setProperty(PropertyOption.PULL_CODE, PullCode.COMMIT);
//			rmsg.setProperty(PropertyOption.MESSAGE_ID, msg.getStringProperty(PropertyOption.MESSAGE_ID));
			msgct ++;
			System.out.printf("=======%d\n", msgct);
			//if(msgct < 3)
				ctx.writeAndFlush(new FrameMessage(MessageType.PULL, rmsg));
				
		}else if(rcode == ResultCode.OK){
			// PULL，  commit成功后返回ok， 则继续pull
			rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);
			rmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			ctx.writeAndFlush(new FrameMessage(MessageType.PULL, rmsg));			
		} */
	}

}


class ProducerClientHandler extends ChannelInboundHandlerAdapter {
	private String clientid;
	private String target_topic;
	private int msgid_seed = 200;
	
	public ProducerClientHandler(String clientid, String target_topic){
		this.clientid = clientid;
		this.target_topic = target_topic;
	}
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
	
		Message msg = Message.create();
		
		msg.setProperty(PropertyOption.CLIENT_ID, clientid);
		msg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
		msg.setProperty(PropertyOption.MESSAGE_ID, String.valueOf(++msgid_seed));
		msg.setProperty(PropertyOption.PRIORITY, 100);
		ctx.writeAndFlush(new FrameMessage(MessageType.SEND_COMMIT, msg));

	}
	
	private int cnt = 10;

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object message) {
		FrameMessage frameMessage = (FrameMessage) message;
		MessageType mtype = frameMessage.getType();
		System.out.println(mtype);
		Message msg = frameMessage.getMessage();
		System.out.println(msg.getPropertiesAsString());

		ResultCode rcode = ResultCode.valueOf( msg.getProperty(PropertyOption.RESULT_CODE) );
		
		Message rmsg = Message.create();
		if(rcode == ResultCode.OK && --cnt > 0){
			// PULL，  commit成功后返回ok， 则继续pull
			rmsg.setProperty(PropertyOption.CLIENT_ID, clientid);
			rmsg.setProperty(PropertyOption.TARGET_TOPIC, target_topic);
			rmsg.setProperty(PropertyOption.MESSAGE_ID, String.valueOf(++msgid_seed));
			rmsg.setProperty(PropertyOption.PRIORITY, 100);
			ctx.writeAndFlush(new FrameMessage(MessageType.SEND_COMMIT, rmsg));			
		}
	}

}
