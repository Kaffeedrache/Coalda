// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;
import coalda.base.DBAccess;
import coalda.data.Reader.InformationType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import java.io.BufferedReader;
import java.io.IOException;


// TODO DB/Files - make independent
// Separate features and labels
// Array for only 10*#fvs - change to vector


/**
@author kesslewd

Loads feature vectors.

*/
public class FVImport {


   /**
      Change to debug mode (a lot of debug lines printed on System.out)
   */
   private boolean debug = false;

   /**
      Access to DB.
   */
   private DBAccess db = null; 

   /**
      This does the actual reading of the data, whether
      from the database or from files.
   */
   private Reader reader;


   /**
      Constructor.
   */
   public FVImport () {
      if (Constants.db) {
         reader = new ReadFromDB();
      } else {
         reader = new ReadFromFile();
      }
      db = new DBAccess();
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

      // Get information from DB/Files
      String fields = "featurevector_id, features, label";
      reader.readLines(InformationType.labels, fields, fvs);

      // Write every line to the vector.
      while (reader.hasNextLine()) {
         String[] line = reader.nextLine();
         result.add("FV " + line[0] + ": " + line[1] + " is " + line[2]);
      }

      return result;

   }


   /**
      Method getTextQuery.
      Constructs the query that gets the text
      belonging to the feature vector.
      
      @param fvid The Feature vector we want to get the text for.
      @return SQL statement that will return the information from the DB.
   */
   private String getTextQuery (int fvid) {
      String sqlstatement = 
            "SELECT f.featurevector_id, s.sentence_id, " 
            +       "array(select word "
            +       "from links l, markables m, words w, featurevectors f "
            +       "where l.markable1=m.markable_id "
            +               "and w.word_id>=m.firstword and w.word_id<=m.lastword "
            +               "and l.link_id=f.link_id and f.featurevector_id=" + fvid 
            +               ") as markable_one, "
            +       "array(select word "
            +       "from links l, markables m, words w, featurevectors f "
            +       "where l.markable2=m.markable_id "
            +               "and w.word_id>=m.firstword and w.word_id<=m.lastword "
            +               "and l.link_id=f.link_id and f.featurevector_id=" + fvid 
            +               ") as markable_two, "
            +       " text "
            + "FROM featurevectors f, links l, markables m1, markables m2, "
            +       "words w1, words w2, sentences s "
            + "WHERE l.markable1=m1.markable_id and l.markable2=m2.markable_id "
            +       "and m1.firstword=w1.word_id and m2.firstword=w2.word_id "
            +       "and w2.doc_id=s.doc_id and "
            +       "((w1.sentence_id<=s.sentence_id and w2.sentence_id>=s.sentence_id) "
            +       "or (w2.sentence_id<=s.sentence_id and w1.sentence_id>=s.sentence_id)) "
            +       "and l.link_id=f.link_id and f.featurevector_id=" + fvid;
      return sqlstatement;
   }


   /**
      Method getTextOfFV.
      Gets the text belonging to the feature vector in an array,
      every line in the array corresponds to a sentence.
      The first row contains the text of markable1, 
      the second the text to markable2.
      
      @param fvid The Feature vector we want to get the text for.
      @return Array containing the text to the feature vector.
            Maximum length of array is 100.
            May be null in case of error or when there is no text.
   */
   public String[] getTextOfFV (int fvid) {

      // If there are no feature vector return null
      if (fvid == 0) {
         return null;
      }

      // Get statement.
      String sqlstatement = getTextQuery(fvid) + ";";

      try {
         // Execute statement
         ResultSet umat;
         umat = db.resultSetQuery(sqlstatement);

         String line = null;
         if (umat == null) {
            if (debug) {
               System.out.println("Sorry, no matching entries in featurevector table found.");
            }
            return null; 

         // We have an entry
         } else {
            if (debug) {
               System.out.println("Found entries in table for FV " + fvid + " : ");
            }

            // Write text into array
            int count = 0;
            String[] fvstring = new String[100];
            // 1 - fvid
            // 2 - sentenceid
            // 3 - markable1
            // 4 - markable2
            // 5 - sentence
            while( umat.next() ){

               // in the first run, write also text of markables
               if (count==0) {
                  line = umat.getString(3); // markable1
                  line = line.replace("{", "");
                  line = line.replace("}", "");
                  line = line.replace(",", " ");
                  if (debug) {
                     System.out.println(" 1st markable: " + line );
                  }
                  fvstring[count]=line;
                  count++;
                  
                  line = umat.getString(4); // markable2
                  line = line.replace("{", "");
                  line = line.replace("}", "");
                  line = line.replace(",", " ");
                  if (debug) {
                     System.out.println(" 2nd markable: " + line );
                  }
                  fvstring[count]=line;
                  count++;
               }

               // Get next sentence
               line = umat.getString(5); // 5 = sentence
               if (debug) {
                  System.out.println(count + "th line of result is: " + line);
               }
               fvstring[count]=line;
               count++;
            }

            return fvstring;
         }
      } catch (SQLException e) {
         System.out.println("Error in : " + sqlstatement);
         e.printStackTrace();
      }
      return null;
   }


