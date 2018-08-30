// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.base.Constants;

import prefuse.data.Graph;
import prefuse.data.Table;


/**
@author kesslewd

Class that reads the result of a calculation with matlab and imports them
into prefuse data structures. Relies on a @link{CalculationAccess} object to
import the data.

*/
public class CalculationImport {


   /**
      This object does the actual loading of the data.
      Depending on whether the data comes from files or
      a db or whatever you imagine.
   */
   private CalculationAccess calcAccess;


   /**
      Constructor.
   */
   public CalculationImport () {
      calcAccess = new CalculationAccess();
   }


   /**
      Method readNodes.
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
   private Table readNodes() {
      Table nodes = new Table();
      calcAccess.readNodes(nodes);
      return nodes;
   }


   /**
      Method readEdges.
      Reads the neighbourhood relations of the SOM
      and converts them into a table of edges.
      Names/Type of the columns added: 
      - Constants.kind (enum kind) : Kind = edge
      - Constants.edgeKey (int) : ID of the edge
      - Constants.edgeSource (int) : ID of the source node 
      - Constants.edgeTarget (int) : ID of the target node 
      
      @return Table with the edges
   */
    private Table readEdges() {
      Table edges = new Table();
      calcAccess.readEdges(edges);
      return edges;
   }


   /**
      Method readAll.
      Gets all the information about nodes and edges
      and puts it all together into a graph.
      All information means:
      - node X/Y coordinates (see @link{readNodes})
      - node weights (see @link{readCodebook})
      - neighbourhood of nodes (see @link{readEdges})
      - node and edge U-Matrix values (see @link{readUmatrix})
      - feature vectors associated with a node (see @link{readBMUs})
      
      @return Graph of nodes connected to edges as specified
         in the data source, is the empty graph in case of error
   */
   public Graph readAll (int calculationID) {

      System.out.println("Importing calculation id: " + calculationID);
      calcAccess.setCalcID(calculationID);

      Graph graph = new Graph();
      try {
         // import nodes
         System.out.println("Importing nodes...");
         Table tableNodes = readNodes();
         System.out.println("... done importing nodes.");

         // import edges
         System.out.println("Importing edges...");
         Table tableEdges = readEdges();
         System.out.println("... done importing edges.");

         // import U-Matrix values
         System.out.println("Importing U-Matrix...");
         try {
            calcAccess.readUmatrix(tableNodes, tableEdges);
         } catch (Exception e) {
            System.out.println("Error while importing U-Matrix.");
            e.printStackTrace();
         }
         System.out.println("... done importing U-Matrix.");

         // import BMUs (Best Matching Units)
         System.out.println("Importing Best Matching Units...");
         try {
            calcAccess.readBMUs(tableNodes);
         } catch (Exception e) {
            System.out.println("Error while importing Best Matching Units.");
            e.printStackTrace();
         }
         System.out.println("... done importing Best Matching Units.");

         // import Codebook
         System.out.println("Importing codebook...");
         try {
            calcAccess.readCodebook(tableNodes);
         } catch (Exception e) {
            System.out.println("Error while importing Codebook.");
            e.printStackTrace();
         }
         System.out.println("... done importing codebook.");

         // calculate proportion of coreferent fvs per node
         System.out.println("Calculate proportions...");
         try {
            calcAccess.getProportions(tableNodes);
         } catch (Exception e) {
            System.out.println("Error while calculing proportions.");
            e.printStackTrace();
         }
         System.out.println("... done calculing proportions.");

         // Create graph 
         System.out.println("Create graph...");
         graph = new Graph(tableNodes, tableEdges, 
               false, Constants.nodeKey, Constants.edgeSource, Constants.edgeTarget);
         System.out.println("... graph created");

      } catch (Exception e) {
         System.out.println("There has been an error while creating the graph.");
         System.out.println("Empty graph is returned.");
         e.printStackTrace();
      }

      return graph;
   }



}
