-- 按luogj的模型的表测试数据


-----------------------------------------------------------------------
---- dest_client_id = '30000002'
---- dest_topic_id = '20000001'
---- ble_id = '10000001'
insert into topic_subscribe_rel_8 values('30000002', '20000001', '1', 10, 30, 600, '1', 'a', now(), 'aa');
-- ble 分担的目标主题关系
insert into ble_dest_topic_rel_8(dest_topic_id, BLE_id, use_status, login_no,opr_time, note ) 
           values('20000001', '10000001', '1', 'aa', 1111, '111');
---------------------------------------------------------

           
INSERT INTO `topic_subscribe_rel_8` VALUES ('Sub119Opr', 'TRecOprCnttDest', '_null', '20', '5000', '5000', '1', 'admin', '2015-07-02 20:51:31', null);
INSERT INTO `ble_dest_topic_rel_8` VALUES ('TRecOprCnttDest', '10000001', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO `dest_topic_info_8` VALUES ('TRecOprCnttDest', '操作类接触信息工单', '1', 'admin', '2015-07-02 15:48:12', null);
INSERT INTO `topic_mapping_rel_8` VALUES ('TRecOprCntt', '_all', '_default', 'TRecOprCnttDest', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO `topic_publish_rel_8` VALUES ('Pub101', 'TRecOprCntt', '_null', '1', 'admin', '2015-07-02 15:19:31', null);
INSERT INTO `client_base_info_8` VALUES ('Pub101', '订单处理', '订单处理', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO `client_base_info_8` VALUES ('Sub119Opr', '统一接触操作类', '统一接触', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO `src_topic_info_8` VALUES ('TRecOprCntt', '操作类接触信息工单', '1', 'admin', '2015-07-02 15:11:39', null);
INSERT INTO `topic_attribute_info_8` VALUES ('TRecOprCntt', '_all', '1', 'admin', '2015-07-02 15:19:33', '不分区');


-- 消费者订阅关系
insert into topic_subscribe_rel_8(client_id, dest_topic_id, client_pswd, max_request, min_timeout, max_timeout, use_status, login_no, opr_time, note)
    values('30000001', '20000001', '1', 10, 30, 600, '1', 'a', now(), 'aa');
    
    
insert into topic_subscribe_rel_8 values('30000002', '20000001', '1', 10, 30, 600, '1', 'a', now(), 'aa');

-- ble 分担的目标主题关系
insert into ble_dest_topic_rel_8(dest_topic_id, BLE_id, use_status, login_no,opr_time, note ) 
           values('20000001', '10000001', '1', 'aa', 1111, '111');
insert into ble_dest_topic_rel_8 values('20000002', '10000001', '1', 'aa', 1111, '111');
insert into ble_dest_topic_rel_8 values('20000003', '10000002', '1', 'aa', 1111, '111');

select * from ble_dest_topic_rel_8 where BLE_id='10000001'
select a.* from topic_subscribe_rel_8 a, ble_dest_topic_rel_8 b
    where b.BLE_id='10000002' and a.dest_topic_id=b.dest_topic_id


-- 消息索引表3.13.3
drop table msgidx_part_0;

CREATE TABLE `msgidx_part_0` (
	`idmm_msg_id` CHAR(60) NOT NULL COMMENT 'idmm创建的消息id',
	`dst_cli_id` CHAR(8) NOT NULL COMMENT '消费者客户端id',
	`dst_topic_id` CHAR(8) NOT NULL COMMENT '目标主题id	',
	`src_commit_code` CHAR(4) NULL DEFAULT NULL,
	`group_id` CHAR(32) NULL DEFAULT NULL COMMENT '分组号, 允许为null, 为null时则不以group_id分组及在途消息管理',
	`priority` INT(11) NOT NULL DEFAULT '100' COMMENT '优先级',
	`idmm_resend` INT(11) NULL DEFAULT NULL,
	`consumer_resend` INT(11) NULL DEFAULT NULL COMMENT '消费者重发次数',
	`create_time` BIGINT(20) NULL DEFAULT NULL COMMENT '生产消息提交时间, 恢复内存时按此字段排序',
	`broker_id` CHAR(8) NULL DEFAULT NULL COMMENT '消费Broker节点id',
	`req_time` BIGINT(20) NULL DEFAULT NULL COMMENT '消费请求时间',
	`commit_code` CHAR(4) NULL DEFAULT NULL COMMENT '消费提交代码',
	`commit_time` BIGINT(20) NULL DEFAULT NULL COMMENT '费提交时间',
	UNIQUE INDEX `Index 1` (`idmm_msg_id`, `dst_cli_id`, `dst_topic_id`),
	INDEX `Index 2` (`dst_cli_id`, `dst_topic_id`)
)
COLLATE='latin1_swedish_ci'
ENGINE=InnoDB;

-- 3.8.5.3.13.	目标主题归属BLE关系表
create table idmmcfg_ble_dsttopic_rel(
	dest_topic_id	char(8)		comment '目标主题id',
	BLE_id		    char(8)		comment 'BLE节点id',
	use_status		char		comment '使用标志 0&1',
	login_no		char(32)    comment '操作工号',		
	opr_time		bigint      comment '操作时间',		
	Note		    varchar(2048) comment '备注',
	UNIQUE INDEX `Index 1` ( dest_topic_id ),
	INDEX `Index 2` (`BLE_id`)
);

insert into idmmcfg_ble_dsttopic_rel values('20000001', '10000001', '1', 'aa', 1111, '111');
insert into idmmcfg_ble_dsttopic_rel values('20000002', '10000001', '1', 'aa', 1111, '111');



-- 3.8.5.3.10.	主题订阅关系表-topic_subscribe_rel_xxx
create table idmmcfg_client_dsttopic_rel(
	client_id		char(8)		comment '客户端id',	
	dest_topic_id	char(8)		comment '目标主题id',	
	client_pswd		char(32)		comment '客户端密码 支持“_null”',	
	max_request		int		comment '最大并发数',	
	min_timeout		int		comment '最小超时时间',	
	max_timeout		int	comment '最大超时时间',		
	use_status		char(1)		comment '使用标志 0&1',	
	login_no		char(32)	comment '操作工号',		
	opr_time		bigint	comment '操作时间',	
	Note		    varchar(2048) comment '备注',
	UNIQUE INDEX `Index 1` (client_id, dest_topic_id)
);

insert into idmmcfg_client_dsttopic_rel values('30000001', '20000001', '1', 1, 1, 1, '1', 'a', 1111, 'aa');
insert into idmmcfg_client_dsttopic_rel values('30000002', '20000001', '1', 1, 1, 1, '1', 'a', 1111, 'aa');
insert into idmmcfg_client_dsttopic_rel values('30000003', '20000001', '1', 1, 1, 1, '1', 'a', 1111, 'aa');

insert into idmmcfg_client_dsttopic_rel values('30000004', '20000002', '1', 1, 1, 1, '1', 'a', 1111, 'aa');



insert into idmmcfg_ble_dsttopic_rel values('20000003', '10000002', '1', 'aa', 1111, '111');
insert into idmmcfg_client_dsttopic_rel values('30000005', '20000003', '1', 10, 1, 1, '1', 'a', 1111, 'aa');

	
-- 恢复内存时取表中数据， 遍历多个分表， 每次按dst_cli_id 和 dst_topic_id 条件取
;

-- 添加新的索引
insert into msgidx_part_%d(idmm_msg_id, dst_cli_id, dst_topic_id, group_id, priority, consumer_resend, create_time, broker_id, req_time, commit_code, commit_time)
   values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
   
-- 消费取走或者消费回滚 更新状态， 更新 req_time, consumer_resend
update from msgidx_part_%d set req_time=?, consumer_resend=consumer_resend+?, broker_id=?
	where idmm_msg_id=? and dst_cli_id=? and dst_topic_id=?

-- 消费 commit 后删除数据
delete from msgidx_part_%d where idmm_msg_id=? and dst_cli_id=? and dst_topic_id=?

