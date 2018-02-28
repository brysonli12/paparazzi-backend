import java.sql.*; // Use classes in java.sql package
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

	public static Connection getConnection() {
		return CONNECTION;
	}
}

// JDK 1.7 and above
class Database {
	private Connection conn = ConnectionManager.getConnection();
	private Statement stmt;

	private static final String DB_NAME = "ebookshop";
	private static long msg_Id = 1L;
	private static final int MAX_IMAGE_LENGTH = 65345;
	private static final String GAME_SINGULAR = "game";
	private static final String GAME_PLURAL = "games";
	private static final String GAME_INFO = "gameInfo";
	private static final String GAME_DURATION = "gameDuration";
	private static final String PLAYER_COUNT = "playerCount";
	private static final String MSG_STATUS = "messagestatus";
	private static final String PLAYER_SINGULAR = "player";
	private static final String PLAYER_PLURAL = "players";
	private static final String CAPITAL_PLAYER = "Player";
	private static final String MESSAGE = "message";
	private static final String MESSAGE_PLURAL = "messages";
	private static final String CAPITAL_MESSAGE = "Message";
	private static final String SENT_FROM = "sentFrom";
	private static final String IMAGE = "image";
	private static final String GAME_ID = "GameId";
	private static final String LOWERCASE_GAME_ID = "gameId";
	private static final String TIMESTAMP = "timestamp";
	private static final String FB_USER_ID = "facebookUserId";
	private static final String GAME_RM_NAME = "gameRoomName";
	private static final String IMAGE_ID = "imageId";
	private static final String RATING = "rating";
	private static final String RATING_PLURAL = "ratings"; // internal and other
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String LOGIN_STATUS = "loginStatus";
	private static final String IMAGE_CONTENT = "imageContent";
	private static final String PLAYER_IDS = "playerIds";
	
	public Database() {

	}

	public JSONObject handleRequest(JSONObject obj, int requestType) {
		JSONObject t = new JSONObject();
		switch (requestType) {
		case 1:
			return login(obj);
		case 2:
			JSONArray games = _getGames(obj);
			if (games.isEmpty()) {
				t.put(GAME_PLURAL, null);
			} else {
				t.put(GAME_PLURAL, games);
			}
			return t;
		case 3:
			return storeMessage(obj);
		case 4:
			return createGame(obj);
		case 6:
			return joinGame(obj); // invite code and playerids
		case 7:
			return rateImage(obj); // rateimage
		case 8:
			JSONObject player = (JSONObject) obj.get(PLAYER_SINGULAR);
			String fbUID = (String) player.get(FB_USER_ID);
			if (!doesUserExist(fbUID)) {
				t.put(MSG_STATUS, "User does not exist");
			} else {
				t.put(MSG_STATUS, "success");
			}
			return t;
		}
		return null;

	}

	// JSONArray wrapped in JSONObject --> unwrapped
	public JSONObject joinGame(JSONObject req) {
		JSONObject status = new JSONObject();
		JSONObject play = (JSONObject) req.get(PLAYER_SINGULAR);
		String playId = (String) play.get(FB_USER_ID);
		String gameName = (String) req.get(GAME_RM_NAME);
		// can't join --> null, otherwise json object return null;
		int result = addPlayerToGame(gameName, playId);
		if (result == -1) // sql error
		{
			status.put(MSG_STATUS, "Server busy, try again.");
			return status;
		} else if (result == -2) // game room full
		{
			status.put(MSG_STATUS, "Unable to join, player count exceeded.");
			return status;
		} else if (result == -3) // game room name not found
		{
			status.put(MSG_STATUS, "Invalid game room entered.");
			return status;
		} else // if (result > 0)
		{
			status.put(MSG_STATUS, "success");
			return status;// return actual json for success
		}

	}

