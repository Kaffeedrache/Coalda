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


}
