import java.sql.*;     // Use classes in java.sql package
 

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

  // sample query purely based on tutorial code
  // temporarily left here just for reference
  public void makeQuery()
  {
      try
      {
        stmt =  conn.createStatement();
         // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
         //   which returns an int indicating the number of rows affected.
         // Increase the price by 7% and qty by 1 for id=1001
         String strUpdate = "update books set price = price*0.7, qty = qty+1 where id = 1001";
         System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
         int countUpdated = stmt.executeUpdate(strUpdate);
         System.out.println(countUpdated + " records affected.");
 
         // Step 3 & 4: Issue a SELECT to check the UPDATE.
         String strSelect = "select * from books where id = 1001";
         System.out.println("The SQL query is: " + strSelect);  // Echo for debugging
         ResultSet rset = stmt.executeQuery(strSelect);
         while(rset.next()) {   // Move the cursor to the next row
            System.out.println(rset.getInt("id") + ", "
                    + rset.getString("author") + ", "
                    + rset.getString("title") + ", "
                    + rset.getDouble("price") + ", "
                    + rset.getInt("qty"));
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

        String strUpdate = "UPDATE users SET first = '" + first + 
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
        String sqlInsert = "insert into users " // need a space
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
      db_utils.makeQuery();
      System.out.println("second time");
      db_utils.makeQuery();
      //db_utils.insertNewUser("UserID1", "U1_first", "U1_last");
      db_utils.updateUser("UserID1", "U1_first", "new_last");
   }
}