	// store gameInfo into table, make sure game room name hasn't been used
	public JSONObject createGame(JSONObject req) {
		System.out.println("CREATE GAME MSG: " + req);

		JSONObject result = new JSONObject();
		JSONObject game = (JSONObject) req.get(GAME_SINGULAR);
		JSONObject gameInfo = (JSONObject) game.get(GAME_INFO);
		String gameRmName = (String) gameInfo.get(GAME_RM_NAME);
		String gameDur = Long.toString((Long) gameInfo.get(GAME_DURATION));
		String maxPlayerCount = Long.toString((Long) gameInfo.get(PLAYER_COUNT));
		JSONArray plays = (JSONArray) game.get(PLAYER_PLURAL);
		String plays_for_db = playerToIdList(plays).toJSONString();

		PreparedStatement storeMsg = null;
		if (doesGameExist(gameRmName)) {
			return null;
		}

		try {

			String sqlInsert = "insert into Game " + "values (?,?,?,?,?,?,?)";
			storeMsg = conn.prepareStatement(sqlInsert);

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
			// storeMsg.setString(5, message);
			System.out.println("The SQL query is: " + sqlInsert); // Echo for
																	// debugging
			System.out.println(storeMsg);
			int countInserted = storeMsg.executeUpdate();
			System.out.println(countInserted + " records inserted into Game.\n");
			return result;
		} catch (SQLException ex) {

			ex.printStackTrace();
			return null;
		}
	}

	public JSONObject storeMessage(JSONObject req) {
		System.out.println("STORE MSG: " + req);
		//
		/*
		 * {"Message":{"sentFrom":{"firstName":"Bryan","lastName":"Ho",
		 * "facebookUserId":"10213545242363283"},"message":"hi",
		 * "image":"..............................................."},"GameId":
		 * 89}
		 */

		JSONObject result = new JSONObject();
		JSONObject actual_msg = (JSONObject) req.get(CAPITAL_MESSAGE);
		JSONObject sentFrom = (JSONObject) actual_msg.get(SENT_FROM);
		String id = (String) sentFrom.get(FB_USER_ID);
		String message = (String) actual_msg.get(MESSAGE);
		JSONObject img = (JSONObject) actual_msg.get(IMAGE);
		String gameId = Long.toString((Long) req.get(GAME_ID));
		PreparedStatement storeMsg = null;

		try {

			String sqlInsert = "insert into Messages " 
					+ "values (?,?,?,?,?,?)";
			storeMsg = conn.prepareStatement(sqlInsert);

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
			long time = cal.getTimeInMillis();

			storeMsg.setString(1, time + "-" + Long.toString(msg_Id));
			storeMsg.setString(2, id);
			storeMsg.setString(3, gameId);
			storeMsg.setTimestamp(4, timestamp);

			// later store message or image depending on available data
			if (message != null) {
				storeMsg.setString(5, message);
				storeMsg.setNull(6, java.sql.Types.VARCHAR); 
			} else if (img != null && ((String) img.get(IMAGE_CONTENT)).length() < MAX_IMAGE_LENGTH) {
				System.out.println("Image size is " + ((String) img.get(IMAGE_CONTENT)).length());
				storeMsg.setNull(5, java.sql.Types.VARCHAR); 
				String imgId = insertNewImage(img, (Long) req.get(GAME_ID));
				if (imgId.equals("null"))
					return null;
				storeMsg.setString(6, imgId);

			} else {
				return null;
			}
			System.out.println("The SQL query is: " + sqlInsert); // Echo for
																	// debugging
			System.out.println(storeMsg);
			int countInserted = storeMsg.executeUpdate();
			System.out.println(countInserted + " records inserted into Message table.\n");
			result.put(TIMESTAMP, time);

			addMessageToGame(gameId, time + "-" + Long.toString(msg_Id));

			return result;
		} catch (SQLException ex) {

			ex.printStackTrace();
			return null;
		}
	}

