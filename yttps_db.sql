/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50553
Source Host           : localhost:3306
Source Database       : yttps_db

Target Server Type    : MYSQL
Target Server Version : 50553
File Encoding         : 65001

Date: 2018-12-27 15:52:24
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for `customer`
-- ----------------------------
DROP TABLE IF EXISTS `customer`;
CREATE TABLE `customer` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `customer_name` text NOT NULL COMMENT '客户名称（一般为企业名称）',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of customer
-- ----------------------------
INSERT INTO `customer` VALUES ('1', 'eservice', '2018-08-21 11:26:37', '2018-08-21 13:31:18');
INSERT INTO `customer` VALUES ('2', '汉堃科技', '2018-08-21 13:31:37', null);

-- ----------------------------
-- Table structure for `record`
-- ----------------------------
DROP TABLE IF EXISTS `record`;
CREATE TABLE `record` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `name` varchar(255) DEFAULT NULL COMMENT '姓名',
  `staff_id` varchar(255) NOT NULL COMMENT '员工号',
  `department` varchar(255) DEFAULT NULL COMMENT '部门名称',
  `record_time` datetime NOT NULL COMMENT '记录时间',
  `create_time` datetime NOT NULL COMMENT '插入数据库时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_Record` (`staff_id`,`record_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of record
-- ----------------------------
INSERT INTO `record` VALUES ('1', '胡通', '123', '信息技术部', '2018-12-27 08:07:52', '2018-12-27 10:07:23');
INSERT INTO `record` VALUES ('2', '胡通', '123', '信息技术部', '2018-12-27 10:07:52', '2018-12-27 11:00:11');

-- ----------------------------
-- Table structure for `user`
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `account` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('1', 'admin', 'HT', 'admin');
