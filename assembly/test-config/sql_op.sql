
-- 单个消息队列对应数据配置， 生产者-Pub101  源主题-TRecOprCntt 目标主题-TRecOprCnttDest 消费者-Sub119Opr
INSERT INTO `topic_subscribe_rel_8` VALUES ('Sub119Opr', 'TRecOprCnttDest', '_null', '20', '5000', '5000', '1', 'admin', '2015-07-02 20:51:31', null);
INSERT INTO `ble_dest_topic_rel_8` VALUES ('TRecOprCnttDest', '10000001', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO `dest_topic_info_8` VALUES ('TRecOprCnttDest', '操作类接触信息工单', '1', 'admin', '2015-07-02 15:48:12', null);
INSERT INTO `topic_mapping_rel_8` VALUES ('TRecOprCntt', '_all', '_default', 'TRecOprCnttDest', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO `topic_publish_rel_8` VALUES ('Pub101', 'TRecOprCntt', '_null', '1', 'admin', '2015-07-02 15:19:31', null);
INSERT INTO `client_base_info_8` VALUES ('Pub101', '订单处理', '订单处理', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO `client_base_info_8` VALUES ('Sub119Opr', '统一接触操作类', '统一接触', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO `src_topic_info_8` VALUES ('TRecOprCntt', '操作类接触信息工单', '1', 'admin', '2015-07-02 15:11:39', null);
INSERT INTO `topic_attribute_info_8` VALUES ('TRecOprCntt', '_all', '1', 'admin', '2015-07-02 15:19:33', '不分区');

-- 一个目标主题-TRecOprCnttDest 对应另一个消费者 Sub119Opr_1
INSERT INTO `client_base_info_8` VALUES ('Sub119Opr_1', '统一接触操作类', '统一接触', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO `topic_subscribe_rel_8` VALUES ('Sub119Opr_1', 'TRecOprCnttDest', '_null', '20', '5000', '5000', '1', 'admin', '2015-07-02 20:51:31', null);

delete from topic_subscribe_rel_8 where client_id='Sub119Opr_1'

SELECT * FROM ble_dest_topic_rel_8
select * from topic_subscribe_rel_8

delete from topic_subscribe_rel_8 where client_id='30000002'
delete from ble_dest_topic_rel_8 where dest_topic_id='20000001'

truncate table msgidx_part_0;
truncate table msgidx_part_4;
truncate table msgidx_part_5;
truncate table msgidx_part_6;

insert into msgidx_part_0 values
('1440122302471::11282::10.162.200.72:43600::1', 'Pub101', 'TRecOprCntt', 'Sub119Opr', 'TRecOprCnttDest', NULL, '226111006', 4, NULL, 0, 1440122302482, '', 0, '', 0, NULL, NULL, NULL),
('1440122323969::1203::10.162.200.89:33849::1', 'Pub101', 'TRecOprCntt', 'Sub119Opr', 'TRecOprCnttDest', NULL, '1261670558', 4, NULL, 0, 1440122323983, '', 0, '', 0, NULL, NULL, NULL);

select * from msgidx_part_0

insert into messagestore_1 values('1440122302471::11282::10.162.200.72:43600::1', '', '', '');
insert into messagestore_1 values('1440122323969::1203::10.162.200.89:33849::1', '', '', '');

select * from messagestore_1


-- 消费通知测试, 存放通知的主题 notice_1, 其消费者 notice_sub_1
-- INSERT INTO client_base_info_8 VALUES ('Sub119Opr_1', '统一接触操作类', '统一接触', '1', 'admin', '2015-07-02 15:09:52', null);
-- INSERT INTO topic_subscribe_rel_8 VALUES ('Sub119Opr_1', 'TRecOprCnttDest', '_null', '20', '5000', '5000', '1', 'admin', '2015-07-02 20:51:31', null);
INSERT INTO dest_topic_info_8 VALUES ('notice_1', '消费通知', '1', 'admin', '2015-07-02 15:48:12', null);
INSERT INTO ble_dest_topic_rel_8 VALUES ('notice_1', '10000001', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO topic_subscribe_rel_8 VALUES ('notice_sub_1', 'notice_1', '_null', '20', '60', '600', '1', 'admin', '2015-07-02 20:51:31', null);
INSERT INTO client_base_info_8 VALUES ('notice_sub_1', '消费通知接收客户端', '消费通知', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO consume_notice_info_8(producer_client_id, src_topic_id, dest_topic_id, consumer_client_id, notice_topic_id, notice_client_id, use_status, login_no, opr_time, note)
	VALUES('Pub101', 'TRecOprCntt', 'TRecOprCnttDest', 'Sub119Opr', 'notice_1', 'notice_sub_1', '1', 'admin', now(), '消费通知测试')

CREATE TABLE `msgidx_part_0` (
	`idmm_msg_id` CHAR(60) NOT NULL COMMENT 'idmm创建的消息id' COLLATE 'utf8mb4_unicode_ci',
	`dst_cli_id` CHAR(32) NOT NULL COMMENT '消费者客户端id' COLLATE 'utf8mb4_unicode_ci',
	`dst_topic_id` CHAR(32) NOT NULL COMMENT '目标主题id	' COLLATE 'utf8mb4_unicode_ci',
	`group_id` CHAR(32) NULL DEFAULT NULL COMMENT '分组号, 允许为null, 为null时则不以group_id分组及在途消息管理' COLLATE 'utf8mb4_unicode_ci',
	`priority` INT(11) NOT NULL DEFAULT '100' COMMENT '优先级',
	`consumer_resend` INT(11) NULL DEFAULT NULL COMMENT '消费者重发次数',
	`create_time` BIGINT(20) NULL DEFAULT NULL COMMENT '生产消息提交时间, 恢复内存时按此字段排序',
	`req_time` BIGINT(20) NULL DEFAULT NULL COMMENT '消费请求时间',
	`commit_code` CHAR(4) NULL DEFAULT NULL COMMENT '消费提交代码' COLLATE 'utf8mb4_unicode_ci',
	`commit_time` BIGINT(20) NULL DEFAULT NULL COMMENT '费提交时间',
	`commit_desc` CHAR(160) NULL DEFAULT NULL COMMENT '消费结果描述' COLLATE 'utf8mb4_unicode_ci',
	`expire_time` BIGINT(20) NULL DEFAULT '0' COMMENT '有效时间，unix时间戳记ms, 0为永久有效',

	`properties` varchar(1024) not null comment '附加属性：next_client_id, next_topic_id,  broker_id, src_commit_code,src_topic_id,produce_cli_id, idmm_resend' COLLATE 'utf8mb4_unicode_ci',

	UNIQUE INDEX `Index 1` (`idmm_msg_id`, `dst_cli_id`, `dst_topic_id`),
	INDEX `Index 2` (`dst_cli_id`, `dst_topic_id`)
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;

create table priority_map_8 (
	`pname` varchar(32) not null comment '优先级名称',
	`pvalue` int not null comment '优先级数字',
	`is_default` char(1) null comment '是否默认优先级， 只能有一个',
	`desc` varchar(64) null comment '描述' COLLATE 'utf8mb4_unicode_ci',
	UNIQUE INDEX `Index 1` (pname)
) comment '优先级名称 与 数字的映射表';

insert into priority_map values('high', 300, '0', '');
insert into priority_map values('middle', 200, '1', '');
insert into priority_map values('low', 100, '0', '');

create table ble_base_info_9 as select * from ble_base_info_8;
create table ble_dest_topic_rel_9 as select * from ble_dest_topic_rel_8;
create table broker_base_info_9 as select * from broker_base_info_8;
create table client_base_info_9 as select * from client_base_info_8;
create table client_limit_info_9 as select * from client_limit_info_8;
create table consume_notice_info_9 as select * from consume_notice_info_8;
create table consume_order_info_9 as select * from consume_order_info_8;
create table dest_topic_info_9 as select * from dest_topic_info_8;
create table src_topic_info_9 as select * from src_topic_info_8;
create table topic_attribute_info_9 as select * from topic_attribute_info_8;
create table topic_mapping_rel_9 as select * from topic_mapping_rel_8;
create table topic_publish_rel_9 as select * from topic_publish_rel_8;
create table topic_subscribe_rel_9 as select * from topic_subscribe_rel_8;
create table priority_map_9 as select * from priority_map_8;
update priority_map_9 set pvalue=400 where pname='low';

INSERT INTO client_base_info_9 VALUES ('Sub119Opr_1', '统一接触操作类', '统一接触', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO topic_subscribe_rel_9 VALUES ('Sub119Opr_1', 'TRecOprCnttDest', '_null', '20', '5000', '5000', '1', 'admin', '2015-07-02 20:51:31', null);




-- 2016-8-13 anhui test, consume notice test
INSERT INTO dest_topic_info_19 VALUES ('notice_1', '消费通知', '1', 'admin', '2015-07-02 15:48:12', null);
INSERT INTO ble_dest_topic_rel_19 VALUES ('notice_1', '10000001', '1', 'admin', '2015-07-02 15:19:29', null);
INSERT INTO topic_subscribe_rel_19 VALUES ('notice_sub_1', 'notice_1', 20, 60, 600, -1,10000,4000, '1', 'admin', '2015-07-02 20:51:31', null);
INSERT INTO client_base_info_19 VALUES ('notice_sub_1', '消费通知接收客户端', '消费通知', '1', 'admin', '2015-07-02 15:09:52', null);
INSERT INTO consume_notice_info_19(producer_client_id, src_topic_id, dest_topic_id, consumer_client_id, notice_topic_id, notice_client_id, use_status, login_no, opr_time, note)
	VALUES('pub_Test', 'Test', 'Test', 'sub_Test', 'notice_1', 'notice_sub_1', '1', 'admin', now(), '消费通知测试')

select * from consume_notice_info_19;
select  c.ble_id, a.src_topic_id, b.client_id as publisher, a.dest_topic_id, d.client_id as consumer
	from topic_mapping_rel_19 a, topic_publish_rel_19 b, ble_dest_topic_rel_19 c, topic_subscribe_rel_19 d
	where -- a.dest_topic_id like '%test%' and
	a.src_topic_id=b.src_topic_id and c.dest_topic_id=a.dest_topic_id and a.dest_topic_id=d.dest_topic_id;
	
select * from consume_notice_info_19

select * from client_limit_info_19 where client_id='pub_Test' and limit_key = 'password'

update client_limit_info_19 set limit_value='d9f8c11f69fe86f3c1aac3a13012b210' where client_id in ('pub_Test', 'sub_Test') and limit_key = 'password';  -- password=pub_Test