	public JSONObject rateImage(JSONObject req) {
		JSONObject result = new JSONObject();
		String gameRmName = (String) req.get(GAME_RM_NAME);
		String imgId = (String) req.get(IMAGE_ID);
		long rate = (Long) req.get(RATING);
		JSONObject play = (JSONObject) req.get(PLAYER_SINGULAR);
		String fbId = (String) play.get(FB_USER_ID);

		String status = "";
		if (!doesUserExist(fbId)) {
			status += " User does not exist. ";
		}
		if (!doesGameExist(gameRmName)) {
			status += " Game room does not exist. ";
		}

		if (status.isEmpty() == false) {
			result.put(MSG_STATUS, status);
		}
		try {
			stmt = conn.createStatement();
			System.out.println("finding the game");
			ResultSet games = stmt.executeQuery("select * from Game where gameRoomName='" + gameRmName + "'");
			if (games.next()) {
				String pIds = games.getString(PLAYER_IDS);

				// possible try/catch block here to catch parsing/ formatting
				// errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject) parser.parse("{\"array\": " + pIds + "}");
				} catch (ParseException e) {
					System.out.println("Messages. Parse error");
					e.printStackTrace();
					if (result.get(MSG_STATUS) == null) {
						result.put(MSG_STATUS, "games playerIds list parse error");
					} else
						result.put(MSG_STATUS,
								(String) result.get(MSG_STATUS) + " | games playerIds list parse error");
					return result;
				}
				JSONArray mIds = (JSONArray) (tmp.get("array"));
				List<String> playerIDS = new ArrayList<String>(mIds);
				// get index of player located in playerList in Game Table
				int playIdx = playerIDS.indexOf(fbId);
				ResultSet rates = stmt.executeQuery("select * from Image where imageId='" + imgId + "'");
				if (rates.next()) {

					String ratings_list = rates.getString(RATING_PLURAL);
					// possible try/catch block here to catch parsing/
					// formatting errors
					// parse already defined JSONParser parser = new
					// JSONParser();
					JSONObject tmp2 = new JSONObject();
					try {
						tmp2 = (JSONObject) parser.parse("{\"array\": " + ratings_list + "}");
					} catch (ParseException e) {
						System.out.println("Messages. Parse error");
						e.printStackTrace();
						if (result.get(MSG_STATUS) == null) {
							result.put(MSG_STATUS, "images ratings_list parse error");
						} else
							result.put(MSG_STATUS,
									(String) result.get(MSG_STATUS) + " | images ratings_list parse error");
						return result;
					}
					JSONArray rNums = (JSONArray) (tmp2.get("array"));
					List<Long> rating_lst = new ArrayList<Long>(rNums);
					rating_lst.set(playIdx, rate);

					String updateIds = "UPDATE Image SET ratings='" + rating_lst.toString() + "' WHERE imageId='"
							+ imgId + "'";

					System.out.println("UPDATING images rating: " + updateIds);
					int countUpdated = stmt.executeUpdate(updateIds);
					result.put(MSG_STATUS, "success");
					// return countUpdated;
					return result;
				}
				// possibly update game duration, etc.

			}
		} catch (SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
			if (result.get(MSG_STATUS) == null) {
				result.put(MSG_STATUS, "Server busy, try again");
			} else
				result.put(MSG_STATUS, (String) result.get(MSG_STATUS) + " | Server busy, try again");
			return result;
		}

