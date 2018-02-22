import java.sql.*;     // Use classes in java.sql package

import java.util.Calendar;
import java.util.TimeZone;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject; 
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ConnectionManager {
	private static final String URL = "jdbc:mysql://localhost:3306/ebookshop?useSSL=false";
	private static final String USER = "root";
	private static final String PASSWORD = "cs130";

	private static final Connection CONNECTION = createConnection();

	private static Connection createConnection() {
		try {
			return DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public static Connection getConnection(){
		return CONNECTION;
	}
}


// JDK 1.7 and above
class Database {
	private Connection conn = ConnectionManager.getConnection();
	private Statement stmt;

	// Will use preparedStatements later after getting a simple version working
	//private PreparedStatement pstmt = conn.prepareStatement("UPDATE users
	//                                   SET first = ?, last = ? WHERE ID = ?");
	private static final String DB_NAME = "ebookshop";
	private static long msg_Id = 1L;
	
	public Database()
	{

	}

	public JSONObject handleRequest(JSONObject obj, int requestType)
	{
		JSONObject t = new JSONObject();
		switch(requestType)
		{
		case 1:
			return login(obj);
		case 2:
			JSONArray games = _getGames(obj);
			if (games.isEmpty())
			{
				t.put("games", null);
			}
			else
			{
				t.put("games", games);
			}
			return t;
		case 3:
			return storeMessage(obj);
		case 4:
			return createGame(obj);
		case 6:
			return joinGame(obj); // invite code and playerids
		}
		return null;

	}

	// JSONArray wrapped in JSONObject --> unwrapped
	public JSONObject joinGame(JSONObject req)
	{
		JSONObject status = new JSONObject();
		JSONObject play = (JSONObject)req.get("player");
		String playId = (String)play.get("facebookUserId");
		String gameName = (String)req.get("gameRoomName");
		// can't join --> null, otherwise json object		return null;
		int result = addPlayerToGame(gameName, playId);
		if (result == -1) // sql error
		{
			status.put("messagestatus", "Server busy, try again.");
			return status;
		}
		else if (result == -2) // game room full
		{
			status.put("messagestatus", "Unable to join, player count exceeded.");
			return status;
		}
		else if(result == -3) // game room name not found
		{
			status.put("messagestatus", "Invalid game room entered.");
			return status;
		}
		else //if (result > 0)
		{
			status.put("messagestatus","success");
			return status;// return actual json for success
		} 
		 
	}
	// store gameInfo into table, make sure game room name hasn't been used
	public JSONObject createGame(JSONObject req)
	{
		System.out.println("CREATE GAME MSG: " + req);


		JSONObject result = new JSONObject();
		JSONObject game = (JSONObject)req.get("game");
		JSONObject gameInfo = (JSONObject) game.get("gameInfo");
		String gameRmName = (String)  gameInfo.get("gameRoomName");
		String gameDur = Long.toString((Long)gameInfo.get("gameDuration"));
		String maxPlayerCount = Long.toString((Long)gameInfo.get("playerCount"));
		JSONArray plays =  (JSONArray)game.get("player");
		String plays_for_db = playerToIdList(plays).toJSONString();
		
		PreparedStatement storeMsg = null;
		if (doesGameExist(gameRmName))
		{
			return null;
		}

		try {
			
			String sqlInsert = "insert into Game " 
					+ "values (?,?,?,?,?,?,?)";
			storeMsg = conn.prepareStatement(sqlInsert);
			//storeMsg.setNull(1, java.sql.Types.VARCHAR);
			//storeMsg.setString(2, id);
			//storeMsg.setLong(3, gameId);

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			long time = cal.getTimeInMillis();
			storeMsg.setLong(1, time);
			storeMsg.setString(2, plays_for_db);
			
			Timestamp timestamp = new Timestamp(time);
			storeMsg.setTimestamp(3, timestamp);
			
			storeMsg.setString(4, "[]");
			storeMsg.setString(5, gameRmName);
			storeMsg.setString(6, gameDur);
			storeMsg.setString(7, maxPlayerCount);

			// later store message or image depending on available data
			//storeMsg.setString(5, message);
			System.out.println("The SQL query is: " + sqlInsert);  // Echo for debugging
			System.out.println(storeMsg);
			int countInserted = storeMsg.executeUpdate();
			System.out.println(countInserted + " records inserted into Game.\n");
			return result;
		} catch (SQLException ex) {

			ex.printStackTrace();
			return null;
		}
	}
	
	public JSONObject storeMessage(JSONObject req)
	{
		System.out.println("STORE MSG: " + req);
		//
		/*
		 * {"Message":{"sentFrom":{"firstName":"Bryan","lastName":"Ho","facebookUserId":"10213545242363283"},"message":"hi"},"GameId":89}
		 */
		
		JSONObject result = new JSONObject();
		JSONObject msgInfo = (JSONObject) req.get("Message");
		JSONObject sentFrom = (JSONObject)  msgInfo.get("sentFrom");
		String id = (String)sentFrom.get("facebookUserId");
		String message = (String) msgInfo.get("message");
		//System.out.println("GAME ID" + req.get("GameID").getClass().getName());
		System.out.println("before get game id");
		String gameId = Long.toString((Long)req.get("GameId"));
		PreparedStatement storeMsg = null;


		try {

			String sqlInsert = "insert into Messages " // need a space "2004-05-23 14:25:10"
					+ "values (?,?,?,?,?,?)";
			storeMsg = conn.prepareStatement(sqlInsert);
			

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
			long time = cal.getTimeInMillis();
			
			storeMsg.setString(1, time + "-" +Long.toString(msg_Id));
			storeMsg.setString(2, id);
			storeMsg.setString(3, gameId);
			storeMsg.setTimestamp(4, timestamp);
			

			// later store message or image depending on available data
			storeMsg.setString(5, message);
			storeMsg.setNull(6, java.sql.Types.VARCHAR); // for now...later store image
			System.out.println("The SQL query is: " + sqlInsert);  // Echo for debugging
			System.out.println(storeMsg);
			int countInserted = storeMsg.executeUpdate();
			System.out.println(countInserted + " records inserted into Message table.\n");
			result.put("timestamp", time);
			
			addMessageToGame(gameId, time + "-" +Long.toString(msg_Id));
			
			
			return result;
		} catch (SQLException ex) {

			ex.printStackTrace();
			return null;
		}		
	}

	public int addMessageToGame(String gameId, String msgId)
	{
		try
		{
			stmt =  conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where gameId=" + gameId);
			if (games.next()) {	
				String msgIds =  games.getString("allMessages");

				// possible try/catch block here to catch parsing/ formatting errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject)parser.parse("{\"array\": " + msgIds + "}" );
				} catch (ParseException e) {
					System.out.println("Messages. Parse error");
					e.printStackTrace();
					return -1;
				}
				JSONArray mIds = (JSONArray) (tmp.get("array"));
				mIds.add(msgId);
				String newMsg = mIds.toJSONString();
				// possibly update game duration, etc.
				String updateIds = "UPDATE Game SET allMessages='" + newMsg + "' WHERE gameId="+gameId;
				System.out.println("UPDATING player messages: " + updateIds);
				int countUpdated = stmt.executeUpdate(updateIds);
				return countUpdated;
			}
		} catch(SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
			return -1;
		}
		return -1;
	}
	
	// Prepared statement
	public int addPlayerToGame(String gameRmName, String playId)
	{
		try
		{
			stmt =  conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where gameRoomName='" + gameRmName + "'");
			if (games.next()) {	
				String playIds =  games.getString("playerIds");
				long maxPlayer =  Long.parseLong(games.getString("playerCount"));

				// possible try/catch block here to catch parsing/ formatting errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject)parser.parse("{\"array\": " + playIds + "}" );
				} catch (ParseException e) {
					System.out.println("Players. Parse error");
					e.printStackTrace();
					return -1;
				}
				JSONArray pIds = (JSONArray) (tmp.get("array"));
				int currSize = pIds.size();
				
				if (currSize + 1 > maxPlayer)
				{
					return -2; // full
				}
				pIds.add(playId);
				
				String newPlay = pIds.toJSONString();
				// possibly update game duration, etc.
				String updateIds = "UPDATE Game SET playerIds='" + newPlay + "' WHERE gameRoomName='"+gameRmName+"'";
				System.out.println("UPDATING player ids: " + updateIds);
				int countUpdated = stmt.executeUpdate(updateIds);
				return countUpdated;
			}
		} catch(SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
			return -1;
		}
		return -3; // gameRoomName not found
	}

	public boolean doesGameExist(String gameName)
	{
		try
		{
			stmt =  conn.createStatement();
			ResultSet gameExists = stmt.executeQuery("select * from Game where gameRoomName = '" + gameName + "'");
			if (!gameExists.next()) // if not found
			{
				return false;
			}
			return true;
		} catch(SQLException ex) {
			ex.printStackTrace();
			return true;
		}
	}

	public boolean doesUserExist(String uId)
	{
		try
		{
			stmt =  conn.createStatement();
			ResultSet userExists = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (!userExists.next()) // if not found
			{
				return false;
			}
			return true;
		} catch(SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public JSONObject login(JSONObject req)
	{
		JSONObject games = new JSONObject();
		JSONObject playInfo = (JSONObject) req.get("Player");
		String uId = (String) playInfo.get("facebookUserId");
		String first = (String) playInfo.get("firstName");
		String last = (String) playInfo.get("lastName");
		JSONObject result = new JSONObject();
		result.put("player", playInfo);
		//games.put("games", "null");

		System.out.println(uId + " " + first + " " + last);

		try
		{
			stmt =  conn.createStatement();
			ResultSet userExists = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (userExists.next())
			{
				if (!userExists.getString("first").equals(first) || !userExists.getString("last").equals(last))
				{
					updateUser(uId, first, last);
				}
				result.put("loginStatus", "returningPlayer");
				result.put("games", HelperTestClasses.randomGamesClass());
			}
			else
			{
				System.out.println("insert new player");
				insertNewUser(uId, first, last);
				result.put("loginStatus", "newPlayer");
				games.put("games", "null");
			}
		} catch (SQLException ex) {
			result.put("loginStatus", "failed");
			result.put("games","null");
			return result;
			//return ex.printStackTrace();
		}


		//result.put("games", games);
		return result;
	}

	public JSONArray getGames(JSONObject obj)
	{
		return HelperTestClasses.randomGamesClass();	  
	}

	/**
	 * Return a JSONArray of players (JSONObjects)
	 *
	 * @param playObjs JSONArray list of player Ids
	 * @return      the JSONArrays of Player JSONObjects in a specified format
	 */
	@SuppressWarnings("unchecked")
	private JSONArray playerToIdList(JSONArray playObjs)
	{
		JSONArray result = new JSONArray();
		try
		{
			stmt =  conn.createStatement();
			for (Object obj: playObjs)
			{
				JSONObject aPlayer = (JSONObject)obj;
				result.add((String) aPlayer.get("facebookUserId"));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}


	/**
	 * Return a JSONArray of players (JSONObjects)
	 *
	 * @param plays JSONArray list of playerUserIds (should not be a string) 
	 *		unserialize before passing to the function
	 * @return      the JSONArrays of players in a specified format
	 */
	private JSONArray getPlayers(JSONArray plays)
	{
		JSONArray result = new JSONArray();
		try
		{
			stmt =  conn.createStatement();
			for (Object obj: plays)
			{
				JSONObject onePlayer = new JSONObject();
				////System.out.println("QUERY: select * from Player where userId = '" + (String)obj + "'");
				ResultSet aUser = stmt.executeQuery("select * from Player where userId = '" + (String)obj + "'");
				if (aUser.next())
				{
					onePlayer.put("firstName", aUser.getString("first"));
					onePlayer.put("lastName", aUser.getString("last"));
					onePlayer.put("facebookUserId", aUser.getString("userId"));
				}
				else
				{
					onePlayer.put("firstName", "null");
					onePlayer.put("lastName", "null");
					onePlayer.put("facebookUserId", (String)obj);
				}
				result.add(onePlayer);
			}
		} catch (SQLException ex) {
			result.add("SQLException");
			return result;
			//return ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Return a JSONObject for ONE player
	 *
	 * @param uId facebook user id of the player
	 * @return      JSONObject representing the player with uId
	 */
	private JSONObject getOnePlayer(String uId)
	{
		JSONObject onePlayer = new JSONObject();
		try
		{
			stmt =  conn.createStatement();

			System.out.println("QUERY(1): select * from Player where userId = '" + uId + "'");
			ResultSet aUser = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (aUser.next())
			{
				onePlayer.put("firstName", aUser.getString("first"));
				onePlayer.put("lastName", aUser.getString("last"));
				onePlayer.put("facebookUserId", aUser.getString("userId"));
			}
			else
			{
				onePlayer.put("firstName", "null");
				onePlayer.put("lastName", "null");
				onePlayer.put("facebookUserId", uId);
			}
		} catch (SQLException ex) {
			onePlayer.put("facebookUserId", uId);
			onePlayer.put("firstName", "error");
			onePlayer.put("lastName", "error");
			return onePlayer;
			//return ex.printStackTrace();
		}
		return onePlayer;
	}
	/**
	 * Return a JSONArray of messages (JSONObjects)
	 *
	 * @param msgs JSONArray list of messageIds (should not be a string) 
	 *		unserialize before passing to the function
	 * @return      the JSONArrays of messages in a specified format
	 */
	private JSONArray _getMessages(JSONArray msgs)
	{
		JSONArray result = new JSONArray();
		try
		{
			stmt =  conn.createStatement();
			for (Object obj: msgs)
			{
				JSONObject oneMsg = new JSONObject();
				System.out.println("QUERY: select * from Messages where msgId = '" + (String)obj + "'");
				ResultSet aMsg = stmt.executeQuery("select * from Messages where msgId = '" + (String)obj + "'");
				if (aMsg.next())
				{
					//oneMsg.put("image", aMsg.getString("first"));\
					oneMsg.put("sentFrom", getOnePlayer(aMsg.getString("sentFrom")));
					String msg = aMsg.getString("message");
					String img = aMsg.getString("image");
					if (msg != null)
					{
						oneMsg.put("message", msg);
					}
					if (img != null)
					{
						oneMsg.put("image", img);
					}
				}
				else
				{
					oneMsg.put("sentFrom", "null");
				}
				result.add(oneMsg);
			}
		} catch (SQLException ex) {
			result.add("SQLException");
			return result;
			//return ex.printStackTrace();
		}
		return result;
	}

	private JSONArray _getGames(JSONObject req)
	{
		JSONArray allGames = new JSONArray();
		JSONObject player = (JSONObject)req.get("Player");
		String fbID = (String)player.get("facebookUserId");
		//System.out.println("Fetching games for " + fbID);
		try
		{
			stmt =  conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where playerIds LIKE \'%" + fbID + "%\'");
			while (games.next()) {	
				//System.out.println("Fetch a game for playerid");
				JSONObject oneGame = new JSONObject();
				JSONObject gameInf = new JSONObject();
				gameInf.put("gameRoomName", games.getString("gameRoomName"));
				gameInf.put("gameDuration", Long.parseLong(games.getString("gameDuration"))); 
				gameInf.put("playerCount", Long.parseLong(games.getString("playerCount"))); 

				String playIds =  games.getString("playerIds");

				// possible try/catch block here to catch parsing/ formatting errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject)parser.parse("{\"array\": " + playIds + "}" );
				} catch (ParseException e) {
					System.out.println("Players. Parse error");
					e.printStackTrace();
				}
				JSONArray pIds = (JSONArray) (tmp.get("array"));
				JSONArray playersInGame = getPlayers(pIds);

				/////System.out.println("players in game " + playersInGame.toJSONString());
				oneGame.put("gameInfo", gameInf);
				oneGame.put("players", playersInGame);
				oneGame.put("gameId", Long.parseLong(games.getString("gameId")));

				String msgIds =  games.getString("allMessages");
				//System.out.println("message ids" + msgIds);

				// get all messages here
				try {
					tmp = (JSONObject)parser.parse("{\"array\": " + msgIds + "}" );
				} catch (ParseException e) {
					System.out.println("Msg. Parse error");
					e.printStackTrace();
				}
				JSONArray mIds = (JSONArray) (tmp.get("array"));
				JSONArray allMsg = _getMessages(mIds);
				oneGame.put("messages", allMsg);

				allGames.add(oneGame);

			}
		} catch(SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
		}
		return allGames;
	}

	/**
	 * Makes a request to the database to update a user's
	 * first and/or last name.
	 *
	 * @param id    the user's id (Primary key - should be unique)
	 * @param first first name of the user (this may have been changed)
	 * @param last  last name of the user (this may have been changed)
	 * @return      the number of records updated
	 */
	public int updateUser(String id, String first, String last)
	{
		try {
			stmt =  conn.createStatement();
			String strUpdate = "UPDATE Player SET first = '" + first + 
					"', last = '" + last + 
					"' WHERE userId = " + "'" + id + "'";
			System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
			int countUpdated = stmt.executeUpdate(strUpdate);
			System.out.println(countUpdated + " records updated.\n");
			return countUpdated;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	/**
	 * Makes a request to the database to insert a new user with userID,
	 * first, and last name
	 *
	 * @param id    the user's id (Primary key - should be unique)
	 * @param first first name of the user
	 * @param last  last name of the user
	 * @return      the number of records updated
	 */
	public int insertNewUser(String id, String first, String last)
	{
		try {
			stmt =  conn.createStatement();
			String sqlInsert = "insert into Player " // need a space
					+ "values (\'"+ id + "\', \'" + first + "\', \'" + last + "\')";
			System.out.println("The SQL query is: " + sqlInsert);  // Echo for debugging
			int countInserted = stmt.executeUpdate(sqlInsert);
			System.out.println(countInserted + " records inserted.\n");
			return countInserted;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	public static void main(String[] args) {
		// Some Manual testing
		Database db_utils = new Database();
		JSONArray ab = new JSONArray();
		ab.add("10213545242363283");
		ab.add("08WK90K00X24GHNR3D90SO");
		System.out.println("PLAYER INFO\n" + db_utils.getPlayers(ab).toString());
		//System.out.println("GAMES\n" + db_utils._getGames());
		//{"Message":{"sentFrom":{"firstName":"Bryan","lastName":"Ho","facebookUserId":"10213545242363283"},"message":"hi"},"GameId":89}

		JSONObject testMsg = new JSONObject();
		testMsg.put("sentFrom", db_utils.getOnePlayer("10213545242363283"));
		testMsg.put("message", "hi");
		JSONObject msg = new JSONObject();
		msg.put("Message", testMsg);
		msg.put("GameId", 89);
		db_utils.storeMessage(msg);
		//db_utils.insertNewUser("UserID1", "U1_first", "U1_last");
		//db_utils.updateUser("UserID1", "U1_first", "new_last");
	}
}
