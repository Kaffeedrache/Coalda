// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


/**
@author kesslewd

Reads a matrix, a vector or several lines from a source.

*/
public abstract class Reader {


   /**
      Type of information to be loaded.
      Will determine if it is a matrix, a vector
      or lines that can be loaded.
   */
   enum InformationType
      {
         nodes, edges, umatrix, bmus, codebook, fvids, // from calculation
         labels
      }


   /**
      ID of the calculation that is imported at the moment
   */
   protected int calcID;


   /**
      Type of information that is imported at the moment
   */
   protected InformationType infoType;


   /**
      Method readMatrix.
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      Type of information must have been set previously.
   */
   public void readMatrix () {
      readMatrix(infoType);
   }


   /**
      Method readMatrix.
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      
      @param type Type of information to read.
   */
   public abstract void readMatrix (InformationType type);


   /**
      Method readVector.
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
   */
   public void readVector () {
      readVector(infoType);
   }


   /**
      Method readVector.
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
      
      @param type Type of information to read.
   */
   public abstract void readVector (InformationType type);


   /**
      Method readCompleteVector.
      Reads a vector of data and returns it in an array.
      Type of information must have been set previously.
      
      @return Array containing single values of the vector.
   */
   public  String[] readCompleteVector () {
      return readCompleteVector(infoType);
   }


   /**
      Method readCompleteVector.
      Reads a vector of data and returns it in an array.
      
      @param type Type of information to read.
      @return Array containing single values of the vector.
   */
   public abstract String[] readCompleteVector (InformationType type);


   /**
      Method hasNextRow.
      Checks if the matrix currently being read
      has a next row.
      
      @return True if there is another row, else false.
   */
   public abstract boolean hasNextRow (); 


   /**
      Method nextRow.
      Returns the next row of the matrix
      currently being read as a String.
      If there is no next row, null is returned.
      
      @return Next row of matrix as String.
   */
   public abstract String nextRow ();


   /**
      Method hasNextValue.
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public abstract boolean hasNextValue ();


   /**
      Method nextValue.
      Returns the next value of the vector or the current row of the matrix
      currently being read as a String.
      If there is no next value, null is returned.
      
      @return Next value of vector/matrix as String.
   */
   public abstract String nextValue ();


   /**
      Method numberOfValues.
      Returns the number of values in the current row of the matrix
      or in the vector currently being read.
      Will return 0 in case of error.
      
      @return int Number of elements.
   */
   public abstract int numberOfValues ();


   /**
      Method setCalcID.
      Sets the current calculation ID.
      
      @param calculationID A valid calculation ID.
   */
   public void setCalcID(int calculationID) {
      calcID = calculationID;
   }


   /**
      Method setInformationType.
      Sets the information type that currently should
      be read.
      
      @param type Information type.
   */
   public void setInformationType(InformationType type) {
      infoType = type;
   }


   /**
      Method readLines.
      Reads lines of data that fit the given condition.
      After reading use nextLine to get the first line.
      Type of information must have been set previously.
      
      @param fields Columns of the data to be read.
      @param condition Condition returned lines must fulfill.
   */
   public void readLines (String fields, String condition) {
      readLines(infoType, fields, condition);
   }


   /**
      Method readLines.
      Reads lines of data that fit the given condition.
      After reading use nextLine to get the first line.
      
      @param type Type of information to read.
      @param fields Columns of the data to be read.
      @param condition Condition returned lines must fulfill.
   */
   public abstract void readLines
         (InformationType type, String fields, String condition);



   /**
      Method hasNextLine.
      Checks if the data currently being read has a next line.
      
      @return True if there is another line, else false.
   */
   public abstract boolean hasNextLine();


   /**
      Method nextLine.
      Returns the next line of the data
      currently being read as a String.
      If there is no next line, null is returned.
      
      @return Next row of line as String.
   */
   public abstract String[] nextLine();


   /**
      Method finalize.
      Gives subclasses the opportunity to clean up
      when the object is no longer needed, e.g.
      close streams or database connections,
      delete temporary files, ...
   */
   public abstract void finalize();


}
