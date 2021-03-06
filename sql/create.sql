-- INNODB supports transaction, row-level locking, and foreign keys
-- this is the default engine

CREATE TABLE Image (
	imageId varchar(30),
	targetPlayer varchar(20),
	ratings varchar(60),
	imageContent varchar(65345),
	PRIMARY KEY(imageId)
);

CREATE TABLE Game (
	gameId varchar(20),
	playerIds varchar(200),
	startTime Bigint, -- update every new game
	allMessages varchar(10000),
	gameRoomName varchar(20), -- game room name
	gameDuration varchar(20),
	playerCount varchar(20), -- number of players
	timePerPerson int,
	maxTurns int, # always 2
	started int,
	paparazzi varchar(30), -- or ID of player
	target varchar(30),
	papHistory varchar(200),
	winner varchar(30),
	lastRateTime Bigint,
	-- get image ids past the start time

	PRIMARY KEY(gameId)
);
-- insert into game values(35, '["10213545242363283","08WK90K00X24GHNR3D90SO"]', '2004-05-23T14:25:10', '["abc"]', "gameRoom1", 3000, 2);


CREATE TABLE Player (
	userId varchar(30),
	first varchar(20),
	last varchar(20),
	PRIMARY KEY(userId)
);

CREATE TABLE Messages (
	msgId varchar(30),
	sentFrom varchar(30), -- userid
	gameId varchar(20),
	sendTime Bigint,
	message varchar(50),
	image varchar(30) 
	-- key for this could be userId or gameId
);