   /**
      Method getTextOfMU.
      Gets the text belonging to all feature vectors
      of a map unit in an array.
      For every feature vector:
      The first row contains the feature vector ID
      the second the text of markable1, 
      the third the text to markable2.
      Then all the sentences.
      When a row comes that contains only numbers,
      it is the FVID of the next feature vector.
      
      @param fvid The Feature vectors we want to get the text for.
      @return Array containing the text to the feature vectors.
            Maximum length of array is 10*#fvs.
            May be null in case of error or when there is no text.
   */
   public String[] getTextOfMU (String fvs) {

      // If there are no feature vector return null
      if (fvs == null || fvs.trim().equals("")) {
         return null;
      }

      // Get statement.
      String sqlstatement = "";
      String[] cols = fvs.split(" ");
      for ( int i=0; i<cols.length; i++ ) {
         int fvid = Integer.parseInt(cols[i]);
         String singlesqlstatement = getTextQuery(fvid);
         if (debug) {
            System.out.println(fvid + " " + singlesqlstatement);
         }
         sqlstatement = sqlstatement + singlesqlstatement;
         if (debug) {
            System.out.println(sqlstatement);
         }
         if (i != cols.length-1) {
            sqlstatement = sqlstatement + " UNION ";
         }
      }
      sqlstatement = sqlstatement + ";";

      if (debug) {
         System.out.println(sqlstatement);
      }

      try {
         // Execute statement
         ResultSet umat;
         umat = db.resultSetQuery(sqlstatement);

         String line = null;
         if (umat == null) {
            if (debug) {
               System.out.println("Sorry, no matching entries in featurevector table found.");
            }
            return null;

         // We have an entry
         } else {
            if (debug) {
               System.out.println("Found entries in table for FVs " + fvs + " : ");
            }

            // Write text into array
            int count = 0;
            int fvid = 0; // 0 may not be a valid fvid
            int length = cols.length*10;
            String[] fvstring = new String[length];
            // 1 - fvid
            // 2 - sentenceid
            // 3 - markable1
            // 4 - markable2
            // 5 - sentence
            while( umat.next() && count<length){

               // We are at the next feature vector,
               // write also id and text of markables
               if (fvid != Integer.parseInt(umat.getString(1))) {
                  
                  // exit if no space in array for next 3 lines
                  if (count >= length-3) {
                     return fvstring;
                  }
                  
                  line = umat.getString(1); // fvID
                  if (debug) {
                     System.out.println(" Feature Vector ID: " + line );
                  }
                  fvstring[count]=line;
                  count++;
                  
                  line = umat.getString(3); // 1st markable
                  line = line.replace("{", "");
                  line = line.replace("}", "");
                  line = line.replace(",", " ");
                  if (debug) {
                     System.out.println(" 1st markable " + line );
                  }
                  fvstring[count]=line;
                  count++;
                  
                  fvid = Integer.parseInt(umat.getString(1)); // 2nd markable
                  line = umat.getString(4);
                  line = line.replace("{", "");
                  line = line.replace("}", "");
                  line = line.replace(",", " ");
                  if (debug) {
                     System.out.println(" 2nd markable " + line );
                  }
                  fvstring[count]=line;
                  count++;
               }

               // Get next sentence
               line = umat.getString(5);
               if (debug) {
                  System.out.println(count + "th line of result is: " + line);
               }
               fvstring[count]=line;
               count++;
            }

            return fvstring;
         }
      } catch (SQLException e) {
         System.out.println("Error in : " + sqlstatement);
         e.printStackTrace();
      }
      return null;
   }


   /**
      Method getFeatureDefinitions.
      Reads the feature definitions from a file
      and returns them as a vector.
      Uses the file specified as feature description.
      
      @return Vector containing in each element
         the definition of a feature.
   */
   public Vector<String> getFeatureDefinitions() {

      Vector<String> definitions = new Vector<String>();

      try {
         BufferedReader br = Constants.openFile(Constants.featureDefinitionsFile);

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


   /**
      Method getMaxFVID.
      Gets the ID of the feature vector that has the
      maximum ID of all those having the original number of
      features. If there are two sets with equal number of 
      features, results may be strange. Also if you somehow
      add features.
      If there is no DB or an error ocurred, 0 is returned.
      
      @return The maximum ID.
   */
   public int getMaxFVID () {
      if (Constants.db) {
         DBAccess db = new DBAccess();
         String offsetString = db.stringQuery(
               "select max(featurevector_id) " + 
               "from featurevectors " + 
               "where array_upper(features,1)=" +
                  "(select max(array_upper(features,1)) from featurevectors);"
            );
         db.finalize();
         try {
            return Integer.parseInt(offsetString);
         } catch (Exception e) {
            return 0;
         }
      } else {
         return 0;
      }
   }


   /**
      Method deleteCreatedFVs.
      Deletes all feature vectors that have been created in the db
      while experimenting with subsets of features.
      Uses getMaxFVID.
      Does nothing, if there is no db.
   */
   public void deleteCreatedFVs() {
      if (Constants.db) {
         DBAccess db = new DBAccess();
         // Get ID of last original feature vector
         int maxID = getMaxFVID();
         // If ID is 0 an error has ocurred 
         // - don't do anything!
         if (maxID != 0) {
            System.out.println("Deleting all feature vectors with ID>" + maxID);
            db.execute("delete from featurevectors where featurevector_id>" + maxID + ";"); 
            db.finalize();
         } else {
            System.out.println("Not deleting anything, an error has ocurred.");
         }
      }
   }


}
