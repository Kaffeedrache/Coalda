// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;

import coalda.base.Constants;


/**
@author kesslewd

Reads a matrix, vector or lines from a file.
*/
public class ReadFromFile extends Reader {


   /**
      Information that is imported at the moment
   */
   private String dataRow;
   private String nextDataRow;
   private String[] dataColumns;

   /**
      File open successful
   */
   private boolean fileOpen;

   /**
      True if it is a vector, false if it is a matrix
   */
   private boolean isVector;

   /**
      Cursor line.
   */
   private int currentRow;
   private int readRows;

   /**
      Cursor column
   */
   private int column;

   /**
      Reader for file
   */
   private BufferedReader br;


   /**
      Method readMatrix.
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      
      @param type Type of information to read.
   */
   public void readMatrix (InformationType type) {

      // Reset everything
      dataColumns = null;
      dataRow = null;
      nextDataRow = null;
      fileOpen = false;
      br = null;

      // we have a matrix
      isVector = false; 

      // Set cursors before first row first entry
      // Read rows are 0
      currentRow = 0;
      readRows = 0;
      column = -1;

      String filename = "";
      switch (type) {
         case codebook:
            filename = Constants.codebookFile;
            break;
         case edges:
            filename = Constants.neighboursFile;
            break;
         case nodes:
            filename = Constants.coordsFile;
            break;
         case umatrix:
            filename = Constants.umatFile;
            break;
         default: // no valid matrix to read!
            return;
      }

      try {
         br = Constants.openFile(filename);
         fileOpen = true;
      } catch (IOException e) {
         e.printStackTrace();
      }

   }


   /**
      Method readVector.
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
      
      @param type Type of information to read.
   */
   public void readVector (InformationType type) {

      // Reset everything
      dataColumns = null;
      dataRow = null;
      nextDataRow = null;
      fileOpen = false;
      br = null;

      // we have a vector
      isVector = true; 

      // Set cursors before first row first entry
      // Read rows are 0
      currentRow = 0;
      readRows = 0;
      column = 0;

      String filename = "";

      switch (type) {
         case bmus: 
            filename = Constants.bmuFile;
            break;
         case fvids:
            filename = Constants.fvidsFile; // TODO 
            break;
         default: // no valid vector to read!
            return;
      }

      try {
         br = Constants.openFile(filename);
         fileOpen = true;
      } catch (IOException e) {
         e.printStackTrace();
      }

   }


   /**
      Method readCompleteVector.
      Reads a vector of data and returns it in an array.
      WARNING! Using this will make you unable to
      use 'nextValue' afterwards!!!
      
      @return Array containing single values of the vector.
   */
   public String[] readCompleteVector (InformationType type) {

      readVector(type);

      Vector<String> vector = new Vector<String>();

      // Just read till there is no more.
      String line;
      while ((line = readLine ()) != null) {
         vector.add(line);
      }

      vector.copyInto(dataColumns);

      // we just read the whole file - ups!
      fileOpen = false;

      return dataColumns;

   }


   /**
      Method readLine.
      Reads a line from a file.
      If unsuccessful, null is returned.
      
      @return The line read.
   */
   private String readLine () {
      if (!fileOpen) {
         return null;
      }
      try {
         readRows++;
         return br.readLine();
      } catch (IOException e) {
         // ignore exceptions.
      }
      return null;
   }


   /**
      Method hasNextRow.
      Checks if the matrix currently being read
      has a next row.
      
      @return True if there is another row, else false.
   */
   public boolean hasNextRow() {

      if (!fileOpen) {
         return false;
      }

      // we have a line in buffer?
      if (currentRow < readRows) {
         return true;
      }

      // else read a new line
      nextDataRow = readLine();
      if (nextDataRow != null) {
         return true;
      }

      // else
      return false;
   }


   /**
      Method hasNextValue.
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public boolean hasNextValue() {

      if (!fileOpen) {
         return false;
      }

      if (isVector) {
         return hasNextRow();
      }

      // if not a vector = a matrix
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

      if (!fileOpen) {
         return null;
      }

      if (currentRow < readRows) { // we did already read a row too much
         dataRow = nextDataRow;
         nextDataRow = null;
      } else {
         dataRow = readLine();
      }

      // we are one row further
      currentRow++;

      // Split up into values of the row
      dataColumns = null;
      if (dataRow != null) {
         dataColumns = dataRow.split(",");
      }

      // Set cursor before first value
      column = -1;

      return dataRow;

   }


   /**
      Method hasNextValue.
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public String nextValue() {

      if (!fileOpen) {
         return null;
      }

      if (isVector) {

         if (currentRow < readRows) { // we did already read a row too much
            dataRow = nextDataRow;
            nextDataRow = null;
         } else {
            // get next Row
            dataRow = readLine();
         }

         // we are one row further
           currentRow++;

         return dataRow;
      }

      // Matrix
      if (hasNextValue()) {
         column++;
         return dataColumns[column];
      } else {
         return null;
      }

   }


   /**
      Method numberOfValues.
      Returns the number of values in the current row of the matrix
      or in the vector currently being read.
      Will return 0 in case of error.
      !! Warning, will return always 1 for a vector!
      
      @return int Number of elements.
   */
   public int numberOfValues() {

      if (!fileOpen) {
         return 0;
      }

      if (isVector && hasNextRow()) {
         return 1; // TODO
      }
      if (isVector && !hasNextRow()) {
         return 0;
      }

      // else is matrix
      if (dataColumns == null) {
         return 0;
      } else {
         return dataColumns.length;
      }
   }


   /**
      Method readLines.
      Reads lines of data that fit the given condition.
      After reading use nextLine to get the first line.
      !! WARNING, not implemented !! -- TODO
      
      @param type Type of information to read.
      @param fields Columns of the data to be read.
      @param condition Condition returned lines must fulfill.
   */
   public void readLines(InformationType type, String fields, String condition) {

      // Reset everything
      dataColumns = null;
      dataRow = null;
      nextDataRow = null;
      fileOpen = false;
      br = null;

      // we have a vector
      isVector = true; 

      // Set cursors before first row first entry
      // Read rows are 0
      currentRow = 0;
      readRows = 0;
      column = 0;

      String filename = "";

      switch (type) {
         case labels: 
            filename = "bla";
            // "SELECT featurevector_id, features, label " +
            //"FROM featurevectors f, links l " +
            //"WHERE (f.link_id=l.link_id " +
            //"AND featurevector_id = any (ARRAY ["
            //	+ whereCondition
            //	+ "]));";
            break;
         default: // no valid line to read!
            return;
      }

      try {
         br = Constants.openFile(filename);
         fileOpen = true;
      } catch (IOException e) {
         e.printStackTrace();
      }

   }


   /**
      Method hasNextLine.
      Checks if the data currently being read has a next line.
      !! WARNING, not implemented !! -- TODO
      
      @return True if there is another line, else false.
   */
   public boolean hasNextLine() {
      return true;
   }


   /**
      Method nextLine.
      Returns the next line of the data
      currently being read as a String.
      If there is no next line, null is returned.
      !! WARNING, not implemented !! -- TODO
      
      @return Next row of line as String.
   */
   public String[] nextLine() {
      return null;
   }


   /**
      Method finalize.
      Close any open file.
   */
   public void finalize() {
      if (fileOpen)
         try {
            br.close();
         } catch (IOException e) {
            // ignore exceptions
         }
   }


}
