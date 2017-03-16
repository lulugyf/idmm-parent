select * from topic_mapping_rel_8



-- 导出  表 idmm2.ble_base_info_8 结构
CREATE TABLE ble_base_info_8 (
  BLE_id decimal(8,0) NOT NULL,
  id_number decimal(1,0) NOT NULL,
  addr_ip char(15) NOT NULL,
  addr_port decimal(5,0) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (BLE_id,id_number)
);

-- 正在导出表  idmm2.ble_base_info_8 的数据：~3 rows (大约)
/*!40000 ALTER TABLE ble_base_info_8 DISABLE KEYS */;
INSERT INTO ble_base_info_8 (BLE_id, id_number, addr_ip, addr_port, use_status, login_no, opr_time, note) VALUES
	(10000001, 1, '10.162.200.211', 5678, '1', 'admin', to_date('2015-06-19 17:05:34', 'yyyy-mm-dd hh24:mi:ss'), '');
INSERT INTO ble_base_info_8 (BLE_id, id_number, addr_ip, addr_port, use_status, login_no, opr_time, note) VALUES
	(10000002, 2, '10.162.200.212', 5678, '1', 'admin', to_date('2015-06-19 17:05:34', 'yyyy-mm-dd hh24:mi:ss'), '');
INSERT INTO ble_base_info_8 (BLE_id, id_number, addr_ip, addr_port, use_status, login_no, opr_time, note) VALUES
	(10000003, 3, '10.162.200.213', 5678, '1', 'admin', to_date('2015-06-19 17:05:34', 'yyyy-mm-dd hh24:mi:ss'), '');
/*!40000 ALTER TABLE ble_base_info_8 ENABLE KEYS */;


-- 导出  表 idmm2.ble_dest_topic_rel_8 结构
CREATE TABLE ble_dest_topic_rel_8 (
  dest_topic_id varchar(32) NOT NULL,
  BLE_id decimal(8,0) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (dest_topic_id,BLE_id)
);

