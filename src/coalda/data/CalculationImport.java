// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;

import prefuse.data.Graph;
import prefuse.data.Table;


/**

Class that reads the result of a calculation with matlab and imports them
into prefuse data structures. Relies on a @link{CalculationAccess} object to
import the data.

@author kesslewd
*/
public class CalculationImport {


   /**
      This object does the actual loading of the data.
      Depending on whether the data comes from files or
      a db or whatever you imagine.
   */
   private CalculationAccess calcAccess;

   /**
      This object calculates additional information
      to enhance the visualization.
   */
   private CalculationEnhancer calcEnhancer;

   /**
      Table containing the nodes of the SOM.
   */
   private Table tableNodes;

   /**
      Table containing the edges of the SOM.
   */
   private Table tableEdges;

   /**
      Constructor.
   */
   public CalculationImport () {
      calcAccess = new CalculationAccess();
      calcEnhancer = new CalculationEnhancer();
   }


   /**
      Reads the nodes of the SOM (the map units) 
      with their X and Y value into a new table.
      Names/Type of the columns added: 
      - Constants.kind (enum kind) : Kind = node
      - Constants.nodeKey (int) : ID of the node
      - Constants.nodeXValue (float) : X Value of the node
         (in SOM space)
      - Constants.nodeYValue (float) : Y Value of the node
         (in SOM space)

      @return Table with the nodes
   */
/*   private Table readNodes() {
      nodes = new Table();
      calcAccess.readNodes(nodes);
      return nodes;
   }*/


   /**
      Reads the neighbourhood relations of the SOM
      and converts them into a table of edges.
      Names/Type of the columns added: 
      - Constants.kind (enum kind) : Kind = edge
      - Constants.edgeKey (int) : ID of the edge
      - Constants.edgeSource (int) : ID of the source node 
      - Constants.edgeTarget (int) : ID of the target node 
      
      @return Table with the edges
   */
/*    private Table readEdges() {
      edges = new Table();
      calcAccess.readEdges(edges);
      return edges;
   }*/

   /**
      Gets some information about nodes
      This information means:
      - node weights (see readCodebook)
      - feature vectors associated with a node (see readBMUs)
      
      @return Table containing the nodes and information.
         Null in case of grave error (missing codebook),
         incomplete information in other error cases.
   */
   public Table readSOMNodesBMUs (int calculationID) {
   
      System.out.println("Importing calculation id: " + calculationID);
      calcAccess.setCalcID(calculationID);
      calcEnhancer.setCalcID(calculationID);
      
      tableNodes = null;
   
      try {
         // import nodes
         System.out.println("Importing nodes and codebook...");
         //Table tableNodes = readNodes(); // old
         //calcAccess.readCodebook(tableNodes); // old
         tableNodes = calcAccess.readCodebook();
         System.out.println("... done importing nodes and codebook.");

         // calculate BMUs (Best Matching Units)
         System.out.println("Importing Best Matching Units...");
         try {
            calcEnhancer.calculateBMUs(tableNodes);
            //calcAccess.readBMUs(tableNodes); // old
         } catch (Exception e) {
            System.out.println("Error while importing Best Matching Units.");
            e.printStackTrace();
         }
         System.out.println("... done importing Best Matching Units.");

      } catch (Exception e) {
         System.out.println("There has been an error while reading information about SOM.");
         e.printStackTrace();
         return null;
      }

      return tableNodes;
   }
   

