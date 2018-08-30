// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.Reader.SingleInfo;

import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;

import prefuse.data.Table;


/**

Loads feature vectors.

@author kesslewd
*/
public class FVImport {


   /**
      Change to debug mode (a lot of debug lines printed on System.out)
   */
   private boolean debug = false;

   /**
      This does the actual reading of the data, whether
      from the database or from files.
   */
   private Reader reader;


   /**
      Constructor.
   */
   public FVImport () {
      reader = Utils.makeReader();
   }


   /**
      Method getFVsOfMU.
      For every feature vector associated with
      the map unit get the features and the label.
      
      @param fvs The feature vectors of the MU.
      @return For every feature vector one entry in the
            Vector containing "FV <id> : <features> is <label>"
   */
   public Vector<String> getFVsOfMU (String fvs) {

      Vector<String> result = new Vector<String>();

      // If there are no feature vector return empty vector.
      if (fvs == null || fvs.trim().equals("")) {
         return result;
      }
      
      // Sort fv ids
      String fvIDs = Utils.sortFVs(fvs);
      
      // Get another reader for the labels
      Reader labelReader = Utils.makeReader();

      // Get information from DB/Files
      reader.readLines(SingleInfo.features,  fvIDs);
      labelReader.readLines(SingleInfo.labels,  fvIDs);
     
      // Write every line to the vector.
      while (reader.hasNextLine()) {
         String[] features = reader.nextLine();
         String[] label = labelReader.nextLine();
         
         // Abort if any of them is null
         if (features == null || label == null) {
            System.out.println("Error while reading features, features are null.");
            break;
         }
         
         // FVs are read sorted, so the readers should be parallel.
         // If not, thow error and abort.
         if (!features[0].trim().equals(label[0].trim())) {
            System.out.println("Error while reading features, "
            		+ "IDs do not correspond: " + features[0] + "!=" + label[0]);
            break;
         }
         
         // If everything is ok write the result to the array
         if (debug) {
            System.out.println("FV " + features[0] + ": " + features[1] + " is " + label[1]);
         }
         result.add("FV " + features[0] + ": " + features[1] + " is " + label[1]);

      }

      // We don't need this reader anymore, cleanup
      labelReader.finalize();
      
      return result;

   }


   /**
      Gets the text belonging to all feature vectors
      of a map unit in a vector.
      For every feature vector:
      The first row contains the feature vector ID
      the second the text of markable1, 
      the third the text to markable2.
      Then all the sentences.
      When a row comes that contains only numbers,
      it is the FVID of the next feature vector.
      
      @param fvid The Feature vectors we want to get the text for.
      @return Vector containing the text to the feature vectors.
            Empty vector in case of error or when there is no text.
   */
   public Vector<String> getTextOfMU (String fvs) {

      Vector<String> result = new Vector<String>();
      
      // If there are no feature vector return null
      if (fvs == null || fvs.trim().equals("")) {
         return result;
      }
      
      // Sort FVs by IDs
      String fvIDs = Utils.sortFVs(fvs);
/*      String[] fvIDs = fvs.split(" ");
      fvIDs = Utils.sortFVs(fvIDs);
      Utils.fvString(fvIDs)*/ 
      
      reader.readLines(SingleInfo.text, fvIDs);

      // 1 - fvid
      // 2 - sentenceid
      // 3 - markable1
      // 4 - markable2
      // 5 - sentence
      
      int fvid = 0; // 0 may not be a valid fvid
      String line;
      
      // Write every line to the vector.
      while (reader.hasNextLine()) {
         
         String[] query = reader.nextLine();
         
         if (debug) {
            System.out.println("FV : " + query[0] 
               + "  field 1: " + query[1]
               + "  field 2: " + query[2]
               + "  field 3: " + query[3]
               );
         }

         // We are at the next feature vector,
         // write also id and text of markables
         if (fvid != Integer.parseInt(query[0])) {
            
            fvid = Integer.parseInt(query[0]); // FV ID
            if (debug) {
               System.out.println(" Feature Vector ID: " + fvid);
            }
            result.add(fvid + "");
          
            line = query[1]; // 1st markable
            line = line.replace("{", "");
            line = line.replace("}", "");
            line = line.replace(",", " ");
            if (debug) {
               System.out.println(" 1st markable " + line);
            }
            result.add(line);

            line = query[2]; // 2nd markable
            line = line.replace("{", "");
            line = line.replace("}", "");
            line = line.replace(",", " ");
            if (debug) {
               System.out.println(" 2nd markable " + line);
            }
            result.add(line);
            
         }
         
         // Add text
         if (debug) {
            System.out.println("this line of result is: " + query[3]);
         }
         result.add(query[3]);
         
      }
   
      return result;
   }


