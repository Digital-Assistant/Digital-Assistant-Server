ALTER TABLE `Sequenceuserclicknodemap` DROP FOREIGN KEY `FKkv6wq7iiai0g0hpc1o8kt9w5i`;
ALTER TABLE `Sequenceuserclicknodemap` DROP FOREIGN KEY `FKrwx7xamcd2je15f5c3cigtiv8`;

ALTER TABLE `SequenceList` CHANGE COLUMN `id` `id` BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE `Userclicknodes` CHANGE COLUMN `id` `id` BIGINT NOT NULL AUTO_INCREMENT;

ALTER TABLE `Sequenceuserclicknodemap`
    CHANGE COLUMN `sequencelistid` `sequencelistid` BIGINT NULL DEFAULT NULL,
    CHANGE COLUMN `userclicknodeid` `userclicknodeid` BIGINT NOT NULL;

ALTER TABLE `Sequenceuserclicknodemap`
    ADD CONSTRAINT `FKkv6wq7iiai0g0hpc1o8kt9w5i` FOREIGN KEY (`sequencelistid`) REFERENCES `SequenceList` (`id`),
    ADD CONSTRAINT `FKrwx7xamcd2je15f5c3cigtiv8` FOREIGN KEY (`userclicknodeid`) REFERENCES `Userclicknodes` (`id`);
