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

2/19/2018
Refactor code to clean up clutter
Added cases for the ClientTester(Player case and send message case).
