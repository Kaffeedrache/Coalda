// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;
import coalda.base.DBAccess;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;


   // TODO change for filereader/dbreader choice


/**
@author kesslewd

Abstraction between the database and the GUI,
gets the labels associated with featurevectors.

*/
public class LabelAccess {


   /**
      Loading of data from database.
   */
   private DBAccess db = new DBAccess();

   /**
      Hashmap for saving the label to a feature
      vector. Key is the feature vector ID, 
      value is the appropriate label.
   */
   private HashMap<Integer, Integer> allLabels = new HashMap<Integer, Integer>();


   /**
      Constructor.
      Load the labels of all feature vectors.
   */
   public LabelAccess () {
      this(null, true);
   }


   /**
      Constructor.
      If doImport is true, load the labels 
      of all feature vectors.
      If doImport is false, no labels are
      loaded (use this for exporting labels).
      
      @param doImport Load labels or not.
   */
   public LabelAccess (boolean doImport) {
      this(null, doImport);
   }


   /**
      Constructor.
      Load the labels of all feature vectors
      passed in the String. Feature vector
      IDs must be separated by spaces.
      
      @param featureVectors Load labels for these feature vectors.
   */
   public LabelAccess (String featureVectors) {
      this(featureVectors, true);
   }


   /**
      Constructor.
      If doImport is true, load the labels of all feature vectors
      passed in the String. Feature vector
      IDs must be separated by spaces.
      If doImport is false, no labels are
      loaded (use this for exporting labels).
      
      @param doImport Load labels or not.
      @param featureVectors Load labels for these feature vectors.
   */
   public LabelAccess (String featureVectors, boolean doImport) {

      // Load feature vectors if mode is input
      if (doImport) {

         String sqlstatement2 =
            "SELECT featurevector_id, f.link_id, label " +
            "FROM featurevectors f, links l " +
            "WHERE f.link_id=l.link_id "
            ;

         // If the parameter contains feature vectors, load
         // only these feature vectors.
         if (featureVectors != null && !featureVectors.equals("")) {
            String fvIDString = featureVectors.trim();
            fvIDString = fvIDString.replaceAll(" ", ",");
            sqlstatement2 = sqlstatement2 + 
               "AND featurevector_id = any(ARRAY[" + fvIDString + "]) ";
         }
         sqlstatement2 = sqlstatement2 + ";";
         ResultSet fvIDs = db.resultSetQuery(sqlstatement2);

         // Save all labels in Hashmap
         Integer key;
         Integer value;
         try {
            while (fvIDs.next()) {
               key = new Integer(fvIDs.getInt(1));
               value = new Integer(fvIDs.getInt(3));
               allLabels.put(key, value);
            }
            // We are finished, get rid of rubbish.
            fvIDs.close();
            db.finalize();
         } catch (SQLException e) {
            e.printStackTrace();
         }

      }
      // mode export - do nothing

   }


   /**
      Method getCoDisref.
      Gets number of feature vectors out of the ones
      passed as a parameter that have already been
      labeled coreferent and disreferent.
      FV IDs must be separated by spaces.

      @param featureVectors 
      @return Array[0] : number of coreferent fvs
         Array[1] : number of disreferent fvs
   */
   public int[] getCoDisref (String featureVectors) {

      String featureVectors2 = featureVectors.trim();
      String[] featureVectorArray = featureVectors2.split(" ");

      int coreferent = 0;
      int disreferent = 0;

      for (int i=0; i<featureVectorArray.length; i++) {

         Integer currentFV = Integer.valueOf(featureVectorArray[i]);

         if (allLabels.get(currentFV).intValue()==Constants.possibleLabelValues[0]) {
            coreferent++;
         }
         if (allLabels.get(currentFV).intValue()==Constants.possibleLabelValues[1]) {
            disreferent++;
         }

      }

      return new int[] {coreferent, disreferent};

   }


   /**
      Method labelFVs.
      Labels the feature vectors
      with the label and confidence value given.
      FV IDs must be separated by spaces.
      See Constants for possible values for
      label and confidence value.

      @param featureVectors The feature vectors to be labeled.
      @param label Which label to assign
      @param confidence Confidence value
   */
   public void labelFVs (String featureVectors, int label, int confidence) {

      String fvIDString = featureVectors.trim();
      fvIDString = fvIDString.replaceAll(" ", ",");

      String sqlstatement = "UPDATE links SET label = " + label
            + " WHERE link_id in " 
            + "(select link_id FROM featurevectors " 
            + "WHERE featurevector_id = any (ARRAY ["
            + fvIDString
            + "]));";
      db.execute(sqlstatement);
   }



}
