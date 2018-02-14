import java.sql.*;     // Use classes in java.sql package

import org.json.simple.JSONArray;
import org.json.simple.JSONObject; 

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
		switch(requestType)
		{
		case 1:
			return login(obj);
		case 2:
			JSONObject t = new JSONObject();
			t.put("games",getGames(obj));
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
    public JSONArray convertToJSON(ResultSet resultSet)
            throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            int total_rows = resultSet.getMetaData().getColumnCount();
            for (int i = 0; i < total_rows; i++) {
                JSONObject obj = new JSONObject();
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase(), resultSet.getObject(i + 1));
                jsonArray.put(obj);
            }
        }
        return jsonArray;
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
				System.out.println("QUERY: select * from Player where userId = '" + (string)obj + "'");
				ResultSet aUser = stmt.executeQuery("select * from Player where userId = '" + (string)obj + "'");
				if (aUser.next())
				{
					onePlayer.put("firstName", aUser.getString("first"));
					onePlayer.put("lastName", aUser.getString("last"));
					onePlayer.put("facebookUserId", aUser.getString("userId"));
				}
				else
				{
					onePlayer.put("firstName", "null");
					onePlayer.put("lastName", "null:);
					onePlayer.put("facebookUserId", (string)obj);
				}
				result.put(onePlayer);
			}
		} catch (SQLException ex) {
			result.put("SQLException");
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
				 gameInf.put("gameRoomName", games.getString("gameRoomName");
				 gameInf.put("gameDuration", games.getString("gameDuration"); // getInt?
			         gameInf.put("playerCount", games.getString("playerCount"); // getInt?
				
				String playIds =  game.getString("playerIds");
					     
				// possible try/catch block here to catch parsing/ formatting errors
			 	JSONArray pIds = new JSONArray(playIds);
				JSONArray playersInGame = getPlayers(pIds);
		   		//JSONArray userGames = convertToJSON(games);
				oneGame.put("gameInfo", gameInf);
				oneGame.put("players", playersInGame);
				oneGame.put("gameId", games.getString("gameId"));
					     
				// get all messages here
					     
				allGames.put(oneGame);
		   		
		 	}
		 } catch(SQLException ex) {
		   ex.printStackTrace();
		}
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
		//DatabaseUtils db_utils = new DatabaseUtils();
		//db_utils.insertNewUser("UserID1", "U1_first", "U1_last");
		//db_utils.updateUser("UserID1", "U1_first", "new_last");
	}
}
