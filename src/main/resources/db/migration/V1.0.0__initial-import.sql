CREATE DATABASE  IF NOT EXISTS `udan` /*!40100 DEFAULT CHARACTER SET utf8mb4 */;
USE `udan`;
-- MySQL dump 10.17  Distrib 10.3.20-MariaDB, for debian-linux-gnu (x86_64)
--
-- Host: localhost    Database: udan
-- ------------------------------------------------------
-- Server version	10.3.20-MariaDB-0ubuntu0.19.10.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `SequenceList`
--

DROP TABLE IF EXISTS `SequenceList`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SequenceList` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createdat` bigint(20) NOT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `name` varchar(5000) DEFAULT NULL,
  `userclicknodelist` varchar(5000) DEFAULT NULL,
  `usersessionid` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Sequenceuserclicknodemap`
--

DROP TABLE IF EXISTS `Sequenceuserclicknodemap`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Sequenceuserclicknodemap` (
  `sequencelistid` int(11) DEFAULT NULL,
  `userclicknodeid` int(11) NOT NULL,
  PRIMARY KEY (`userclicknodeid`),
  KEY `FKkv6wq7iiai0g0hpc1o8kt9w5i` (`sequencelistid`),
  CONSTRAINT `FKkv6wq7iiai0g0hpc1o8kt9w5i` FOREIGN KEY (`sequencelistid`) REFERENCES `SequenceList` (`id`),
  CONSTRAINT `FKrwx7xamcd2je15f5c3cigtiv8` FOREIGN KEY (`userclicknodeid`) REFERENCES `Userclicknodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Userclicknodes`
--

DROP TABLE IF EXISTS `Userclicknodes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `Userclicknodes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clickednodename` varchar(2000) DEFAULT NULL,
  `clickedpath` varchar(3000) DEFAULT NULL,
  `createdat` bigint(20) NOT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `html5` int(11) DEFAULT NULL,
  `objectdata` longtext DEFAULT NULL,
  `sessionid` varchar(500) DEFAULT NULL,
  `urlpath` longtext DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `domainpatterns`
--

DROP TABLE IF EXISTS `domainpatterns`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `domainpatterns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createdat` bigint(20) NOT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `otherpatterntype` varchar(500) DEFAULT NULL,
  `patterntype` varchar(500) DEFAULT NULL,
  `patternvalue` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `index_javascript_events`
--

DROP TABLE IF EXISTS `index_javascript_events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `index_javascript_events` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clickednodename` varchar(5000) DEFAULT NULL,
  `created_at` bigint(20) NOT NULL,
  `data` longtext DEFAULT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `sessionid` varchar(500) DEFAULT NULL,
  `urlpath` longtext DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-11-22 14:20:33
