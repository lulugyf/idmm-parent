
CREATE TABLE `ble_base_info_%d` (
	`BLE_id` DECIMAL(8,0) NOT NULL COMMENT 'BLE节点标识',
	`id_number` DECIMAL(1,0) NOT NULL COMMENT '节点序号',
	`addr_ip` CHAR(15) NOT NULL COMMENT '节点ip地址',
	`addr_port` DECIMAL(5,0) NOT NULL COMMENT '节点通信端口',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1\\r\\n指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；\\r\\n生产中实际运行的节点状态可以从zookeeper中获得； \\r\\n1：在用\\r\\n0：停用',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`BLE_id`, `id_number`),
	UNIQUE INDEX `BLE_id` (`BLE_id`, `id_number`)
)
COMMENT='BLE基本信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `ble_dest_topic_rel_%d` (
	`dest_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`BLE_id` DECIMAL(8,0) NOT NULL COMMENT 'BLE节点标识',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`dest_topic_id`, `BLE_id`),
	UNIQUE INDEX `dest_topic_id` (`dest_topic_id`, `BLE_id`)
)
COMMENT='目标主题归属BLE关系表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `broker_base_info_%d` (
	`broker_id` DECIMAL(8,0) NOT NULL COMMENT 'Broker节点标识 : >0',
	`comm_ip` CHAR(15) NOT NULL COMMENT 'ip地址 : IP地址格式',
	`comm_port` DECIMAL(15,0) NOT NULL COMMENT '通信端口 : 1025-65535',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1\\r\\n指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；\\r\\n生产中实际运行的节点状态可以从zookeeper中获得； \\r\\n1：在用\\r\\n0：停用',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`broker_id`)
)
COMMENT='Broker基本信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `client_base_info_%d` (
	`client_id` VARCHAR(32) NOT NULL DEFAULT '',
	`sub_system` CHAR(32) NOT NULL COMMENT '归属子系统 : 子系统名称',
	`client_desc` VARCHAR(2048) NOT NULL COMMENT 'Client身份说明 : 自定义格式',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0：停用\\r\\n1：使用',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`client_id`)
)
COMMENT='client基本信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `client_limit_info_%d` (
	`client_id` CHAR(8) NOT NULL COMMENT 'client标识',
	`limit_key` CHAR(8) NOT NULL COMMENT '限制类型',
	`limit_value` VARCHAR(2048) NOT NULL COMMENT '限制范围',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`client_id`, `limit_key`)
)
COMMENT='client访问控制表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `consume_notice_info_%d` (
	`producer_client_id` CHAR(32) NOT NULL COMMENT '生产者客户端id',
	`src_topic_id` CHAR(32) NOT NULL COMMENT '原始主题id : 前缀s',
	`dest_topic_id` CHAR(32) NOT NULL COMMENT '目标主题id',
	`consumer_client_id` CHAR(32) NOT NULL COMMENT '消费者客户端',
	`notice_topic_id` CHAR(32) NOT NULL COMMENT '消费结果目标主题',
	`notice_client_id` CHAR(32) NOT NULL COMMENT '接收消费结果的客户端id，可以和生产者相同',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`producer_client_id`, `src_topic_id`, `dest_topic_id`, `consumer_client_id`, `notice_topic_id`),
	UNIQUE INDEX `producer_client_id` (`producer_client_id`, `src_topic_id`, `dest_topic_id`, `consumer_client_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `consume_order_info_%d` (
	`producer_client_id` CHAR(8) NOT NULL COMMENT '生产者客户端id',
	`src_topic_id` CHAR(8) NOT NULL COMMENT '原始主题id : 前缀s',
	`attribute_key` CHAR(32) NOT NULL COMMENT '属性key : 含_all',
	`attribute_value` CHAR(32) NOT NULL COMMENT '属性value : 含_default',
	`dest_topic_id` CHAR(8) NOT NULL COMMENT '目标主题id',
	`consumer_client_id` CHAR(8) NOT NULL COMMENT '消费者客户端',
	`consume_seq` INT(11) NOT NULL COMMENT '消费次序 : 从0开始计数',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`producer_client_id`, `src_topic_id`, `attribute_key`, `attribute_value`, `dest_topic_id`, `consumer_client_id`, `consume_seq`),
	UNIQUE INDEX `producer_client_id` (`producer_client_id`, `src_topic_id`, `attribute_key`, `attribute_value`, `dest_topic_id`, `consumer_client_id`, `consume_seq`)
)
COMMENT='消费顺序配置表 : 同一个clientid+原始主题+业务属性可能产生N个目标主题，这些目标主题的消费次序由consum'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `dest_topic_info_%d` (
	`dest_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`dest_topic_desc` VARCHAR(2048) NOT NULL COMMENT '目标主题描述',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`dest_topic_id`)
)
COMMENT='目标主题信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `priority_map_%d` (
	`pname` VARCHAR(32) NOT NULL COMMENT '优先级名称',
	`pvalue` INT(11) NOT NULL COMMENT '优先级数字',
	`is_default` CHAR(1) NULL DEFAULT NULL COMMENT '是否默认优先级， 只能有一个,取值Y|N',
	`note` VARCHAR(64) NULL DEFAULT NULL COMMENT '描述' COLLATE 'utf8mb4_unicode_ci',
	UNIQUE INDEX `Index 1` (`pname`)
)
COMMENT='优先级名称 与 数字的映射表'
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB;

CREATE TABLE `src_topic_info_%d` (
	`src_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`src_topic_desc` VARCHAR(2048) NOT NULL COMMENT '原始主题描述',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`src_topic_id`)
)
COMMENT='原始主题信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `topic_attribute_info_%d` (
	`src_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`attribute_key` CHAR(32) NOT NULL COMMENT '属性key : 含_all',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`src_topic_id`, `attribute_key`),
	UNIQUE INDEX `src_topic_id` (`src_topic_id`, `attribute_key`)
)
COMMENT='主题分区属性表 : 如果没有按照分区路由的需求，则只需要配置一个“_all”属性即可，表示不需要分区的情况；\r\n如果有按'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `topic_mapping_rel_%d` (
	`src_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`attribute_key` CHAR(32) NOT NULL COMMENT '属性key : 含_all',
	`attribute_value` CHAR(32) NOT NULL COMMENT '属性value : 含_default',
	`dest_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`src_topic_id`, `attribute_key`, `attribute_value`, `dest_topic_id`)
)
COMMENT='主题映射关系表 : 可以针对每一个属性值设置一个或者多个目标主题；\r\n属性值可以是“_default”，表示如果生产者没'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `topic_publish_rel_%d` (
	`client_id` VARCHAR(32) NOT NULL DEFAULT '',
	`src_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`client_pswd` CHAR(32) NULL DEFAULT NULL COMMENT '客户端密码 : 支持“_null”',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`client_id`, `src_topic_id`),
	UNIQUE INDEX `client_id` (`client_id`, `src_topic_id`)
)
COMMENT='主题发布关系表 : 用于描述生产者客户端发布原始主题消息的权限关系；\r\n如果client_pswd的密码为“_null”'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;


