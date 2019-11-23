create table if not exists domainpatterns
(
	id serial not null
		constraint domainpatterns_pkey
			primary key,
	createdat bigint not null,
	domain varchar(500),
	otherpatterntype varchar(500),
	patterntype varchar(500),
	patternvalue varchar(500)
);


create table if not exists index_javascript_events
(
	id serial not null
		constraint index_javascript_events_pkey
			primary key,
	clickednodename varchar(5000),
	created_at bigint not null,
	data text,
	domain varchar(500),
	sessionid varchar(500),
	urlpath text
);


create table if not exists sequencelist
(
	id serial not null
		constraint sequencelist_pkey
			primary key,
	createdat bigint not null,
	domain varchar(500),
	name varchar(5000),
	userclicknodelist varchar(5000),
	usersessionid varchar(500)
);


create table if not exists userclicknodes
(
	id serial not null
		constraint userclicknodes_pkey
			primary key,
	clickednodename varchar(2000),
	clickedpath varchar(3000),
	createdat bigint not null,
	domain varchar(500),
	html5 integer,
	objectdata text,
	sessionid varchar(500),
	urlpath text
);


create table if not exists sequenceuserclicknodemap
(
	sequencelistid integer
		constraint fkkv6wq7iiai0g0hpc1o8kt9w5i
			references sequencelist,
	userclicknodeid integer not null
		constraint sequenceuserclicknodemap_pkey
			primary key
		constraint fkrwx7xamcd2je15f5c3cigtiv8
			references userclicknodes
);
