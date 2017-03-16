package com.sitech.crmpd.idmm.ble.store;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

final class OPItem {
	volatile protected long create_time; //保存请求开始处理的时间
	volatile protected int idx;
	volatile protected BlockingQueue<JournalOP> q = new ArrayBlockingQueue<JournalOP>(2);  //应答队列
	volatile protected OPReq req = new OPReq(OPReq.Type.ADD); //请求数据
	volatile protected OPAns ans = new OPAns(); //应答数据
	volatile protected Store store; //数据存储对象
	public long tm() { return create_time ;}
	protected OPItem(int i){idx = i;}


	volatile protected int stage;   // 当前阶段，  0: netty发起的请求  1: db-thread 处理完后的请求
	volatile protected BlockingQueue<OPItem> queue; //数据库操作完成后，需要把请求再回送给内存线程(mem-thread)
}
