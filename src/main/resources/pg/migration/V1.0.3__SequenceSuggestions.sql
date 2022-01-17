ALTER TABLE sequencelist ADD COLUMN isValid INT NOT NULL DEFAULT 1;
ALTER TABLE sequencelist ADD COLUMN isIgnored INT NOT NULL DEFAULT 0;

update sequencelist as t1 set usersessionid=t2.userauthid FROM user_session_data t2 WHERE t2.usersessionid=t1.usersessionid  AND t1.usersessionid is not null;
