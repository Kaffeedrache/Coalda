// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.sql.ResultSet;
import java.sql.SQLException;

import coalda.base.DBAccess;


/**
@author kesslewd

Reads a matrix, vector or lines from the calculation table of a database.

*/
public class ReadFromDB extends Reader {


   /**
      Connection to the DB		
   */
   private DBAccess db = null;

   /**
      Information that is imported at the moment
   */
   private String[] dataRows;
   private String[] dataColumns;
   private ResultSet resultSet;

   /**
      Cursor line.
   */
   private int row;

   /**
      Cursor column 
   */
   private int column;


   /**
      Constructor creates connection to DB
   */
   public ReadFromDB () {
      db = new DBAccess();
   }


   /**
      Method readMatrix.
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      
      @param type Type of information to read.
   */
   public void readMatrix (InformationType type) {

      dataColumns = null;
      dataRows = null;
      String dbColumn = "";

      switch (type) {
         case codebook:
            dbColumn = "codebook";
            break;
         case edges:
            dbColumn = "map_neighbours";
            break;
         case nodes:
            dbColumn = "map_coords";
            break;
         case umatrix:
            dbColumn = "u_matrix";
            break;
         default: // no valid matrix to read!
            return;
      }

      // Get values from DB
      String sqlstatement = 
         "SELECT " + dbColumn + " FROM calculations " +
         "WHERE calculation_id=" + calcID;
      String result = db.stringQuery(sqlstatement);

      // Split into rows {{row1},{row2},...}
      if (result != null && !result.equals("")) {
         dataRows = result.split("},");
      }

      // Set cursors before first row first entry
      row = -1;
      column = -1;

   }


   /**
      Method readVector.
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
      
      @param type Type of information to read.
   */
   public void readVector(InformationType type) {

      dataColumns = null;
      dataRows = null;
      String dbColumn = "";

      switch (type) {
         case bmus: 
            dbColumn = "bmus";
            break;
         case fvids:
            dbColumn = "featurevector_ids";
            break;
         default: // no valid matrix to read!
            return;
      }

      // Get values from DB
      String sqlstatement = 
         "SELECT " + dbColumn + " FROM calculations " +
         "WHERE calculation_id=" + calcID;
      String result = db.stringQuery(sqlstatement);

      // Delete { } from row
      // Split into rows {{row1},{row2},...}
      if (result != null && !result.equals("")) {
         result = result.replace("{", "");
         result = result.replace("}", "");
           dataColumns = result.split(",");
      }

      // Set cursors before first row first entry
      row = -1;
      column = -1;

   }


   /**
      Method readCompleteVector.
      Reads a vector of data and returns it in an array.
      Type of information must have been set previously.
      
      @param type Type of information to read.
      @return Array containing single values of the vector.
   */
   public String[] readCompleteVector (InformationType type) {
      readVector(type);
      return dataColumns;
   }


   /**
      Method hasNextRow.
      Checks if the matrix currently being read
      has a next row.
      
      @return True if there is another row, else false.
   */
   public boolean hasNextRow() {
      if (dataRows == null) {
         return false;
      } else {
         return (row+1)<dataRows.length;
      }
   }


   /**
      Method hasNextValue.
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public boolean hasNextValue() {
      if (dataColumns == null) {
         return false;
      } else {
         return (column+1)<dataColumns.length;
      }
   }


   /**
      Method nextRow.
      Returns the next row of the matrix
      currently being read as a String.
      If there is no next row, null is returned.
      
      @return Next row of matrix as String.
   */
   public String nextRow() {

      if (hasNextRow()) {

         // Go to next row
         row++;
         String line = dataRows[row];

         // Delete enclosing { } from row
         line = line.replace("{", "");
         line = line.replace("}", "");

         // Split up into values of the row
           dataColumns = line.split(",");

         // Set cursor before first value
         column = -1;

           return line;

      } else {
            // There is no next row
            dataColumns = null;
            return null;
      }
   }


   /**
      Method hasNextValue.
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public String nextValue() {
      if (hasNextValue()) {
         // Move to next element
         column++;
         // Return that
         return dataColumns[column];
      } else {
         // There is no next element
         return null;
      }
   }


   /**
      Method numberOfValues.
      Returns the number of values in the current row of the matrix
      or in the vector currently being read.
      Will return 0 in case of error.
      
      @return int Number of elements.
   */
   public int numberOfValues() {
      if (dataColumns == null) {
         // We have no current data - 0
         return 0;
      } else {
         return dataColumns.length;
      }
   }


   /**
      Method readLines.
      Reads lines of data that fit the given condition.
      After reading use nextLine to get the first line.
      
      @param type Type of information to read.
      @param fields Columns of the data to be read.
      @param condition Condition returned lines must fulfill.
   */
   public void readLines(InformationType type, String fields, String condition) {

      resultSet = null;
      String sqlStatement = "";
      String whereCondition = condition.replaceAll(" ", ","); 

      switch (type) {
         case labels:
            sqlStatement = "SELECT featurevector_id, features, label " +
               "FROM featurevectors f, links l " +
               "WHERE (f.link_id=l.link_id " +
               "AND featurevector_id = any (ARRAY ["
                  + whereCondition
                  + "]));";
            break;
         default: // no valid result set
            return;
      }

      resultSet = db.resultSetQuery(sqlStatement);

   }


   /**
      Method hasNextLine.
      Checks if the data currently being read has a next line.
      
      @return True if there is another line, else false.
   */
   public boolean hasNextLine() {
      try {
         return resultSet.isLast();
      } catch (SQLException e) {
         return false;
      }
   }


   /**
      Method nextLine.
      Returns the next line of the data
      currently being read as a String.
      If there is no next line, null is returned.
      
      @return Next row of line as String.
   */
   public String[] nextLine() {

      String[] result = new String[2];

      if (hasNextLine()) { // for labels
         try {
            resultSet.next();
            result[0] = resultSet.getString(1); // FeatureVectors
            result[0] = resultSet.getString(2); // Label
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }

      return result;

   }


   /**
      Method finalize.
      Close connection to db and resultset (if any).
   */
   public void finalize() {
      try {
         if (resultSet != null) {
            resultSet.close();
         }
         db.finalize();
      } catch (SQLException e) {
         e.printStackTrace();
      }
   }


}
