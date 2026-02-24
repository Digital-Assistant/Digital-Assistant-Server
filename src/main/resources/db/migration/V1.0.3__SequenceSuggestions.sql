ALTER TABLE `SequenceList` ADD COLUMN IF NOT EXISTS `isValid` TINYINT NOT NULL DEFAULT 1 AFTER `deleted`;
ALTER TABLE `SequenceList` ADD COLUMN IF NOT EXISTS `isIgnored` TINYINT NOT NULL DEFAULT 0 AFTER `isValid`;

update SequenceList t1 inner join user_session_data t2 on t2.usersessionid COLLATE utf8mb4_unicode_ci = t1.usersessionid COLLATE utf8mb4_unicode_ci set t1.usersessionid=t2.userauthid where t1.usersessionid is not null;
