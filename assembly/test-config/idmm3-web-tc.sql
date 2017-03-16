SET SESSION FOREIGN_KEY_CHECKS=0;

/* Drop Tables */

DROP TABLE IF EXISTS tc_ble_dest_topic_xxx;
DROP TABLE IF EXISTS tc_ble_xxx;
DROP TABLE IF EXISTS tc_broker_xxx;
DROP TABLE IF EXISTS tc_client_limit_xxx;
DROP TABLE IF EXISTS tc_client_xxx;
DROP TABLE IF EXISTS tc_consume_notice_xxx;
DROP TABLE IF EXISTS tc_consume_order_xxx;
DROP TABLE IF EXISTS tc_dest_topic_xxx;
DROP TABLE IF EXISTS tc_pri_map_xxx;
DROP TABLE IF EXISTS tc_sequence;
DROP TABLE IF EXISTS tc_src_topic_xxx;
DROP TABLE IF EXISTS tc_topic_attr_xxx;
DROP TABLE IF EXISTS tc_topic_map_xxx;
DROP TABLE IF EXISTS tc_topic_pub_xxx;
DROP TABLE IF EXISTS tc_topic_sub_xxx;
DROP TABLE IF EXISTS tc_version;
DROP TABLE IF EXISTS tc_version_check;
DROP TABLE IF EXISTS tc_white_index_xxx;
DROP TABLE IF EXISTS tc_white_xxx;




/* Create Tables */

-- 目标主题归属BLE关系表
CREATE TABLE tc_ble_dest_topic_xxx
(
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	BLE_id numeric(8) NOT NULL COMMENT 'BLE节点标识',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (dest_topic_id),
	UNIQUE (dest_topic_id)
) COMMENT = '目标主题归属BLE关系表' DEFAULT CHARACTER SET utf8;


-- BLE基本信息表
CREATE TABLE tc_ble_xxx
(
	BLE_id numeric(8) NOT NULL COMMENT 'BLE节点标识',
	id_number numeric(1) NOT NULL COMMENT '节点序号',
	addr_ip char(15) NOT NULL COMMENT '节点ip地址',
	addr_port numeric(5) NOT NULL COMMENT '节点通信端口',
	-- 0&1
	-- 指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；
	-- 生产中实际运行的节点状态可以从zookeeper中获得； 
	-- 1：在用
	-- 0：停用
	use_status char NOT NULL COMMENT '使用标志 : 0&1
指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；
生产中实际运行的节点状态可以从zookeeper中获得； 
1：在用
0：停用',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (BLE_id, id_number),
	UNIQUE (BLE_id, id_number),
	UNIQUE (BLE_id, addr_ip, addr_port)
) COMMENT = 'BLE基本信息表' DEFAULT CHARACTER SET utf8;


-- Broker基本信息表
CREATE TABLE tc_broker_xxx
(
	-- >0
	broker_id numeric(8) NOT NULL COMMENT 'Broker节点标识 : >0',
	-- IP地址格式
	comm_ip char(15) NOT NULL COMMENT 'ip地址 : IP地址格式',
	-- 1025-65535
	comm_port numeric(15) NOT NULL COMMENT '通信端口 : 1025-65535',
	-- 0&1
	-- 指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；
	-- 生产中实际运行的节点状态可以从zookeeper中获得； 
	-- 1：在用
	-- 0：停用
	use_status char NOT NULL COMMENT '使用标志 : 0&1
指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；
生产中实际运行的节点状态可以从zookeeper中获得； 
1：在用
0：停用',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (broker_id)
) COMMENT = 'Broker基本信息表' DEFAULT CHARACTER SET utf8;


-- client访问控制表
CREATE TABLE tc_client_limit_xxx
(
	-- 客户端id
	client_id char(32) NOT NULL COMMENT '客户端id : 客户端id',
	limit_key char(8) NOT NULL COMMENT '限制类型',
	limit_value varchar(2048) NOT NULL COMMENT '限制范围',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (client_id, limit_key)
) COMMENT = 'client访问控制表' DEFAULT CHARACTER SET utf8;


-- client基本信息表
CREATE TABLE tc_client_xxx
(
	-- 客户端id
	client_id char(32) NOT NULL COMMENT '客户端id : 客户端id',
	-- 子系统名称
	sub_system char(32) NOT NULL COMMENT '归属子系统 : 子系统名称',
	-- 自定义格式
	client_desc varchar(2048) NOT NULL COMMENT 'Client身份说明 : 自定义格式',
	-- 0：停用
	-- 1：使用
	use_status char NOT NULL COMMENT '使用标志 : 0：停用
1：使用',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (client_id)
) COMMENT = 'client基本信息表' DEFAULT CHARACTER SET utf8;


