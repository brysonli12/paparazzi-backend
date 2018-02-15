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
			t.put("games",getGames(obj));
			return t;
		case 3:
			t.put("timeStamp", storeMessage(obj));
			return t;
		}
		return null;

	}
	/**
	 * Convert a result set into a JSON Array
	 * @param resultSet
	 * @return a JSONArray
	 * @throws Exception
	 * @author marlonlom (link in References file)
	 */
	/*public JSONArray convertToJSON(ResultSet resultSet)
			throws Exception {
		JSONArray jsonArray = new JSONArray();
		while (resultSet.next()) {
			int total_rows = resultSet.getMetaData().getColumnCount();
			for (int i = 0; i < total_rows; i++) {
				JSONObject obj = new JSONObject();
				obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));
				jsonArray.put(obj);
			}
		}
		return jsonArray;
	}*/


	public JSONObject storeMessage(JSONObject req)
	{
		System.out.println("STORE MSG: " + req);
		
		/*
		 * {"Message":{"sentFrom":{"firstName":"Bryan","lastName":"Ho","facebookUserId":"10213545242363283"},"message":"hi"},"GameId":89}
		 */
		
		JSONObject result = new JSONObject();
		JSONObject msgInfo = (JSONObject) req.get("Message");
		JSONObject sentFrom = (JSONObject)  msgInfo.get("sentFrom");
		String id = (String)sentFrom.get("facebookUserId");
		String message = (String)msgInfo.get("message");
		int gameId = (Integer) req.get("GameId");
		PreparedStatement storeMsg = null;
		
		
		try {
			int time = (int)System.currentTimeMillis();
			String sqlInsert = "insert into Messages " // need a space "2004-05-23 14:25:10"
					+ "values (?,?,?,?,?,?)";
			storeMsg = conn.prepareStatement(sqlInsert);
			storeMsg.setNull(1, java.sql.Types.VARCHAR);
			storeMsg.setString(2, id);
			storeMsg.setInt(3, gameId);
			
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
			storeMsg.setTimestamp(4, timestamp);
			
			// later store message or image depending on available data
			storeMsg.setString(5, message);
			storeMsg.setNull(6, java.sql.Types.VARCHAR); // for now...later store image
			System.out.println("The SQL query is: " + sqlInsert);  // Echo for debugging
			System.out.println(storeMsg);
			int countInserted = storeMsg.executeUpdate();
			System.out.println(countInserted + " records inserted.\n");
			result.put("timestamp", time);
			return result;
		} catch (SQLException ex) {
			
			ex.printStackTrace();
			return null;
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
				System.out.println("QUERY: select * from Player where userId = '" + (String)obj + "'");
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

	private JSONArray _getGames()
	{
		JSONArray allGames = new JSONArray();
		try
		{
			stmt =  conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game");
			while (games.next()) {	
				JSONObject oneGame = new JSONObject();
				JSONObject gameInf = new JSONObject();
				gameInf.put("gameRoomName", games.getString("gameRoomName"));
				gameInf.put("gameDuration", games.getInt("gameDuration")); // getInt?
				gameInf.put("playerCount", games.getInt("playerCount")); // getInt?

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
				//JSONArray userGames = convertToJSON(games);
				oneGame.put("gameInfo", gameInf);
				oneGame.put("players", playersInGame);
				oneGame.put("gameId", games.getInt("gameId"));

				String msgIds =  games.getString("allMessages");
				System.out.println("message ids" + msgIds);
				
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
		System.out.println("GAMES\n" + db_utils._getGames());
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
