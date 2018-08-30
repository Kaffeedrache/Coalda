// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.base;


import java.io.BufferedReader;
import java.io.IOException;


/**

This class contains a few constants to be used throughout the software.

@author kesslewd

*/
public class Constants {

   /**
      Config file to use.
   */
   public static String configFile = "coalda.properties";

   /**
      Use the db or read information from files.
      Possible values are:
      0 - read from files
      1 - read from db, raw SQL
      2 - read from db, simpleORM
   */
   public static int db = 0;


   // ====== Information for labeling ======

   /**
      Names for the possible labels as shown to the user.
   */
   public static String[] possibleLabels = {"coref", "disref", "unknown"};

   /**
      Possible labels as assigned in the data.
   */
   public static int[] possibleLabelValues = {1, 0, -1};
   // coref = 1
   // disref = 0
   // unlabeled = -1


   // ====== Information for layout ======

   /**
      Grid-layout.
      Location is based on nodeXValue and nodeYValue.
   */
   public static String layoutGrid = "grid";

   /**
      Force-directed Layout.
      Spring lenght is based on U-matrix value.
   */
   public static String layoutForce = "forcedirected";

   /**
      Possible layouts for the graph.
      Write default field at index 0.
   */
   public static String[] possibleLayouts = 
   {
      layoutGrid,
      layoutForce
   };
   

   // ====== Information for the visualization ======

   /**
      Kinds of visualItems shown in the visualization.
   */
   public static enum itemKind {
      node,
      edge,
      fv,
      other;
   }


   // Fields of visual items

   /**
      Field of visualItem,
      used for determining the kind of a visualItem,
      can be of enumKind.
   */
   public static String kind = "Label";

   /**
      String for kind of visualItem = node.
   */
   public static String nodeKind = "node";

   /**
      String for kind of visualItem = edge.
   */
   public static String edgeKind = "edge";

   /**
      String for kind of visualItem = fv.
   */
   public static String fvKind = "fv";


   /**
      Field of visualItem of kind node,
      used for saving the ID of the node.
   */
   public static String nodeKey = "NodeKey";

   /**
      Field of visualItem of kind node,
      used for saving the x value of the node
      in output space.
   */
   public static String nodeXValue = "XValue";

   /**
      Field of visualItem of kind node,
      used for saving the y value of the node
      in output space.
   */
   public static String nodeYValue = "YValue";

   /**
      Field of visualItem of kind node,
      used for saving the U-matrix value.
   */
   public static String nodeUmatValue = "UmatValue";

   /**
      Field of visualItem of kind node,
      used for saving the IDs of all feature vectors associated
      with this node. IDs have to be separated by spaces.
      When the node has no associated feature vectors,
      the String can be null or the empty String.
   */
   public static String nodeFVectors = "FeatureVectors";

   /**
      Field of visualItem of kind node,
      used for saving the number of feature vectors
      associated with this node.
   */
   public static String nodeFVNumber = "FVNumber";

   /**
      Field of visualItem of kind node,
      actually there are as many fields for the weights
      as there are features in the input space,
      each one has as its name nodeWeight + i,
      i being the ID of the feature.
   */
   public static String nodeWeight = "Weight_";

   /**
      Field of visualItem of kind node,
      used for saving the number of  
      feature vectors associated with this node
      that have this label.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeLabel_ + i,
      i being the name of the label.
   */
   public static String nodeLabel = "Label_";

   /**
      Field of visualItem of kind node,
      used for saving the percentage of  
      feature vectors associated with this node
      that have this label.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeProportion_ + i,
      i being the name of the label.
   */
   public static String nodeProportion = "Proportion_";

   /**
      Field of visualItem of kind node,
      used for saving a label of the node consisting of
      to numbers "a/b" with "a" being the number of
      coreferent feature vectors associated with that node
      and "b" the number of disreferent feature vectors.
   */
   public static String nodeCoDisLabel = "AllLabeled";

   /**
      Field of visualItem of kind edge,
      used for saving the ID of the edge.
   */
   public static String edgeKey = "EdgeKey";

   /**
      Field of visualItem,
      used for the ID of the source/start node of
      the edge.
   */
   public static String edgeSource = "Source";

   /**
      Field of visualItem of kind edge,
      used for saving the target/end node of
      the edge.
   */
   public static String edgeTarget = "Target";

   /**
      Field of visualItem of kind edge,
      used for saving the U-matrix value
      of the edge.
   */
   public static String edgeUmatValue = "UmatValue";

   /**
      Field of visualItem of kind feature vector,
      used for saving the ID of the feature vector.
   */
   public static String featureID = "ID";

   /**
      Field of visualItem of kind feature vector,
      used for saving the label of the feature vector.
   */
   public static String featureLabel = "FVLabel";

   /**
      Field of visualItem of kind feature vector,
      used for saving the array of features of the 
      feature vector.
   */
   public static String features = "Features";


   /**
      Possible fields that can be used for
      coloring the nodes.
      Write default field at index 0.
   */
    public static String[] possibleNodeColorings = 
   {
      nodeUmatValue,
      nodeFVNumber,
   };

   /**
      Possible fields that can be used for
      the nodelabel of the nodes.
      Write default field at index 0.
   */
   public static String[] possibleNodeLabels = 
   {
      nodeCoDisLabel,
      nodeFVNumber,
      nodeKey
   };


   // ====== Information for feature definition file ======
   
   /**
      File where the names of the features are defined,
      used for displaying them to the user in the side pane
      and calculating the number of features.
      (read from config file)
   */
   public static String featureDefinitionsFile = "";


   // ====== Information for matlab ======

   /**
      Computer where the matlab SOM server is running.
      (read from config file)
   */
   public static String matlabServer = null;


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


   // ====== Initialize ======


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
         BufferedReader br = Utils.openFile(configFile);
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
               dbCreateScript = value; 
            }
            if (key.equals("db_url")) {
               dbLocation = value; 
            }
            if (key.equals("db_username")) {
               dbUser = value; 
            }
            if (key.equals("db_password")) {
               dbPassword = value; 
            }

            // File locations
            if (key.equals("textFile")) {
               textFile = value; 
            }
            if (key.equals("wordFile")) {
               wordFile = value; 
            }
            if (key.equals("markableFile")) {
               markableFile = value; 
            }
            if (key.equals("linkFile")) {
               linkFile = value; 
            }
            if (key.equals("featureFile")) {
               featureFile = value; 
            }
            if (key.equals("featureDefinitionsFile")) {
               featureDefinitionsFile = value; 
            }

            // matlab settings
            if (key.equals("matlabServer")) {
            matlabServer = value; 
            }

         }

      } catch (IOException e) {
         System.out.println("Error while reading configuration file - settings are ignored.");
         e.printStackTrace();
      }

   }


}
