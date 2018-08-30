// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data.export;


import coalda.base.Constants;
import coalda.data.CalculationImport;
import coalda.data.FVImport;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import prefuse.data.Table;
import prefuse.util.collections.IntIterator;


/**

Exports a SOM to a text file.

@author kesslewd
*/
public class MapExporter {
   
   /**
      Loads data of a calculation.
   */
   private CalculationImport calcImport;
   
   /**
      Nodes of the SOM.
   */
   private Table tableNodes;

   /**
      Loads data of feature vectors.
   */
   private FVImport importData;

   /**
      Character used for linebreak in Textfile.
   */
   private String newline = "\n";


   /**
      Constructor.
   */
   public MapExporter () {
      calcImport = new CalculationImport();
      importData = new FVImport();
   }

   /**
      Reads all information about the SOM nodes.
      @param calculationID The ID of the calculation to be loaded.
   */
   public void readAll(int calculationID) {
      tableNodes = calcImport.readSOMNodes(calculationID);
   }
   

   /**
      Exports the information about the SOM to a textfile.
      First a calculation has to be loaded with 'readAll'.
      Format is:
      MU number: <nodeID>
      <other node information>
      Assigned Feature Vectors: <FV IDs>
      <Feature Vectors>
      <Text for first associated FV>
      <Text for second associated FV> ...
      @param location Path of the file the SOM is exported to.
   */
   public void exportToTextFile (String location) {

      File file = new File(location);
      try {
         file.createNewFile();
         BufferedWriter out = new BufferedWriter(new FileWriter(file));

         // Go through all the nodes
         IntIterator nodeKeys = tableNodes.rows();

         while (nodeKeys.hasNext()) {

            // Get id of node
            int nodeKey = nodeKeys.nextInt();
            out.write("MU number: " + nodeKey + newline);

            // Write map unit information
            if ( tableNodes.canGetString(Constants.nodeXValue) ) {
               out.write("X Value: " + tableNodes.getString(nodeKey, Constants.nodeXValue) + newline);
            }
            if ( tableNodes.canGetString(Constants.nodeYValue) ) {
               out.write("Y Value: " + tableNodes.getString(nodeKey, Constants.nodeYValue) + newline);
            }
            if ( tableNodes.canGetString(Constants.nodeFVNumber) ) {
               out.write("FV number: " + tableNodes.getString(nodeKey, Constants.nodeFVNumber) + newline);
            }
            if ( tableNodes.canGetString(Constants.nodeUmatValue) ) {
               out.write("U-Matrix Value: " + tableNodes.getString(nodeKey, Constants.nodeUmatValue) + newline);
            }

            // Print labeled fvs
            if ( tableNodes.canGetString(Constants.nodeAllLabeled) ) {
               out.write("All labeled (gold standard): " + tableNodes.getString(nodeKey, Constants.nodeAllLabeledGold) + newline);
            }
            for (int k=0; k<Constants.possibleLabels.length; k++) {
               String name = Constants.possibleLabels[k];
               if ( tableNodes.canGetString(Constants.nodeLabelGold + name) ) {
                  out.write("Labeled as " + name + " (gold): " + tableNodes.getString(nodeKey, Constants.nodeLabelGold + name) + newline);
               }
            }
            for (int k=0; k<Constants.possibleLabels.length; k++) {
               String name = Constants.possibleLabels[k];
               if ( tableNodes.canGetString(Constants.nodeProportionGold + name) ) {
                  out.write("% " + name + "/total (gold): " + tableNodes.getString(nodeKey, Constants.nodeProportionGold + name) + newline);
               }
            }
            if ( tableNodes.canGetString(Constants.nodeAllLabeledAssigned) ) {
               out.write("All labeled (assigned): " + tableNodes.getString(nodeKey, Constants.nodeAllLabeledAssigned) + newline);
            }
            
            for (int k=0; k<Constants.possibleLabels.length; k++) {
               String name = Constants.possibleLabels[k];
               if ( tableNodes.canGetString(Constants.nodeLabelAssigned + name) ) {
                  out.write("Labeled as " + name + " (assigned): " + tableNodes.getString(nodeKey, Constants.nodeLabelAssigned + name) + newline);
               }
            }
            for (int k=0; k<Constants.possibleLabels.length; k++) {
               String name = Constants.possibleLabels[k];
               if ( tableNodes.canGetString(Constants.nodeProportionAssigned + name) ) {
                  out.write("% " + name + "/total (assigned): " + tableNodes.getString(nodeKey, Constants.nodeProportionAssigned + name) + newline);
               }
            }

            // Weights
            int i=1;
            while ( tableNodes.canGetString(Constants.nodeWeight + i) ) {
               out.write(Constants.nodeWeight + " " + i + ": " 
                     + tableNodes.getString(nodeKey, Constants.nodeWeight+i) + newline);
               i++;
            }

            // Write feature vector ID associated with this node
            String fvs = null;
            if ( tableNodes.canGetString(Constants.nodeFVectors) ) {
               fvs = tableNodes.getString(nodeKey, Constants.nodeFVectors);
               out.write("Assigned Feature Vectors: " + fvs + newline);
            }

            // Write feature vectors associated with this node (if any)
            if (fvs != null && !fvs.equals("")) {
               String[] cols = fvs.split(" ");
               Vector<String> result = importData.getFVsOfMU(fvs);
               for ( int j=0; j<result.size(); j++ ) {
                  out.write("FV " + cols[j] + ": " + result.get(j) + newline);
               } 

               // Write text associated with feature vectors
               Vector<String> result2 = importData.getTextOfMU(fvs);
               int id = 0;
               for ( int k=0; k<result2.size(); k++ ) {
                  if (result2.get(k) != null) {
                     if (isNumber(result2.get(k))) {
                        out.newLine();
                        out.write("FV ID : ");
                        id = 0;
                     }
                     if (id == 1) {
                        out.write("Markable 1: ");
                     } else if (id == 2) {
                        out.write("Markable 2: ");
                     }
                     out.write(result2.get(k) + newline);
                     id++;
                  }
               }
            }

            // Next node
            out.newLine();
            out.write(" --------- next map unit ------------ ");
            out.newLine();

         }

         System.out.println("Finished.");
         out.close(); 

      } catch (IOException e) {
         System.out.println("There has been an error while exporting the data to file " + location);
         e.printStackTrace();
      }
   }


   /**
      Checks if a String consists ONLY of numbers.
      Empty String will return true.
      @param text The String to check.
      @return True, if String consists only of numbers, else false.
   */
   private boolean isNumber (String text) {
      for (int i=0; i<text.length(); i++) {
         if (!Character.isDigit(text.charAt(i))) {
            return false;
         }
      }
      return true;
   }


}