   /**
      Reads the feature definitions from a file
      and returns them as a vector.
      Uses the file specified as feature description.
      
      @return Vector containing in each element
         the definition of a feature.
   */
   public Vector<String> getFeatureDefinitions() {

      Vector<String> definitions = new Vector<String>();

      try {
         BufferedReader br = Utils.openFile(Constants.featureDefinitionsFile);

         String line;

         while ((line=br.readLine()) != null ) {
            line = line.trim();
            // Before the # is the code, afterwards the 
            // comment/name of the feature
            int raute = line.indexOf('#');
            if (raute!=-1) {
               definitions.add(line.substring(raute+1));
            } else {
               // If there is no comment, add feature code
               definitions.add(line);
            }
         }

         return definitions;

      } catch (IOException e) {
         System.out.println("Error while reading feature definitions.");
         e.printStackTrace();
         return definitions;
      }
   }
   
   
   private float[] gimmeFeatures (String featureString) {
      String featuresString = featureString.replace("{", "");
      featuresString = featuresString.replace("}", "");
      featuresString = featuresString.trim();
      String[] featuresStringArray = featuresString.split(" ");
      float[] features = new float[featuresStringArray.length];
      for (int i=0; i<featuresStringArray.length; i++) {
         features[i] = Float.parseFloat(featuresStringArray[i]);
      }
      return features;
   }
   

   // TODO put this in prefuse Table, because we read data 3 times!!
   
   /**
      Returns the values for normalizing the
      featurevectors according to the 'var' normalization.
      
      This normalization normalizes the variance of a variable
      to unity and its mean to zero.
      Formula for variable x: x' = (x - x_mean)/x_sdev
      where x_mean is the mean and x_sdev the standard deviation
      of the variable.
      
      @return For every feature two values: 1. mean and 2. standard deviation
   */
   public float[][] normalizeFVs_var (String fvIDs) {
      

      reader.readLines(SingleInfo.features, fvIDs);
      
      String[] featurevector = reader.nextLine();
      boolean first = true;
      float[] features = gimmeFeatures (featurevector[1]); 
      

      float[][] normalizationParams = new float[features.length][2];
      
      float[] sum = new float[features.length];
      
      float[] squareDist = new float[features.length];
      
      int elements = 0;


      // Calculate the mean
      while (reader.hasNextLine()) {

         elements++;
         
         if (first) {
            // We already did read the first line, to calculate
            // the number of vectors we have to add.
            first = false;
         } else {
            featurevector = reader.nextLine();
            features = gimmeFeatures(featurevector[1]);
         }
         
         // Sum up all values
         for (int i=0; i<features.length; i++) {
            sum[i] += features[i];
         }
         
      }

      // Mean = sum / number of elements
      for (int i=0; i<features.length; i++) {
         normalizationParams[i][0] = sum[i]/(float)elements;
      }
      

      // Reset reader
      reader.readLines(SingleInfo.features, fvIDs);

      // Calculate the standard deviation
      while (reader.hasNextLine()) {

         featurevector = reader.nextLine();
         features = gimmeFeatures(featurevector[1]);
         
         // Sum up all distances to the mean, squared
         for (int i=0; i<features.length; i++) {
            float dist = features[i] - normalizationParams[i][0];
            squareDist[i] = squareDist[i] + dist * dist;
         }
         
      }
      
      // Standard deviation = sqrt(sum_square_dist / number of elements)
      for (int i=0; i<features.length; i++) {
         normalizationParams[i][1] = (float) Math.sqrt(squareDist[i]/(float)elements);
      }
      
      return normalizationParams;
   
   }
   
   

   /**
   */
   public Table getFeaturetable (String fvIDs) {
      
      Table fvTable = new Table();

      // Add the columns for the table of nodes
      // 'kind' is always 'node'
      // number of feature vectors is 0 (for the moment)
      fvTable.addColumn(Constants.kind, Constants.itemKind.class, Constants.itemKind.fv);
      fvTable.addColumn(Constants.featureID, int.class);

      reader.readLines(SingleInfo.features, fvIDs);
      
      String[] featurevector = reader.nextLine();
      float[] features = gimmeFeatures (featurevector[1]);
      boolean first = true;
      for (int i=1; i<=features.length; i++){
         fvTable.addColumn(Constants.features + i, double.class);
      }

      // Add dummy node with ID 0 
      // because prefuse starts at ID 0, but we at ID 1
      // and prefuse node IDs cannot jump a value
      fvTable.addRow(); // for 0

      int element = 1;
      int id = 0;

      while (reader.hasNextLine()) {

         if (first) {
            // We already did read the first line, to calculate
            // the number of vectors we have to add.
            first = false;
         } else {
            featurevector = reader.nextLine();
            features = gimmeFeatures(featurevector[1]);
         }
         id = Integer.parseInt(featurevector[0]);
         
         if (debug) {
            System.out.println("lineno " + element + " (id " + id + ")" + " : " + featurevector[1]);
         }
         
         // Add new node to the table
         fvTable.addRow();
         fvTable.set(element, Constants.featureID, new Integer(id));

         // Add all features
         for (int i=1; i<=features.length; i++) {
            fvTable.set(element, Constants.features + i, new Double(features[i-1]));
         }

         if (debug) {
            System.out.print("lineno " + element 
                  + " (id " + fvTable.getString(element, Constants.featureID) + ")" + ": ");
            for ( int i1=1; i1<=features.length; i1++ ) {
               System.out.print(" - Feature" + i1 + "=" + fvTable.getString(element, Constants.features + i1));
            }
            System.out.println();
         }

         element++;
         
      }
      
      // Remove dummy node with ID 0
      fvTable.removeRow(0);
      
      return fvTable;
   
   }


}
