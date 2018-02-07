-- INNODB supports transaction, row-level locking, and foreign keys
-- this is the default engine

CREATE TABLE Image (
	imageId varchar(5)
	sentFrom varchar (20)
	personInPhoto varchar(20),
	score int,
	PRIMARY KEY(imageId)
);

CREATE TABLE Game (
	playerIds varchar(100),
	-- photos
	papCounter int, -- paparazzi counter
	playerCount int, -- number of players
	gameId int,
	startTime timestamp,
	-- all chat messages
	gameRm varchar(20), -- game room name
	paparazzi int,
	PRIMARY KEY(gameId)
);

CREATE TABLE Player (
	userId varchar(20),
	first varchar(20),
	last varchar(20),
	PRIMARY KEY(userId)
);

CREATE TABLE Messages (
	userId varchar(20),
	gameId int,
	sendTime timestamp,
	message varchar(50),
	imageId varchar(20)
	-- key for this could be userId or gameId
);