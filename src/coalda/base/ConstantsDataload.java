// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.base;


/**

This class contains a few constants to be used throughout the software.
All constants are related to the loading of data, whether from db
or from files.

@author kesslewd

*/
public class ConstantsDataload {

   // ====== Information for import from database ======

   /**
      Tablenames in the database for calculation table.
   */
   public static String calctable = "calculations";

   /**
      Tablenames in the database for word table.
   */
   public static String wordtable = "words";

   /**
      Tablenames in the database for markable table.
   */
   public static String markabletable = "markables";

   /**
      Tablenames in the database for entity table.
   */
   public static String entitytable = "entities";

   /**
      Tablenames in the database for link table.
   */
   public static String linktable = "links";

   /**
      Tablenames in the database for sentences table.
   */
   public static String texttable = "sentences";

   /**
      Tablenames in the database for feature vector table.
   */
   public static String featuretable = "featurevectors";

   /**
      File path to the script used for the creation of the db.
   */
   public static String dbCreateScript = null;

   /**
      Class name of driver for database.
   */
   protected static String dbDriver = "org.postgresql.Driver";

   /**
      Location of the database.
   */
   public static String dbLocation = null;

   /**
      User of the database.
   */
   protected static String dbUser = null;

   /**
      Password for the user of the database.
   */
   protected static String dbPassword = null;


   // ====== Information for import from files ======

   /**
      File used for the import of a corpus,
      containing the sentences of the corpus.
   */
   public static String textFile = "";

   /**
      File used for the import of a corpus,
      containing the words of the corpus.
   */
   public static String wordFile = "";

   /**
      File used for the import of a corpus,
      containing the markables of the corpus.
   */
   public static String markableFile = "";

   /**
      File used for the import of a corpus,
      containing the links of the corpus.
   */
   public static String linkFile = "";

   /**
      File used for the import of feature vectors,
      contains the feature vectors.
   */
   public static String featureFile = "";

   /**
      File used for the import of calculations,
      contains map coordinates in output space.
   */
   public static String coordsFile = "/media/sda5/Diplomarbeit/svnvis_coalda/data/sm.map.coords";

   /**
      File used for the import of calculations,
      contains the neighbourhood relations between
      nodes in output space.
   */
   public static String neighboursFile =  "/media/sda5/Diplomarbeit/svnvis_coalda/data/sm.map.neighbors";

   /**
      File used for the import of calculations,
      contains the U-matrix values for nodes
      and edges.
   */
   public static String umatFile =  "/media/sda5/Diplomarbeit/svnvis_coalda/data/umat";

   /**
      File used for the import of calculations,
      contains the BMU for every feature vector.
   */
   public static String bmuFile =  "/media/sda5/Diplomarbeit/svnvis_coalda/data/data.coords";

   /**
      File used for the import of calculations,
      contains the weights of the nodes
      in the input space dimensions.
   */
   public static String codebookFile =  "/media/sda5/Diplomarbeit/svnvis_coalda/data/sm.codebook";

   /**
      File used for the import of calculations,
      contains the IDs of the feature vectors
      used for calculation.
   */
   public static String fvidsFile = "";
   
}