CREATE TABLE `topic_subscribe_rel_%d` (
	`client_id` VARCHAR(32) NOT NULL DEFAULT '',
	`dest_topic_id` VARCHAR(32) NOT NULL DEFAULT '',
	`client_pswd` CHAR(32) NULL DEFAULT NULL COMMENT '客户端密码 : 支持“_null”',
	`max_request` INT(3) NULL DEFAULT NULL COMMENT '最大并发数',
	`min_timeout` INT(8) NULL DEFAULT NULL COMMENT '最小超时时间',
	`max_timeout` INT(8) NULL DEFAULT NULL COMMENT '最大超时时间',
	`use_status` CHAR(1) NOT NULL COMMENT '使用标志 : 0&1',
	`login_no` CHAR(32) NULL DEFAULT NULL COMMENT '操作工号',
	`opr_time` DATETIME NULL DEFAULT NULL COMMENT '操作时间',
	`note` VARCHAR(2048) NULL DEFAULT NULL COMMENT '备注',
	PRIMARY KEY (`client_id`, `dest_topic_id`),
	UNIQUE INDEX `client_id` (`client_id`, `dest_topic_id`)
)
COMMENT='主题订阅关系表 : 用于描述消费者客户端接收主题消息的关系；\r\n如果client_pswd的密码为“_null”，则表示'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

CREATE TABLE `white_list_%d` (
	`ip` VARCHAR(15) NOT NULL COMMENT 'ip地址',
	`index_id` VARCHAR(60) NOT NULL COMMENT '索引id : 用于与white_list_index关联',
	`use_status` VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '使用标志 : 0&1',
	PRIMARY KEY (`ip`, `use_status`)
)
COMMENT='白名单信息表'
COLLATE='utf8_general_ci'
ENGINE=InnoDB;

