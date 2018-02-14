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
	gameId int,
	playerIds varchar(200),
	gameId int,
	startTime timestamp,
	-- all chat messages
	gameRoomName varchar(20), -- game room name
	gameDuration int,
	playerCount int--, -- number of players
	-- paparazzi int,
	-- papCounter int, -- paparazzi counter
	-- host?

	PRIMARY KEY(gameId)
);

CREATE TABLE Player (
	userId varchar(30),
	first varchar(20),
	last varchar(20),
	PRIMARY KEY(userId)
);

CREATE TABLE Messages (
	msgId varchar
	sentFrom varchar(30), -- userid
	gameId int,
	sendTime timestamp,
	message varchar(50),
	imageId varchar(100)
	-- key for this could be userId or gameId
);
