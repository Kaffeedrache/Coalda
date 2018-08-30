// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data.evaluation;


import coalda.base.Constants;

import coalda.data.CalculationImport;
import coalda.data.LabelImport;
import coalda.data.LabelImport.LabelPair;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;


/**

Evaluates the quality of a SOM.

@author kesslewd
*/
public class SOMEvaluator {

   /**
      Class for saving an evaluation result.
      Containing precision, recall and F1-measure.
   */
   public static class ResultEV {
      public double precision;
      public double recall;
      public double fmeasure;
   }
   
   /**
      Loads calculation data.
   */
   private CalculationImport calcImport;

   /**
      All nodes of the SOM.
   */
   private Table tableNodes;

   /**
      Loads labels of feature vectors.
   */
   private LabelImport allLabels;

   /**
      Debug mode.
   */
   private boolean debug = false;


   /**
      Constructor.
      Loads objects for data access.
   */
   public SOMEvaluator () {
      calcImport = new CalculationImport();
      allLabels = new LabelImport();
   }


   /**
      Method evaluate.
      Evaluates a SOM according to the following rules:
   
      To introduce a measure of quality for the SOM that is being visualized,
      we attempt to label the feature vectors with the SOM using the gold 
      standard labels. For every node of the SOM, the label of all associated 
      feature vectors is set to be the gold standard label of the majority of 
      its associated feature vectors. So if a node has five coreferent 
      feature vectors and seven disreferent feature vectors, all of the 
      feature vectors would be labeled as disreferent.
      Nomenclature:
      Label in gold standard coreferent 
         & labeled as coreferent : true positives (tp)
         & labeled as disreferent : false negatives (fn)
      Label in gold standard disreferent
         & labeled as coreferent : false positives (fp)
         & labeled as disreferent : true negatives (tn)
      After all feature vectors have been labeled, we compare the assigned
      labels to the gold standard labels. There are four different
      possibilites. From these numbers we can compute precision and recall.
      Precision P indicates how many of the feature vectors that have been
      labeled coreferent by the software are coreferent in the gold standard.
      Recall R indicates how many of the feature vectors that are coreferent 
      in the gold standard have been labeled as such by the software.
         P = tp / (tp+fp)
         R = tp / (tp+fn)
      To create one measure out of precision and recall, the F1-measure 
      F_1 is computed. The F1-measure is the weighted harmonic mean of 
      precision and recall.
         F_1 =  2PR / (P+R)

      @param calculationID The ID of the calculation to evaluate.
      @return Object containing precision, recall and f1-measure of the calculation.
    */
   public ResultEV evaluate (int calculationID) {

      System.out.println("Evaluating calculation id: " + calculationID);

      tableNodes = calcImport.readSOMNodesBMUs(calculationID);

      int truePositives = 0; // labeled coref and is coref
      int trueNegatives = 0; // labeled disref and is disref
      int falsePositives = 0; // labeled coref but is disref
      int falseNegatives = 0; // labeled disref but is coref

      // Go through all the nodes
      IntIterator nodeKeys = tableNodes.rows();

      while (nodeKeys.hasNext()) {
         
         int nodeKey = nodeKeys.nextInt();
         
         // Get feature vectors associated with the node
         String featureVectors = tableNodes.getString(nodeKey, Constants.nodeFVectors);
         
         if (featureVectors != null) { // Node has associated feature vectors

            // Get number of coreferent/disrefernt feature vectors
            LabelPair amount = allLabels.getCoDisref(featureVectors);
            int coreferent = amount.coreferent;
            int disreferent = amount.disreferent;

            // New label is the label of the maximum class
            int newlabel = 0;
            if (coreferent > disreferent) {
               newlabel = 1;
            // if half half -> label is coreferent
            } else if (coreferent == disreferent) {
               newlabel = 1;
            }

            // Add to appropriate classes
            if (newlabel == 1) {
               // all that are really coreferent are true positives
               truePositives += coreferent;
               // all that are really disreferent are false positives
               falsePositives += disreferent;
            } else {
               // all that are really disreferent are true negatives
               trueNegatives += disreferent;
               // all that are really coreferent are false negatives
               falseNegatives += coreferent;
            }

            if (debug) {
                   System.out.println("node " + nodeKey + " : "
                     + coreferent + " fvs coref, " + disreferent + " disref, "
                     + " label = " + newlabel);
                   System.out.println("so far we have tp=" + truePositives + "; tn="
                     + trueNegatives + "; fp=" 
                     + falsePositives + "; fn = " + falseNegatives);
            }

         } else {
            // for nodes without feature vectors do nothing
         }

      }

      // We have finished with all nodes.
      // Calculate precision, recall and f1.

      System.out.println("               | Really coref | Really disref |");
      System.out.println("Labeled coref  | " + truePositives + " | " + falsePositives + " | ");
      System.out.println("Labeled disref | " + falseNegatives + " | " + trueNegatives + " | ");

      float precision = (float)truePositives / (float)(truePositives + falsePositives);
      System.out.println("Precision = " + precision);
      float recall = (float)truePositives / (float)(truePositives + falseNegatives);
      System.out.println("Recall = " + recall);
      float f1 = 2 * precision * recall / (precision + recall);
      System.out.println("F1 = " + f1);

      ResultEV rev = new ResultEV();
      rev.precision = precision;
      rev.recall = recall;
      rev.fmeasure = f1;
      return rev;

   }



}