-- 消费结果回送配置表
CREATE TABLE tc_consume_notice_xxx
(
	producer_client_id char(32) NOT NULL COMMENT '生产者客户端id',
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	consumer_client_id char(32) NOT NULL COMMENT '消费者客户端',
	notice_topic_id char(32) NOT NULL COMMENT '保存消费结果的目标主题',
	-- 接收消费结果的客户端id，可以和生产者相同
	notice_client_id char(32) NOT NULL COMMENT '接收消费结果的客户端id : 接收消费结果的客户端id，可以和生产者相同',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (producer_client_id, src_topic_id, dest_topic_id, consumer_client_id)
) COMMENT = '消费结果回送配置表' DEFAULT CHARACTER SET utf8;


-- 消费顺序配置表 : 同一个clientid+原始主题+业务属性可能产生N个目标主题，这些目标主题的消费次序由consum
CREATE TABLE tc_consume_order_xxx
(
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	-- 含_all
	attribute_key char(32) NOT NULL COMMENT '属性key : 含_all',
	-- 含_default
	attribute_value char(32) NOT NULL COMMENT '属性value : 含_default',
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	consumer_client_id char(32) NOT NULL COMMENT '消费者客户端',
	-- 从0开始计数
	consume_seq int NOT NULL COMMENT '消费次序 : 从0开始计数',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (src_topic_id, attribute_key, attribute_value, dest_topic_id, consumer_client_id, consume_seq)
) COMMENT = '消费顺序配置表 : 同一个clientid+原始主题+业务属性可能产生N个目标主题，这些目标主题的消费次序由consum' DEFAULT CHARACTER SET utf8;


-- 目标主题信息表
CREATE TABLE tc_dest_topic_xxx
(
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	dest_topic_desc varchar(2048) NOT NULL COMMENT '目标主题描述',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (dest_topic_id)
) COMMENT = '目标主题信息表' DEFAULT CHARACTER SET utf8;


-- 优先级映射表 : 优先级名称 与 数字的映射表
CREATE TABLE tc_pri_map_xxx
(
	-- 优先级名称
	pname varchar(32) NOT NULL COMMENT '优先级名称 : 优先级名称',
	-- 优先级数字
	pvalue int NOT NULL COMMENT '优先级数字 : 优先级数字',
	-- 是否默认优先级， 只能有一个
	-- Y 是
	-- N 否
	-- 
	is_default char(1) COMMENT '是否默认优先级 : 是否默认优先级， 只能有一个
Y 是
N 否
',
	-- 描述
	note varchar(64) COMMENT '描述 : 描述',
	PRIMARY KEY (pvalue),
	UNIQUE (pvalue)
) COMMENT = '优先级映射表 : 优先级名称 与 数字的映射表' DEFAULT CHARACTER SET utf8;


-- 序列表
CREATE TABLE tc_sequence
(
	table_name varchar(50) NOT NULL COMMENT '表名',
	column_name varchar(50) NOT NULL COMMENT '列名',
	next_value int NOT NULL COMMENT '下一个值',
	seq_increment int NOT NULL COMMENT '步长',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (table_name, column_name)
) COMMENT = '序列表' DEFAULT CHARACTER SET utf8;


-- 原始主题信息表
CREATE TABLE tc_src_topic_xxx
(
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	src_topic_desc varchar(2048) NOT NULL COMMENT '原始主题描述',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (src_topic_id)
) COMMENT = '原始主题信息表' DEFAULT CHARACTER SET utf8;


-- 主题分区属性表 : 如果没有按照分区路由的需求，则只需要配置一个“_all”属性即可，表示不需要分区的情况；
-- 如果有按
CREATE TABLE tc_topic_attr_xxx
(
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	-- 含_all
	attribute_key char(32) NOT NULL COMMENT '属性key : 含_all',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (src_topic_id, attribute_key),
	UNIQUE (src_topic_id, attribute_key)
) COMMENT = '主题分区属性表 : 如果没有按照分区路由的需求，则只需要配置一个“_all”属性即可，表示不需要分区的情况；
如果有按' DEFAULT CHARACTER SET utf8;


-- 主题映射关系表 : 可以针对每一个属性值设置一个或者多个目标主题；
-- 属性值可以是“_default”，表示如果生产者没
CREATE TABLE tc_topic_map_xxx
(
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	-- 含_all
	attribute_key char(32) NOT NULL COMMENT '属性key : 含_all',
	-- 含_default
	attribute_value char(32) NOT NULL COMMENT '属性value : 含_default',
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (src_topic_id, attribute_key, attribute_value, dest_topic_id)
) COMMENT = '主题映射关系表 : 可以针对每一个属性值设置一个或者多个目标主题；
属性值可以是“_default”，表示如果生产者没' DEFAULT CHARACTER SET utf8;


