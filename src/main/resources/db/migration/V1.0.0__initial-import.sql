DROP TABLE IF EXISTS `SequenceList`;
CREATE TABLE `SequenceList` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createdat` bigint(20) NOT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `name` varchar(5000) DEFAULT NULL,
  `userclicknodelist` varchar(5000) DEFAULT NULL,
  `usersessionid` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `Sequenceuserclicknodemap`;
CREATE TABLE `Sequenceuserclicknodemap` (
  `sequencelistid` int(11) DEFAULT NULL,
  `userclicknodeid` int(11) NOT NULL,
  PRIMARY KEY (`userclicknodeid`),
  KEY `FKkv6wq7iiai0g0hpc1o8kt9w5i` (`sequencelistid`),
  CONSTRAINT `FKkv6wq7iiai0g0hpc1o8kt9w5i` FOREIGN KEY (`sequencelistid`) REFERENCES `SequenceList` (`id`),
  CONSTRAINT `FKrwx7xamcd2je15f5c3cigtiv8` FOREIGN KEY (`userclicknodeid`) REFERENCES `Userclicknodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `Userclicknodes`;
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

DROP TABLE IF EXISTS `domainpatterns`;
CREATE TABLE `domainpatterns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `createdat` bigint(20) NOT NULL,
  `domain` varchar(500) DEFAULT NULL,
  `otherpatterntype` varchar(500) DEFAULT NULL,
  `patterntype` varchar(500) DEFAULT NULL,
  `patternvalue` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `index_javascript_events`;
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