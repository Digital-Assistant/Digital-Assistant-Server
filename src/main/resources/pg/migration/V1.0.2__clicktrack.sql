CREATE TABLE clicktrack (
	                                         id BIGSERIAL NOT NULL,
	                                         usersessionid VARCHAR(500) NOT NULL,
	                                         clicktype VARCHAR(200) NOT NULL,
	                                         clickedname VARCHAR(2000) NOT NULL,
	                                         recordid BIGINT NULL,
	                                         createdat BIGINT NULL,
	                                         PRIMARY KEY (id));
