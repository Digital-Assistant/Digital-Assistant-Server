ALTER TABLE `ClickTrack`
    CHANGE COLUMN `clicktype` `event_name` VARCHAR(200) NOT NULL COMMENT 'This is used to store the click event name',
    CHANGE COLUMN `clickedname` `event_value` VARCHAR(2000) NOT NULL COMMENT 'This is used to store the click event value if provided',
    CHANGE COLUMN `recordid` `sequence_id` BIGINT(20) NULL DEFAULT NULL COMMENT 'This is used to store the recording sequence id';


ALTER TABLE `ClickTrack`
    ADD COLUMN `domain` VARCHAR(5000) NULL COMMENT 'This is used to store the domain name' AFTER `sequence_id`;