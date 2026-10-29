-- Owner of a click node (same value as SequenceList.usersessionid), set by the Server from the caller's token so
-- that only the recorder can change the node (authPKCE PK03). Nullable: rows created before this migration have no
-- owner and are checked through the recording they belong to.
ALTER TABLE `Userclicknodes` ADD COLUMN IF NOT EXISTS `usersessionid` VARCHAR(500) NULL DEFAULT NULL AFTER `sessionid`;
