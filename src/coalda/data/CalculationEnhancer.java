// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.Reader.SingleInfo;
import coalda.data.Reader.VectorInfo;
import coalda.data.Reader.MatrixInfo;

import prefuse.data.Table;
import prefuse.data.tuple.TupleSet;
import prefuse.util.DataLib;
import prefuse.util.collections.IntIterator;

import prefuse.data.expression.Predicate;
import prefuse.data.expression.OrPredicate;
import prefuse.data.expression.ComparisonPredicate;
import prefuse.data.expression.parser.ExpressionParser;

import java.lang.Math;


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
      
      // TODO dies funktioniert nur wenn Kanten sortiert...
      
      
      // Initialize node 1 at coordinates (1,1)
      nodes.set(1, Constants.nodeXValue, 1.0);
      nodes.set(1, Constants.nodeYValue, 0.0);
      
      
      // Go through all the edges
      IntIterator edgeKeys = edges.rows();

      while (edgeKeys.hasNext()) {

         int edgeKey = edgeKeys.nextInt();
         System.out.println("edge: " + edgeKey);
         
         // Get source and target node
         int source = edges.getInt(edgeKey, Constants.edgeSource);
         int target = edges.getInt(edgeKey, Constants.edgeTarget);

         int smaller = Math.min(source, target);
         int bigger = Math.max(source, target);

         // If nodes with consecutive ids are neighbours -
         // x-value is the same
         // y-value is the one of the smaller+1
         if (bigger-smaller ==1) {
            
            // TODO andere Map topologien
            double offset = 0.5 + (double)((smaller+1) % 2) * (-1);
            
            nodes.set(bigger, Constants.nodeXValue, nodes.getDouble(smaller, Constants.nodeXValue) + offset);
            nodes.set(bigger, Constants.nodeYValue, nodes.getDouble(smaller, Constants.nodeYValue) - 1);
         } else {
            // For other nodes X-value + 1
            if (nodes.getFloat(bigger, Constants.nodeXValue) == 0) {
               nodes.set(bigger, Constants.nodeXValue, nodes.getFloat(smaller, Constants.nodeXValue) + 1);
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
      
      
      // Go through all the edges
      IntIterator edgeKeys = edges.rows();

      while (edgeKeys.hasNext()) {

         int edgeKey = edgeKeys.nextInt();
            System.out.println("edge: " + edgeKey);
         
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
         
         // Set value for the edge
         edges.set(edgeKey, Constants.edgeUmatValue, distance);
            System.out.println("umatvalue: " + distance);
         
      }
      
      
      // Go through all the nodes
      IntIterator nodeKeys = nodes.rows();

      while (nodeKeys.hasNext()) {

         int nodeKey = nodeKeys.nextInt();
            System.out.println("node: " + nodeKey);
         
         // Get all edges that have this node as source/target
         Predicate filter = new OrPredicate(
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeSource), ExpressionParser.parse(nodeKey+"")),
               new ComparisonPredicate(2, ExpressionParser.parse(Constants.edgeTarget), ExpressionParser.parse(nodeKey+""))
               );
         System.out.println(filter);
         Table nodeEdges = edges.select (filter, null);
         
         // Go through all these edges
         IntIterator nodeEdgesKeys = nodeEdges.rows();

         double uMatrixValue = 0;
         int number = 1;
         while (nodeEdgesKeys.hasNext()) {

            int nodeEdgesKey = nodeEdgesKeys.nextInt();
            System.out.println("has edge: " + nodeEdgesKey);
            
            number++;
            
            // Get umatrix value
            uMatrixValue = uMatrixValue + nodeEdges.getDouble(nodeEdgesKey, Constants.edgeUmatValue);
            System.out.println("umatvalue: " +  nodeEdges.getDouble(nodeEdgesKey, Constants.edgeUmatValue) +
               "umatvalue ges: " + uMatrixValue);
            
         }
         
         // Calculate mean
         uMatrixValue = uMatrixValue / (double) number;
            System.out.println("final node umatvalue: " + uMatrixValue);
         
         // Set value for the node
         nodes.set(nodeKey, Constants.nodeUmatValue, uMatrixValue);
         
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
   public void getProportions(Table nodes){

      // Add the fields for the table of nodes.
      // For every possible label there is a field
      // with the number of feature vectors that
      // have this label and one the proportion of fvs
      // that have this label out of all fvs assigned
      // to that node.
      for (int i=0; i<Constants.possibleLabelValues.length; i++){
         String name = Constants.possibleLabels[i];
         nodes.addColumn(Constants.nodeLabel + name, int.class, new Integer(0));
         nodes.addColumn(Constants.nodeProportion + name, float.class, new Float(0));
      }
      // Additionally there is a field that contains all labels
      nodes.addColumn(Constants.nodeCoDisLabel, String.class, new String("-"));

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
               
            // Get number of feature vectors for every label
            int labels[] = allLabels.getLabels(featureVectors);

            // TODO confidence value?

            String overview = "";
            for (int j=0; j<labels.length; j++) {
               int fvnumber = labels[j];
               if (fvnumber != 0) {
                  String name = Constants.possibleLabels[j];
                  int total = nodes.getInt(nodeKey, Constants.nodeFVNumber);
                  float proportion = (float) fvnumber / (float) total;

                  if (debug) {
                     System.out.println("node " + nodeKey + " : "
                           + fvnumber + " fvs with label " + name + " out of " 
                           + total + " total = " + proportion);
                  }

                  // Set values for node
                  nodes.set(nodeKey, Constants.nodeLabel + name, new Integer(fvnumber));
                  nodes.set(nodeKey, Constants.nodeProportion + name, new Float(proportion));
               }
               overview = overview + "/" + fvnumber;
            }

            // Set label with all labelinformation (cutting off the first /)
            nodes.set(nodeKey, Constants.nodeCoDisLabel, overview.substring(1));
         } else {
            // for nodes without feature vectors set a default label 
            // to enable the label renderer to show something
            nodes.set(nodeKey, Constants.nodeCoDisLabel, "0");
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