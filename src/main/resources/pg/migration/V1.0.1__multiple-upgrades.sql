ALTER TABLE sequencelist
	ADD COLUMN deleted INT NOT NULL DEFAULT 0;


CREATE TABLE sequencevotes (
	                                             id BIGSERIAL NOT NULL,
	                                             usersessionid VARCHAR(500) NOT NULL,
	                                             sequenceid BIGINT NOT NULL,
	                                             upvote INT NOT NULL,
	                                             downvote INT NOT NULL,
	                                             createdat BIGINT NOT NULL,
	                                             updatedat BIGINT NOT NULL,
	                                             PRIMARY KEY (id));

CREATE TABLE user_auth_data (
	                                             id BIGSERIAL NOT NULL,
	                                             authid VARCHAR(2000) NOT NULL,
	                                             emailid VARCHAR(2000) NULL,
	                                             authsource VARCHAR(500) NOT NULL,
	                                             createdat BIGINT NOT NULL,
	                                             PRIMARY KEY (id));

CREATE TABLE user_session_data (
	                                                id BIGSERIAL NOT NULL,
	                                                userauthid BIGINT NOT NULL,
	                                                usersessionid VARCHAR(500) NOT NULL,
	                                                createdat BIGINT NOT NULL,
	                                                PRIMARY KEY (id));
