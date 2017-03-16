-- --------------------------------------------------------
-- 主机:                           172.21.3.101
-- 服务器版本:                        5.6.24-enterprise-commercial-advanced - MySQL Enterprise Server - Advanced Edition (Commercial)
-- 服务器操作系统:                      linux-glibc2.5
-- HeidiSQL 版本:                  8.2.0.4675
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- 导出  表 idmm2.broker_base_info_8 结构
CREATE TABLE IF NOT EXISTS `broker_base_info_8` (
  `broker_id` decimal(8,0) NOT NULL COMMENT 'Broker节点标识 : >0',
  `comm_ip` char(15) NOT NULL COMMENT 'ip地址 : IP地址格式',
  `comm_port` decimal(15,0) NOT NULL COMMENT '通信端口 : 1025-65535',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1\r\n指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；\r\n生产中实际运行的节点状态可以从zookeeper中获得； \r\n1：在用\r\n0：停用',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`broker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Broker基本信息表';

-- 正在导出表  idmm2.broker_base_info_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `broker_base_info_8` DISABLE KEYS */;
INSERT INTO `broker_base_info_8` (`broker_id`, `comm_ip`, `comm_port`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	(1, '172.21.1.36', 2202, '1', 'admin', '2015-06-19 17:04:17', '');
/*!40000 ALTER TABLE `broker_base_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.client_base_info_8 结构
CREATE TABLE IF NOT EXISTS `client_base_info_8` (
  `client_id` varchar(32) NOT NULL DEFAULT '',
  `sub_system` char(32) NOT NULL COMMENT '归属子系统 : 子系统名称',
  `client_desc` varchar(2048) NOT NULL COMMENT 'Client身份说明 : 自定义格式',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0：停用\r\n1：使用',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='client基本信息表';

-- 正在导出表  idmm2.client_base_info_8 的数据：~2 rows (大约)
/*!40000 ALTER TABLE `client_base_info_8` DISABLE KEYS */;
INSERT INTO `client_base_info_8` (`client_id`, `sub_system`, `client_desc`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('Pub108', 'oneBOSS', '生产者测试', '1', 'admin', '2015-08-14 19:15:38', NULL),
	('Sub108', 'oneBOSS', 'CRM同步到一级BOSS', '1', 'admin', '2015-08-14 19:15:38', NULL);
/*!40000 ALTER TABLE `client_base_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.client_limit_info_8 结构
CREATE TABLE IF NOT EXISTS `client_limit_info_8` (
  `client_id` char(8) NOT NULL COMMENT 'client标识',
  `limit_key` char(8) NOT NULL COMMENT '限制类型',
  `limit_value` varchar(2048) NOT NULL COMMENT '限制范围',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`client_id`,`limit_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='client访问控制表';

-- 正在导出表  idmm2.client_limit_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE `client_limit_info_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `client_limit_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.dest_topic_info_8 结构
CREATE TABLE IF NOT EXISTS `dest_topic_info_8` (
  `dest_topic_id` varchar(32) NOT NULL DEFAULT '',
  `dest_topic_desc` varchar(2048) NOT NULL COMMENT '目标主题描述',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dest_topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='目标主题信息表';

-- 正在导出表  idmm2.dest_topic_info_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `dest_topic_info_8` DISABLE KEYS */;
INSERT INTO `dest_topic_info_8` (`dest_topic_id`, `dest_topic_desc`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('TUrStatusToOboss', '用户状态CRM同步至一级BOSS', '1', 'admin', '2015-08-14 19:15:38', NULL);
/*!40000 ALTER TABLE `dest_topic_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.src_topic_info_8 结构
CREATE TABLE IF NOT EXISTS `src_topic_info_8` (
  `src_topic_id` varchar(32) NOT NULL DEFAULT '',
  `src_topic_desc` varchar(2048) NOT NULL COMMENT '原始主题描述',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`src_topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='原始主题信息表';

-- 正在导出表  idmm2.src_topic_info_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `src_topic_info_8` DISABLE KEYS */;
INSERT INTO `src_topic_info_8` (`src_topic_id`, `src_topic_desc`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('TUrStatusToOboss', '原始主题-用户状态CRM同步至一级BOSS', '1', 'admin', '2015-08-14 19:15:38', NULL);
/*!40000 ALTER TABLE `src_topic_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.topic_attribute_info_8 结构
CREATE TABLE IF NOT EXISTS `topic_attribute_info_8` (
  `src_topic_id` varchar(32) NOT NULL DEFAULT '',
  `attribute_key` char(32) NOT NULL COMMENT '属性key : 含_all',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`src_topic_id`,`attribute_key`),
  UNIQUE KEY `src_topic_id` (`src_topic_id`,`attribute_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题分区属性表 : 如果没有按照分区路由的需求，则只需要配置一个“_all”属性即可，表示不需要分区的情况；\r\n如果有按';

-- 正在导出表  idmm2.topic_attribute_info_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `topic_attribute_info_8` DISABLE KEYS */;
INSERT INTO `topic_attribute_info_8` (`src_topic_id`, `attribute_key`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('TUrStatusToOboss', '_all', '1', 'admin', '2015-08-14 19:15:39', '不分区');
/*!40000 ALTER TABLE `topic_attribute_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.topic_mapping_rel_8 结构
CREATE TABLE IF NOT EXISTS `topic_mapping_rel_8` (
  `src_topic_id` varchar(32) NOT NULL DEFAULT '',
  `attribute_key` char(32) NOT NULL COMMENT '属性key : 含_all',
  `attribute_value` char(32) NOT NULL COMMENT '属性value : 含_default',
  `dest_topic_id` varchar(32) NOT NULL DEFAULT '',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`src_topic_id`,`attribute_key`,`attribute_value`,`dest_topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题映射关系表 : 可以针对每一个属性值设置一个或者多个目标主题；\r\n属性值可以是“_default”，表示如果生产者没';

-- 正在导出表  idmm2.topic_mapping_rel_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `topic_mapping_rel_8` DISABLE KEYS */;
INSERT INTO `topic_mapping_rel_8` (`src_topic_id`, `attribute_key`, `attribute_value`, `dest_topic_id`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('TUrStatusToOboss', '_all', '_default', 'TUrStatusToOboss', '1', 'admin', '2015-08-14 19:15:39', NULL);
/*!40000 ALTER TABLE `topic_mapping_rel_8` ENABLE KEYS */;


-- 导出  表 idmm2.topic_publish_rel_8 结构
CREATE TABLE IF NOT EXISTS `topic_publish_rel_8` (
  `client_id` varchar(32) NOT NULL DEFAULT '',
  `src_topic_id` varchar(32) NOT NULL DEFAULT '',
  `client_pswd` char(32) DEFAULT NULL COMMENT '客户端密码 : 支持“_null”',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`client_id`,`src_topic_id`),
  UNIQUE KEY `client_id` (`client_id`,`src_topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题发布关系表 : 用于描述生产者客户端发布原始主题消息的权限关系；\r\n如果client_pswd的密码为“_null”';

-- 正在导出表  idmm2.topic_publish_rel_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `topic_publish_rel_8` DISABLE KEYS */;
INSERT INTO `topic_publish_rel_8` (`client_id`, `src_topic_id`, `client_pswd`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('Pub108', 'TUrStatusToOboss', '_null', '1', 'admin', '2015-08-14 19:15:39', NULL);
/*!40000 ALTER TABLE `topic_publish_rel_8` ENABLE KEYS */;


-- 导出  表 idmm2.topic_subscribe_rel_8 结构
CREATE TABLE IF NOT EXISTS `topic_subscribe_rel_8` (
  `client_id` varchar(32) NOT NULL DEFAULT '',
  `dest_topic_id` varchar(32) NOT NULL DEFAULT '',
  `client_pswd` char(32) DEFAULT NULL COMMENT '客户端密码 : 支持“_null”',
  `max_request` int(3) DEFAULT NULL COMMENT '最大并发数',
  `min_timeout` int(8) DEFAULT NULL COMMENT '最小超时时间',
  `max_timeout` int(8) DEFAULT NULL COMMENT '最大超时时间',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`client_id`,`dest_topic_id`),
  UNIQUE KEY `client_id` (`client_id`,`dest_topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='主题订阅关系表 : 用于描述消费者客户端接收主题消息的关系；\r\n如果client_pswd的密码为“_null”，则表示';

-- 正在导出表  idmm2.topic_subscribe_rel_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `topic_subscribe_rel_8` DISABLE KEYS */;
INSERT INTO `topic_subscribe_rel_8` (`client_id`, `dest_topic_id`, `client_pswd`, `max_request`, `min_timeout`, `max_timeout`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('Sub108', 'TUrStatusToOboss', '_null', 20, 5000, 5000, '1', 'admin', '2015-08-14 19:15:39', NULL);
/*!40000 ALTER TABLE `topic_subscribe_rel_8` ENABLE KEYS */;


-- 导出  表 idmm2.t_sequence 结构
CREATE TABLE IF NOT EXISTS `t_sequence` (
  `table_name` varchar(50) NOT NULL COMMENT '表名',
  `column_name` varchar(50) NOT NULL COMMENT '列名',
  `next_value` int(11) NOT NULL COMMENT '下一个值',
  `seq_increment` int(11) NOT NULL COMMENT '步长',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_name`,`column_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='序列表';

-- 正在导出表  idmm2.t_sequence 的数据：~10 rows (大约)
/*!40000 ALTER TABLE `t_sequence` DISABLE KEYS */;
INSERT INTO `t_sequence` (`table_name`, `column_name`, `next_value`, `seq_increment`, `note`) VALUES
	('BLE_BASE_INFO_5', 'BLE_ID', 1, 1, NULL),
	('BLE_BASE_INFO_6', 'BLE_ID', 2, 1, NULL),
	('BLE_BASE_INFO_7', 'BLE_ID', 3, 1, NULL),
	('BLE_BASE_INFO_8', 'BLE_ID', 2, 1, NULL),
	('BROKER_BASE_INFO_5', 'BROKER_ID', 1, 1, NULL),
	('BROKER_BASE_INFO_6', 'BROKER_ID', 3, 1, NULL),
	('BROKER_BASE_INFO_7', 'BROKER_ID', 2, 1, NULL),
	('BROKER_BASE_INFO_8', 'BROKER_ID', 2, 1, NULL),
	('IDMM_MANAGER_BK_MON_DATA', 'id', 4, 1, NULL),
	('IDMM_VERSION_INFO', 'CONFIG_VERSION', 9, 1, NULL);
/*!40000 ALTER TABLE `t_sequence` ENABLE KEYS */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
