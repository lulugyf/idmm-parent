package com.sitech.crmpd.idmm2.broker.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author heihuwudi@gmail.com Created By: 2016/1/20.下午 05:41
 */
@Configuration
public class FlumeLogger {
	private static final Logger LOGGER = LoggerFactory.getLogger(FlumeLogger.class);
	
	// 端到端负载日志接收地址和端口
	@Value("${e2e.log_svr_addr:0.0.0.0}")
	private String log_svr_addr;
	@Value("${e2e.log_svr_port:44443}")
	private int log_svr_port;

	private static final String TAB = "~!~";

	// 发送日志：直接发送UDP报文
	public void sendFlumeLog(String logstr) {
		LOGGER.info("log_svr_addr={},log_svr_port={},buf={}", log_svr_addr, log_svr_port, logstr);
		DatagramSocket socket=null;
		try {
			byte[] buf = logstr.getBytes("UTF-8");
			
			DatagramPacket dataGramPacket = new DatagramPacket(buf, buf.length,
					InetAddress.getByName(log_svr_addr), log_svr_port);
			socket = new DatagramSocket();
			socket.send(dataGramPacket);

		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if (socket != null){
				socket.close();
			}
		}
	}

	public void sendFlumeLog(String traceId, String callId,
			String parentCallId, String startTime, String endTime,
			String server_ip) {
		try {
			StringBuilder sb = new StringBuilder(512);

			sb.append(traceId);
			sb.append(TAB);
			sb.append(callId);
			sb.append(TAB);
			sb.append(parentCallId);
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("mq_req");
			sb.append(TAB);
			sb.append("idmm");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append(startTime);
			sb.append(TAB);
			sb.append(endTime);
			sb.append(TAB);
			sb.append(server_ip);
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("broker");
			sb.append(TAB);
			sb.append("0");
			sb.append(TAB);
			sb.append("OK");
			sb.append(TAB);
			sb.append("");
			sb.append(TAB);
			sb.append("");

			sendFlumeLog(sb.toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
