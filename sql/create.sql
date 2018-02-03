-- INNODB supports transaction, row-level locking, and foreign keys
-- this is the default engine

CREATE TABLE Photo (
	personInPhoto varchar(20),
	score int
);

CREATE TABLE GameRoom (
	-- all new Ids userIds PlayersInGame.  
	-- 		alternate approach is to serialize the list of ids in one variable
	--		but you don't do that in normalized relational dtabases.
	-- photos
	counter int, -- paparazzi counter
	gameId int,
	startTime timestamp,
	-- all chat messages
	gameRm varchar(20), -- game room name
	paparazzi int,
	PRIMARY KEY(gameId)
);

CREATE TABLE PlayersInGame (
	gameId int,
	numPlayers int,
	id1 varchar(20), 
	id2 varchar(20), 
	id3 varchar(20), 
	id4 varchar(20), 
	id5 varchar(20), 
	id6 varchar(20), 
	id7 varchar(20), 
	id8 varchar(20), 
	id9 varchar(20), 
	id10 varchar(20), 
	PRIMARY KEY(gameId)
);

CREATE TABLE User (
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