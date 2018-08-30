// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.dataset.SSelectMode;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;
import coalda.base.DBAccess;
import de.unistuttgart.ais.sukre.database.model.CalculationModel;
import de.unistuttgart.ais.sukre.database.model.FeatureVectorModel;
import de.unistuttgart.ais.sukre.database.model.LinkModel;
import de.unistuttgart.ais.sukre.database.model.MarkableModel;
import de.unistuttgart.ais.sukre.database.model.SentenceModel;
import de.unistuttgart.ais.sukre.database.utils.DBConnectionProvider;

/**

Reads a matrix, vector or lines from the object-relational mapping of
a table of a database.

@author kesslewd

*/
public class ReadFromORM extends Reader {


   /**
      Connection to the DB    
   */
   private SSessionJdbc ses = null;
   
   /**
      Information that is imported at the moment, vector
   */
   private String[] dataVector;
   
   /**
      Information that is imported at the moment, matrix
   */
   private String[][] dataMatrix;

   /**
      Information that is imported at the moment, matrix
   */
   private Iterator<FeatureVectorModel> fviterator = null;
   
   /**
      Cursor line for matrix.
   */
   private int row;
   
   /**
      Cursor column for matrix and vector. 
   */
   private int column;
   
   /**
      Type of information that is imported at the moment for lines.
   */
   private SingleInfo informationType;


