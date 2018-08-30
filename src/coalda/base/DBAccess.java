// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.base;


import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import de.unistuttgart.ais.sukre.database.utils.DBConnectionProvider;


/*
 TODO cleanup
 TODO debug Ausgaben
 TODO create Table - Skript nur am Anfagn einlesen
 TODO finalize close connection
 TODO Singleton?
 TODO Security checks
 TODO !result.next - !! moves cursor!
 TODO close ResultSet
 */


/**


Provides access to a Postgres database.

@author kesslewd
*/
public class DBAccess {

   /**
      Debug mode (a lot of System.out.println-s).
   */
   private boolean debug = false;

   /**
      Connection to database.
   */
   private Connection con;

   /**
      A statement.
   */
   private Statement stmt;


   /*
      Constructors/Destructors
      ----------------------------------------------------------------------------------
   */

   /** 
      Constructor.
      
      Opens a database and creates a connection to it and
      a statement.
   */
   public DBAccess () {
      try {
         connect();
      } catch (ClassNotFoundException e) {
         System.out.println("Error ! DB-Driver not found.");
         e.printStackTrace();
      } catch (SQLException e) {
         System.out.println("Error ! Error while connecting with the DB.");
         e.printStackTrace();
      }
   }


   /** 
      Destructor.
      
      Closes the connection to the database.
   */
   public void finalize () {
      try {
         if (debug) {
            System.out.println("DB Connection closed.");
         }
         stmt.close();
         con.close();
      }
      catch ( SQLException e ){
        System.out.println("Couldn't close connection to DB, sorry.");
        System.out.println(e.getMessage());
      }
   }


   /*
      Methods for managing the connection
      ----------------------------------------------------------------------------------
   */

   /** 
      Method connect.
      Opens a database and creates a connection to it and
      a statement.
      
      @throws
      ClassNotFoundException: if DB-driver is not found
      SQLException: if some problem connecting occurs
   */
   public void connect () throws ClassNotFoundException, SQLException {

      if (debug) {
         System.out.println("Initializing DB connection.");
      }

      DBConnectionProvider dbCon = new DBConnectionProvider(Constants.configFile);

      // Open connection
      con = dbCon.getConnection();

      // Create a statment
      stmt = con.createStatement();

   }


   /** 
      Method connected.
      Checks if a connection to the DB is existing.
         
      @return true if a connection exists, else false
   */
   public boolean connected () {
      return (con != null) && (stmt != null);
   }


   /** 
      Method closeConnection.
      Closes the connection to the database.
   */
   public void closeConnection () {
      try {
         stmt.close();
         con.close();
      }
      catch ( SQLException e ){ // ignore
      }
   }


   /*
      Methods for managing the connection
      ----------------------------------------------------------------------------------
   */

   /** 
      Method tableExists
      
   */
   public boolean tableExists(String tablename) {
      String sqlstatement = "SELECT 1 FROM pg_class where relname='" + tablename + "';";
      if (debug) {
         System.out.println("Check table exists " + tablename);
         System.out.println(sqlstatement);
      }
      ResultSet result;
      try {
         result = stmt.executeQuery(sqlstatement);
         return ((result != null) && result.next());
      } catch (SQLException e) {
         return false;
      }
   }