-- 主题发布关系表 : 用于描述生产者客户端发布原始主题消息的权限关系；
CREATE TABLE tc_topic_pub_xxx
(
	-- 客户端id
	client_id char(32) NOT NULL COMMENT '客户端id : 客户端id',
	-- 前缀s
	src_topic_id char(32) NOT NULL COMMENT '原始主题id : 前缀s',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (client_id, src_topic_id),
	UNIQUE (client_id, src_topic_id)
) COMMENT = '主题发布关系表 : 用于描述生产者客户端发布原始主题消息的权限关系；' DEFAULT CHARACTER SET utf8;


-- 主题订阅关系表 : 用于描述消费者客户端接收主题消息的关系；
CREATE TABLE tc_topic_sub_xxx
(
	-- 客户端id
	client_id char(32) NOT NULL COMMENT '客户端id : 客户端id',
	dest_topic_id char(32) NOT NULL COMMENT '目标主题id',
	max_request int(3) COMMENT '最大并发数',
	min_timeout int(8) COMMENT '最小超时时间',
	max_timeout int(8) COMMENT '最大超时时间',
	-- 消费速度限制， 单位 n/miniute
	consume_speed_limit int DEFAULT -1 NOT NULL COMMENT '消费速度限制 : 消费速度限制， 单位 n/miniute',
	-- 积压消息数  最大值
	max_messages int COMMENT '积压消息数最大值 : 积压消息数  最大值',
	-- 积压消息数告警值
	warn_messages int COMMENT '积压消息数告警值 : 积压消息数告警值',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (client_id, dest_topic_id),
	UNIQUE (client_id, dest_topic_id)
) COMMENT = '主题订阅关系表 : 用于描述消费者客户端接收主题消息的关系；' DEFAULT CHARACTER SET utf8;


-- 版本管理表 : 该表中有且仅有一条记录的版本状态为’1’，就是当前在用的配置版本号；当有新版本发布时，在该表中插入新的记
CREATE TABLE tc_version
(
	-- 配置版本号,>0
	config_version numeric(8) NOT NULL COMMENT '配置版本号 : 配置版本号,>0',
	-- 0 审核通过
	-- 1 使用中
	-- 2 编辑中
	-- 3 待审核
	version_status char NOT NULL COMMENT '版本状态 : 0 审核通过
1 使用中
2 编辑中
3 待审核',
	version_desc varchar(2048) NOT NULL COMMENT '版本描述',
	-- 操作工号
	login_no varchar(32) COMMENT '操作工号 : 操作工号',
	opr_time datetime COMMENT '操作时间',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (config_version)
) COMMENT = '版本管理表 : 该表中有且仅有一条记录的版本状态为’1’，就是当前在用的配置版本号；当有新版本发布时，在该表中插入新的记' DEFAULT CHARACTER SET utf8;


-- 版本配置数据校验规则表
CREATE TABLE tc_version_check
(
	check_type varchar(60) NOT NULL COMMENT '校验大类',
	check_num varchar(10) NOT NULL COMMENT '校验规则编号',
	check_object varchar(60) NOT NULL COMMENT '校验对象',
	check_desc varchar(200) NOT NULL COMMENT '校验规则中文描述',
	check_sql varchar(4000) NOT NULL COMMENT '校验sql',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	note varchar(2048) COMMENT '备注',
	PRIMARY KEY (check_type, check_num)
) COMMENT = '版本配置数据校验规则表' DEFAULT CHARACTER SET utf8;


-- 白名单信息索引表
CREATE TABLE tc_white_index_xxx
(
	-- 用于与white_list_index关联
	index_id varchar(60) NOT NULL COMMENT '索引id : 用于与white_list_index关联',
	begin_ip varchar(15) NOT NULL COMMENT '起始ip地址',
	end_ip varchar(15) NOT NULL COMMENT '终止ip地址',
	-- 0&1
	use_status varchar(1) NOT NULL COMMENT '使用标志 : 0&1',
	PRIMARY KEY (index_id)
) COMMENT = '白名单信息索引表' DEFAULT CHARACTER SET utf8;


-- 白名单信息表
CREATE TABLE tc_white_xxx
(
	ip varchar(15) NOT NULL COMMENT 'ip地址',
	-- 用于与white_list_index关联
	index_id varchar(60) NOT NULL COMMENT '索引id : 用于与white_list_index关联',
	-- 0&1
	use_status varchar(1) DEFAULT '1' NOT NULL COMMENT '使用标志 : 0&1',
	PRIMARY KEY (ip, use_status)
) COMMENT = '白名单信息表' DEFAULT CHARACTER SET utf8;



