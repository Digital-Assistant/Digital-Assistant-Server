ALTER TABLE `udan`.`SequenceList` ADD COLUMN `isValid` TINYINT NOT NULL DEFAULT 1 AFTER `deleted`;
ALTER TABLE `udan`.`SequenceList` ADD COLUMN `isIgnored` TINYINT NOT NULL DEFAULT 0 AFTER `isValid`;

update SequenceList t1 inner join user_session_data t2 on t2.usersessionid=t1.usersessionid set t1.usersessionid=t2.userauthid where t1.usersessionid is not null;
