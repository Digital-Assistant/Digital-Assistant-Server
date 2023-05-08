ALTER TABLE `voice_plugin`.`index_javascript_events` 
CHANGE COLUMN `clickednodename` `clickednodename` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `data` `data` TEXT NULL DEFAULT NULL ,
CHANGE COLUMN `domain` `domain` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `sessionid` `sessionid` VARCHAR(500) NULL DEFAULT NULL ,
CHANGE COLUMN `urlpath` `urlpath` VARCHAR(5000) NULL DEFAULT NULL ;


ALTER TABLE `voice_plugin`.`SequenceList` 
CHANGE COLUMN `name` `name` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `userclicknodelist` `userclicknodelist` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `usersessionid` `usersessionid` VARCHAR(500) NULL DEFAULT NULL ;


ALTER TABLE `voice_plugin`.`Userclicknodes` 
CHANGE COLUMN `clickednodename` `clickednodename` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `clickedpath` `clickedpath` VARCHAR(5000) NULL DEFAULT NULL ,
CHANGE COLUMN `domain` `domain` VARCHAR(500) NULL DEFAULT NULL ,
CHANGE COLUMN `objectdata` `objectdata` TEXT NULL DEFAULT NULL ,
CHANGE COLUMN `sessionid` `sessionid` VARCHAR(500) NULL DEFAULT NULL ,
CHANGE COLUMN `urlpath` `urlpath` VARCHAR(5000) NULL DEFAULT NULL ;