   /** 
      Method tableEmpty.
      Checks if a table is empty.
      Use tableExists to check if the table exists before
      checking if it is empty if you are not sure.
      
      @param tablename The name of the table to check.
      @return True if the table exists and is empty, false if it is
         not empty or it doesn't exist or any error ocurred.
   */
   public boolean tableEmpty(String tablename) {
      String sqlstatement = "SELECT * FROM " + tablename + ";";
      if (debug) {
         System.out.println(sqlstatement);
      }
      try {
         ResultSet result = stmt.executeQuery(sqlstatement);
         if ((result == null) || !result.next()) {
            if (debug) {
               System.out.println("Table empty");
            }
            return true;
         } else {
            if (debug) {
               System.out.println("Table not empty");
            }
            return false;
         }
      } catch (SQLException e) {
         System.out.println("Error while processing: " + sqlstatement);
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
      return false;
   }


   /** 
      Method rowExists.
      Checks if the statement returns a row.
      
      @param sqlstatement Statement to be executed on the db.
      @return True if the statement has a row as a result,
            else false if there is no row as a result or any error ocurred.
   */
   public boolean rowExists(String sqlstatement) {
      if (debug) {
         System.out.println("Check row exists :" + sqlstatement);	
      }
      try {
         ResultSet result = stmt.executeQuery(sqlstatement);
         if ((result == null) || !result.next()) {
            if (debug) {
               System.out.println("Result empty");
            }
            return false;
         } else {
            if (debug) {
               System.out.println("Result not empty");
            }
            return true;
         }
      } catch (SQLException e) {
         System.out.println("Error while processing: " + sqlstatement);
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
      return false;
   }


   /** 
      Method stringQuery.
      Method for queries where there is only one column
      in one row returned (or of interest).
      
      @param sqlstatement The statement to execute.
      @return The first column of the first row of the result
         is returned, if there are no rows that match
         the query 'null' is returned.
   */
   public String stringQuery(String sqlstatement) {
      try {
         ResultSet result = stmt.executeQuery(sqlstatement);
         if (result == null) {
            if (debug) {
               System.out.println("Sorry, result empty.");
            }
            return null;
         } else {
            while( result.next() ){
               return result.getString(1);
            }
         }
      } catch (SQLException e) {
         System.out.println("Error while processing: " + sqlstatement);
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
      return null;
   }


   /** 
      Method resultSetQuery.
      Method for queries where the whole result set
      is of interest.
      
      @param sqlstatement The statement to execute.
      @return The result, if there are no rows that match
         the query 'null' is returned.
   */
   public ResultSet resultSetQuery(String sqlstatement) {
      check(sqlstatement);
      try {
         ResultSet result = stmt.executeQuery(sqlstatement);
         if (result == null) {
            if (debug) {
               System.out.println("Sorry, result empty.");
            }
            return null;
         } else {
            if (debug) {
               System.out.println("Found entries in table. "); 
            }
            return result;
         }
      } catch (SQLException e) {
         System.out.println("Error while processing: " + sqlstatement);
         System.out.println(e.getMessage());
         e.printStackTrace();
      }
      return null;
   }


   /** 
      Method execute.
      Method for data manipulation inside the db where
      no result is expected.
      
      @return false on error, true on succesful completion
   */
   public boolean execute(String sqlstatement) {
      if (debug) {
         System.out.println("execute " + sqlstatement);
      }
      try {
         return stmt.execute(sqlstatement);
      } catch (SQLException e) {
         System.out.println("Error while processing: " + sqlstatement);
         System.out.println(e.getMessage());
         if (debug) {
            e.printStackTrace();
         }
      }
      return false;
   }


   /** 
      Method prepareStatement.
      Prepares a preparedStatement.
      You have to call this before calling
      executePreparedStatement.
      
      @param sqlstatement The prepared statement as a String.
      @return The preparedStatement as an Object.
   */
   public PreparedStatement prepareStatement(String sqlstatement) {
      if (debug) {
         System.out.println("Prepare statement " + sqlstatement);
      }
      try {
         return con.prepareStatement(sqlstatement);
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return null;
   }


   /** 
      Method executePreparedStatement.
      Executes a preparedStatement with the parameters.
      
      @param statement PreparedStatement to be executed.
      @param params The parameters to be inserted at the
         i-th places in the prepared statement. There must be
         exactly the same number of parameters as the prepared
         statements needs (the number of ?).
      @return True if executed successful. False in event of error.
      
   */
   public boolean executePreparedStatement 
            (PreparedStatement statement, int... params) {
      if (debug) {
         System.out.println("execute " + statement);
      }
      try {
         for (int i=0; i<params.length; i++) {
            statement.setInt(i+1, params[i]); 
         }
         statement.executeUpdate();
         return true;
      } catch (SQLException e) {
         return false;
      }
   }


   /** 
      Method tableReset.
      If the table doesn't exist it is created.
      If the table does exist all entries are deleted.
      Delete cascade.
      
      @param tablename Table to be reset.
      @return True if executed successful. False in event of error.
   */
   public boolean tableReset (String tablename) {
      execute("DROP TABLE IF EXISTS " + tablename + " CASCADE;");
      return createTable(tablename);
   }


   /** 
      Method tableReset.
      If the table doesn't exist it is created.
      If the table does exist, it cannot be created
      and 'false' is returned.
      
      @param tablename Table to be created.
      @return True if executed successful. False in event of error.
   */
   public boolean createTable (String tablename) {
      try {

         // Open file with create skript
         // and search for the table to be created.
         BufferedReader br = Utils.openFile(ConstantsDataload.dbCreateScript);
         String line;
         String createStatement = "";
         boolean inStatement = false;
         while ( (line=br.readLine()) != null )  {
            line = line.trim();
            if (!line.startsWith("--") && !line.equals("")) {
               if (debug) {
                  System.out.println("processing line " + line);
               }
               if (line.matches("create table .*")) {
                  String[] words = line.split(" ");
                  if (debug) {
                     System.out.println("create table found in " + line);
                     System.out.println("table is " + words[2]);
                  }
                  if (tablename.equals(words[2])) {
                     createStatement = line;
                     inStatement = true;
                  }
               } else if (inStatement && line.indexOf(';')!=-1) { // there is a ; in the line
                  createStatement = createStatement + " " + line;
                  inStatement = false;
                  break; // we have it
               } else if (inStatement) {
                  createStatement = createStatement + " " + line;
               }
            }
         }

         // We have found the statement for this table - execute
         if (debug) {
            System.out.println(createStatement);
         }
         execute(createStatement);
      } catch (IOException e) {
         System.out.println("Error while creating table " + tablename);
         return false;
      }
      return true;
   }

   /** 
      Method check
      Check for BAD things ;)
      
      @param sqlstatement The statement to check.
      @return True if the statement is ok, else false.
   */
   private boolean check (String sqlstatement) {
      if (debug) {
         System.out.println("Check if " + sqlstatement + " is ok...");
      }
      return true;
   }


}