// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.


package coalda.base;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Comparator;

import coalda.data.ReadFromDB;
import coalda.data.ReadFromFile;
import coalda.data.ReadFromORM;
import coalda.data.Reader;
import coalda.data.WriteToORM;
import coalda.data.Writer;

import prefuse.util.io.IOLib;



/**

Assortion of useful static methods.

@author kesslewd
*/
public class Utils {
 

   /**
      Initializes all variables values in this class
      from the configuration file.
      Variable values that can be set in the config file are:
      - dbCreateScript
      - db_url
      - db_username
      - db_password
      - featureDefinitionsFile
      - matlabServer
   */
   public static void getSettings() {
      try {
         BufferedReader br = openFile(Constants.configFile);
         String line = null;

         while ( (line=br.readLine()) != null )  {

            // Get key/value pairs
            String[] pair = line.split("=");
            String key = pair[0];
            key = key.trim();
            String value = pair[1];
            value = value.trim();

            // Set attribute corresponding to key
            // to the value from the config file.

            // Database attributes
            if (key.equals("dbCreateScript")) {
               ConstantsDataload.dbCreateScript = value; 
            }
            if (key.equals("db_url")) {
               ConstantsDataload.dbLocation = value; 
            }
            if (key.equals("db_username")) {
               ConstantsDataload.dbUser = value; 
            }
            if (key.equals("db_password")) {
               ConstantsDataload.dbPassword = value; 
            }

            // File locations
            if (key.equals("textFile")) {
               ConstantsDataload.textFile = value; 
            }
            if (key.equals("wordFile")) {
               ConstantsDataload.wordFile = value; 
            }
            if (key.equals("markableFile")) {
               ConstantsDataload.markableFile = value; 
            }
            if (key.equals("linkFile")) {
               ConstantsDataload.linkFile = value; 
            }
            if (key.equals("featureFile")) {
               ConstantsDataload.featureFile = value; 
            }
            if (key.equals("featureDefinitionsFile")) {
               Constants.featureDefinitionsFile = value; 
            }

            // matlab settings
            if (key.equals("matlabServer")) {
               Constants.matlabServer = value; 
            }

         }

      } catch (IOException e) {
         System.out.println("Error while reading configuration file - settings are ignored.");
         e.printStackTrace();
      }

   }
   
   
   /**
      Method for opening a file at the given location.
      @param location Path to the file.
      @throws IOException if file is not found or cannot be opened.
      @return Buffered Reader for read access to the file.
   */
   public static BufferedReader openFile (String location) throws IOException {
      InputStream is = IOLib.streamFromString(location);
      if ( is == null ) {
         throw new IOException("Couldn't find " + location
               + ". Not a valid file, URL, or resource locator.");
      }
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      return br;
   }
   
   
   /**
      Comparator for two Strings that are Integers.
   */
   private static class StringCompare implements Comparator<String> {

      /**
         Method for comparing two Strings containing an int.
         @param arg0 First parameter.
         @param arg1 Second parameter.
         @return A negative integer if the first argument is less than the second.
               Zero if both arguments are equal.
               A positive integer if the first argument is greater than the second.
      */
      public int compare(String arg0, String arg1) {
         int a = Integer.parseInt(arg0);
         int b = Integer.parseInt(arg1);
         return (a-b);
      }

      
   }
   
   
   /**
      Method for sorting an array of Featurevector IDs.
      @param featurevectors FVIDs.
      @return Sorted array of FVIDs.
   */
   public static String[] sortFVs (String[] featurevectors) {
      
      String[] ids2 = featurevectors;
      Comparator<String> c = new StringCompare();      
      Arrays.sort(ids2, c);
           
      return ids2;
   }


   /**
      Method for sorting the Featurevector IDs contained in a String.
      @param featurevectors FVIDs, separated by spaces.
      @return Sorted FVIDs in a String separated by spaces.
   */
   public static String sortFVs (String featurevectors) {
      
      String[] fvs = sortFVs(featurevectors.split(" "));
           
      return fvString(fvs);
   }
   
   
   /**
      Method for putting all IDs of the Array in a String.
      @param featurevectors FVIDs.
      @return FVIDs in a String separated by spaces.
   */
   public static String fvString (String[] featurevectors) {
      
      String fvs = "";
      for (int i=0; i<featurevectors.length;i++) {
        fvs = fvs + " " + featurevectors[i];
      }
      fvs = fvs.trim();
           
      return fvs;
   }
   
   
   /**
      Method for selecting a Reader according to the settings.
      A Reader can read from a Database or Files.
      Using simple SQL or simpleORM.
      Here would be the place to change if you want to 
      use a different database product, just write a new
      Reader and change here.
      Classes can also load a specific Reader by themselves
      if they don't want to have the flexibility of changing
      the datasource.
      May return null if Constants.db has no valid value
      @return Reader from where the settings in Constants.db say.
   */
   public static Reader makeReader() {

      switch (Constants.db) {
         case 0:
            return new ReadFromFile();
         case 1:
            return new ReadFromDB();
         case 2:
            return new ReadFromORM();
         default:
            return null;
      }
      
   }
   
   
   /**
      Method for selecting a Writer according to the settings.
      A Writer can write to a Database or Files.
      Using simple SQL or simpleORM.
      Here would be the place to change if you want to 
      use a different database product, just write a new
      Reader and change here.
      Classes can also load a specific Writer by themselves
      if they don't want to have the flexibility of changing
      the datasource.
      May return null if Constants.db has no valid value
      @return Reader from where the settings in Constants.db say.
   */
   public static Writer makeWriter() {
   
      switch (Constants.db) {
         case 2:
            return new WriteToORM();
         default:
            return null;
      }
      
   }
   
   
   
}
