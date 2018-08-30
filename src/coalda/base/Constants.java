// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.base;

import de.unistuttgart.ais.sukre.database.utils.IDataBaseConstants;


/**

This class contains a few constants to be used throughout the software.

@author kesslewd

*/
public class Constants {

   /**
      Config file to use.
   */
   public static String configFile = IDataBaseConstants.DEFAULT_DATABASE_PROPERTIES_FILE_PATH;

   /**
      Use the db or read information from files.
      Possible values are:
      0 - read from files
      1 - read from db, raw SQL
      2 - read from db, simpleORM
   */
   public static int db = 2;


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
      ConnVis Layout.
      Shows the connectedness of the SOM by drawing
      edges between nodes where feature vectors having
      one of the nodes as bmu have the other as second bmu.
   */
   public static String layoutConnVis = "connvis";
   
   /**
      Possible layouts for the graph.
      Write default field at index 0.
   */
   public static String[] possibleLayouts = 
   {
      layoutGrid,
      layoutForce,
      layoutConnVis
   };
   

   // ====== Information for the visualization ======

   /**
      Kinds of visualItems shown in the visualization.
   */
   public static enum itemKind {
      node,
      edge,
      edge2,
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
      used for saving the median U-matrix value 
      (mean of upper and lower median).
   */
   public static String nodeUmatValueMedian = "UmatValueMedian";

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
      i being the ID of the feature (starting with 1).
   */
   public static String nodeWeight = "Weight_";

   /**
      Field of visualItem of kind node,
      used for saving the number of  
      feature vectors associated with this node
      that have this label in the gold standard.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeLabel_ + i,
      i being the name of the label.
   */
   public static String nodeLabelGold = "LabelGold_";

   /**
      Field of visualItem of kind node,
      used for saving the percentage of  
      feature vectors associated with this node
      that have this label in the gold standard.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeProportion_ + i,
      i being the name of the label.
   */
   public static String nodeProportionGold = "ProportionGold_";
   
   /**
      Field of visualItem of kind node,
      used for saving the number of  
      feature vectors associated with this node
      that have this label assigned.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeLabel_ + i,
      i being the name of the label.
   */
   public static String nodeLabelAssigned = "LabelAssigned_";
   
   /**
      Field of visualItem of kind node,
      used for saving the percentage of  
      feature vectors associated with this node
      that have this label assigned.
      There are as many fields for the labels
      as there are possible labels
      each one has as its name nodeProportion_ + i,
      i being the name of the label.
   */
   public static String nodeProportionAssigned = "ProportionAssigned_";

   /**
      Field of visualItem of kind node,
      used for saving a label of the node consisting of
      to numbers "a/b" with "a" being the number of
      coreferent feature vectors associated with that node
      and "b" the number of disreferent feature vectors.
   */
   public static String nodeAllLabeledGold = "AllLabeledGold";

   /**
      Field of visualItem of kind node,
      used for saving a label of the node consisting of
      to numbers "a/b" with "a" being the number of
      coreferent feature vectors associated with that node
      and "b" the number of disreferent feature vectors.
   */
   public static String nodeAllLabeledAssigned = "AllLabeledAssigned";

   /**
      Field of visualItem of kind node,
      used for saving a label of the node consisting of
      to numbers "a/b" with "a" being the number of
      coreferent feature vectors associated with that node
      and "b" the number of disreferent feature vectors.
   */
   public static String nodeAllLabeled = "AllLabeled";

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
      Field of visualItem of kind edge,
      used for saving connectedness value
      indicating how many feature vectors that
      have one of the edge's nodes as a bmu
      have the other as a second bmu.
   */
   public static String edgeConnvis = "ConnVis";
   
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
      There are as many fields for the features
      as there are features
      each one has as its name features_ + i,
      i being the number of the features.
   */
   public static String features = "Features_";


   /**
      Possible fields that can be used for
      coloring the nodes.
      Write default field at index 0.
   */
    public static String[] possibleNodeColorings = 
   {
      nodeUmatValue,
      nodeUmatValueMedian,
      nodeFVNumber
   };

    /**
       Giving the possibility to assign no label.
    */
    public static String none = "none";
    
   /**
      Possible fields that can be used for
      the nodelabel of the nodes.
      Write default field at index 0.
   */
   public static String[] possibleNodeLabels = 
   {
      nodeAllLabeledGold,
      nodeAllLabeledAssigned,
      nodeAllLabeled,
      nodeFVNumber,
      nodeKey,
      none
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



}
