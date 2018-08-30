// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.sql.ResultSet;
import java.sql.SQLException;

import coalda.base.DBAccess;


/**

Reads a matrix, vector or lines from the calculation table of a database.

@author kesslewd

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
      Did we call next?
   */
   private boolean nextCalled;

   private String selectFields;

   private SingleInfo informationType;


   /**
      Constructor creates connection to DB
   */
   public ReadFromDB () {
      db = new DBAccess();
   }



   // --------------- Methods for Matrix and Vector ---------------
   
   /**
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      
      @param type Type of information to read.
   */
   public void readMatrix (MatrixInfo type) {

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
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
      
      @param type Type of information to read.
   */
   public void readVector(VectorInfo type) {

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
      Reads a vector of data and returns it in an array.
      Type of information must have been set previously.
      
      @param type Type of information to read.
      @return Array containing single values of the vector.
   */
   public String[] readCompleteVector (VectorInfo type) {
      readVector(type);
      return dataColumns;
   }


   /**
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



   // --------------- Methods for Lines ---------------
   
   
   /**
      Reads and returns one line of data.
      What data depends on the type of SingleInfo.
      nextCalcID : the next calculation ID that is not used.
      other: null
      
      @param type Type of information to read.
      @return String with the information read.
   */
   public String readOneLine (SingleInfo type) {
   
      resultSet = null;
      String sqlStatement = "";
   
      switch (type) {
         case nextCalcID:
            sqlStatement = "select nextval('calculation_id_seq')";
            break;         
         case normalization:
            sqlStatement = "select normalization from calculations where calculation_id=" + calcID;
            break;
         default: // no valid result set
            return null;
      }
   
      return db.stringQuery(sqlStatement);
   
   }
   
   /**
      Reads lines of data for the given feature vectors.
      After reading use nextLine to get the first line.
      
      @param type Type of information to read.
      @param featureVectors Which IDs to select.
   */
   public void readLines (SingleInfo type, String featureVectors)  {

      // Reset
      resultSet = null;
      nextCalled = false;
      selectFields = "";
      informationType = type;
      
      // Do we have FVs?
      String whereCondition = null;
      if (featureVectors != null && !featureVectors.trim().equals("")) {
         whereCondition = featureVectors.trim();
         whereCondition = whereCondition.replaceAll(" ", ","); 
      }

      String sqlStatement = "";
      
      switch (type) {
      
         case features: // return id + features
            selectFields = "featurevector_id, features";
            sqlStatement = "SELECT  " + selectFields + 
            " FROM featurevectors";
            if (whereCondition != null) {
               sqlStatement = sqlStatement +
               " WHERE featurevector_id in ("
               + whereCondition + ")";
            }
            sqlStatement = sqlStatement +
               " ORDER BY featurevector_id"
                  + ";";
            break;
            
         case labels: // return id + label
            selectFields = "f.featurevector_id, l.label";
            sqlStatement = "SELECT  " + selectFields + 
            " FROM featurevectors f, links l" +
            " WHERE f.link_id=l.link_id ";
            if (whereCondition != null) {
               sqlStatement = sqlStatement +
               " AND featurevector_id in ("
               + whereCondition + ")";
            }
            sqlStatement = sqlStatement +
               " ORDER BY featurevector_id"
                  + ";";
            break;
            
         case text: // return id + m1 + m2 + text
            
            selectFields = "f.featurevector_id, markable_one, markable_two, text";
            
            // We don't do this for all feature vectors...
            // Too much work...
            if (whereCondition == null) break;
            
            // Get statement for every single fv and join
            String[] cols = whereCondition.split(",");
            for ( int i=0; i<cols.length; i++ ) {
               int fvid = Integer.parseInt(cols[i]);
               String singlesqlstatement = getTextQuery(fvid);
               sqlStatement = sqlStatement + singlesqlstatement;
               if (i != cols.length-1) {
                  sqlStatement = sqlStatement + " UNION ";
               }
            }
            sqlStatement = sqlStatement +
               " ORDER BY featurevector_id"
                  + ";";            break;
            
         default: // this is a singleInfo of only one line
            break;
      }
      //System.out.println(sqlStatement);
   
      resultSet = db.resultSetQuery(sqlStatement);
   }
   

   /**
      Checks if the data currently being read has a next line.
      
      @return True if there is another line, else false.
   */
   public boolean hasNextLine() {

      try {
         if (nextCalled) {
            return true;
         }
         if (resultSet.next()) {
            nextCalled = true;
            return true;
         } else return false;
         
      } catch (SQLException e) {
         e.printStackTrace();
         return false;
      }
   }

   /**
      Returns the next line of the data
      currently being read as a String.
      If there is no next line, null is returned.
      Depending on the SingleInfo Type read,
      the following information is returned.
      features: row with 2 columns
         row[0] : feature vector ID
         row[1] : features of this vector
      labels: row with 2 columns
         row[0] : feature vector ID
         row[1] : gold label of this feature vector
         row[2] : assigned label of this feature vector
      text: row with 4 columns
         row[0] : feature vector ID
         row[1] : markable 1 of the link
         row[2] : markable 2 of the link
         row[3] : sentences between the two markables
      other: the information returned by 'readOneLine'
         as row[0]
      
      @return Next row of line as String.
   */
   public String[] nextLine() {
      
      // Types that are one line
      if (informationType == SingleInfo.nextCalcID) {
         String[] result = new String[1];
         result[0] = readOneLine(informationType);
         return result;
      }
      
      // For all other types create array
      String[] fields = selectFields.split(", ");
      String[] line = new String[fields.length];

      if (hasNextLine()) {
         try {
            // Get next element in result set
            if (!nextCalled) {
               resultSet.next();
            } else {
               nextCalled = false;
            }

            // Get fields we want and put them in result
            for (int i=0; i<fields.length;i++) {
               line[i] = resultSet.getString(i+1);
            }
         } catch (SQLException e) {
            e.printStackTrace();
         }
      }
      
      // Give it back
      return line;
   }



   /**
      Constructs the query that gets the text
      belonging to the feature vector.
      
      @param fvid The Feature vector we want to get the text for.
      @return (HUGE) SQL statement that will return the information from the DB.
   */
   private String getTextQuery (int fvid) {
      String sqlstatement = 
            "SELECT f.featurevector_id, " 
            +       "array(select word "
            +       "from links l, markables m, words w, featurevectors f "
            +       "where l.markable1=m.markable_id "
            +               "and w.word_id>=m.firstword and w.word_id<=m.lastword "
            +               "and l.link_id=f.link_id and f.featurevector_id=" + fvid 
            +               ") as markable_one, "
            +       "array(select word "
            +       "from links l, markables m, words w, featurevectors f "
            +       "where l.markable2=m.markable_id "
            +               "and w.word_id>=m.firstword and w.word_id<=m.lastword "
            +               "and l.link_id=f.link_id and f.featurevector_id=" + fvid 
            +               ") as markable_two, "
            +       " text "
            + "FROM featurevectors f, links l, markables m1, markables m2, "
            +       "words w1, words w2, sentences s "
            + "WHERE l.markable1=m1.markable_id and l.markable2=m2.markable_id "
            +       "and m1.firstword=w1.word_id and m2.firstword=w2.word_id "
            +       "and w2.doc_id=s.doc_id and "
            +       "((w1.sentence_id<=s.sentence_id and w2.sentence_id>=s.sentence_id) "
            +       "or (w2.sentence_id<=s.sentence_id and w1.sentence_id>=s.sentence_id)) "
            +       "and l.link_id=f.link_id and f.featurevector_id=" + fvid;
      return sqlstatement;
   }
   
   
   
   
   // --------------- Other methods ---------------
   
   /**
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
