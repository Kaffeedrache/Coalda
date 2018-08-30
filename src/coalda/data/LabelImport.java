// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.Reader.SingleInfo;

import java.util.HashMap;


/**

Abstraction between the database and the GUI,
gets the labels associated with featurevectors.

@author kesslewd
*/
public class LabelImport {

   /**
      Class containing number of coreferent and
      disreferent feature vectors associated with a node.
   */
   public static class LabelPair {
      public int coreferent;
      public int disreferent;
   }
   
   /**
      Loading of data.
   */
   private Reader reader;

   /**
      Hashmap for saving the label to a feature
      vector. Key is the feature vector ID, 
      value is the appropriate label.
   */
   private HashMap<Integer, Integer> allLabels = new HashMap<Integer, Integer>();


   /**
      Load the labels of all feature vectors.
   */
   public LabelImport () {
      this(null);
   }


   /**
      Load the labels of all feature vectors passed as parameter.
      Feature vector IDs must be separated by spaces.
      
      @param featureVectors Load labels for these feature vectors.
   */
   public LabelImport (String featureVectors) {
      
      String fvs = null;
      
      // Do we have fvs?
      if (featureVectors != null) {
         fvs = featureVectors.trim();
      
         String[] fvIDs = fvs.split(" ");
    
         // Sort by IDs
         fvIDs = Utils.sortFVs(fvIDs);
         
         fvs = Utils.fvString(fvIDs);
      }
      
      reader = Utils.makeReader();

      // Get information from DB/Files
      reader.readLines(SingleInfo.labels, fvs);

      // Save all labels in Hashmap
      Integer key;
      Integer value;
      
      while (reader.hasNextLine()) {
         String[] label = reader.nextLine();
         key = new Integer(label[0]); // id
         value = new Integer(label[1]); // label
         allLabels.put(key, value);
      }

   }


   /**
      Gets number of feature vectors out of the ones
      passed as a parameter that have already been
      labeled coreferent and disreferent.
      FV IDs must be separated by spaces.

      @param featureVectors 
      @return labelpair.coreferent : number of coreferent fvs
         lapelpair.disreferent : number of disreferent fvs

      @deprecated
   */
   public LabelPair getCoDisref (String featureVectors) {

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

      LabelPair lp = new LabelPair();
      lp.coreferent = coreferent;
      lp.disreferent = disreferent;

      return lp;

   }


   /**
      Gets number of feature vectors out of the ones
      passed as a parameter for every label.
      FV IDs must be separated by spaces.

      @param featureVectors 
      @return Array where each index i contains the number of 
         feature vectors that have label i
   */
   public int[] getLabels (String featureVectors) {

      String featureVectors2 = featureVectors.trim();
      String[] featureVectorArray = featureVectors2.split(" ");

      // Initialize label array with 0
      int[] labels = new int[Constants.possibleLabelValues.length];

      for (int i=0; i<featureVectorArray.length; i++) {

         // Get current vector and label
         Integer currentFV = Integer.valueOf(featureVectorArray[i]);
         int currentFVLabel = allLabels.get(currentFV).intValue();

         // Find place of that label in array
         int position = -1;
         for (int j=0; j<Constants.possibleLabelValues.length; j++) {
            if (currentFVLabel == Constants.possibleLabelValues[j]) {
               position = j;
               break;
            }
         }
        
         // Increase number of vectors that have that label
         // If the label is not in the list of possible labels,
         // it is discarded.
         if (position != -1) {
            labels[position]++;
         }

      }

      return labels;

   }

}
