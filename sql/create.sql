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
	startTime datetime,
	allMessages varchar(500),
	gameRoomName varchar(20), -- game room name
	gameDuration int,
	playerCount int, -- number of players
	-- paparazzi int,
	-- papCounter int, -- paparazzi counter
	-- host?

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
	gameId int,
	sendTime datetime,
	message varchar(50),
	image varchar(100)
	-- key for this could be userId or gameId
);
