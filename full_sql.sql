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

-- 导出  表 idmm2.ble_base_info_8 结构
CREATE TABLE IF NOT EXISTS `ble_base_info_8` (
  `BLE_id` decimal(8,0) NOT NULL COMMENT 'BLE节点标识',
  `id_number` decimal(1,0) NOT NULL COMMENT '节点序号',
  `addr_ip` char(15) NOT NULL COMMENT '节点ip地址',
  `addr_port` decimal(5,0) NOT NULL COMMENT '节点通信端口',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1\r\n指配置上的节点使用标志，以方便人工停用某些节点，不是实际生产中节点的实际运行状态；\r\n生产中实际运行的节点状态可以从zookeeper中获得； \r\n1：在用\r\n0：停用',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`BLE_id`,`id_number`),
  UNIQUE KEY `BLE_id` (`BLE_id`,`id_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='BLE基本信息表';

-- 正在导出表  idmm2.ble_base_info_8 的数据：~3 rows (大约)
/*!40000 ALTER TABLE `ble_base_info_8` DISABLE KEYS */;
INSERT INTO `ble_base_info_8` (`BLE_id`, `id_number`, `addr_ip`, `addr_port`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	(10000001, 1, '10.162.200.211', 5678, '1', 'admin', '2015-06-19 17:05:34', ''),
	(10000002, 2, '10.162.200.212', 5678, '1', 'admin', '2015-06-19 17:05:34', ''),
	(10000003, 3, '10.162.200.213', 5678, '1', 'admin', '2015-06-19 17:05:34', '');
/*!40000 ALTER TABLE `ble_base_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.ble_dest_topic_rel_8 结构
CREATE TABLE IF NOT EXISTS `ble_dest_topic_rel_8` (
  `dest_topic_id` varchar(32) NOT NULL DEFAULT '',
  `BLE_id` decimal(8,0) NOT NULL COMMENT 'BLE节点标识',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dest_topic_id`,`BLE_id`),
  UNIQUE KEY `dest_topic_id` (`dest_topic_id`,`BLE_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='目标主题归属BLE关系表';

-- 正在导出表  idmm2.ble_dest_topic_rel_8 的数据：~1 rows (大约)
/*!40000 ALTER TABLE `ble_dest_topic_rel_8` DISABLE KEYS */;
INSERT INTO `ble_dest_topic_rel_8` (`dest_topic_id`, `BLE_id`, `use_status`, `login_no`, `opr_time`, `note`) VALUES
	('TUrStatusToOboss', 10000001, '1', 'admin', '2015-08-14 19:15:39', NULL);
/*!40000 ALTER TABLE `ble_dest_topic_rel_8` ENABLE KEYS */;


-- 导出  表 idmm2.consume_notice_info_8 结构
CREATE TABLE IF NOT EXISTS `consume_notice_info_8` (
  `producer_client_id` char(8) NOT NULL COMMENT '生产者客户端id',
  `src_topic_id` char(8) NOT NULL COMMENT '原始主题id : 前缀s',
  `dest_topic_id` char(8) NOT NULL COMMENT '目标主题id',
  `consumer_client_id` char(8) NOT NULL COMMENT '消费者客户端',
  `notice_topic_id` char(8) NOT NULL COMMENT '消费结果目标主题',
  `notice_client_id` char(8) NOT NULL COMMENT '接收消费结果的客户端id，可以和生产者相同',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`producer_client_id`,`src_topic_id`,`dest_topic_id`,`consumer_client_id`,`notice_topic_id`),
  UNIQUE KEY `producer_client_id` (`producer_client_id`,`src_topic_id`,`dest_topic_id`,`consumer_client_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 正在导出表  idmm2.consume_notice_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE `consume_notice_info_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `consume_notice_info_8` ENABLE KEYS */;


-- 导出  表 idmm2.consume_order_info_8 结构
CREATE TABLE IF NOT EXISTS `consume_order_info_8` (
  `producer_client_id` char(8) NOT NULL COMMENT '生产者客户端id',
  `src_topic_id` char(8) NOT NULL COMMENT '原始主题id : 前缀s',
  `attribute_key` char(32) NOT NULL COMMENT '属性key : 含_all',
  `attribute_value` char(32) NOT NULL COMMENT '属性value : 含_default',
  `dest_topic_id` char(8) NOT NULL COMMENT '目标主题id',
  `consumer_client_id` char(8) NOT NULL COMMENT '消费者客户端',
  `consume_seq` int(11) NOT NULL COMMENT '消费次序 : 从0开始计数',
  `use_status` char(1) NOT NULL COMMENT '使用标志 : 0&1',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`producer_client_id`,`src_topic_id`,`attribute_key`,`attribute_value`,`dest_topic_id`,`consumer_client_id`,`consume_seq`),
  UNIQUE KEY `producer_client_id` (`producer_client_id`,`src_topic_id`,`attribute_key`,`attribute_value`,`dest_topic_id`,`consumer_client_id`,`consume_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消费顺序配置表 : 同一个clientid+原始主题+业务属性可能产生N个目标主题，这些目标主题的消费次序由consum';

-- 正在导出表  idmm2.consume_order_info_8 的数据：~0 rows (大约)
/*!40000 ALTER TABLE `consume_order_info_8` DISABLE KEYS */;
/*!40000 ALTER TABLE `consume_order_info_8` ENABLE KEYS */;


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


-- 导出  表 idmm2.idmm_version_info 结构
CREATE TABLE IF NOT EXISTS `idmm_version_info` (
  `config_version` decimal(8,0) NOT NULL COMMENT '配置版本号 : 配置版本号,>0',
  `version_status` char(1) NOT NULL COMMENT '版本状态 : 版本状态,0&1',
  `version_desc` varchar(2048) NOT NULL COMMENT '版本描述',
  `login_no` char(32) DEFAULT NULL COMMENT '操作工号',
  `opr_time` datetime DEFAULT NULL COMMENT '操作时间',
  `note` varchar(2048) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='版本管理表 : 该表中有且仅有一条记录的版本状态为’1’，就是当前在用的配置版本号；当有新版本发布时，在该表中插入新的记';

-- 正在导出表  idmm2.idmm_version_info 的数据：~3 rows (大约)
/*!40000 ALTER TABLE `idmm_version_info` DISABLE KEYS */;
INSERT INTO `idmm_version_info` (`config_version`, `version_status`, `version_desc`, `login_no`, `opr_time`, `note`) VALUES
	(6, '0', '1', 'xxx', '2015-05-18 12:56:22', '中文'),
	(7, '0', '测试1', 'admin', '2015-06-19 17:02:14', ''),
	(8, '1', '测试版本1', 'admin', '2015-06-19 17:09:48', '0619');
/*!40000 ALTER TABLE `idmm_version_info` ENABLE KEYS */;


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
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

-- 增加消费速度限制， 单位 n/miniute
alter table topic_subscribe_rel_9
  add consume_speed_limit int not null default -1;

-- 增加 积压消息数  最大值和告警值
alter table topic_subscribe_rel_9
  add max_messages int null,
  add warn_messages int null;