   /**
      Constructor creates connection to DB
   */
   public ReadFromORM() {
      
      // Set Log Level = 0 to avoid annoying outputs.
      // Errors are still shown.
      SLog.getSessionlessLogger().setLevel(0);
      
      // Open a connection
      Connection con = null;
      try {
         con = DBConnectionProvider.getInstanceFromDefault().getConnection();
      } catch (SQLException e) {
         e.printStackTrace();
         return;
      }
      
      // Open a session
      ses = SSessionJdbc.getThreadLocalSession();
      if (ses == null) {
         ses = SSessionJdbc.open(con, "Data Import");
         ses.begin();
      }
      
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

      dataVector = null;
      dataMatrix = null;
      CalculationModel cm = ses.find(CalculationModel.META, new Integer(calcID));

      switch (type) {
         case codebook:
            dataMatrix = cm.getCodebook();
            break;
         case edges:
            dataMatrix = cm.getNeighbours();
            break;
         case nodes:
            dataMatrix = cm.getCoords();
            break;
         case umatrix:
            dataMatrix = cm.getUmatrix();
            break;
      }

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

      dataVector = null;
      dataMatrix = null;
      CalculationModel cm = ses.find(CalculationModel.META, new Integer(calcID));

      switch (type) {
         case bmus: 
            dataVector = cm.getBMUs();
            break;
         case fvids:
            dataVector = cm.getFeaturevectors();
            break;
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
      return dataVector;
   }


   /**
      Checks if the matrix currently being read
      has a next row.
      
      @return True if there is another row, else false.
   */
   public boolean hasNextRow() {
      if (dataMatrix == null) {
         return false;
      } else {
         return (row+1)<dataMatrix.length;
      }
   }


   /**
      Checks if the vector or the current row of the matrix
      currently being read has a next value.
      
      @return True if there is another value, else false.
   */
   public boolean hasNextValue() {
      
      if (dataVector == null) {
         return false;
      } else {
         return (column+1)<dataVector.length;
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

         // Set cursor before first value
         column = -1;
         
         dataVector = dataMatrix[row];

         // create String to return
         String result = "";
         for (int i=0; i<dataVector.length; i++) {
            result = result + " " + dataVector[i];
         }
         return result;

      } else {
            // There is no next row
            dataVector = null;
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
         return dataVector[column];
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
      if (dataVector == null) {
         // We have no current data - 0
         return 0;
      } else {
         return dataVector.length;
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
   public String readOneLine(SingleInfo type) {
      String sqlStatement = "";
   
      switch (type) {
         case nextCalcID:
            sqlStatement = "select nextval('calculation_id_seq')";
            break;
         case normalization:
            sqlStatement = "select " + CalculationModel.NORMALIZATION.getColumnName() + " from calculations where calculation_id=" + calcID;
            break;
         case usedFeatures:
            sqlStatement = "select " + CalculationModel.FEATURE_SUBSET.getColumnName() + " from calculations where calculation_id=" + calcID;
            break;
         default: // no valid result set
            return null;
      }
   
      DBAccess db = new DBAccess();
      return db.stringQuery(sqlStatement);
   }


   
   
   /**
      Reads lines of data for the given feature vectors.
      After reading use nextLine to get the first line.
      
      @param type Type of information to read.
      @param featureVectors Which IDs to select.
   */
   public void readLines (SingleInfo type, String featureVectors) {
      
      // Reset fields.
      fviterator = null;
      informationType = type;
   
      // Do we have fvs? If yes, split into Array.
      Integer[] fvIDs = null;
      // Trim String and create Array of Integers
      if (featureVectors != null && !featureVectors.trim().equals("")) {
         String fvIDString = featureVectors.trim();
         String[] fvs = fvIDString.split(" ");
         fvIDs = new Integer[fvs.length];
         for (int i = 0; i < fvs.length; i++) {
            fvIDs[i] = new Integer(Integer.parseInt(fvs[i]));
         }
      }
      
      // Create query
      SQuery<FeatureVectorModel> fvquery = 
            new SQuery<FeatureVectorModel>(FeatureVectorModel.META);
      if (fvIDs != null) {
         // Get all links for the featureVectors with the given IDs
         fvquery.select(SSelectMode.SALL)
            .in(FeatureVectorModel.ID, (Object[]) fvIDs)
            .ascending(FeatureVectorModel.ID);
      } else { // get it for all fv_ids
         fvquery.select(SSelectMode.SALL)
            .ascending(FeatureVectorModel.ID);
      }
      
      // Get result and save an iterator for access
      SQueryResult<FeatureVectorModel> featurevectors = ses.query(fvquery);
      fviterator = featurevectors.iterator();

   }


   /**
      Checks if the data currently being read has a next line.
      
      @return True if there is another line, else false.
   */
   public boolean hasNextLine() {
      // Type for one line - return true
      if (informationType == SingleInfo.nextCalcID) 
         return true;
      // Type for more than one line - check fviterator
      if (fviterator != null) 
         return fviterator.hasNext();
      // If fviterator is null, nothing was read
      return false;
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

      // Result to be returned
      String[] line = null;
      
      if (hasNextLine()) {
         switch (informationType) {
         
            case features: // return  id + features
               line = new String[2]; 
               if (fviterator != null) {
                  //System.out.println("fvs");
                  FeatureVectorModel current = fviterator.next();
                  //System.out.println(current.allFields());
                  line[0] = current.getID() + "";
                  Double[] features = current.getFeatures();
                  String featureString = "";
                  for (int i=0; i<features.length;i++) {
                     featureString = featureString + " " + features[i];
                  }
                  featureString = featureString.trim();
                  line[1] = "{" + featureString + "}";
               }
               break;
            
            case labels: // return  id + label
               line = new String[3]; 
               if (fviterator != null) {
                  //System.out.println("labels"); // TEST
                  FeatureVectorModel current = fviterator.next();
                  //System.out.println(current.allFields()); // TEST
                  int fvId = current.getID();
                  //System.out.println("FV: " + fvId); // TEST
                  int linkID = current.getLinkID();
                  //System.out.println("FV: " + fvId + " - LinkID: " + linkID); // TEST
                  LinkModel link = ses.find(LinkModel.META, new Integer(linkID));
                  //System.out.println("FV: " + fvId + " - LinkID: " + link.getID());  // TEST
                  //LinkModel link = current.getLink(); // sometimes doesn't work...
                  //System.out.println("FV: " + fvId + "Label: " + link.getLabel());  // TEST
                  line[0] = fvId + "";
                  line[1] = link.getLabelGold() + "";
                  line[2] = link.getLabelAssigned() + ""; // TODO assigned/gold
                  //System.out.println("return " + line[0] + "|" + line[1]); // TEST
               }
               break;
            
            case text: // return  id + m1 + m2 + text (complete)
               if (fviterator != null) {
                  line = new String[4]; 
                  //System.out.println("text");  // TEST
                  FeatureVectorModel current = fviterator.next();
                  //System.out.println(current.allFields());  // TEST
                  int fvId = current.getID();
                  
                  // Get Markables and their content
                  LinkModel link = current.getLink();
                  Integer m1ID = link.getFirstMarkableID();
                  Integer m2ID = link.getSecondMarkableID();
                  //System.out.println("Link " + link.getID() + " - m1 " + m1ID + " - m2 " + m2ID);  // TEST
                  MarkableModel m1 = ses.find(MarkableModel.META, m1ID);
                  MarkableModel m2 = ses.find(MarkableModel.META, m2ID);
   //               System.out.println("M1 " + m1.getID()           // TEST
   //                     + " - first " + m1.getFirstWordID() 
   //                     + " - last " + m1.getLastWordID()
   //                     + " - content " + m1.getContent()
   //                     );
                  String markable1 = m1.getContent();
   //               System.out.println("M2 " + m2.getID()        // TEST
   //                     + " - first " + m2.getFirstWordID() 
   //                     + " - last " + m2.getLastWordID()
   //                     + " - content " + m2.getContent()
   //                     );
                  String markable2 = m2.getContent();
                  
                  // Get text
                  int sentence1 = m1.getSentenceID().intValue();
                  int sentence2 = m2.getSentenceID().intValue();
                  // We assume both markables are in the same document
                  int document = m1.getDocumentID().intValue();
                  // If order of markables is wrong, change values
                  if (sentence2 < sentence1) {
                     int helper = sentence1;
                     sentence1 = sentence2;
                     sentence2 = helper;
                  }
                  SQuery<SentenceModel> textQ = new SQuery<SentenceModel>(SentenceModel.META);
                  // Get all sentences between sentence IDs
                  textQ.select(SSelectMode.SNORMAL)
                        .eq(SentenceModel.DOCUMENT_ID, new Integer(document))
                        .ge(SentenceModel.SENTENCEID_ID, new Integer(sentence1))
                        .le(SentenceModel.SENTENCEID_ID, new Integer(sentence2))
                        .ascending(SentenceModel.SENTENCEID_ID);
                  SQueryResult<SentenceModel> sentences = ses.query(textQ);
                  Iterator<SentenceModel> textiterator = sentences.iterator();
                  
                  // Write to a String
                  String text = "";
                  while (textiterator.hasNext()) {
                     SentenceModel sm = textiterator.next();
                     text = text + sm.getSentence() + " \n";
                  }
                  
                  // Add things to result
                  line[0] = fvId + "";
                  line[1] = markable1;
                  line[2] = markable2;
                  line[3] = text;
                  
               }
               break;
               
            default: // this is a singleInfo of only one line
               line = new String[1];
               line[0] =  readOneLine(informationType);
               break;
         }
      }
      return line;
      
   }


   // --------------- Other methods ---------------
   
   /**
      Does nothing.
   */
   public void finalize() {
   }


}