CREATE TABLE `voice_plugin`.`user_auth_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `authid` VARCHAR(2000) NOT NULL,
  `emailid` VARCHAR(2000) NULL,
  `authsource` VARCHAR(500) NOT NULL,
  `createdat` BIGINT NOT NULL,
  PRIMARY KEY (`id`));

  CREATE TABLE `voice_plugin`.`user_session_data` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `userauthid` BIGINT NOT NULL,
  `usersessionid` VARCHAR(500) NOT NULL,
  `createdat` BIGINT NOT NULL,
  PRIMARY KEY (`id`));