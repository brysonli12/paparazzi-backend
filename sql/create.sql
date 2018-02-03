-- INNODB supports transaction, row-level locking, and foreign keys
-- this is the default engine

CREATE TABLE Photo (
	personInPhoto varchar(20),
	score int
)

CREATE TABLE GameRoom (
	-- all new Ids userIds GameRoomIDs
	-- photos
	counter int, -- paparazzi counter
	gameId int,
	startTime timestamp,
	-- all chat messages
	gameRm varchar(20), -- game room name
	paparazzi int
)

CREATE TABLE GameRoomIDs (
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
)

CREATE TABLE User (
	userId varchar(20),
	first varchar(20),
	last varchar(20),
) ENGINE = INNODB 

CREATE TABLE Message (
	userId varchar(20),
	gameId int,
	message varchar(50)
)