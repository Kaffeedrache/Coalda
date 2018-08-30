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
      Debug mode. A lot of things on stdout.
   */
   private boolean debug = false;
   
   /**
      Hashmap for saving the gold label to a feature
      vector. Key is the feature vector ID, 
      value is the appropriate label.
   */
   private HashMap<Integer, Integer> goldLabels = new HashMap<Integer, Integer>();

   /**
      Hashmap for saving the assigned label to a feature
      vector. Key is the feature vector ID, 
      value is the appropriate label.
   */
   private HashMap<Integer, Integer> assignedLabels = new HashMap<Integer, Integer>();

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
      Integer valueGold;
      Integer valueAssigned;
      
      while (reader.hasNextLine()) {
         String[] label = reader.nextLine();
         key = new Integer(label[0]); // id
         valueGold = new Integer(label[1]); // label
         valueAssigned = new Integer(label[2]); // label assigned
         goldLabels.put(key, valueGold);
         assignedLabels.put(key, valueAssigned);
         
         if (debug) {
            System.out.println("Put fv " + key 
                  + " label(gold)=" + valueGold + " label(assigned)=" + valueAssigned);
         }
      }

   }


   /**
      Gets number of feature vectors out of the ones
      passed as a parameter that have already been
      labeled coreferent and disreferent.
      FV IDs must be separated by spaces.
      Uses gold labels.

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

         if (goldLabels.get(currentFV).intValue()==Constants.possibleLabelValues[0]) {
            coreferent++;
         }
         if (goldLabels.get(currentFV).intValue()==Constants.possibleLabelValues[1]) {
            disreferent++;
         }

      }

      LabelPair lp = new LabelPair();
      lp.coreferent = coreferent;
      lp.disreferent = disreferent;

      return lp;

   }


   /**
      Gets number of feature vectors that have a label
      as gold standard label for every label.
      Includes only the feature vectors passed as parameter.
      Uses gold standard labels.
      FV IDs must be separated by spaces.

      @param featureVectors 
      @return Array where each index i contains the number of 
         feature vectors that have label i
   */
   public int[] getGoldLabels (String featureVectors) {

      String featureVectors2 = featureVectors.trim();
      String[] featureVectorArray = featureVectors2.split(" ");

      // Initialize label array with 0
      int[] labels = new int[Constants.possibleLabelValues.length];

      for (int i=0; i<featureVectorArray.length; i++) {

         // Get current vector and label
         Integer currentFV = Integer.valueOf(featureVectorArray[i]);
         if (!goldLabels.containsKey(currentFV)) {
            System.out.println("ERROR! FV ID " + currentFV + " does not exist in the labels table!");
            continue; // avoid crash in next line
         }
         int currentFVLabel = goldLabels.get(currentFV).intValue();

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
   

   /**
      Gets number of feature vectors that have a label
      as assigned label for every label.
      Includes only the feature vectors passed as parameter.
      Uses assigned labels.
      FV IDs must be separated by spaces.

      @param featureVectors 
      @return Array where each index i contains the number of 
         feature vectors that have label i
   */
   public int[] getAssignedLabels (String featureVectors) {

      String featureVectors2 = featureVectors.trim();
      String[] featureVectorArray = featureVectors2.split(" ");

      // Initialize label array with 0
      int[] labels = new int[Constants.possibleLabelValues.length];

      for (int i=0; i<featureVectorArray.length; i++) {

         // Get current vector and label
         Integer currentFV = Integer.valueOf(featureVectorArray[i]);
         if (!assignedLabels.containsKey(currentFV)) {
            System.out.println("ERROR! FV ID " + currentFV + " does not exist in the labels table!");
            continue; // avoid crash in next line
         }
         int currentFVLabel = assignedLabels.get(currentFV).intValue();

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
