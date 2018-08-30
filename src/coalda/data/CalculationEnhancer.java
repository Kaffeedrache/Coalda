// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.util.Arrays;

import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.expression.AndPredicate;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.Predicate;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.util.DataLib;
import prefuse.util.collections.IntIterator;
import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.Reader.VectorInfo;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration.Normalization;


/**

Class that reads the result of a calculation with matlab and imports them
into prefuse data structures.

@author kesslewd
*/
public class CalculationEnhancer  {


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
      ID of the calculation that is imported at the moment
    */
   private int calcID;


   /**
      TODO describe
    */
   int[][] connectedness = null;
   
   
   /**
      Constructor loads appropriate reader for reading from
      database or files, according to the value of
      Constants.db
   */
   public CalculationEnhancer () {
      reader = Utils.makeReader();
   }
   

   /**
      Sets the calculation ID for the current calculation.
      
      @param calculationID ID of current calculation.
   */
   public void setCalcID (int calculationID){
      calcID = calculationID;
      reader.setCalcID(calculationID); 
   }
   
   
   
   /**
      Reads the nodes of the SOM (the map units) 
      with their X and Y value into an existing
      table (nodes are appended at the end).
      Names/Type of the columns added: 
      - Constants.nodeXValue (float) : X Value of the node
         (in SOM space)
      - Constants.nodeYValue (float) : Y Value of the node
         (in SOM space)
      
      @param nodes Table of nodes where the coordinates are added
      @param edges Table with the neighbourhood relations
      
   */
   public void calculateNodes(Table nodes, Table edges) { 
      
      nodes.addColumn(Constants.nodeXValue, float.class, new Float(0.0));
      nodes.addColumn(Constants.nodeYValue, float.class, new Float(0.0));
            
      // Determine map topology
      
      // Find out number of neighbours for a map unit
      int neighbours = 0;
      int maxIndex = nodes.getMaximumRow();
      int minIndex = nodes.getMinimumRow();
      int middleIndex = (maxIndex + minIndex)/2;
      
      // Find out how many neighbour nodes a map unit has for three
      // nodes in the middle of the map.
      // None of the nodes can be at the left or right border
      // if there are more than two rows // TODO what happens with two/one rows?
      // (because the index is in the middle).
      // The nodes can only be at the top or bottom border,
      // but because we take three consecutive nodes, in the worst
      // case one is at the bottom border, the next at the top border
      // and the last is a regular one inside the grid // TODO what happens with two/one columns?

      
      for (int mu=middleIndex; mu<middleIndex+3; mu++) {
         int neighboursTmp = 0;
         
         // Find all edges starting and ending at the given node
         Predicate neighboursFilter = new OrPredicate(
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeTarget), ExpressionParser.parse(mu+"")),
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeSource), ExpressionParser.parse(mu+""))
               );
         Table neighbourEdges = edges.select (neighboursFilter, null);

         // Count these edges
         neighboursTmp = neighbourEdges.getRowCount();
         if (debug) {
            System.out.println("For mu " + mu + " we have " + neighboursTmp + " neighbours");
         }
         
         // If this is the maximum, save it.
         if (neighboursTmp > neighbours) {
            neighbours = neighboursTmp;
         }
      }
      
      if (debug) {
         System.out.println("We have " + neighbours + " neighbours");
      }
      
      // Find out map shape
      // Find all edges starting and ending at node one
      Predicate neighboursFilter = new OrPredicate(
            new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeTarget), ExpressionParser.parse(minIndex+"")),
            new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeSource), ExpressionParser.parse(minIndex+""))
            );
      Table neighbourEdges = edges.select (neighboursFilter, null);

      // Count these edges
      int neighboursFirst = neighbourEdges.getRowCount();
      if (debug) {
         System.out.println("For mu " + minIndex + " we have " + neighboursFirst + " neighbours");
      }
      

      boolean toroid = false;
      
      if (neighbours == neighboursFirst) { // Toroid
         toroid = true;
         if (debug) {
            System.out.println("We have a toroid shape");
         }
      } 
      else if (neighboursFirst == 2 ) { // Sheet // TODO replace 2 with generic 
         if (debug) {
            System.out.println("We have a sheet shape");
         }
      } else { // Cylinder if more than 2, but not as much as all
         if (debug) {
            System.out.println("We have a cylinder shape");
         }
      }
      
      
      // Initialize node 1 at coordinates (1,0)
      nodes.set(1, Constants.nodeXValue, new Float(1.0));
      nodes.set(1, Constants.nodeYValue, new Float(0.0));
      

      // Line index is the number of a node in a vertical
      // line (column) of the SOM. Even and odd nodes 
      // must be positioned differently for hexagonal maps.
      // The value is reset later, so the initial value
      // is of no matter.
      int lineIndex = 0;
      
      // Go through all the edges
      // TODO nodes have to be sorted by smaller node key!
      IntIterator edgeKeys = edges.rows();
      
      while (edgeKeys.hasNext()) {

         int edgeKey = edgeKeys.nextInt();
         
         // Get source and target node
         int source = edges.getInt(edgeKey, Constants.edgeSource);
         int target = edges.getInt(edgeKey, Constants.edgeTarget);
         if (debug) {
            System.out.println("Edge " + edgeKey + " from " + source + " to " + target);
         }

         int smaller = Math.min(source, target);
         int bigger = Math.max(source, target);

         // If nodes with consecutive ids are neighbours
         // they are in the same SOM line/column.
         // Set the x- and y-values based on the smaller
         // node's values.
         // This assumes a sorting of the edges by
         // key of the smaller node.
         if (bigger-smaller == 1) {

            // TODO the following is a bit of a hack
            
            if (!toroid) {
               
               // Get edge from smaller-1 to smaller, if exists
               int smaller2 = smaller-1;
               Predicate filter = new AndPredicate(
                     new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeTarget), ExpressionParser.parse(smaller+"")),
                     new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeSource), ExpressionParser.parse(smaller2+""))
                     );
               Table nodeEdges = edges.select (filter, null);
               
               // If such an edge exists, the two nodes bigger and smaller
               // are in the same line (vertical line/column/whatever).
               // If no such edge exists, one is at the lower edge of the map
               // and the other is at the upper edge of the map.
               // We then have to reset the count of nodes in one line.
               IntIterator nodeEdgesKeys = nodeEdges.rows();
               if (nodeEdgesKeys.hasNext()) {
                  // there is such an edge - increase counter
                  lineIndex++;
               } else {
                  // there is no such edge - reset counter
                  lineIndex = 1;
               }
            } else {
               // TODO how determine rows in toroid map
            }
            
            double x = 0;
            double y = 0;

            // Hexagonal map:
            // X-values are different for even and odd nodes,
            // even nodes are "indented" a bit
            // (starting counting from 1)
            // Y-values are decreased by one for each node,
            // so that the first node is in the upper left corner.
            if (neighbours == 6) {
               // Offset is 0.5 for even nodes, -0.5 for odd nodes
               double offset = 0.5 + (double)((lineIndex+1) % 2) * (-1);
               x = nodes.getDouble(smaller, Constants.nodeXValue) + offset;
               y = nodes.getDouble(smaller, Constants.nodeYValue) - 1;
            }

            // Rectangular map:
            // X-values are the same for all nodes in a line.
            // Y-values are decreased by one for each node,
            // so that the first node is in the upper left corner.
            if (neighbours == 4) {
               x = nodes.getDouble(smaller, Constants.nodeXValue);
               y = nodes.getDouble(smaller, Constants.nodeYValue) - 1;
            }
            
            if (debug) {
               System.out.println("Coordinates for node " + bigger + ": " + x + " | " + y);
            }
            
            // Set values
            nodes.set(bigger, Constants.nodeXValue, new Float(x));
            nodes.set(bigger, Constants.nodeYValue, new Float(y));
            

         } else {

            // If the two nodes are not directly consecutive keys,
            // they are not in the same line. Increase X-value by one
            // if the value has not been set before.
            // In cylindric or toroid shapes it can happen, that the 
            // value has been set by an earlier node and the nodes are
            // too far on the left (for example the link 1-166 sets the
            // x-value of 166 to 2, but it should be at the very right
            // of the map, for example node 151 has already value 11
            // and is also connected to 166), then set the value to
            // the higher value+1.
            // Y-values will be determined by the order in the line
            // this is left for later.
            
            if (nodes.getDouble(bigger, Constants.nodeXValue) == 0 ||
                  nodes.getDouble(bigger, Constants.nodeXValue) < nodes.getDouble(smaller, Constants.nodeXValue)) {

               // This node is in the next line
               double x = nodes.getDouble(smaller, Constants.nodeXValue) + 1;

               if (debug) {
                  System.out.println("Coordinates " + bigger + ": " + x + " | -");
               }
               nodes.set(bigger, Constants.nodeXValue, new Float(x)); ///nodes.getFloat(smaller, Constants.nodeXValue) + 1);
            }
         }
         
      }
      
      
   }
   
   
   
   /**
      Reads the values of an U-Matrix into the tables
      of nodes and edges. At the end every node and
      every edge should have an U-Matrix value.
      This is only tested for hexagonal shapes.
      Names/Type of the columns added to the table nodes: 
      - Constants.nodeUmatValue (float) : U-Matrix value of
         this node (the average of the U-Matrix 
         values of all the edges)
      Names of the columns added to the table edges: 
      - Constants.edgeUmatValue (float) : U-Matrix value
         of this edge

      @param nodes Table with the nodes (must be not null)
      @param edges Table with the edges (must be not null)
    */
   public void calculateUmatrix(Table nodes, Table edges) {

      // Add the field for the table of edges
      edges.addColumn(Constants.edgeUmatValue, float.class);
      
      // Add the field for the table of nodes
      nodes.addColumn(Constants.nodeUmatValue, float.class);
      nodes.addColumn(Constants.nodeUmatValueMedian, float.class);
      
      
      // Go through all the edges
      IntIterator edgeKeys = edges.rows();

      while (edgeKeys.hasNext()) {

         int edgeKey = edgeKeys.nextInt();
         
         // Get source and target node
         int source = edges.getInt(edgeKey, Constants.edgeSource);
         int target = edges.getInt(edgeKey, Constants.edgeTarget);

         // Calculate euclidic distance of both nodes
         int i=1;
         double distance = 0;
         while ( nodes.canGetDouble(Constants.nodeWeight + i) ) {
            double weightSource = nodes.getDouble(source, Constants.nodeWeight + i);
            double weightTarget = nodes.getDouble(target, Constants.nodeWeight + i);
            distance = distance + (weightSource - weightTarget) *  (weightSource - weightTarget);
            i++;
         }
         distance = Math.sqrt(distance);
         
         if (debug) {
            System.out.println("Edge " + edgeKey + " umatValue=" + distance);
         }
         
         // Set value for the edge
         edges.set(edgeKey, Constants.edgeUmatValue, new Float(distance));
         
      }
            
      // Go through all the nodes
      IntIterator nodeKeys = nodes.rows();

      while (nodeKeys.hasNext()) {

         int nodeKey = nodeKeys.nextInt();
         
         // Get all edges that have this node as source/target
         Predicate filter = new OrPredicate(
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeSource), ExpressionParser.parse(nodeKey+"")),
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeTarget), ExpressionParser.parse(nodeKey+""))
               );
         Table nodeEdges = edges.select (filter, null);
         
         // Go through all these edges
         // Add up u matrix values
         // Save them in Array for median calculation
         IntIterator nodeEdgesKeys = nodeEdges.rows();
         double uMatrixValue = 0;
         int i = 0;
         int number = nodeEdges.getRowCount();
         double[] edgeValues = new double[number];
         while (nodeEdgesKeys.hasNext()) {
            int nodeEdgesKey = nodeEdgesKeys.nextInt();
            uMatrixValue = uMatrixValue + nodeEdges.getDouble(nodeEdgesKey, Constants.edgeUmatValue);
            edgeValues[i] = nodeEdges.getDouble(nodeEdgesKey, Constants.edgeUmatValue);
            i++;
         }
         
         // Calculate mean u-matrix value
         uMatrixValue = uMatrixValue / (double) number;
         
         if (debug) {
            System.out.println("Node " + nodeKey + " has " + number + " edges, mean umatValue=" + uMatrixValue);
         }
         
         // Set value for the node for u matrix value
         nodes.set(nodeKey, Constants.nodeUmatValue, new Float(uMatrixValue));

         // Calculate median u-matrix value
         // For odd number of edges: take median
         // For even number of edges: take mean of upper and lower median
         Arrays.sort(edgeValues);
         
         double medianUMatrixValue = 0;
         if (number % 2 == 0) {
            medianUMatrixValue = edgeValues[number/2-1] + edgeValues [number/2];
            medianUMatrixValue = medianUMatrixValue/2;
         } else {
            medianUMatrixValue = edgeValues[number/2];
         }

         if (debug) {
            System.out.println("Node " + nodeKey + ", median umatValue=" + medianUMatrixValue);
         }
         
         // Set value for the node
         nodes.set(nodeKey, Constants.nodeUmatValueMedian, new Float(medianUMatrixValue));
         
      }
      
   
   }
   
   

   /**
      Calculates the number of coreferent and 
      disreferent feature vectors associated with this
      node and the proportions.
      Names/Type of the columns added: 
      - nodeProportion (float) : Proportion of coreferent
          fv out of all fvs associated with this node.
      - nodeProportionDis (float) : Proportion of disreferent
          fv out of all fvs associated with this node.
      - nodeCorefNumber (int) : Number of coreferent
          fv associated with this node.
      - nodeDisrefNumber (int) : Number of disreferent
          fv associated with this node.
      - nodeCoDisLabel (String) : Label 
          (nodeCorefNumber / nodeDisrefNumber)
      
      @param nodes Table with the nodes
    */
   public void getProportions(Table nodes) {

      // Add the fields for the table of nodes.
      // For every possible label there is a field
      // with the number of feature vectors that
      // have this label and one the proportion of fvs
      // that have this label out of all fvs assigned
      // to that node.
      for (int i=0; i<Constants.possibleLabelValues.length; i++){
         String name = Constants.possibleLabels[i];
         nodes.addColumn(Constants.nodeLabelGold + name, int.class, new Integer(0));
         nodes.addColumn(Constants.nodeProportionGold + name, float.class, new Float(0));
         nodes.addColumn(Constants.nodeLabelAssigned + name, int.class, new Integer(0));
         nodes.addColumn(Constants.nodeProportionAssigned + name, float.class, new Float(0));
      }
      // Additionally there is a field that contains all gold/assigned/all labels
      nodes.addColumn(Constants.nodeAllLabeledGold, String.class, new String("-"));
      nodes.addColumn(Constants.nodeAllLabeledAssigned, String.class, new String("-"));
      nodes.addColumn(Constants.nodeAllLabeled, String.class, new String("-"));

      // Get all feature vectors used in this calculation
      String[] fvIDArray = reader.readCompleteVector(VectorInfo.fvids);

      // Write them to a String separated with spaces
      String fvIDs = "";
      if (fvIDArray != null && fvIDArray.length != 0) {
         for (String fvID : fvIDArray) {
            fvIDs = fvIDs + " " + fvID;
         }
         fvIDs = fvIDs.trim();
      }

      // There are all the labels
      LabelImport allLabels = new LabelImport(fvIDs);

      // Go through all the nodes
      IntIterator nodeKeys = nodes.rows();

      while (nodeKeys.hasNext()) { 
         
         int nodeKey = nodeKeys.nextInt();

         // Get feature vectors associated with the node
         String featureVectors = nodes.getString(nodeKey, Constants.nodeFVectors);

         if (featureVectors != null) { // Node has associated feature vectors
               
            // Get number of feature vectors for every label (gold and assigned)
            // These are two parallel arrays.
            int goldLabels[] = allLabels.getGoldLabels(featureVectors);
            int assignedLabels[] = allLabels.getAssignedLabels(featureVectors);

            // TODO confidence value?

            String allGold = "";
            String allAssigned = "";
            for (int j=0; j<goldLabels.length; j++) {
               int fvnumberGold = goldLabels[j];
               int fvnumberAssigned = assignedLabels[j];
               String name = Constants.possibleLabels[j];
               int total = nodes.getInt(nodeKey, Constants.nodeFVNumber);
               if (fvnumberGold != 0) {
                  float proportionGold = (float) fvnumberGold / (float) total;

                  if (debug) {
                     System.out.println("node " + nodeKey + " : "
                           + fvnumberGold + " fvs with gold label " + name + " out of " 
                           + total + " total = " + proportionGold);
                  }

                  // Set values for node
                  nodes.set(nodeKey, Constants.nodeLabelGold + name, new Integer(fvnumberGold));
                  nodes.set(nodeKey, Constants.nodeProportionGold + name, new Float(proportionGold));
               }
               if (fvnumberAssigned != 0) {
                  float proportionAssigned = (float) fvnumberAssigned / (float) total;

                  if (debug) {
                     System.out.println("node " + nodeKey + " : "
                           + fvnumberAssigned + " fvs with label " + name + " out of " 
                           + total + " total = " + proportionAssigned);
                  }

                  // Set values for node
                  nodes.set(nodeKey, Constants.nodeLabelAssigned + name, new Integer(fvnumberAssigned));
                  nodes.set(nodeKey, Constants.nodeProportionAssigned + name, new Float(proportionAssigned));
               }
               allGold = allGold + "/" + fvnumberGold;
               allAssigned = allAssigned + "/" + fvnumberAssigned;
            }

            // Set labels with all labelinformation (cutting off the first /)
            nodes.set(nodeKey, Constants.nodeAllLabeledGold, allGold.substring(1));
            nodes.set(nodeKey, Constants.nodeAllLabeledAssigned, allAssigned.substring(1));
            nodes.set(nodeKey, Constants.nodeAllLabeled, 
                  allGold.substring(1) + "\n" + allAssigned.substring(1));
         } else {
            // for nodes without feature vectors set a default label 
            // to enable the label renderer to show something
            //nodes.set(nodeKey, Constants.nodeCoDisLabel, "0");
         }

      }

   }



   /**
      Gets the Best Matching Unit (BMU) for every feature
      vector and saves the IDs of all feature vectors who
      have this map unit as a BMU with every map unit. 
      Names of the columns added: 
      - Constants.nodeFVNumber (int) : number of feature vectors
         associated with a node
      - Constants.nodeFVectors (String) :IDs of feature
         vectors that have this node as BMU, IDs are
         separated with spaces

      @param nodes Table with the nodes

   */
   public void calculateBMUs (Table nodes) {

      // Add the fields for the table of nodes
      if (!nodes.canGetString(Constants.nodeFVectors)) {
         nodes.addColumn(Constants.nodeFVectors, String.class);
      }
      
      // Create adjacency and connectivity matrix
      // I ignore 0/0 of matrix because node numbers start at 1
      int numberOfNodes = nodes.getRowCount() + 1;
      int[][] cumulativeAdjacency = new int[numberOfNodes][numberOfNodes];
      connectedness = new int[numberOfNodes][numberOfNodes];   
      
      // Get all feature vectors and their features used in this calculation
      String[] fvIDArray = reader.readCompleteVector(VectorInfo.fvids);
      String fvIDs = null;
      if (fvIDArray != null) {
         // Take only the specified fv ids
         fvIDs = Utils.fvString(fvIDArray);
         if (debug) {
            System.out.println("Take only fvs " + fvIDs);
         }
      }

      // Read all specified fvs from db
      FVImport fvimport = new FVImport();
      Table featurevectors = fvimport.getFeaturetable(fvIDs);
      
      // Get number of features
      // we start at 1 because we always do
      int numberOriginal=1;
      while (featurevectors.canGetDouble(Constants.features + numberOriginal)) {
         numberOriginal++;
      }

      // Get normalization method used for calculation
      CalculationAccess ca = new CalculationAccess();
      ca.setCalcID(calcID);
      Normalization normalizationMethod = ca.getNorm();
      
      // Get features used for calculation
      // If null is returned all features have been used.
      // Then create an array for all features where
      // all numbers are contained.
      int[] usedFeatures = ca.getUsedFeatures();
      if (usedFeatures == null) {
         usedFeatures = new int[numberOriginal-1];
         for (int i=0; i<numberOriginal-1; i++) {
            usedFeatures[i] = i+1;
         }
      }
      // +1 because we always start at 1.
      // EXCEPT for the usedFeatures array!!
      int numberUsed = usedFeatures.length + 1;
      
      // Arrays for saving normalization parameters
      // (array[0] is always ignored)
      double[] mean = new double[numberOriginal];
      double[] sDeviation = new double[numberOriginal];
      double[] min = new double[numberOriginal];
      double[] max = new double[numberOriginal];

      // TODO implement others, see Vesanto et al. page 35
      // Get Normalization parameters
      // for all features (also those that are not used).
      switch (normalizationMethod) {
         case var: // need: mean, standard deviation
         case logistic: // need: mean, standard deviation
            for (int i=1; i<numberOriginal; i++) {
               mean[i] = DataLib.mean(featurevectors.tuples(), Constants.features + i);
               sDeviation[i] = DataLib.deviation(featurevectors.tuples(), Constants.features + i, mean[i]);
               if (debug) {
                  System.out.println("feature number" + i + " mean=" + mean[i] + " sdev=" + sDeviation[i]);
               }
            }
            break;
         case log: // need: min
            for (int i=1; i<numberOriginal; i++) {
               Tuple minTuple = DataLib.min(featurevectors.tuples(), Constants.features + i);
               min[i] = minTuple.getDouble(Constants.features + i);        
               if (debug) {
                  System.out.println("feature number" + i + " min=" + min[i]);
               }
            }
            break;
         case range: // need: min, max
            for (int i=1; i<numberOriginal; i++) {
               Tuple minTuple = DataLib.min(featurevectors.tuples(), Constants.features + i);
               min[i] = minTuple.getDouble(Constants.features + i);
               Tuple maxTuple = DataLib.max(featurevectors.tuples(), Constants.features + i);
               max[i] = maxTuple.getDouble(Constants.features + i);
               if (debug) {
                  System.out.println("feature number" + i + " min=" + min[i] + " max=" + max[i]);
               }
            }
            break;
         default : // not implemented yet
            break; 
      }

      // Iterator through all the feature vectors,
      // normalize them and compare with all node weight vectors
      // Save BMU and second BMU
      IntIterator fvs = featurevectors.rows();
      while (fvs.hasNext()) {
         
         int fvIndex = fvs.nextInt();
         int fvID = featurevectors.getInt(fvIndex, Constants.featureID);
         
         // Normalize feature vector
         // Here only used features are saved, not all features!
         // We ignore field features[0] and start at 1
         double[] features = new double[numberUsed];


         // TODO implement others, see Vesanto et al. page 35
         switch (normalizationMethod) {
         
            case var: // Normalization var
               // x' = (x - x_mean)/x_sdev
               for (int i=1; i<numberUsed; i++) {
                  int index = usedFeatures[i-1];
            	   if (sDeviation[index]!= 0) { 
            		   features[i] = (featurevectors.getDouble(fvIndex, Constants.features + index) - mean[index])/sDeviation[index];
            		   if (debug) {
            		      System.out.println("index: " + index + " features[" + i + "]=" 
            		            + "(" + featurevectors.getDouble(fvIndex, Constants.features + index) + "-" + mean[index] + ")/" 
            		            + sDeviation[index] + "=" + features[i]);
            		   }
            	   } else {
            		   features[i] = mean[index];
            		   if (debug) {
                        System.out.println("index: " + index + " features[" + i + "]=" + mean[index]);
                     }
            	   }
               }
      
               break;
               
            case logistic: // Normalization logistic
               
               // First apply normalization var
               // x' = (x - x_mean)/x_sdev
               for (int i=1; i<numberUsed; i++) {
                  int index = usedFeatures[i-1];
                  double value = 0;
                  if (sDeviation[index]!= 0) { 
                     value = (featurevectors.getDouble(fvIndex, Constants.features + index) - mean[index])/sDeviation[index];
                     if (debug) {
                        System.out.println("index: " + index + " features[" + i + "]=" 
                              + "(" + featurevectors.getDouble(fvIndex, Constants.features + index) + "-" + mean[index] + ")/" 
                              + sDeviation[index] + "=" + features[i]);
                     }
                  } else {
                     value = mean[index];
                     if (debug) {
                        System.out.println("index: " + index + " features[" + i + "]=" + mean[index]);
                     }
                  }
                  
                  // Then apply logistic function
                  // x'' = 1/(1+e^-x')
                  features[i] = 1 / (1 + Math.exp(value * (-1)));
                  if (debug) {
                     System.out.println("index: " + index + " features[" + i + "]=" + features[i]);
                  }
               }
               break;
               
            case log: // Normalization log
               // x' = ln(x-min(x)+1);
               for (int i=1; i<numberUsed; i++) {
                  int index = usedFeatures[i-1];
                  features[i] = Math.log(featurevectors.getDouble(fvIndex, Constants.features + index) - min[index] + 1);
               }
               break;
               
            case range: // Normalization range
               // x' = (x-min(x)) / (max(x)-min(x))
               for (int i=1; i<numberUsed; i++) {
                  int index = usedFeatures[i-1];
                  if (max[index] != min[index]) {
                     features[i] = (featurevectors.getDouble(fvIndex, Constants.features + index) - min[index]) /
                           (max[index] - min[index]);
                  } else { // All feature vectors have the same value (= max = min)
                     features[i] = max[index];
                  }
               }
               break;
               
            default : // not implemented
               break;
         }
         
         if (debug) {
            System.out.print("FV " + fvID + " original: " );
            for (int i=1; i<numberOriginal; i++) {
               System.out.print(featurevectors.getDouble(fvIndex, Constants.features + i) + " ");
            }
            System.out.println();

            System.out.print("FV " + fvID + " normalized/only used features: " );
            for (int i=1; i<numberUsed; i++) {
               System.out.print(features[i] + " ");
            }
            System.out.println();
         }
         
         // For every feature vector calculate distance to all nodes
         // and save the two closest nodes.
         
         int bmu = 0;
         double bmuDistance = Double.MAX_VALUE;
         int bmu2 = 0;
         double bmu2Distance = Double.MAX_VALUE;

         IntIterator nodeKeys = nodes.rows();

         while (nodeKeys.hasNext()) {
            
            int nodeKey = nodeKeys.nextInt();
            
            // Calculate euclidean distance for this node
            double distance = 0;
            int i = 1;
            while (nodes.canGetDouble(Constants.nodeWeight + i)) {
               double tmp = nodes.getDouble(nodeKey, Constants.nodeWeight + i) - features[i];
               distance = distance +  tmp * tmp;
               i++;
            }
            distance = Math.sqrt(distance);
            
            if (debug) {
               System.out.println("Checking node " + nodeKey + " distance is " + distance);
            }
            
            // Compare with distances until now
            if (bmuDistance > distance) {
               // Set new node as BMU
               bmu = nodeKey;
               bmuDistance = distance;
            } else if (bmu2Distance > distance) {
               // Set new node as BMU
               bmu2 = nodeKey;
               bmu2Distance = distance;
            }
            
         }
         
         if (debug) {
            System.out.println("FV " + fvID + " bmu = " + bmu + " bmu2 = " + bmu2);
         }
         
         // Add this pair of bmu/bmu2 to the cumulativeAdjacency matrix
         cumulativeAdjacency[bmu][bmu2]++;
         
         // Add current feature vector to FV-List of the node
         // (map unit) that it has as a BMU
         String muVectors = nodes.getString(bmu, Constants.nodeFVectors);
         int muFVNumber;
         if (muVectors != null) { // Node has associated feature vectors
            muVectors = muVectors + " " + fvID;
            muFVNumber = nodes.getInt(bmu,Constants.nodeFVNumber) + 1;
         } else { // Node has no feature vectors yet 
            muVectors = "" + fvID;
            muFVNumber = 1;
         }
         nodes.set(bmu, Constants.nodeFVectors, muVectors);
         nodes.set(bmu, Constants.nodeFVNumber, Integer.valueOf(muFVNumber));

         if (debug) {
            System.out.println("lineno" + fvIndex 
                  + " - MU " + nodes.getString(bmu, Constants.nodeKey)
                  + " - FVs (" + nodes.getInt(bmu, Constants.nodeFVNumber) + ") " 
                  + nodes.getString(bmu, Constants.nodeFVectors));
         }
         
      }

      // Determine level of connectedness out of cumulativeAdjacency matrix
      for (int i=1; i<numberOfNodes; i++) {
         for (int j=1; j<numberOfNodes; j++) {
            connectedness[i][j] = cumulativeAdjacency[i][j] + cumulativeAdjacency[j][i];
         }
      }

      if (debug) {
         
         System.out.println("cumulative Adjacency: ");
         for (int i=1; i<numberOfNodes; i++) {
            for (int j=1; j<numberOfNodes; j++) {
               if (cumulativeAdjacency[i][j] != 0) {
                  System.out.println("i " + i + " j " + j + " -> " + cumulativeAdjacency[i][j]);
               }
            }
         }
   
         System.out.println("Connectedness: ");
         for (int i=1; i<numberOfNodes; i++) {
            for (int j=i; j<numberOfNodes; j++) {
               if (connectedness[i][j] != 0) {
               System.out.println("i " + i + " j " + j + " -> " + connectedness[i][j]);
               }
            }
         }
      }
   }



   /**
      Gets the connectedness of two nodes.
      For existing edges of the topology the connectedness
      is added as a field.
      For topology violation where bmu/bmu2 pairs are not 
      connected by the topology, additional edges are added to
      the edge table. These edges have the kind 'edge2'.
     
      Names of the column added to edges: 
      - Constants.edgeConnvis (int) : number of feature vectors
         having one node of the edge as a bmu and the other as second bmu

      @param nodes Table with the nodes
      @param edges Table with the edges

    */
   public void calculateConnectedness (Table nodes, Table edges) { 

      // Add the field for the table of edges
      edges.addColumn(Constants.edgeConnvis, int.class, new Integer(0));
            
      // Check for connectedness matrix
      if (connectedness == null) {
         // creates connectedness matrix as a sideeffect
         calculateBMUs(nodes);
      }

      // Go through all the edges
      IntIterator edgeKeys = edges.rows();

      while (edgeKeys.hasNext()) {

         int edgeKey = edgeKeys.nextInt();
         
         // Get source and target node
         int source = edges.getInt(edgeKey, Constants.edgeSource);
         int target = edges.getInt(edgeKey, Constants.edgeTarget);

         // Get level of connectedness
         int connectivity = connectedness[source][target];
         
         if (connectivity != 0) {
            // Set level to 0
            // (this is needed that we don't add these edges also
            // to the additional edges table)
            connectedness[source][target] = 0;
            connectedness[target][source] = 0; // redundant, but better safe than sorry
            
            // Set value for the edge
            edges.set(edgeKey, Constants.edgeConnvis, new Float(connectivity));
            
            if (debug) {
               System.out.println("Edge " + edgeKey + " from " + source 
                     + " to " + target + " connectivity = " + connectivity);
            }
         }
         
      }
      
      // This creates the edges that are additional
      int element = edges.getMaximumRow() + 1;
      for (int i=1; i<connectedness.length; i++) {
         for (int j=i; j<connectedness.length; j++) {
            if (connectedness[i][j] != 0) {
               
               // Get level of connectedness
               int connectivity = connectedness[i][j];
               
               // Add new edge to edge table
               edges.addRow();
               edges.set(element, Constants.kind, Constants.itemKind.edge2); // additional edge!
               edges.set(element, Constants.edgeKey, Integer.valueOf(element));
               edges.set(element, Constants.edgeSource, Integer.valueOf(i));
               edges.set(element, Constants.edgeTarget, Integer.valueOf(j));  
               edges.set(element, Constants.edgeConnvis, new Integer(connectivity));
               

               if (debug) {
                  System.out.println("Add additional edge " + element + " from " + i + " to " + j + ", connectivity " + connectivity);
               }
               
               element++;
            }
         }
      }
      
   }







   /**
      Cleanup.
   */
   public void finalize() {
      reader.finalize();
   }
   
   
   
}