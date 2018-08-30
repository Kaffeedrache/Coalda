// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


/**

Reads a matrix, a vector or several lines from a source.

@author kesslewd

*/
public abstract class Reader {


   /**
      Type of information to be loaded.
      Will determine if it is a matrix, a vector
      or lines that can be loaded.
   */
   enum MatrixInfo
      {
         nodes, edges, umatrix, codebook, // matrices from calculations table 
      }


   /**
      Type of information to be loaded.
      Will determine if it is a matrix, a vector
      or lines that can be loaded.
   */
   enum VectorInfo
      {
         bmus, fvids, // vectors from calculations table
      }

   /**
      Type of information to be loaded.
      Will determine if it is a matrix, a vector
      or lines that can be loaded.
   */
   enum SingleInfo
      {
         features, // lines from featurevectors table
         labels, // lines from links table (join fvs)
         text, // lines from a lot of tables
         nextCalcID // line from sequence calculation_id
      }

   /**
      ID of the calculation that is imported at the moment
   */
   protected int calcID;


   /**
      Reads a matrix of data.
      Afterwards use nextRow to position
      the cursor on the first row.
      Then use nextValue to get the first value.
      
      @param type Type of information to read.
   */
   public abstract void readMatrix (MatrixInfo type);


   /**
      Reads a vector of data.
      After reading use nextValue to get the first value.
      Type of information must have been set previously.
      
      @param type Type of information to read.
   */
   public abstract void readVector (VectorInfo type);


   /**
      Reads a vector of data and returns it in an array.
      
      @param type Type of information to read.
      @return Array containing single values of the vector.
   */
   public abstract String[] readCompleteVector (VectorInfo type);


   /**
      Checks if the matrix currently being read has a next row.
      
      @return True if there is another row, else false.
   */
   public abstract boolean hasNextRow (); 


   /**
      Returns the next row of the matrix
      currently being read as a String.
      If there is no next row, null is returned.
      
      @return Next row of matrix as String.
   */
   public abstract String nextRow ();


   /**
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public abstract boolean hasNextValue ();


   /**
      Returns the next value of the vector or the current row of the matrix
      currently being read as a String.
      If there is no next value, null is returned.
      
      @return Next value of vector/matrix as String.
   */
   public abstract String nextValue ();


   /**
      Returns the number of values in the current row of the matrix
      or in the vector currently being read.
      Will return 0 in case of error.
      
      @return int Number of elements.
   */
   public abstract int numberOfValues ();


   /**
      Sets the current calculation ID.
      
      @param calculationID A valid calculation ID.
   */
   public void setCalcID(int calculationID) {
      calcID = calculationID;
   }


   /**
      Reads and returns one line of data.
      What data depends on the type of SingleInfo.
      nextCalcID : the next calculation ID that is not used.
      other: null
      
      @param type Type of information to read.
      @return String with the information read.
   */
   public abstract String readOneLine(SingleInfo type); 


   /**
      Reads lines of data for the given feature vectors.
      After reading use nextLine to get the first line.
      
      @param type Type of information to read.
      @param featureVectors Which IDs to select.
   */
   public abstract void readLines (SingleInfo type, String featureVectors);


   /**
      Checks if the data currently being read has a next line.
      
      @return True if there is another line, else false.
   */
   public abstract boolean hasNextLine();


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
         row[1] : label of this feature vector
      text: row with 4 columns
         row[0] : feature vector ID
         row[1] : markable 1 of the link
         row[2] : markable 2 of the link
         row[3] : sentences between the two markables
      other: the information returned by 'readOneLine'
         as row[0]
      
      @return Next row as String array.
   */
   public abstract String[] nextLine();


   /**
      Gives subclasses the opportunity to clean up
      when the object is no longer needed, e.g.
      close streams or database connections,
      delete temporary files, ...
   */
   public abstract void finalize();


}
