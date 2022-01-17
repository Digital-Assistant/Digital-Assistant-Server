ALTER TABLE `voice_plugin_prod`.`SequenceList`
	ADD COLUMN `deleted` TINYINT NOT NULL DEFAULT 0 AFTER `usersessionid`;


CREATE TABLE `voice_plugin_prod`.`sequence-votes` (
	                                             `id` BIGINT NOT NULL AUTO_INCREMENT,
	                                             `usersessionid` VARCHAR(500) NOT NULL,
	                                             `sequenceid` BIGINT NOT NULL,
	                                             `upvote` TINYINT NOT NULL,
	                                             `downvote` TINYINT NOT NULL,
	                                             `createdat` BIGINT NOT NULL,
	                                             `updatedat` BIGINT NOT NULL,
	                                             PRIMARY KEY (`id`));

ALTER TABLE `voice_plugin_prod`.`sequence-votes`
	RENAME TO  `voice_plugin_prod`.`sequencevotes` ;

CREATE TABLE `voice_plugin_prod`.`user_auth_data` (
	                                             `id` BIGINT NOT NULL AUTO_INCREMENT,
	                                             `authid` VARCHAR(2000) NOT NULL,
	                                             `emailid` VARCHAR(2000) NULL,
	                                             `authsource` VARCHAR(500) NOT NULL,
	                                             `createdat` BIGINT NOT NULL,
	                                             PRIMARY KEY (`id`));

CREATE TABLE `voice_plugin_prod`.`user_session_data` (
	                                                `id` BIGINT NOT NULL AUTO_INCREMENT,
	                                                `userauthid` BIGINT NOT NULL,
	                                                `usersessionid` VARCHAR(500) NOT NULL,
	                                                `createdat` BIGINT NOT NULL,
	                                                PRIMARY KEY (`id`));