-- 正在导出表  idmm2.ble_dest_topic_rel_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE ble_dest_topic_rel_8 DISABLE KEYS */;
INSERT INTO ble_dest_topic_rel_8 (dest_topic_id, BLE_id, use_status, login_no, opr_time, note) VALUES
	('notice_1', 10000001, '1', 'admin', to_date('2015-07-02 15:19:29', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO ble_dest_topic_rel_8 (dest_topic_id, BLE_id, use_status, login_no, opr_time, note) VALUES
	('TRecOprCnttDest', 10000001, '1', 'aa', to_date('2000-11-11 00:00:00', 'yyyy-mm-dd hh24:mi:ss'), '111');
/*!40000 ALTER TABLE ble_dest_topic_rel_8 ENABLE KEYS */;




-- 导出  表 idmm2.ble_not_found 结构
CREATE TABLE ble_not_found (
  msg_id varchar(128) NOT NULL,
  dest_topic_id char(32) NOT NULL,
  properties varchar2(2048) NOT NULL,
  op_time timestamp NOT NULL,
  PRIMARY KEY (msg_id,dest_topic_id)
);


-- 导出  表 idmm2.broker_base_info_8 结构
CREATE TABLE broker_base_info_8 (
  broker_id decimal(8,0) NOT NULL,
  comm_ip char(15) NOT NULL,
  comm_port decimal(15,0) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (broker_id)
);

-- 正在导出表  idmm2.broker_base_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE broker_base_info_8 DISABLE KEYS */;
INSERT INTO broker_base_info_8 (broker_id, comm_ip, comm_port, use_status, login_no, opr_time, note) VALUES
	(1, '172.21.1.36', 2202, '1', 'admin', to_date('2015-06-19 17:04:17', 'yyyy-mm-dd hh24:mi:ss'), '');
/*!40000 ALTER TABLE broker_base_info_8 ENABLE KEYS */;


-- 导出  表 idmm2.client_base_info_8 结构
CREATE TABLE client_base_info_8 (
  client_id varchar(32) NOT NULL,
  sub_system char(32) NOT NULL,
  client_desc varchar(2048) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (client_id)
);

-- 正在导出表  idmm2.client_base_info_8 的数据：~4 rows (大约)
/*!40000 ALTER TABLE client_base_info_8 DISABLE KEYS */;
INSERT INTO client_base_info_8 (client_id, sub_system, client_desc, use_status, login_no, opr_time, note) VALUES
	('notice_sub_1', '消费通知接收客户端', '消费通知', '1', 'admin', to_date('2015-07-02 15:09:52', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO client_base_info_8 (client_id, sub_system, client_desc, use_status, login_no, opr_time, note) VALUES
	('Pub101', '订单处理', '订单处理', '1', 'admin', to_date('2015-07-02 15:09:52', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO client_base_info_8 (client_id, sub_system, client_desc, use_status, login_no, opr_time, note) VALUES
	('Pub108', 'oneBOSS', '生产者测试', '1', 'admin', to_date('2015-08-14 19:15:38', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO client_base_info_8 (client_id, sub_system, client_desc, use_status, login_no, opr_time, note) VALUES
	('Sub108', 'oneBOSS', 'CRM同步到一级BOSS', '1', 'admin', to_date('2015-08-14 19:15:38', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO client_base_info_8 (client_id, sub_system, client_desc, use_status, login_no, opr_time, note) VALUES
	('Sub119Opr', '统一接触操作类', '统一接触', '1', 'admin', to_date('2015-07-02 15:09:52', 'yyyy-mm-dd hh24:mi:ss'), NULL);
/*!40000 ALTER TABLE client_base_info_8 ENABLE KEYS */;


-- 导出  表 idmm2.client_limit_info_8 结构
CREATE TABLE client_limit_info_8 (
  client_id char(8) NOT NULL,
  limit_key char(8) NOT NULL,
  limit_value varchar(2048) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (client_id,limit_key)
);


-- 导出  表 idmm2.consume_notice_info_8 结构
CREATE TABLE consume_notice_info_8 (
  producer_client_id char(32) NOT NULL,
  src_topic_id char(32) NOT NULL,
  dest_topic_id char(32) NOT NULL,
  consumer_client_id char(32) NOT NULL,
  notice_topic_id char(32) NOT NULL,
  notice_client_id char(32) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (producer_client_id,src_topic_id,dest_topic_id,consumer_client_id,notice_topic_id)
);


-- 正在导出表  idmm2.consume_notice_info_9 的数据：~1 rows (大约)
/*!40000 ALTER TABLE consume_notice_info_9 DISABLE KEYS */;
INSERT INTO consume_notice_info_8 (producer_client_id, src_topic_id, dest_topic_id, consumer_client_id, notice_topic_id, notice_client_id, use_status, login_no, opr_time, note) VALUES
	('Pub101', 'TRecOprCntt', 'TRecOprCnttDest', 'Sub119Opr', 'notice_1', 'notice_sub_1', '1', 'admin', to_date('2016-08-16 15:19:34', 'yyyy-mm-dd hh24:mi:ss'), '消费通知测试');
/*!40000 ALTER TABLE consume_notice_info_9 ENABLE KEYS */;


-- 导出  表 idmm2.consume_order_info_8 结构
CREATE TABLE consume_order_info_8 (
  producer_client_id char(8) NOT NULL,
  src_topic_id char(8) NOT NULL,
  attribute_key char(32) NOT NULL,
  attribute_value char(32) NOT NULL,
  dest_topic_id char(8) NOT NULL,
  consumer_client_id char(8) NOT NULL,
  consume_seq number(11) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (producer_client_id,src_topic_id,attribute_key,attribute_value,dest_topic_id,consumer_client_id,consume_seq)
);


-- 导出  表 idmm2.dest_topic_info_8 结构
CREATE TABLE dest_topic_info_8 (
  dest_topic_id varchar(32) NOT NULL ,
  dest_topic_desc varchar(2048) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (dest_topic_id)
);

-- 正在导出表  idmm2.dest_topic_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE dest_topic_info_8 DISABLE KEYS */;
INSERT INTO dest_topic_info_8 (dest_topic_id, dest_topic_desc, use_status, login_no, opr_time, note) VALUES
	('notice_1', '消费通知', '1', 'admin', to_date('2015-07-02 15:48:12', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO dest_topic_info_8 (dest_topic_id, dest_topic_desc, use_status, login_no, opr_time, note) VALUES
	('TRecOprCnttDest', '操作类接触信息工单', '1', 'admin', to_date('2015-07-02 15:48:12', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO dest_topic_info_8 (dest_topic_id, dest_topic_desc, use_status, login_no, opr_time, note) VALUES
	('TUrStatusToOboss', '用户状态CRM同步至一级BOSS', '1', 'admin', to_date('2015-08-14 19:15:38', 'yyyy-mm-dd hh24:mi:ss'), NULL);
/*!40000 ALTER TABLE dest_topic_info_8 ENABLE KEYS */;


-- 导出  表 idmm2.idmm_version_info 结构
CREATE TABLE idmm_version_info (
  config_version decimal(8,0) NOT NULL,
  version_status char(1) NOT NULL,
  version_desc varchar(2048) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (config_version)
);

-- 正在导出表  idmm2.idmm_version_info 的数据：~3 rows (大约)
/*!40000 ALTER TABLE idmm_version_info DISABLE KEYS */;
INSERT INTO idmm_version_info (config_version, version_status, version_desc, login_no, opr_time, note) VALUES
	(8, '1', '测试版本1', 'admin', to_date('2015-06-19 17:09:48', 'yyyy-mm-dd hh24:mi:ss'), '0619');
/*!40000 ALTER TABLE idmm_version_info ENABLE KEYS */;


-- 导出  表 idmm2.messagestore_0 结构
CREATE TABLE messagestore_0 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);


-- 导出  表 idmm2.messagestore_1 结构
CREATE TABLE messagestore_1 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);



-- 导出  表 idmm2.messagestore_2 结构
CREATE TABLE messagestore_2 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);

-- 导出  表 idmm2.messagestore_3 结构
CREATE TABLE messagestore_3 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);

-- 导出  表 idmm2.messagestore_4 结构
CREATE TABLE messagestore_4 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);
CREATE TABLE messagestore_5 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE messagestore_6 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);
CREATE TABLE messagestore_7 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);
CREATE TABLE messagestore_8 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);
CREATE TABLE messagestore_9 (
  id varchar(128) NOT NULL,
  properties varchar(2048) NULL,
  systemProperties varchar(1024) NULL,
  content blob,
  createtime number(20) NULL,
  PRIMARY KEY (id)
);


-- 导出  表 idmm2.message_notfound 结构
CREATE TABLE message_notfound (
  id varchar(128) NOT NULL,
  found_time number(20) DEFAULT '0',
  next_scan_time number(20) DEFAULT '0',
  scan_retries number(11) DEFAULT '0',
  PRIMARY KEY (id)
  
);
create index index1 on message_notfound(next_scan_time);

-- 正在导出表  idmm2.message_notfound 的数据：~0 rows (大约)
/*!40000 ALTER TABLE message_notfound DISABLE KEYS */;
INSERT INTO message_notfound (id, found_time, next_scan_time, scan_retries) VALUES
	('1440665420116::1::127.0.0.1:62228::2', 1440670085605, 1440670145605, 0),
	('1441216290401::1::127.0.0.1:50462::1::2', 1441216868958, 1441216928958, 0);
/*!40000 ALTER TABLE message_notfound ENABLE KEYS */;


-- 导出  表 idmm2.message_notfound_his 结构
CREATE TABLE message_notfound_his (
  id varchar(128) NOT NULL,
  found_time number(20) DEFAULT '0',
  next_scan_time number(20) DEFAULT '0',
  scan_retries number(11) DEFAULT '0',
  PRIMARY KEY (id)
);
create index index1_1 on message_notfound_his(next_scan_time);


CREATE TABLE msgidx_part_0 (
  idmm_msg_id varchar(60) NOT NULL,
  produce_cli_id varchar(32) NULL,
  src_topic_id varchar(32) NULL,
  dst_cli_id varchar(32) NOT NULL,
  dst_topic_id varchar(32) NOT NULL,
  src_commit_code varchar(4) NULL,
  group_id varchar(32) NULL,
  priority number(11) NOT NULL ,
  idmm_resend number(11) NULL,
  consumer_resend number(11) NULL,
  create_time number(20) NULL,
  broker_id varchar(21) NULL,
  req_time number(20) NULL,
  commit_code varchar(4) NULL,
  commit_time number(20) NULL,
  commit_desc varchar(1024) NULL,
  next_topic_id varchar(32) NULL,
  next_client_id varchar(32) NULL,
  expire_time number(20),
  PRIMARY KEY (idmm_msg_id,dst_cli_id,dst_topic_id)
);
create Index msgidx_part_0_idx on msgidx_part_0(dst_cli_id,dst_topic_id);


-- 导出  表 idmm2.msgidx_part_err 结构
CREATE TABLE msgidx_part_err (
  idmm_msg_id varchar(60) NOT NULL,
  produce_cli_id varchar(32) NULL,
  src_topic_id varchar(32) NULL,
  dst_cli_id varchar(32) NOT NULL ,
  dst_topic_id varchar(32) NOT NULL ,
  src_commit_code varchar(4) NULL,
  group_id varchar(32) NULL,
  priority number(11) NOT NULL ,
  idmm_resend number(11) NULL,
  consumer_resend number(11) NULL,
  create_time number(20) NULL,
  broker_id varchar(21) NULL,
  req_time number(20) NULL,
  commit_code varchar(4) NULL,
  commit_time number(20) NULL,
  commit_desc varchar(160) NULL,
  next_topic_id varchar(8) NULL,
  next_client_id varchar(8) NULL,
  PRIMARY KEY (idmm_msg_id,dst_cli_id,dst_topic_id)
);

-- 导出  表 idmm2.msgidx_part_his_0 结构
CREATE TABLE msgidx_part_his_0 (
  idmm_msg_id varchar(60) NOT NULL,
  produce_cli_id varchar(32) NULL,
  src_topic_id varchar(32) NULL,
  dst_cli_id varchar(32) NOT NULL,
  dst_topic_id varchar(32) NOT NULL,
  src_commit_code varchar(4) NULL,
  group_id varchar(32) NULL,
  priority number(11) NOT NULL ,
  idmm_resend number(11) NULL,
  consumer_resend number(11) NULL,
  create_time number(20) NULL,
  broker_id varchar(21) NULL,
  req_time number(20) NULL,
  commit_code varchar(4) NULL,
  commit_time number(20) NULL,
  commit_desc varchar(160) NULL,
  next_topic_id varchar(32) NULL,
  next_client_id varchar(32) NULL,
  expire_time number(20) DEFAULT '0',
  PRIMARY KEY  (idmm_msg_id,dst_cli_id,dst_topic_id)
);
-- 导出  表 idmm2.priority_map_8 结构
CREATE TABLE priority_map_8 (
  pname varchar(32) NOT NULL,
  pvalue number(11) NOT NULL,
  is_default char(1) NULL,
  note varchar(64) NULL,
  PRIMARY KEY (pname)
);

-- 正在导出表  idmm2.priority_map_8 的数据：~2 rows (大约)
/*!40000 ALTER TABLE priority_map_8 DISABLE KEYS */;
INSERT INTO priority_map_8 (pname, pvalue, is_default, note) VALUES
	('high', 300, '0', '');
INSERT INTO priority_map_8 (pname, pvalue, is_default, note) VALUES
	('low', 100, '0', '');
INSERT INTO priority_map_8 (pname, pvalue, is_default, note) VALUES
	('middle', 200, '1', '');

-- 导出  表 idmm2.src_topic_info_8 结构
CREATE TABLE src_topic_info_8 (
  src_topic_id varchar(32) NOT NULL ,
  src_topic_desc varchar(2048) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (src_topic_id)
);

INSERT INTO src_topic_info_8 (src_topic_id, src_topic_desc, use_status, login_no, opr_time, note) VALUES
	('TUrStatusToOboss', '原始主题-用户状态CRM同步至一级BOSS', '1', 'admin', to_date('2015-08-14 19:15:38', 'yyyy-mm-dd hh24:mi:ss'), NULL);
INSERT INTO src_topic_info_8 (src_topic_id, src_topic_desc, use_status, login_no, opr_time, note) VALUES
	('TRecOprCntt', '操作类接触信息工单', '1', 'admin', to_date('2015-07-02 15:11:39', 'yyyy-mm-dd hh24:mi:ss'), NULL);



-- 导出  表 idmm2.topic_attribute_info_8 结构
CREATE TABLE topic_attribute_info_8 (
  src_topic_id varchar(32) NOT NULL ,
  attribute_key char(32) NOT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (src_topic_id,attribute_key)
);

-- 正在导出表  idmm2.topic_attribute_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE topic_attribute_info_8 DISABLE KEYS */;
INSERT INTO topic_attribute_info_8 (src_topic_id, attribute_key, use_status, login_no, opr_time, note) VALUES
	('TRecOprCntt', '_all', '1', 'admin', to_date('2015-07-02 15:19:33', 'yyyy-mm-dd hh24:mi:ss'), '不分区');
/*!40000 ALTER TABLE topic_attribute_info_8 ENABLE KEYS */;


drop table topic_mapping_rel_8;
-- 导出  表 idmm2.topic_mapping_rel_8 结构
CREATE TABLE topic_mapping_rel_8 (
  src_topic_id varchar(32) NOT NULL ,
  attribute_key varchar(32) NOT NULL,
  attribute_value varchar(32) NOT NULL,
  dest_topic_id varchar(32) NOT NULL ,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (src_topic_id,attribute_key,attribute_value,dest_topic_id)
);

-- 正在导出表  idmm2.topic_mapping_rel_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE topic_mapping_rel_8 DISABLE KEYS */;
INSERT INTO topic_mapping_rel_8 (src_topic_id, attribute_key, attribute_value, dest_topic_id, use_status, login_no, opr_time, note) VALUES
	('TRecOprCntt', '_all', '_default', 'TRecOprCnttDest', '1', 'admin', to_date('2015-07-02 15:19:29', 'yyyy-mm-dd hh24:mi:ss'), NULL);
/*!40000 ALTER TABLE topic_mapping_rel_8 ENABLE KEYS */;


-- 导出  表 idmm2.topic_publish_rel_8 结构
CREATE TABLE topic_publish_rel_8 (
  client_id varchar(32) NOT NULL ,
  src_topic_id varchar(32) NOT NULL ,
  client_pswd char(32) NULL,
  use_status char(1) NOT NULL,
  login_no char(32) NULL,
  opr_time date NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (client_id,src_topic_id)
);

-- 正在导出表  idmm2.topic_publish_rel_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE topic_publish_rel_8 DISABLE KEYS */;
INSERT INTO topic_publish_rel_8 (client_id, src_topic_id, client_pswd, use_status, login_no, opr_time, note) VALUES
	('Pub101', 'TRecOprCntt', '_null', '1', 'admin', to_date('2015-07-02 15:19:31', 'yyyy-mm-dd hh24:mi:ss'), NULL);
/*!40000 ALTER TABLE topic_publish_rel_8 ENABLE KEYS */;


drop table topic_subscribe_rel_8;
CREATE TABLE topic_subscribe_rel_8 (
  client_id varchar(32) NOT NULL,
  dest_topic_id varchar(32) NOT NULL,
  client_pswd char(32) DEFAULT NULL ,
  max_request number(3) DEFAULT NULL,
  min_timeout number(8) DEFAULT NULL,
  max_timeout number(8) DEFAULT NULL,
  use_status char(1) NOT NULL,
  login_no char(32) DEFAULT NULL ,
  opr_time date DEFAULT NULL,
  note varchar(2048) DEFAULT NULL ,
  consume_speed_limit number(11) DEFAULT '0',
  max_messages number(11) DEFAULT '10000',
  warn_messages number(11) DEFAULT '1000',
  PRIMARY KEY (client_id,dest_topic_id)
)

-- 正在导出表  idmm2.topic_subscribe_rel_9 的数据：~3 rows (大约)
/*!40000 ALTER TABLE topic_subscribe_rel_9 DISABLE KEYS */;
INSERT INTO topic_subscribe_rel_8 (client_id, dest_topic_id, client_pswd, max_request, min_timeout, max_timeout, use_status, login_no, opr_time, note, consume_speed_limit, max_messages, warn_messages) VALUES
	('notice_sub_1', 'notice_1', '_null', 20, 5000, 5000, '1', 'admin', to_date('2015-07-02 20:51:31', 'yyyy-mm-dd hh24:mi:ss'), NULL, 0, 10000, 1000);
INSERT INTO topic_subscribe_rel_8 (client_id, dest_topic_id, client_pswd, max_request, min_timeout, max_timeout, use_status, login_no, opr_time, note, consume_speed_limit, max_messages, warn_messages) VALUES
	('Sub108', 'TUrStatusToOboss', '_null', 20, 5000, 5000, '1', 'admin', to_date('2015-08-14 19:15:39', 'yyyy-mm-dd hh24:mi:ss'), NULL, 0, 10000, 1000);
INSERT INTO topic_subscribe_rel_8 (client_id, dest_topic_id, client_pswd, max_request, min_timeout, max_timeout, use_status, login_no, opr_time, note, consume_speed_limit, max_messages, warn_messages) VALUES
	('Sub119Opr', 'TRecOprCnttDest', '1', 10, 10, 600, '1', 'a', to_date('2015-08-22 13:39:38', 'yyyy-mm-dd hh24:mi:ss'), 'aa', 0, 300, 100);


-- 导出  表 idmm2.t_sequence 结构
CREATE TABLE t_sequence (
  table_name varchar(50) NOT NULL,
  column_name varchar(50) NOT NULL,
  next_value number(11) NOT NULL,
  seq_increment number(11) NOT NULL,
  note varchar(2048) NULL,
  PRIMARY KEY (table_name,column_name)
);

-- 正在导出表  idmm2.t_sequence 的数据：~10 rows (大约)
/*!40000 ALTER TABLE t_sequence DISABLE KEYS */;
-- INSERT INTO t_sequence (table_name, column_name, next_value, seq_increment, note) VALUES
INSERT INTO t_sequence VALUES	('BLE_BASE_INFO_5', 'BLE_ID', 1, 1, NULL);
INSERT INTO t_sequence VALUES		('BLE_BASE_INFO_6', 'BLE_ID', 2, 1, NULL);
INSERT INTO t_sequence VALUES		('BLE_BASE_INFO_7', 'BLE_ID', 3, 1, NULL);
INSERT INTO t_sequence VALUES		('BLE_BASE_INFO_8', 'BLE_ID', 2, 1, NULL);
INSERT INTO t_sequence VALUES		('BROKER_BASE_INFO_5', 'BROKER_ID', 1, 1, NULL);
INSERT INTO t_sequence VALUES		('BROKER_BASE_INFO_6', 'BROKER_ID', 3, 1, NULL);
INSERT INTO t_sequence VALUES		('BROKER_BASE_INFO_7', 'BROKER_ID', 2, 1, NULL);
INSERT INTO t_sequence VALUES		('BROKER_BASE_INFO_8', 'BROKER_ID', 2, 1, NULL);
INSERT INTO t_sequence VALUES		('IDMM_MANAGER_BK_MON_DATA', 'id', 4, 1, NULL);
INSERT INTO t_sequence VALUES		('IDMM_VERSION_INFO', 'CONFIG_VERSION', 9, 1, NULL);


-- 导出  表 idmm2.white_list_8 结构
CREATE TABLE white_list_8 (
  ip varchar(15) NOT NULL,
  index_id varchar(60) NOT NULL,
  use_status varchar(1) NOT NULL,
  PRIMARY KEY (ip,use_status)
);