		// update corresponding index in the rating list (in the Image table)
		// with the new rating
		if (result.get(MSG_STATUS) == null) {
			result.put(MSG_STATUS, "Something went wrong, ");
		} else
			result.put(MSG_STATUS, (String) result.get(MSG_STATUS)
					+ " | Something went wrong, and I don't know what. Please try again");
		return result;
	}

	public int addMessageToGame(String gameId, String msgId) {
		try {
			stmt = conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where gameId='" + gameId + "'");
			if (games.next()) {
				String msgIds = games.getString("allMessages");

				// possible try/catch block here to catch parsing/ formatting
				// errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject) parser.parse("{\"array\": " + msgIds + "}");
				} catch (ParseException e) {
					System.out.println("Messages. Parse error");
					e.printStackTrace();
					return -1;
				}
				JSONArray mIds = (JSONArray) (tmp.get("array"));
				mIds.add(msgId);
				String newMsg = mIds.toJSONString();
				// possibly update game duration, etc.
				String updateIds = "UPDATE Game SET allMessages='" + newMsg + "' WHERE gameId='" + gameId + "'";
				System.out.println("UPDATING player messages: " + updateIds);
				int countUpdated = stmt.executeUpdate(updateIds);
				return countUpdated;
			}
		} catch (SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
			return -1;
		}
		return -1;
	}

	// Prepared statement
	public int addPlayerToGame(String gameRmName, String playId) {
		try {
			stmt = conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where gameRoomName='" + gameRmName + "'");
			if (games.next()) {
				String playIds = games.getString(PLAYER_IDS);
				long maxPlayer = Long.parseLong(games.getString(PLAYER_COUNT));

				// possible try/catch block here to catch parsing/ formatting
				// errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject) parser.parse("{\"array\": " + playIds + "}");
				} catch (ParseException e) {
					System.out.println("Players. Parse error");
					e.printStackTrace();
					return -1;
				}
				JSONArray pIds = (JSONArray) (tmp.get("array"));
				int currSize = pIds.size();

				if (currSize + 1 > maxPlayer) {
					return -2; // full
				}

				pIds.add(playId);

				String newPlay = pIds.toJSONString();
				// possibly update game duration, etc.
				String updateIds = "UPDATE Game SET playerIds='" + newPlay + "' WHERE gameRoomName='" + gameRmName
						+ "'";
				System.out.println("UPDATING player ids: " + updateIds);
				int countUpdated = stmt.executeUpdate(updateIds);
				return countUpdated;
			}
		} catch (SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
			return -1;
		}
		return -3; // gameRoomName not found
	}

	public long getPlayerCount(long gameId) {
		try {
			stmt = conn.createStatement();
			ResultSet gameExists = stmt.executeQuery("select * from Game where gameId = " + gameId);
			if (!gameExists.next()) // if not found
			{
				return -1;
			}
			return gameExists.getLong(PLAYER_COUNT);
		} catch (SQLException ex) {
			ex.printStackTrace();
			return -1;
		}
	}

	public boolean doesGameExist(String gameName) {
		try {
			stmt = conn.createStatement();
			ResultSet gameExists = stmt.executeQuery("select * from Game where gameRoomName = '" + gameName + "'");
			if (!gameExists.next()) // if not found
			{
				return false;
			}
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return true;
		}
	}

	public boolean doesUserExist(String uId) {
		try {
			stmt = conn.createStatement();
			ResultSet userExists = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (!userExists.next()) // if not found
			{
				return false;
			}
			return true;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public JSONObject login(JSONObject req) {
		JSONObject games = new JSONObject();
		JSONObject playInfo = (JSONObject) req.get(CAPITAL_PLAYER);
		String uId = (String) playInfo.get(FB_USER_ID);
		String first = (String) playInfo.get(FIRST_NAME);
		String last = (String) playInfo.get(LAST_NAME);
		JSONObject result = new JSONObject();
		result.put(PLAYER_SINGULAR, playInfo);

		System.out.println(uId + " " + first + " " + last);

		try {
			stmt = conn.createStatement();
			ResultSet userExists = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (userExists.next()) {
				if (!userExists.getString("first").equals(first) || !userExists.getString("last").equals(last)) {
					updateUser(uId, first, last);
				}
				result.put(LOGIN_STATUS, "returningPlayer");
				//result.put("games", HelperTestClasses.randomGamesClass());
			} else {
				System.out.println("insert new player");
				insertNewUser(uId, first, last);
				result.put(LOGIN_STATUS, "newPlayer");
				//games.put("games", "null");
			}
		} catch (SQLException ex) {
			result.put(LOGIN_STATUS, "failed");
			//result.put("games", "null");
			return result;
			// return ex.printStackTrace();
		}
		return result;
	}

	public JSONArray getGames(JSONObject obj) {
		return HelperTestClasses.randomGamesClass();
	}

	/**
	 * Return a JSONArray of players (JSONObjects)
	 *
	 * @param playObjs
	 *            JSONArray list of player Ids
	 * @return the JSONArrays of Player JSONObjects in a specified format
	 */
	@SuppressWarnings("unchecked")
	private JSONArray playerToIdList(JSONArray playObjs) {
		JSONArray result = new JSONArray();
		try {
			stmt = conn.createStatement();
			for (Object obj : playObjs) {
				JSONObject aPlayer = (JSONObject) obj;
				result.add((String) aPlayer.get(FB_USER_ID));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Return a JSONArray of players (JSONObjects)
	 *
	 * @param plays
	 *            JSONArray list of playerUserIds (should not be a string)
	 *            unserialize before passing to the function
	 * @return the JSONArrays of players in a specified format
	 */
	private JSONArray getPlayers(JSONArray plays) {
		JSONArray result = new JSONArray();
		try {
			stmt = conn.createStatement();
			for (Object obj : plays) {
				JSONObject onePlayer = new JSONObject();
				//// System.out.println("QUERY: select * from Player where
				//// userId = '" + (String)obj + "'");
				ResultSet aUser = stmt.executeQuery("select * from Player where userId = '" + (String) obj + "'");
				if (aUser.next()) {
					onePlayer.put(FIRST_NAME, aUser.getString("first"));
					onePlayer.put(LAST_NAME, aUser.getString("last"));
					onePlayer.put(FB_USER_ID, aUser.getString("userId"));
				} else {
					onePlayer.put(FIRST_NAME, "null");
					onePlayer.put(LAST_NAME, "null");
					onePlayer.put(FB_USER_ID, (String) obj);
				}
				result.add(onePlayer);
			}
		} catch (SQLException ex) {
			result.add("SQLException");
			return result;
			// return ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Return a JSONObject for ONE player
	 *
	 * @param uId
	 *            facebook user id of the player
	 * @return JSONObject representing the player with uId
	 */
	private JSONObject getOnePlayer(String uId) {
		JSONObject onePlayer = new JSONObject();
		try {
			stmt = conn.createStatement();

			// System.out.println("QUERY(1): select * from Player where userId =
			// '" + uId + "'");
			ResultSet aUser = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
			if (aUser.next()) {
				onePlayer.put(FIRST_NAME, aUser.getString("first"));
				onePlayer.put(LAST_NAME, aUser.getString("last"));
				onePlayer.put(FB_USER_ID, aUser.getString("userId"));
			} else {
				onePlayer.put(FIRST_NAME, "null");
				onePlayer.put(LAST_NAME, "null");
				onePlayer.put(FB_USER_ID, uId);
			}
		} catch (SQLException ex) {
			onePlayer.put(FB_USER_ID, uId);
			onePlayer.put(FIRST_NAME, "error");
			onePlayer.put(LAST_NAME, "error");
			return onePlayer;
			// return ex.printStackTrace();
		}
		return onePlayer;
	}

	/**
	 * Return a JSONArray of messages (JSONObjects)
	 *
	 * @param msgs
	 *            JSONArray list of messageIds (should not be a string)
	 *            unserialize before passing to the function
	 * @return the JSONArrays of messages in a specified format
	 */
	private JSONArray _getMessages(JSONArray msgs) {
		JSONArray result = new JSONArray();
		try {
			stmt = conn.createStatement();
			for (Object obj : msgs) {
				JSONObject oneMsg = new JSONObject();
				/// System.out.println("QUERY: select * from Messages where
				/// msgId = '" + (String)obj + "'");
				ResultSet aMsg = stmt.executeQuery("select * from Messages where msgId = '" + (String) obj + "'");
				if (aMsg.next()) {
					oneMsg.put(SENT_FROM, getOnePlayer(aMsg.getString(SENT_FROM)));
					String msg = aMsg.getString(MESSAGE);
					String imgId = aMsg.getString(IMAGE);
					if (msg != null) {
						oneMsg.put(MESSAGE, msg);
					}
					if (imgId != null) {
						JSONObject anImage = new JSONObject();
						ResultSet aImg = stmt.executeQuery("select * from Image where imageId = '" + imgId + "'");
						if (aImg.next()) {
							anImage.put(IMAGE_ID, imgId);
							anImage.put(IMAGE_CONTENT, aImg.getString(IMAGE_CONTENT));
							String rArray = aImg.getString(RATING_PLURAL);
							JSONParser parser = new JSONParser();
							JSONObject tmp = new JSONObject();
							try {
								tmp = (JSONObject) parser.parse("{\"array\": " + rArray + "}");
							} catch (ParseException e) {
								System.out.println("ratings araray. Parse error");
								e.printStackTrace();
								result.add("Ratings array parse error");
								return result;
							}
							JSONArray pIds = (JSONArray) (tmp.get("array"));

							anImage.put(RATING_PLURAL, pIds);
							// insert target player later
						}
						oneMsg.put(IMAGE, anImage);
					}
				} else {
					oneMsg.put(SENT_FROM, "null");
				}
				result.add(oneMsg);
			}
		} catch (SQLException ex) {
			result.add("SQLException");
			return result;
			// return ex.printStackTrace();
		}
		return result;
	}

	private JSONArray _getGames(JSONObject req) {
		JSONArray allGames = new JSONArray();
		JSONObject player = (JSONObject) req.get(CAPITAL_PLAYER);
		String fbID = (String) player.get(FB_USER_ID);
		// System.out.println("Fetching games for " + fbID);
		try {
			stmt = conn.createStatement();
			ResultSet games = stmt.executeQuery("select * from Game where playerIds LIKE \'%" + fbID + "%\'");
			while (games.next()) {
				// System.out.println("Fetch a game for playerid");
				JSONObject oneGame = new JSONObject();
				JSONObject gameInf = new JSONObject();
				gameInf.put(GAME_RM_NAME, games.getString(GAME_RM_NAME));
				gameInf.put(GAME_DURATION, Long.parseLong(games.getString(GAME_DURATION)));
				gameInf.put(PLAYER_COUNT, Long.parseLong(games.getString(PLAYER_COUNT)));

				String playIds = games.getString(PLAYER_IDS);

				// possible try/catch block here to catch parsing/ formatting
				// errors
				JSONParser parser = new JSONParser();
				JSONObject tmp = new JSONObject();
				try {
					tmp = (JSONObject) parser.parse("{\"array\": " + playIds + "}");
				} catch (ParseException e) {
					System.out.println("Players. Parse error");
					e.printStackTrace();
				}
				JSONArray pIds = (JSONArray) (tmp.get("array"));
				JSONArray playersInGame = getPlayers(pIds);

				oneGame.put(GAME_INFO, gameInf);
				oneGame.put(PLAYER_PLURAL, playersInGame);
				oneGame.put(LOWERCASE_GAME_ID, Long.parseLong(games.getString(LOWERCASE_GAME_ID)));

				String msgIds = games.getString("allMessages");
				// System.out.println("message ids" + msgIds);

				// get all messages here
				try {
					tmp = (JSONObject) parser.parse("{\"array\": " + msgIds + "}");
				} catch (ParseException e) {
					System.out.println("Msg. Parse error");
					e.printStackTrace();
				}
				JSONArray mIds = (JSONArray) (tmp.get("array"));
				JSONArray allMsg = _getMessages(mIds);
				oneGame.put(MESSAGE_PLURAL, allMsg);

				allGames.add(oneGame);

			}
		} catch (SQLException ex) {
			// return something else if exception
			ex.printStackTrace();
		}
		return allGames;
	}

	/**
	 * Makes a request to the database to update a user's first and/or last
	 * name.
	 *
	 * @param id
	 *            the user's id (Primary key - should be unique)
	 * @param first
	 *            first name of the user (this may have been changed)
	 * @param last
	 *            last name of the user (this may have been changed)
	 * @return the number of records updated
	 */
	public int updateUser(String id, String first, String last) {
		try {
			stmt = conn.createStatement();
			String strUpdate = "UPDATE Player SET first = '" + first + "', last = '" + last + "' WHERE userId = " + "'"
					+ id + "'";
			System.out.println("The SQL query is: " + strUpdate); // Echo for
																	// debugging
			int countUpdated = stmt.executeUpdate(strUpdate);
			System.out.println(countUpdated + " records updated.\n");
			return countUpdated;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	/**
	 * Makes a request to the database to store a new image. Returns id of newly
	 * stored image.
	 * 
	 *
	 * @param newImg
	 *            a JSONObject with imageContent and gameId
	 * @return id of newly stored image ("null" if can't store)
	 */
	public String insertNewImage(JSONObject newImg, long gameId) {
		String imgCt = (String) newImg.get(IMAGE_CONTENT);
		String imgId = Long.toString(System.currentTimeMillis());
		PreparedStatement imgStore = null;
		try {

			String sqlInsert = "insert into Image " // need a space
					+ "values (?,?,?,?)";
			imgStore = conn.prepareStatement(sqlInsert);
			imgStore.setString(1, imgId);
			imgStore.setNull(2, java.sql.Types.VARCHAR);
			JSONArray initial_ratings = new JSONArray();
			long playCt = getPlayerCount(gameId);
			if (playCt == -1)
				return "null";
			for (long tmp = 0L; tmp < playCt; tmp += 1L) {
				initial_ratings.add(0);
				System.out.println("tmp" + tmp + " numPlayers " + playCt);
			}
			System.out.println("after the creation of initial ratings");
			List<String> a = new ArrayList<String>(initial_ratings);
			System.out.println("the new string as arraylist " + a);
			imgStore.setString(3, initial_ratings.toJSONString());
			imgStore.setString(4, imgCt);
			// System.out.println("The SQL query is: " + sqlInsert); // Echo for
			// debugging
			int countInserted = imgStore.executeUpdate();
			System.out.println(countInserted + " records inserted into Image.\n");
			if (countInserted > 0)
				return imgId;
			else
				return "null";
		} catch (SQLException ex) {
			ex.printStackTrace();
			return "null"; // failed
		}
	}

	/**
	 * Makes a request to the database to insert a new user with userID, first,
	 * and last name
	 *
	 * @param id
	 *            the user's id (Primary key - should be unique)
	 * @param first
	 *            first name of the user
	 * @param last
	 *            last name of the user
	 * @return the number of records updated
	 */
	public int insertNewUser(String id, String first, String last) {
		try {
			stmt = conn.createStatement();
			String sqlInsert = "insert into Player " // need a space
					+ "values (\'" + id + "\', \'" + first + "\', \'" + last + "\')";
			System.out.println("The SQL query is: " + sqlInsert); // Echo for
																	// debugging
			int countInserted = stmt.executeUpdate(sqlInsert);
			System.out.println(countInserted + " records inserted.\n");
			return countInserted;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	/**
	 * Makes a request to the database to clear all messages.
	 *
	 * 
	 * @return the number of records updated
	 */
	public int clearMessages() {
		try {
			stmt = conn.createStatement();
			String sqlInsert = "update game set allMessages=\"[]\"";
			System.out.println("The SQL query is: " + sqlInsert); // Echo for
																	// debugging
			int countInserted = stmt.executeUpdate(sqlInsert);
			System.out.println(countInserted + " records updated (messages cleared).\n");

			sqlInsert = "truncate table messages";
			System.out.println("The SQL query is: " + sqlInsert); // Echo for
																	// debugging
			countInserted = stmt.executeUpdate(sqlInsert);
			System.out.println(countInserted + " records updated (messages table cleared).\n");
			return countInserted;
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return -1;
	}

	public static void main(String[] args) {
		// Some Manual testing
		Database db_utils = new Database();
		// JSONArray ab = new JSONArray();
		// ab.add("10213545242363283");
		// ab.add("08WK90K00X24GHNR3D90SO");
		// System.out.println("PLAYER INFO\n" +
		// db_utils.getPlayers(ab).toString());
		// System.out.println("GAMES\n" + db_utils._getGames());
		// {"Message":{"sentFrom":{"firstName":"Bryan","lastName":"Ho","facebookUserId":"10213545242363283"},"message":"hi"},"GameId":89}

		JSONObject testMsg = new JSONObject();
		testMsg.put(SENT_FROM, db_utils.getOnePlayer("10213545242363283"));
		testMsg.put(MESSAGE, "hi\0123 4");
		JSONObject msg = new JSONObject();
		msg.put(CAPITAL_MESSAGE, testMsg);
		msg.put(GAME_ID, 89L);
		db_utils.storeMessage(msg);
		// db_utils.insertNewUser("UserID1", "U1_first", "U1_last");
		// db_utils.updateUser("UserID1", "U1_first", "new_last");
	}
}