   /**
      Gets all the information about nodes
      and puts it all together into a table.
      All information means:
      - node X/Y coordinates (see readNodes})
      - node weights (see readCodebook)
      - node U-Matrix values (see readUmatrix)
      - feature vectors associated with a node (see readBMUs)
      
      @return Table containing the nodes and all information.
         Null in case of grave error (missing codebook or edges),
         incomplete information in other error cases.
   */
   public Table readSOMNodes (int calculationID) {

      System.out.println("Importing calculation id: " + calculationID);
      calcAccess.setCalcID(calculationID);
      calcEnhancer.setCalcID(calculationID);
      
      tableNodes = null;

      try {
         // import nodes
         System.out.println("Importing nodes and codebook...");
         //Table tableNodes = readNodes(); // old
         //calcAccess.readCodebook(tableNodes); // old
         tableNodes = calcAccess.readCodebook();
         System.out.println("... done importing nodes and codebook.");

         // import edges
         System.out.println("Importing edges...");
         tableEdges = new Table();
         calcAccess.readEdges(tableEdges);
         System.out.println("... done importing edges.");

         // calculate node coordinates
         System.out.println("Calculating node coordinates...");
         try {
            calcEnhancer.calculateNodes(tableNodes, tableEdges);
         } catch (Exception e) {
            System.out.println("Error while calculating node coordinates.");
            e.printStackTrace();
         }
         System.out.println("... done calculating node coordinates.");
         
         // calculate U-Matrix values
         System.out.println("Calculating U-Matrix...");
         try {
            //calcAccess.readUmatrix(tableNodes, tableEdges); // old
            calcEnhancer.calculateUmatrix(tableNodes, tableEdges);
         } catch (Exception e) {
            System.out.println("Error while calculating U-Matrix.");
            e.printStackTrace();
         }
         System.out.println("... done calculating U-Matrix.");

         // calculate BMUs (Best Matching Units)
         System.out.println("Calculating Best Matching Units...");
         try {
            //calcAccess.readBMUs(tableNodes); // old
            calcEnhancer.calculateBMUs(tableNodes);
         } catch (Exception e) {
            System.out.println("Error while calculating Best Matching Units.");
            e.printStackTrace();
         }
         System.out.println("... done calculating Best Matching Units.");

         // calculate proportion of coreferent fvs per node
         System.out.println("Calculate proportions...");
         try {
            calcEnhancer.getProportions(tableNodes);
         } catch (Exception e) {
            System.out.println("Error while calculating proportions.");
            e.printStackTrace();
         }
         System.out.println("... done calculating proportions.");

      } catch (Exception e) {
         System.out.println("There has been an error while reading information about SOM.");
         e.printStackTrace();
         return null;
      }
      
      calcAccess.finalize();

      return tableNodes;
   }


   /**
      Gets all the information about nodes and edges
      and puts it all together into a graph.
      All information means:
      - node X/Y coordinates (see readNodes)
      - node weights (see readCodebook)
      - neighbourhood of nodes (see readEdges)
      - node and edge U-Matrix values (see readUmatrix)
      - feature vectors associated with a node (see readBMUs)
      
      @return Graph of nodes connected to edges as specified
         in the data source, is the empty graph in case of error
   */
   public Graph readGraph (int calculationID) {
     
      // Read all information
      readSOMNodes(calculationID);
      
      // Create additional edges for connectedness-visualization
      System.out.println("Create additional edges...");
      calcEnhancer.calculateConnectedness(tableNodes, tableEdges);
      System.out.println("... edges created.");
      
      // Create Graph
      Graph graph = new Graph();
      try { 
         System.out.println("Create graph...");
         graph = new Graph(tableNodes, tableEdges, 
               false, Constants.nodeKey, Constants.edgeSource, Constants.edgeTarget);
         System.out.println("... graph created.");
   
      } catch (Exception e) {
         System.out.println("There has been an error while creating the graph.");
         System.out.println("Empty graph is returned.");
         e.printStackTrace();
      }
   
      return graph;
   }
   
   
   /**
      Returns a table containing all nodes of a SOM.
      Useful to call only after having called readSOMNodes.
      @return table containing all nodes of the SOM with their information.
   */
   public Table getNodeTable() {
      return tableNodes;
   }
   
   
   /**
      Returns a table containing all edges of a SOM.
      Useful to call only after having called readSOMNodes.
      @return table containing all edges of the SOM with their information.
   */
   public Table getEdgeTable() {
      return tableEdges;
   }

}
