2/5/2018
Simple backend server. This server can so far handle only a player JSON request.
To test: 
-json simple library required
-launch launcher.java first then clientTester.java
-modify variable settings in HelperTestClasses.java

2/6/2018
Simple back end server now using HTTP protocol. Still only handles player JSON request.
To test:
-json simple library required
-need to access com.sun.httpserver
-launch Launcher.java first then ClientTester.java
-modify variable settings in HelperTestClasses.java

Added:
uses HTTP protocol
now handles player request by specifying http://<ip>:<port>/<context>
	where <ip> is IP found in HelperTestClasses
	where <port> is port found in HelperTestClasses
	where <context> is found in HelperTestClasses

Removed:
removed AppServer.java as raw sockets are not needed anymore

2/7/2018:
Separated login and get games functions (added /getgames)
Login working.  Getgames working but frontend needs to fix something
Changed related functions JSONObject response to communicate with frontend

Added:
retrieve game handler to separate login/ get games. 

2/22/2018 ACCUMULATIVE:
Special update as we didn't update the changes after 2/7/2018 to now

-Able to send images and messages.
-Able to rate images.
-Able to join a game room.
-Able to check if user is in our database.

-Changes to JSONObjects:
Message
Game
Image

TODO:
Refactor DataBaseUtils

2/27/2018:
-Refactored DataBaseUtils

3/3/2018:
-Added skeleton code for GameLogicThread
-Added skeleton code for StartGameHandler

3/8/2018:
-Worked on and finished starting game
-Worked on initial steps of Game Logic (already have logic written down, mostly just converting to code)