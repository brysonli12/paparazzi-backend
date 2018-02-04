-- INNODB supports transaction, row-level locking, and foreign keys
-- this is the default engine

CREATE TABLE Photo (
	personInPhoto varchar(20),
	score int
);

CREATE TABLE GameRoom (
	playerIds varchar(100),
	-- photos
	counter int, -- paparazzi counter
	gameId int,
	startTime timestamp,
	-- all chat messages
	gameRm varchar(20), -- game room name
	paparazzi int,
	PRIMARY KEY(gameId)
);

CREATE TABLE Users (
	userId varchar(20),
	first varchar(20),
	last varchar(20),
	PRIMARY KEY(userId)
);

CREATE TABLE Messages (
	userId varchar(20),
	gameId int,
	sendTime timestamp,
	message varchar(50)
	-- key for this could be userId or gameId
);
