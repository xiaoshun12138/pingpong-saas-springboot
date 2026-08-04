mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 8.0.45, for macos26.3 (arm64)
--
-- Host: localhost    Database: pingpong_saas
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `course_consumption`
--

DROP TABLE IF EXISTS `course_consumption`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_consumption` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `store_id` bigint NOT NULL COMMENT '门店ID，逻辑外键，无物理约束',
  `student_id` bigint NOT NULL COMMENT '学员ID，逻辑外键，无物理约束',
  `coach_id` bigint NOT NULL COMMENT '上课教练ID，逻辑外键，无物理约束',
  `course_order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `schedule_id` bigint DEFAULT NULL,
  `lessons` int DEFAULT NULL COMMENT '本次消课课时',
  `remark` varchar(256) DEFAULT NULL COMMENT '上课内容',
  `record_date` date NOT NULL COMMENT '上课日期',
  `record_time` time DEFAULT NULL COMMENT '上课时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_student_id` (`student_id`) COMMENT '用于按学员查消课历史',
  KEY `idx_coach_id` (`coach_id`) COMMENT '用于按教练查消课排名',
  KEY `idx_record_date` (`record_date`) COMMENT '用于按日期查消课',
  KEY `idx_deleted` (`deleted`),
  KEY `idx_coach_date` (`coach_id`,`record_date`) COMMENT '用于按教练查本月消课排名',
  KEY `idx_student_date` (`student_id`,`record_date`) COMMENT '用于按学员查消课历史',
  KEY `idx_course_order_id` (`course_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消课记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_consumption`
--

LOCK TABLES `course_consumption` WRITE;
/*!40000 ALTER TABLE `course_consumption` DISABLE KEYS */;
INSERT INTO `course_consumption` VALUES (1,1,1,4,1,NULL,1,'正手攻球基础','2026-07-10','14:00:00','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(2,1,1,4,1,NULL,1,'反手推挡练习','2026-07-15','15:00:00','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(3,1,1,5,2,NULL,1,'发球动作纠正','2026-07-18','10:00:00','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(4,1,3,5,4,NULL,1,'步伐移动训练','2026-07-12','16:00:00','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(5,2,5,6,6,NULL,1,'正手拉球入门','2026-07-14','09:00:00','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(6,1,1,4,1,NULL,1,'curl测试消课','2026-07-29','15:00:00','2026-07-29 15:43:16','2026-07-29 15:43:16',0),(7,1,1,4,1,NULL,1,'教练测试消课','2026-07-29','16:00:00','2026-07-29 15:44:10','2026-07-29 15:44:10',0),(8,1,1,4,1,NULL,1,'教练测试','2026-07-29','16:10:00','2026-07-29 15:59:54','2026-07-29 15:59:54',0),(9,1,1,4,1,15,1,'正手攻球','2026-07-29','09:00:00','2026-07-29 16:29:11','2026-08-03 01:16:20',0),(10,1,4,2,5,16,1,'','2026-07-27','09:00:00','2026-07-29 16:30:47','2026-08-03 01:16:20',0),(11,1,2,2,3,17,1,'','2026-07-27','10:30:00','2026-07-29 16:34:50','2026-08-03 01:16:20',0),(12,1,1,5,2,18,1,'','2026-07-28','09:00:00','2026-07-29 16:35:22','2026-08-03 01:16:20',0),(13,1,4,2,5,19,1,'','2026-07-28','09:00:00','2026-07-29 16:41:06','2026-08-03 01:16:20',0),(14,1,3,2,4,20,1,'','2026-07-28','09:00:00','2026-07-29 16:41:06','2026-08-03 01:16:20',0),(15,1,4,2,5,21,1,'','2026-07-28','10:30:00','2026-07-29 16:41:21','2026-08-03 01:16:20',0),(16,1,2,2,3,24,1,'','2026-07-28','10:30:00','2026-07-29 16:41:21','2026-08-03 01:16:20',0),(17,1,1,2,2,23,1,'','2026-07-28','09:00:00','2026-07-29 16:41:41','2026-08-03 01:16:20',0),(18,1,2,2,3,24,1,'','2026-07-28','10:30:00','2026-07-29 16:41:46','2026-08-03 01:16:20',0),(19,1,7,2,8,28,1,'','2026-07-28','14:30:00','2026-07-30 18:39:08','2026-08-03 01:16:20',0),(20,1,13,2,14,29,1,'','2026-07-28','14:30:00','2026-07-30 23:37:31','2026-08-03 01:16:20',0),(21,2,5,3,6,30,1,'','2026-07-27','09:00:00','2026-07-30 23:38:23','2026-08-03 01:16:20',0),(22,2,14,3,15,31,1,'','2026-07-27','10:30:00','2026-07-30 23:38:57','2026-08-03 01:16:20',0),(23,2,14,3,15,32,1,'','2026-07-26','10:30:00','2026-07-31 00:04:46','2026-08-03 01:16:20',0),(24,2,5,3,6,47,1,'','2026-07-29','14:30:00','2026-07-31 00:05:22','2026-08-03 01:16:20',0),(25,2,14,3,16,36,1,'','2026-07-28','10:30:00','2026-07-31 00:41:32','2026-08-03 01:16:20',0),(26,2,14,3,17,35,1,'','2026-07-27','14:30:00','2026-07-31 00:43:01','2026-08-03 01:16:20',0),(27,2,14,3,16,36,1,'','2026-07-28','10:30:00','2026-07-31 00:43:56','2026-08-03 01:16:20',0),(28,2,15,3,18,37,1,'','2026-07-28','14:30:00','2026-07-31 01:39:39','2026-08-03 01:16:20',0),(29,2,16,3,19,41,1,'','2026-07-28','14:30:00','2026-07-31 01:39:39','2026-08-03 01:16:20',0),(30,2,5,3,6,39,1,'','2026-07-28','14:30:00','2026-07-31 01:39:39','2026-08-03 01:16:20',0),(31,2,14,3,16,40,1,'','2026-07-28','14:30:00','2026-07-31 01:39:39','2026-08-03 01:16:20',0),(32,2,16,3,19,41,1,'','2026-07-28','14:30:00','2026-07-31 01:39:49','2026-08-03 01:16:20',0),(33,1,18,2,22,42,1,'','2026-07-29','10:30:00','2026-07-31 14:49:21','2026-08-03 01:16:20',0),(34,2,15,3,18,44,1,'','2026-07-29','14:30:00','2026-07-31 14:56:07','2026-08-03 01:16:20',0),(35,2,14,3,16,45,1,'','2026-07-29','14:30:00','2026-07-31 14:56:07','2026-08-03 01:16:20',0),(36,2,5,3,6,47,1,'','2026-07-29','14:30:00','2026-07-31 14:56:07','2026-08-03 01:16:20',0),(37,2,5,3,6,47,1,'','2026-07-29','14:30:00','2026-07-31 14:56:14','2026-08-03 01:16:20',0),(38,1,21,2,25,48,1,'','2026-08-02','09:00:00','2026-08-02 17:33:16','2026-08-03 01:16:20',0),(39,1,10,2,9,49,1,'','2026-08-02','09:00:00','2026-08-03 00:38:28','2026-08-03 01:16:20',0),(40,1,18,2,22,NULL,1,'','2026-08-02','09:00:00','2026-08-03 00:38:41','2026-08-03 00:38:41',0),(41,1,21,2,25,53,1,'','2026-08-02','10:30:00','2026-08-03 00:42:25','2026-08-03 01:16:20',0),(42,1,18,2,22,54,1,'','2026-08-02','10:30:00','2026-08-03 00:42:25','2026-08-03 01:16:20',0),(43,1,12,2,12,55,1,'','2026-08-02','10:30:00','2026-08-03 00:42:39','2026-08-03 01:16:20',0),(44,1,10,2,9,NULL,1,'','2026-08-02','14:00:00','2026-08-03 00:43:16','2026-08-03 00:43:16',0),(45,1,7,2,21,57,1,'','2026-08-02','14:00:00','2026-08-03 00:45:33','2026-08-03 01:16:20',0),(46,1,18,2,22,61,1,'','2026-08-02','14:00:00','2026-08-03 00:46:41','2026-08-03 01:16:20',0),(47,1,18,2,22,61,1,'','2026-08-02','14:00:00','2026-08-03 00:46:49','2026-08-03 01:16:20',0),(48,1,11,2,11,NULL,1,'','2026-08-03','09:00:00','2026-08-03 00:49:14','2026-08-03 00:49:14',0),(50,1,11,2,11,NULL,1,'','2026-08-03','10:30:00','2026-08-03 00:49:39','2026-08-03 00:49:39',0),(53,1,11,2,11,NULL,1,'','2026-08-02','15:00:00','2026-08-03 00:55:10','2026-08-03 00:55:10',0),(54,1,13,2,14,NULL,1,'','2026-08-05','14:00:00','2026-08-03 00:55:47','2026-08-03 00:55:47',0),(56,1,17,2,23,114,1,'','2026-08-03','09:00:00','2026-08-03 01:41:21','2026-08-03 01:41:21',0),(57,1,13,2,13,115,1,'','2026-08-03','10:30:00','2026-08-03 01:41:37','2026-08-03 01:41:37',0),(58,1,11,2,11,116,1,'','2026-08-03','10:00:00','2026-08-03 01:45:30','2026-08-03 01:45:30',0),(59,1,2,2,7,117,1,'','2026-08-02','19:00:00','2026-08-03 01:48:46','2026-08-03 01:48:46',0),(61,1,17,2,23,119,1,'','2026-08-03','14:00:00','2026-08-03 01:50:30','2026-08-03 01:50:30',0),(62,1,11,2,11,120,1,'','2026-08-03','15:30:00','2026-08-03 01:50:43','2026-08-03 01:50:43',0),(63,1,7,2,21,121,1,'','2026-08-03','18:00:00','2026-08-03 01:50:53','2026-08-03 01:50:53',0),(67,2,23,3,26,125,1,'','2026-08-04','09:00:00','2026-08-04 15:55:00','2026-08-04 15:55:00',0),(68,2,22,3,27,126,1,'','2026-08-04','09:00:00','2026-08-04 15:55:00','2026-08-04 15:55:00',0);
/*!40000 ALTER TABLE `course_consumption` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_order`
--

DROP TABLE IF EXISTS `course_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号，全平台唯一',
  `type` varchar(8) DEFAULT 'new' COMMENT '类型：new-新报 renew-续费',
  `store_id` bigint NOT NULL DEFAULT '0' COMMENT '下单时门店ID（冗余）',
  `student_id` bigint NOT NULL COMMENT '学员ID，逻辑外键，无物理约束',
  `sales_id` bigint DEFAULT NULL COMMENT '销售员工ID',
  `coach_id` bigint DEFAULT NULL COMMENT '负责教练ID，逻辑外键，无物理约束',
  `course_type_id` bigint DEFAULT NULL COMMENT '课包类型ID',
  `paid_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `total_lessons` int NOT NULL COMMENT '购买总课时',
  `remaining_lessons` int NOT NULL COMMENT '剩余课时',
  `consumed_lessons` int DEFAULT '0' COMMENT '已消耗课时',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `source` varchar(16) DEFAULT NULL COMMENT '成交渠道：douyin-抖音 meituan-美团 referral-转介绍 ground-地推 walkin-自然到店 sales_call-销售回访 other-其他',
  `remark` varchar(512) DEFAULT NULL COMMENT '备注',
  `status` varchar(16) DEFAULT 'active' COMMENT '状态：active-正常 refunded-退款 voided-作废 completed-已完结',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`) COMMENT '订单号唯一',
  KEY `idx_student_id` (`student_id`) COMMENT '用于按学员ID查订单历史',
  KEY `idx_coach_id` (`coach_id`) COMMENT '用于按教练查业绩',
  KEY `idx_status` (`status`),
  KEY `idx_source` (`source`),
  KEY `idx_created_at` (`created_at`) COMMENT '用于按时间范围查订单',
  KEY `idx_deleted` (`deleted`),
  KEY `idx_student_status` (`student_id`,`status`) COMMENT '用于查学员有效订单',
  KEY `idx_sales_id` (`sales_id`) COMMENT '用于按销售查业绩排名',
  KEY `idx_course_type_id` (`course_type_id`),
  KEY `idx_sales_created` (`sales_id`,`created_at`) COMMENT '用于按销售查本月业绩排名'
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_order`
--

LOCK TABLES `course_order` WRITE;
/*!40000 ALTER TABLE `course_order` DISABLE KEYS */;
INSERT INTO `course_order` VALUES (1,'ORD-20260701-001','new',1,1,7,4,2,3000.00,30,24,6,4,'referral',NULL,'active','2026-07-29 15:15:10','2026-07-29 16:29:11',0),(2,'ORD-20260701-002','new',1,1,7,5,1,500.00,10,0,3,3,'meituan',NULL,'refunded','2026-07-29 15:15:10','2026-07-31 00:39:00',0),(3,'ORD-20260702-001','new',1,2,7,4,3,8800.00,100,97,3,3,'referral',NULL,'active','2026-07-29 15:15:10','2026-07-29 16:41:46',0),(4,'ORD-20260703-001','new',1,3,7,5,2,2800.00,30,28,2,1,'douyin',NULL,'active','2026-07-29 15:15:10','2026-07-29 16:41:06',0),(5,'ORD-20260704-001','new',1,4,7,4,1,500.00,10,7,3,3,'walkin',NULL,'active','2026-07-29 15:15:10','2026-07-29 16:41:21',0),(6,'ORD-20260705-001','new',2,5,8,6,2,3000.00,30,25,5,5,'ground',NULL,'active','2026-07-29 15:15:10','2026-07-31 14:56:14',0),(7,'ORD202607300001','new',1,2,NULL,NULL,4,0.00,2,1,1,5,NULL,'','active','2026-07-30 18:22:37','2026-08-03 01:52:30',0),(8,'ORD202607300002','new',1,7,NULL,NULL,5,2333.00,10,9,1,1,NULL,'','active','2026-07-30 18:25:24','2026-07-30 18:39:08',0),(9,'ORD202607300003','new',1,10,7,2,6,3900.00,30,29,1,3,NULL,'','active','2026-07-30 20:16:55','2026-08-03 00:46:59',0),(10,'ORD202607300004','new',1,11,7,5,4,1.00,2,2,0,0,NULL,'','active','2026-07-30 21:31:37','2026-07-30 21:31:37',0),(11,'ORD202607300005','new',1,11,7,5,6,3900.00,30,27,3,13,NULL,'','active','2026-07-30 21:32:02','2026-08-03 01:50:43',0),(12,'ORD202607300006','new',1,12,7,2,4,1.00,2,1,1,3,NULL,'','active','2026-07-30 21:36:24','2026-08-03 01:15:02',0),(13,'ORD202607300007','new',1,13,7,2,4,1.00,2,1,1,1,NULL,'','active','2026-07-30 21:38:44','2026-08-03 01:41:37',0),(14,'ORD202607300008','renew',1,13,7,2,7,7200.00,60,58,2,2,NULL,'','active','2026-07-30 21:45:06','2026-08-03 00:55:47',0),(15,'ORD202607300009','new',2,14,8,6,5,1500.00,10,8,2,2,NULL,'','active','2026-07-30 23:38:43','2026-07-31 00:04:46',0),(16,'ORD202607310001','renew',2,14,8,6,7,7200.00,60,56,4,4,NULL,'','active','2026-07-31 00:26:50','2026-07-31 14:56:07',0),(17,'ORD202607310002','renew',2,14,8,6,7,7000.00,62,0,1,2,NULL,'','refunded','2026-07-31 00:42:49','2026-07-31 00:43:28',0),(18,'ORD202607310003','new',2,15,8,3,4,1.00,2,0,2,2,NULL,'','active','2026-07-31 00:44:16','2026-07-31 14:56:07',0),(19,'ORD202607310004','new',2,16,8,3,4,1.00,2,0,2,2,NULL,'','active','2026-07-31 00:44:39','2026-07-31 01:39:49',0),(20,'ORD202607310005','new',1,17,7,2,6,3900.00,30,30,0,0,NULL,'','active','2026-07-31 14:46:37','2026-07-31 14:46:37',0),(21,'ORD202607310006','renew',1,7,7,2,8,11000.00,100,98,2,6,NULL,'','active','2026-07-31 14:47:00','2026-08-03 01:51:43',0),(22,'ORD202607310007','new',1,18,NULL,NULL,4,1.00,3,0,3,7,NULL,'','active','2026-07-31 14:47:46','2026-08-03 00:46:49',0),(23,'ORD202607310008','renew',1,17,7,2,6,3900.00,30,28,2,2,NULL,'','active','2026-07-31 14:48:43','2026-08-03 01:50:30',0),(24,'ORD202608010001','new',1,20,NULL,3,4,300.00,2,2,0,0,NULL,NULL,'active','2026-08-01 23:57:49','2026-08-01 23:57:49',0),(25,'ORD202608020001','new',1,21,7,4,4,1.00,2,0,2,2,NULL,'','active','2026-08-02 00:09:14','2026-08-03 00:42:25',0),(26,'ORD202608040001','renew',2,23,8,3,8,11000.00,100,99,1,5,NULL,'','active','2026-08-04 15:54:31','2026-08-04 16:06:08',0),(27,'ORD202608040002','renew',2,22,8,3,7,7200.00,60,59,1,1,NULL,'','active','2026-08-04 15:54:39','2026-08-04 15:55:00',0),(28,'ORD202608040003','new',2,24,8,3,4,1.00,2,2,0,0,NULL,'','active','2026-08-04 15:56:43','2026-08-04 15:56:43',0);
/*!40000 ALTER TABLE `course_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course_type`
--

DROP TABLE IF EXISTS `course_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '课包ID',
  `name` varchar(64) NOT NULL COMMENT '课包名称',
  `total_lessons` int NOT NULL COMMENT '包含课时数（参考值，订单可覆盖）',
  `list_price` decimal(10,2) NOT NULL COMMENT '原价',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用 1-启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='课包类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course_type`
--

LOCK TABLES `course_type` WRITE;
/*!40000 ALTER TABLE `course_type` DISABLE KEYS */;
INSERT INTO `course_type` VALUES (4,'体验卡',2,1.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0),(5,'月卡',10,1500.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0),(6,'季卡',30,3900.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0),(7,'年卡',60,7200.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0),(8,'组合卡',100,11000.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0),(9,'成人提高班次卡',1,220.00,1,'2026-07-30 17:52:54','2026-07-30 17:52:54',0);
/*!40000 ALTER TABLE `course_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `lesson_schedule`
--

DROP TABLE IF EXISTS `lesson_schedule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `lesson_schedule` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint NOT NULL,
  `coach_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `course_order_id` bigint DEFAULT NULL,
  `schedule_date` date NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `lesson_content` varchar(128) DEFAULT NULL,
  `remark` varchar(256) DEFAULT NULL,
  `status` varchar(16) DEFAULT 'scheduled',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_schedule` (`coach_id`,`student_id`,`schedule_date`,`start_time`,`deleted`),
  KEY `idx_coach_date` (`coach_id`,`schedule_date`),
  KEY `idx_store_date` (`store_id`,`schedule_date`)
) ENGINE=InnoDB AUTO_INCREMENT=129 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `lesson_schedule`
--

LOCK TABLES `lesson_schedule` WRITE;
/*!40000 ALTER TABLE `lesson_schedule` DISABLE KEYS */;
INSERT INTO `lesson_schedule` VALUES (1,1,4,1,1,'2026-07-22','14:00:00','14:30:00','正手弧圈球',NULL,'scheduled','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(2,1,4,2,3,'2026-07-22','14:30:00','15:00:00','基础动作',NULL,'scheduled','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(3,1,5,3,4,'2026-07-23','10:00:00','10:30:00','反手拧拉',NULL,'scheduled','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(4,2,6,5,6,'2026-07-23','14:00:00','14:30:00','发球练习',NULL,'scheduled','2026-07-29 15:15:10','2026-07-29 15:15:10',0),(6,1,4,4,5,'2026-07-27','09:00:00','10:30:00','','','scheduled','2026-07-29 15:41:26','2026-07-29 15:41:26',0),(15,1,4,1,1,'2026-07-29','09:00:00','10:30:00','正手攻球',NULL,'scheduled','2026-07-29 16:29:11','2026-07-29 16:29:11',0),(16,1,2,4,5,'2026-07-27','09:00:00','10:30:00','','','scheduled','2026-07-29 16:30:47','2026-07-29 16:30:47',0),(17,1,2,2,3,'2026-07-27','10:30:00','12:00:00','','','scheduled','2026-07-29 16:34:50','2026-07-29 16:34:50',0),(18,1,5,1,2,'2026-07-28','09:00:00','10:30:00','','','scheduled','2026-07-29 16:35:22','2026-07-29 16:35:22',0),(19,1,2,4,5,'2026-07-28','09:00:00','10:30:00','','','scheduled','2026-07-29 16:41:06','2026-07-29 16:41:06',0),(20,1,2,3,4,'2026-07-28','09:00:00','10:30:00','','','scheduled','2026-07-29 16:41:06','2026-07-29 16:41:06',0),(21,1,2,4,5,'2026-07-28','10:30:00','12:00:00','','','scheduled','2026-07-29 16:41:21','2026-07-29 16:41:21',0),(23,1,2,1,2,'2026-07-28','09:00:00','10:30:00','','','scheduled','2026-07-29 16:41:41','2026-07-29 16:41:41',0),(24,1,2,2,3,'2026-07-28','10:30:00','12:00:00','','','scheduled','2026-07-29 16:41:46','2026-07-29 16:41:46',0),(28,1,2,7,8,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-30 18:39:08','2026-07-30 18:39:08',0),(29,1,2,13,14,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-30 23:37:31','2026-07-30 23:37:31',0),(30,2,3,5,6,'2026-07-27','09:00:00','10:30:00','','','scheduled','2026-07-30 23:38:23','2026-07-30 23:38:23',0),(31,2,3,14,15,'2026-07-27','10:30:00','12:00:00','','','scheduled','2026-07-30 23:38:57','2026-07-30 23:38:57',0),(32,2,3,14,15,'2026-07-26','10:30:00','12:00:00','','','scheduled','2026-07-31 00:04:46','2026-07-31 00:04:46',0),(35,2,3,14,17,'2026-07-27','14:30:00','16:00:00','','','scheduled','2026-07-31 00:43:01','2026-07-31 00:43:01',0),(36,2,3,14,16,'2026-07-28','10:30:00','12:00:00','','','scheduled','2026-07-31 00:43:56','2026-07-31 00:43:56',0),(37,2,3,15,18,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-31 01:39:39','2026-07-31 01:39:39',0),(39,2,3,5,6,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-31 01:39:39','2026-07-31 01:39:39',0),(40,2,3,14,16,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-31 01:39:39','2026-07-31 01:39:39',0),(41,2,3,16,19,'2026-07-28','14:30:00','16:00:00','','','scheduled','2026-07-31 01:39:49','2026-07-31 01:39:49',0),(42,1,2,18,22,'2026-07-29','10:30:00','12:00:00','','','scheduled','2026-07-31 14:49:21','2026-07-31 14:49:21',0),(44,2,3,15,18,'2026-07-29','14:30:00','16:00:00','','','scheduled','2026-07-31 14:56:07','2026-07-31 14:56:07',0),(45,2,3,14,16,'2026-07-29','14:30:00','16:00:00','','','scheduled','2026-07-31 14:56:07','2026-07-31 14:56:07',0),(47,2,3,5,6,'2026-07-29','14:30:00','16:00:00','','','scheduled','2026-07-31 14:56:14','2026-07-31 14:56:14',0),(48,1,2,21,25,'2026-08-02','09:00:00','10:30:00','','','scheduled','2026-08-02 17:33:16','2026-08-02 17:33:16',0),(49,1,2,10,9,'2026-08-02','09:00:00','10:30:00','','','scheduled','2026-08-03 00:38:28','2026-08-03 00:38:28',0),(53,1,2,21,25,'2026-08-02','10:30:00','12:00:00','','','scheduled','2026-08-03 00:42:25','2026-08-03 00:42:25',0),(54,1,2,18,22,'2026-08-02','10:30:00','12:00:00','','','scheduled','2026-08-03 00:42:25','2026-08-03 00:42:25',0),(55,1,2,12,12,'2026-08-02','10:30:00','12:00:00','','','scheduled','2026-08-03 00:42:39','2026-08-03 00:42:39',0),(57,1,2,7,21,'2026-08-02','14:00:00','15:30:00','','','scheduled','2026-08-03 00:45:33','2026-08-03 00:45:33',0),(61,1,2,18,22,'2026-08-02','14:00:00','15:30:00','','','scheduled','2026-08-03 00:46:49','2026-08-03 00:46:49',0),(114,1,2,17,23,'2026-08-03','09:00:00','10:30:00','','','scheduled','2026-08-03 01:41:21','2026-08-03 01:41:21',0),(115,1,2,13,13,'2026-08-03','10:30:00','12:00:00','','','scheduled','2026-08-03 01:41:37','2026-08-03 01:41:37',0),(116,1,2,11,11,'2026-08-03','10:00:00','11:30:00','',NULL,'scheduled','2026-08-03 01:45:30','2026-08-03 01:45:30',0),(117,1,2,2,7,'2026-08-02','19:00:00','20:30:00','',NULL,'scheduled','2026-08-03 01:48:46','2026-08-03 01:48:46',0),(119,1,2,17,23,'2026-08-03','14:00:00','15:30:00','','','scheduled','2026-08-03 01:50:30','2026-08-03 01:50:30',0),(120,1,2,11,11,'2026-08-03','15:30:00','17:00:00','','','scheduled','2026-08-03 01:50:43','2026-08-03 01:50:43',0),(121,1,2,7,21,'2026-08-03','18:00:00','19:30:00','','','scheduled','2026-08-03 01:50:53','2026-08-03 01:50:53',0),(125,2,3,23,26,'2026-08-04','09:00:00','10:30:00','','','scheduled','2026-08-04 15:55:00','2026-08-04 15:55:00',0),(126,2,3,22,27,'2026-08-04','09:00:00','10:30:00','','','scheduled','2026-08-04 15:55:00','2026-08-04 15:55:00',0);
/*!40000 ALTER TABLE `lesson_schedule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `monthly_target`
--

DROP TABLE IF EXISTS `monthly_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `monthly_target` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `staff_id` bigint DEFAULT NULL COMMENT '员工ID（为空表示门店级目标）',
  `target_type` varchar(16) NOT NULL COMMENT '目标类型：revenue-营收 lessons-消课课时 new_students-新签学员',
  `target_amount` decimal(10,2) DEFAULT NULL COMMENT '金额目标（revenue类型用）',
  `target_count` int DEFAULT NULL COMMENT '数量目标（lessons/new_students类型用）',
  `target_month` date NOT NULL COMMENT '目标月份',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_month` (`store_id`,`target_month`),
  KEY `idx_staff_month` (`staff_id`,`target_month`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='月度目标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `monthly_target`
--

LOCK TABLES `monthly_target` WRITE;
/*!40000 ALTER TABLE `monthly_target` DISABLE KEYS */;
INSERT INTO `monthly_target` VALUES (1,1,NULL,'sales',50000.00,NULL,'2026-07-01','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(2,1,NULL,'consumption',NULL,200,'2026-07-01','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(3,2,NULL,'sales',30000.00,NULL,'2026-07-01','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(4,2,NULL,'consumption',NULL,120,'2026-07-01','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(5,1,NULL,'consumption',30000.00,NULL,'2026-07-01','2026-07-31 14:38:11','2026-07-31 14:38:11',0),(6,2,NULL,'consumption',30000.00,NULL,'2026-07-01','2026-07-31 14:38:11','2026-07-31 14:38:11',0),(7,1,NULL,'sales',50000.00,NULL,'2026-08-01','2026-08-04 15:51:40','2026-08-04 15:51:40',0),(8,2,NULL,'sales',50000.00,NULL,'2026-08-01','2026-08-04 15:51:40','2026-08-04 15:51:40',0),(9,1,NULL,'consumption',50000.00,NULL,'2026-08-01','2026-08-04 15:51:56','2026-08-04 15:51:56',0),(10,2,NULL,'consumption',50000.00,NULL,'2026-08-01','2026-08-04 15:51:56','2026-08-04 15:51:56',0);
/*!40000 ALTER TABLE `monthly_target` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `refund_log`
--

DROP TABLE IF EXISTS `refund_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `refund_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID',
  `course_order_id` bigint NOT NULL COMMENT '关联订单ID',
  `student_id` bigint NOT NULL COMMENT '学员ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额',
  `refund_lessons` int NOT NULL COMMENT '退回课时数',
  `reason` varchar(256) DEFAULT NULL COMMENT '退款原因',
  `operator_id` bigint NOT NULL COMMENT '操作人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '退款时间',
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_course_order_id` (`course_order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退款日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `refund_log`
--

LOCK TABLES `refund_log` WRITE;
/*!40000 ALTER TABLE `refund_log` DISABLE KEYS */;
INSERT INTO `refund_log` VALUES (1,1,2,1,50.00,1,'学员搬家换城市',2,'2026-07-29 15:15:10',0),(2,1,2,1,350.00,7,'测试退款',1,'2026-07-31 00:39:00',0),(3,2,17,14,6887.10,61,'搬家',3,'2026-07-31 00:43:28',0);
/*!40000 ALTER TABLE `refund_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `staff`
--

DROP TABLE IF EXISTS `staff`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `staff` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '员工ID',
  `store_id` bigint DEFAULT NULL COMMENT '所属门店ID，逻辑外键，无物理约束。老板为NULL',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `role` varchar(16) NOT NULL COMMENT '角色：boss-老板 shop_owner-店长 coach-教练 sales-销售',
  `password` varchar(128) DEFAULT NULL COMMENT '加密密码',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-离职 1-在职',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_store_phone` (`store_id`,`phone`) COMMENT '同一门店下手机号唯一',
  KEY `idx_store_id` (`store_id`),
  KEY `idx_role` (`role`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='员工表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `staff`
--

LOCK TABLES `staff` WRITE;
/*!40000 ALTER TABLE `staff` DISABLE KEYS */;
INSERT INTO `staff` VALUES (1,NULL,'刘国梁','13800000001','boss','$2a$10$D5AWTKcAvF0DXWFMQ2dFXukVcJ9nD2.MGe6agNNMwmGT7KqfBa1Fm','2023-01-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(2,1,'张建国','13800000002','shop_owner','$2a$10$l7H0ZcOLGq5b57W79f6nv.KPZPXB3bFwtccWbyDZND.PqwV3JxFvq','2023-01-15',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(3,2,'李建国','13800000003','shop_owner','$2a$10$E9Sn69dAfLrMMwKbh2QHIOmTKPIulBsGeIxB0UFahyo9k4rqc5A9.','2023-02-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(4,1,'马龙','13800000004','coach','$2a$10$Ud2pBiPXksqvWEgfLLRLTe.LWTF4HlkXI0EzLLJllZi9ouCDQgS3O','2023-03-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(5,1,'樊振东','13800000005','coach','$2a$10$Iuy8pTsqc8DbmhO0WakNr.OqxE6yknTNgvjZVdtrZaWPzAmEEzWgq','2023-04-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(6,2,'王楚钦','13800000006','coach','$2a$10$h74BlQ3RcsuhQB30DB.nVuu.Bu9KuuQEBOV/e2/KqCNHXjZ2mIy6C','2023-05-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(7,1,'孙颖莎','13800000007','sales','$2a$10$gmXD9CKM3SZ1zNXkj6ow0.w6rRZ5hoBV.HSUXJjUZ0HTYooKXKAAC','2023-06-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(8,2,'陈梦','13800000008','sales','$2a$10$SVLqdkXKXIP7Cnd4BsxwbuOvXeuLTsVlbYyiw5SGBktyNkfYicrjS','2023-07-01',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0);
/*!40000 ALTER TABLE `staff` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `store`
--

DROP TABLE IF EXISTS `store`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `store` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `name` varchar(64) NOT NULL COMMENT '店名',
  `address` varchar(256) DEFAULT NULL COMMENT '地址',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-关闭 1-营业中',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除：0-正常 1-删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='门店表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `store`
--

LOCK TABLES `store` WRITE;
/*!40000 ALTER TABLE `store` DISABLE KEYS */;
INSERT INTO `store` VALUES (1,'旗舰店（新街口）','南京市玄武区新街口88号','025-88880001',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(2,'河西万达店','南京市建邺区万达广场3楼','025-88880002',1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0);
/*!40000 ALTER TABLE `store` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学员ID',
  `store_id` bigint NOT NULL COMMENT '所属门店ID，逻辑外键，无物理约束',
  `primary_coach_id` bigint DEFAULT NULL COMMENT '带教教练ID，关联staff表',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `phone` varchar(20) DEFAULT '',
  `age` int DEFAULT NULL COMMENT '年龄',
  `address` varchar(256) DEFAULT NULL COMMENT '住址',
  `source` varchar(16) DEFAULT NULL COMMENT '获客来源：douyin-抖音 meituan-美团 referral-转介绍 ground-地推 walkin-自然到店 other-其他',
  `registered_at` datetime DEFAULT NULL COMMENT '注册日期（首次订单创建时间）',
  `last_lesson_at` datetime DEFAULT NULL COMMENT '最近上课日期（最近一次消课时间）',
  `total_remaining_lessons` int DEFAULT '0' COMMENT '剩余总课时（所有有效订单的剩余课时之和）',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-流失 1-在读 2-暂停',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_id` (`store_id`),
  KEY `idx_name` (`name`) COMMENT '用于按姓名模糊搜索学员',
  KEY `idx_source` (`source`),
  KEY `idx_status` (`status`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
INSERT INTO `student` VALUES (1,1,4,'小明','13900000001',8,'南京市玄武区长江路10号','meituan','2026-07-29 15:15:10','2026-07-29 16:41:41',24,9,1,'2026-07-29 15:15:10','2026-07-31 00:39:00',0),(2,1,4,'小红','13900000002',7,'南京市玄武区中山路20号','referral','2026-07-29 15:15:10','2026-08-03 01:52:25',98,8,1,'2026-07-29 15:15:10','2026-08-03 01:52:30',0),(3,1,5,'小刚','13900000003',10,'南京市鼓楼区湖南路30号','douyin','2026-07-29 15:15:10','2026-07-29 16:41:06',28,3,1,'2026-07-29 15:15:10','2026-07-30 21:49:56',0),(4,1,4,'小花','13900000004',6,'南京市秦淮区夫子庙路5号','walkin','2026-07-29 15:15:10','2026-07-29 16:41:21',7,4,1,'2026-07-29 15:15:10','2026-07-30 21:49:56',0),(5,2,6,'小丽','13900000005',9,'南京市建邺区万达华府1栋','ground','2026-07-29 15:15:10','2026-07-31 14:56:14',25,5,1,'2026-07-29 15:15:10','2026-07-31 14:56:14',0),(6,2,6,'小强','13900000006',11,'南京市建邺区万达华府3栋','referral',NULL,NULL,0,1,1,'2026-07-29 15:15:10','2026-07-29 15:15:10',0),(7,1,NULL,'米豆腐','',NULL,NULL,NULL,'2026-07-30 18:25:24','2026-08-03 01:51:39',107,8,1,'2026-07-30 18:25:24','2026-08-03 01:51:43',0),(10,1,4,'小方','',NULL,NULL,NULL,'2026-07-30 20:16:55','2026-08-03 00:43:17',29,4,1,'2026-07-30 20:16:55','2026-08-03 00:46:59',0),(11,1,5,'小a','',NULL,NULL,NULL,'2026-07-30 21:31:37','2026-08-03 01:50:43',29,14,1,'2026-07-30 21:31:37','2026-08-03 01:50:43',0),(12,1,2,'小b','',NULL,NULL,NULL,'2026-07-30 21:36:24','2026-08-03 01:14:54',1,3,1,'2026-07-30 21:36:24','2026-08-03 01:15:02',0),(13,1,2,'小c','',NULL,NULL,NULL,'2026-07-30 21:38:44','2026-08-03 01:41:38',59,4,1,'2026-07-30 21:38:44','2026-08-03 01:41:37',0),(14,2,6,'阿伟','',NULL,NULL,NULL,'2026-07-30 23:38:44','2026-07-31 14:56:08',64,10,1,'2026-07-30 23:38:43','2026-07-31 14:56:07',0),(15,2,3,'小李','',NULL,NULL,NULL,'2026-07-31 00:44:17','2026-07-31 14:56:08',0,2,1,'2026-07-31 00:44:16','2026-07-31 14:56:07',0),(16,2,3,'小胖','12376723656',NULL,NULL,'老带新','2026-07-31 00:44:40','2026-07-31 01:39:50',0,6,1,'2026-07-31 00:44:39','2026-07-31 01:39:49',0),(17,1,2,'小f','',NULL,NULL,NULL,'2026-07-31 14:46:37','2026-08-03 01:50:31',58,6,0,'2026-07-31 14:46:37','2026-08-03 01:50:30',0),(18,1,NULL,'小p','',NULL,NULL,NULL,'2026-07-31 14:47:47','2026-08-03 00:46:49',0,9,1,'2026-07-31 14:47:46','2026-08-03 00:46:49',0),(19,1,3,'测试学员001','13900000001',NULL,NULL,NULL,'2026-08-01 23:56:55',NULL,0,0,1,'2026-08-01 23:56:54','2026-08-01 23:58:11',1),(20,1,3,'手机号测试002','13900000002',NULL,NULL,NULL,'2026-08-01 23:57:49',NULL,2,0,1,'2026-08-01 23:57:49','2026-08-01 23:58:11',1),(21,1,4,'小宝','83737377373',NULL,NULL,NULL,'2026-08-02 00:09:14','2026-08-03 00:42:25',0,4,1,'2026-08-02 00:09:14','2026-08-03 00:42:25',0),(22,2,3,'neo','12121212121',NULL,NULL,NULL,'2026-08-04 15:53:23','2026-08-04 15:55:01',59,2,1,'2026-08-04 15:53:22','2026-08-04 15:55:00',0),(23,2,3,'la','32323232323',NULL,NULL,NULL,'2026-08-04 15:53:54','2026-08-04 16:05:49',99,6,1,'2026-08-04 15:53:53','2026-08-04 16:06:08',0),(24,2,3,'dl','55666565656',NULL,NULL,NULL,'2026-08-04 15:56:44',NULL,2,0,1,'2026-08-04 15:56:43','2026-08-04 15:56:43',0);
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `weekly_target`
--

DROP TABLE IF EXISTS `weekly_target`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `weekly_target` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `store_id` bigint NOT NULL COMMENT '门店ID',
  `staff_id` bigint DEFAULT NULL COMMENT '员工ID（为空表示门店级目标）',
  `target_type` varchar(16) NOT NULL COMMENT '目标类型：revenue-营收 lessons-消课课时 new_students-新签学员',
  `target_amount` decimal(10,2) DEFAULT NULL COMMENT '金额目标（revenue类型用）',
  `target_count` int DEFAULT NULL COMMENT '数量目标（lessons/new_students类型用）',
  `target_week` date NOT NULL COMMENT '目标周（周一日期）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_store_week` (`store_id`,`target_week`),
  KEY `idx_staff_week` (`staff_id`,`target_week`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='周目标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `weekly_target`
--

LOCK TABLES `weekly_target` WRITE;
/*!40000 ALTER TABLE `weekly_target` DISABLE KEYS */;
INSERT INTO `weekly_target` VALUES (1,1,NULL,'sales',12000.00,NULL,'2026-07-20','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(2,1,NULL,'consumption',NULL,50,'2026-07-20','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(3,2,NULL,'sales',8000.00,NULL,'2026-07-20','2026-07-29 15:15:10','2026-07-30 18:42:15',0),(4,2,NULL,'consumption',NULL,30,'2026-07-20','2026-07-29 15:15:10','2026-07-30 18:42:15',0);
/*!40000 ALTER TABLE `weekly_target` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-04 16:11:14
