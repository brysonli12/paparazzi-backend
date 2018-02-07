import java.sql.*;     // Use classes in java.sql package
import org.json.simple.JSONObject; 
import org.json.JSONArray;  

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
class DatabaseUtils {
  private Connection conn = ConnectionManager.getConnection();
  private Statement stmt;
  // Will use preparedStatements later after getting a simple version working
  //private PreparedStatement pstmt = conn.prepareStatement("UPDATE users
  //                                   SET first = ?, last = ? WHERE ID = ?");
  private static final String DB_NAME = "ebookshop";

  public DatabaseUtils()
  {

  }

    /**
     * Convert a result set into a JSON Array
     * @param resultSet
     * @return a JSONArray
     * @throws Exception
     */
    public static JSONArray convertToJSON(ResultSet resultSet)
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
      }
  }

  public JSONObject login(String uId, String first, String last)
  {
    JSONObject result = new JSONObject();
    JSONObject playInfo = new JSONObject();
    playInfo.put("firstName", first);
    playInfo.put("lastName", last);
    playInfo.put("facebookUserId", uId);
    result.put("Player", playInfo);
    try
    {
      ResultSet userExists = stmt.executeQuery("select * from Player where userId = '" + uId + "'");
      if (userExists.next())
      {
        if (!userExists.getString("first").equals(first) || !userExists.getString("last").equals(last))
          updateUser(uId, first, last);
        result.put("loginStatus", "returningPlayer");
      }
      else
      {
        insertNewUser(uId, first, last);
        result.put("loginStatus", "newPlayer");
      }
    } catch (SQLException ex) {
      result.put("loginStatus", "failed");
      return result;
      //return ex.printStackTrace();
    }
  }

  public JSONArray getGames()
  {
    try
      {
        stmt =  conn.createStatement();
         ResultSet games = stmt.executeQuery("select * from Game");

           JSONArray userGames = convertToJSON(games);
           return userGames;
         } catch(SQLException ex) {
           ex.printStackTrace();
        }
  }
  
  public int makeUpdateQuery(String strUpdate)
  {
      try
      {
        stmt =  conn.createStatement();

         System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
         int countUpdated = stmt.executeUpdate(strUpdate);
         System.out.println(countUpdated + " records affected.");
         return countUpdated;
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
      DatabaseUtils db_utils = new DatabaseUtils();
      //db_utils.makeQuery();
      //System.out.println("second time");
      //db_utils.makeQuery();
      //db_utils.insertNewUser("UserID1", "U1_first", "U1_last");
      db_utils.updateUser("UserID1", "U1_first", "new_last");
   }
}