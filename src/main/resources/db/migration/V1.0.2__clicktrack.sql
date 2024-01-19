CREATE TABLE `ClickTrack` (
	                                         `id` BIGINT NOT NULL AUTO_INCREMENT,
	                                         `usersessionid` VARCHAR(500) NOT NULL,
	                                         `clicktype` VARCHAR(200) NOT NULL,
	                                         `clickedname` VARCHAR(2000) NOT NULL,
	                                         `recordid` BIGINT NULL,
	                                         `createdat` BIGINT(20) NULL,
	                                         PRIMARY KEY (`id`));
