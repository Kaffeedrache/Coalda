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


/**

Class that reads the result of a calculation with matlab and imports them
into prefuse data structures.

@author kesslewd
*/
public class CalculationAccess  {


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
   public CalculationAccess () {
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
      @deprecated
      Reads the nodes of the SOM (the map units) 
      with their X and Y value into an existing
      table (nodes are appended at the end).
      Names/Type of the columns added: 
      - Constants.kind (enum kind) : Kind = node
      - Constants.nodeKey (int) : ID of the node
      - Constants.nodeXValue (float) : X Value of the node
         (in SOM space)
      - Constants.nodeYValue (float) : Y Value of the node
         (in SOM space)
      
      @param nodes Table where the nodes are added
      
   */
   public void readNodes(Table nodes) {

      // Add the columns for the table of nodes
      // 'kind' is always 'node'
      // number of feature vectors is 0 (for the moment)
      nodes.addColumn(Constants.kind, Constants.itemKind.class, Constants.itemKind.node);
      nodes.addColumn(Constants.nodeKey, int.class);
      nodes.addColumn(Constants.nodeXValue, float.class);
      nodes.addColumn(Constants.nodeYValue, float.class);
      nodes.addColumn(Constants.nodeFVNumber, int.class, new Integer(0));

      // Add dummy node with ID 0 
      // because prefuse starts at ID 0, but we at ID 1
      // and prefuse node IDs cannot jump a value
      nodes.addRow(); // for 0

      // node ID
      int element = 1;

      reader.readMatrix(MatrixInfo.nodes);

      while (reader.hasNextRow()) {

         String row = reader.nextRow();

         if (debug) {
            System.out.println("lineno " + element + " : " + row);
         }

         // Add values to new table row
         nodes.addRow();
         nodes.set(element, Constants.nodeKey, Integer.valueOf(element));
         float x = Float.valueOf(reader.nextValue()).floatValue();    
         nodes.set(element, Constants.nodeXValue, new Float(x));
         float y = Float.valueOf(reader.nextValue()).floatValue() * (-1);
         nodes.set(element, Constants.nodeYValue, new Float(y));

         // Check if values are correct, print error if not (also if not in debug mode)
         if (debug) {
            System.out.println("ID " + element + " : " 
                  + nodes.getFloat(element, Constants.nodeXValue) + "(" +  x + ")" 
                  + " - " + nodes.getFloat(element, Constants.nodeYValue) + "(" +  y + ")");
         }
         if ((x != nodes.getFloat(element, Constants.nodeXValue)) || (y != nodes.getFloat(element, Constants.nodeYValue))) {
            System.out.println("!! ERROR in node " + element + " : " 
                  + nodes.getFloat(element, Constants.nodeXValue) + "(" +  x + ")" 
                  + " - " + nodes.getFloat(element, Constants.nodeYValue) + "(" +  y + ")");
         }

         element++;

      }

       // Remove dummy node with ID 0
      nodes.removeRow(0);

   }


   /**
      Reads the neighbourhood relations of the SOM
      and converts them into a table of edges. Edges
      are appended to an existing table.
      Names/Type of the columns added: 
      - Constants.kind (enum kind) : Kind = edge
      - Constants.edgeKey (int) : ID of the edge
      - Constants.edgeSource (int) : ID of the source node 
      - Constants.edgeTarget (int) : ID of the target node 
      
      @param edges Table with the edges
   */
   public void readEdges(Table edges){

      // Add the fields for the table of edges
      // 'kind' is always 'edge'
      edges.addColumn(Constants.kind, Constants.itemKind.class, Constants.itemKind.edge);
      edges.addColumn(Constants.edgeKey, int.class);
      edges.addColumn(Constants.edgeSource, int.class);
      edges.addColumn(Constants.edgeTarget, int.class);

      reader.readMatrix(MatrixInfo.edges);

      int lineno = 1;
      int element = 0;

      while (reader.hasNextRow()) {

         String row = reader.nextRow();

         if (debug) {
            System.out.println("lineno" + lineno + " : " + row);
         }

         int i = 0;

         while (reader.hasNextValue()) {

            String value = reader.nextValue();

            // Matrix value 1 for M[i,j] means that i is connected to j 
            if (Integer.parseInt(value) == 1) {
               
               // Add values to new table row
               edges.addRow();
               edges.set(element, Constants.edgeKey, Integer.valueOf(element));
               edges.set(element, Constants.edgeSource, Integer.valueOf(lineno));
               int target = i+1;
               edges.set(element, Constants.edgeTarget, Integer.valueOf(target));    

               // Check if values are correct, print error if not (also if not in debug mode)
               if (debug) {
                  System.out.println("edge ID " + element + " : " 
                        + edges.getInt(element, Constants.edgeSource) + "(" +  lineno + ")" 
                        + " - " + edges.getInt(element, Constants.edgeTarget) + "(" +  target + ")");
               }
               if ((lineno != edges.getInt(element, Constants.edgeSource)) || (target != edges.getInt(element, Constants.edgeTarget))) {
                  System.out.println("!! ERROR in node " + element + " : " 
                        + edges.getInt(element, Constants.edgeSource) + "(" +  lineno + ")" 
                        + " - " + edges.getInt(element, Constants.edgeTarget) + "(" +  target + ")");
               }

               element++;
            }

               i++;

         }

         lineno++;

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
   public void readBMUs(Table nodes) {
      // TODO sort FVs?

      // Add the fields for the table of nodes
      nodes.addColumn(Constants.nodeFVectors, String.class);

      reader.readVector(VectorInfo.bmus);

      Reader fvReader = Utils.makeReader();

      fvReader.setCalcID(calcID); 
      fvReader.readVector(VectorInfo.fvids);

      // ID of current feature vector
      int fvid = 0;
      boolean haveFVs = false;
      if (fvReader.numberOfValues() > 0) {
         haveFVs = true;
         if (debug) {
            System.out.println("We have fv ids");
         }
      }

      while (reader.hasNextValue()) {

         if (haveFVs) {
            fvid = Integer.parseInt(fvReader.nextValue());
         } else {
            fvid++;
         }

         String row = reader.nextValue();

         if (debug) {
            System.out.println("lineno" + fvid + " : " + row);
         }

         // Add current feature vector to FV-List of the node
         // (map unit) that it has as a BMU
         int mu = Integer.parseInt(row);
         String muVectors = nodes.getString(mu, Constants.nodeFVectors);
         int muFVNumber;
         if (muVectors != null) { // Node has associated feature vectors
            muVectors = muVectors + " " + fvid;
            muFVNumber = nodes.getInt(mu,Constants.nodeFVNumber) + 1;
         } else { // Node has no feature vectors yet 
            muVectors = "" + fvid;
            muFVNumber = 1;
         }
         nodes.set(mu, Constants.nodeFVectors, muVectors);
         nodes.set(mu, Constants.nodeFVNumber, Integer.valueOf(muFVNumber));

         if (debug) {
            System.out.println("lineno" + fvid 
                  + " - MU " + nodes.getString(mu, Constants.nodeKey)
                  + " - FVs (" + nodes.getInt(mu, Constants.nodeFVNumber) + ") " 
                  + nodes.getString(mu, Constants.nodeFVectors));
         }

      }

      // We won't need this reader anymore
      // close db connection or file
      fvReader.finalize();

   }

   /**
      Reads the weights of every map unit of the SOM 
      in every dimension of the feature vector space.
      Names/Type of the columns added: 
      - Constants.nodeWeightI (float) : weight of this
         map unit in dimension I (where I ranges from
         1 to number of dimensions) 
         
      @returns Table with the nodes
    */
   public Table readCodebook(){
      Table nodes = new Table();
      readCodebook(nodes, true);
      return nodes;
   }


   /**
      Reads the weights of every map unit of the SOM 
      in every dimension of the feature vector space.
      Names/Type of the columns added: 
      - Constants.nodeWeightI (float) : weight of this
         map unit in dimension I (where I ranges from
         1 to number of dimensions) 
         
      @param nodes Table with the nodes
    */
   public void readCodebook(Table nodes){
      readCodebook(nodes, false);
   }
   
   
   /**
      Reads the weights of every map unit of the SOM 
      in every dimension of the feature vector space.
      Names/Type of the columns added: 
      - Constants.nodeWeightI (float) : weight of this
         map unit in dimension I (where I ranges from
         1 to number of dimensions) 
         
      @param nodes Table with the nodes
      @param create Create nodes or take existing nodes
    */
   public void readCodebook(Table nodes, boolean create){

      if (create) {
            
         // Add the columns for the table of nodes
         // 'kind' is always 'node'
         // number of feature vectors is 0 (for the moment)
         nodes.addColumn(Constants.kind, Constants.itemKind.class, Constants.itemKind.node);
         nodes.addColumn(Constants.nodeKey, int.class);
         nodes.addColumn(Constants.nodeFVNumber, int.class, new Integer(0));

         // Add dummy node with ID 0 
         // because prefuse starts at ID 0, but we at ID 1
         // and prefuse node IDs cannot jump a value
         nodes.addRow(); // for 0

      }
      
      // Get codebook values
      reader.readMatrix(MatrixInfo.codebook);

      // Add weight fields for number of features.
      // Get first row and determine number of features,
      // add so many columns to the nodes table
      String row = reader.nextRow();
      boolean first = true;
        for (int i=1; i<=reader.numberOfValues(); i++){
         nodes.addColumn(Constants.nodeWeight + i, float.class);
      }

      // ID of map unit/node
      int lineno = 1;

      // Read in values row by row
      while (reader.hasNextRow()) {

         if (first) {
            // We already did read the first line, to calculate
            // the number of fields we have to add.
            first = false;
         } else {
            row = reader.nextRow();
         }

         if (debug) {
            System.out.println("lineno " + lineno + " : " + row);
         }
         
         if (create) {
            // Add new node to the table
            nodes.addRow();
            nodes.set(lineno, Constants.nodeKey, Integer.valueOf(lineno));
         }

         // Add all features
         int i = 0;
         while (reader.hasNextValue()) {
            Float f = Float.valueOf(reader.nextValue());
            int element = i+1;
            nodes.set(lineno, Constants.nodeWeight + element, f);
            i++;
         }

         if (debug) {
            System.out.print("lineno " + lineno + ": ");
            for ( int i1=1; i1<=reader.numberOfValues(); i1++ ) {
               System.out.print(" - Weight" + i1 + "=" + nodes.getString(lineno, Constants.nodeWeight + i1));
            }
            System.out.println();
         }

          lineno++;

      }
      
      if (create) {
         // Remove dummy node with ID 0
         nodes.removeRow(0);
      }
   }


   /**
      @deprecated
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
   public void readUmatrix(Table nodes, Table edges) {

      // TODO Different shapes of map
      
      // Add the field for the table of nodes
      nodes.addColumn(Constants.nodeUmatValue, float.class);

      // Add the field for the table of edges
      edges.addColumn(Constants.edgeUmatValue, float.class);

      reader.readMatrix(MatrixInfo.umatrix);

      // Numeration goes column-wise, so first row first column is
      // node 1, third row first column is node 2, etc.
      // Out of the first line we get the number of nodes in one row
      // #nodesRow= ceil(cols.length/2)
      // The number of nodes in one column, 'distance' = #nodes / #nodesRow 
      String row = reader.nextRow();
      TupleSet ts = nodes;
      int numberNodes = DataLib.max(ts, Constants.nodeKey).getInt(Constants.nodeKey);
      int numberRow = (int)java.lang.Math.ceil( (double)reader.numberOfValues() /2);
      int distance = numberNodes / numberRow;
      if (debug) {
         System.out.println("Total number of nodes " + numberNodes);
         System.out.println("Number of nodes in a row " + numberRow);
         System.out.println("= Number of nodes in a column " + distance);
      }

      boolean first = true;
      int lineno = 1;

      // Read in values row by row
      // we are starting with row 1 (lineno) and element 1 (i)
      while (reader.hasNextRow()) {

         // Take current row
         if (first) {
            // We already did read the first line, to calculate
            // the number of fields we have to add.
            first = false;
         } else {
            row = reader.nextRow();
         }

         if (debug) {
            System.out.println("lineno" + lineno + " : " + row);
         }

         // Calculate node number for the first node in the row
         int nodeno = (int)java.lang.Math.ceil((double)lineno/2);

         int i = 0;
         while (reader.hasNextValue()) {
            i++;

            Float f = Float.valueOf(reader.nextValue());

            // Odd row and odd element 
            //      -> node value of node[nodeno]
            if ((lineno % 2 == 1) && (i % 2 == 1)) { 

               nodes.set(nodeno, Constants.nodeUmatValue, f);

               if (debug) {
                  System.out.println("node " + nodeno
                        + " | Value: " +  nodes.getFloat(nodeno, Constants.nodeUmatValue));
               }

               nodeno = nodeno + distance;

               // Now continue with the next element in this row, 
               // because all that follows is for edges
               continue;
            }

            // For each edge calculate start node (node1) and target node (node2).
            // This is a lot of voodoo...
            int node1 = 0;
            int node2 = 0;
            int edgeno = 0;

            // Odd row and even element
            //     -> edge value between node[nodeno-distance] and node[nodeno]
            if ((lineno % 2 == 1) && (i % 2 == 0)) {

               node1 = nodeno-distance;
               node2 = nodeno;

               // Every node has 3 edges, we want the second of them
               // minus all those at the margin who have only one edge
               edgeno = (node1)*3-2 - 2*(int)java.lang.Math.floor((double)node1/distance);
               if (java.lang.Math.ceil((double)lineno/2) == distance)  {
                  edgeno++;
               }

            }

            // Even row and odd element
            //     -> edge value between node[nodeno] node[nodeno+1]
            if ((lineno % 2 == 0) && (i % 2 == 1)) {

               node1 = nodeno;
               node2 = nodeno+1;

               // Every node has 3 edges, we want the first of them
               // minus all those at the margin who have only one edge
               edgeno = (node1)*3-3 - 2*(int)java.lang.Math.floor((double)node1/distance);

               // Due to matlab strange saving not 3 edges for every node but
               // 2 or 4 we have to subtract one edge for nodes in every second row
               // (that is in total every forth row), except for the nodes at the
               // right border -> see next if
               if ((i != reader.numberOfValues()) && (lineno % 4 == 0)) {
                  edgeno--;
               }
               // nodes at the right border are strange
               if (i == reader.numberOfValues()) {
                  edgeno = edgeno - lineno + 2;
               }

               // Go to the next node for the next element
               nodeno = nodeno + distance;
            }

            // Even row and even element
            //    first -> edge value between node[nodeno+1] and node[nodeno+offset]
            //    second -> edge value between node[nodeno] and node[nodeno+1+offset]
            if ((lineno % 2 == 0) && (i % 2 == 0)) {

               if (lineno % 4 == 2) {
                  node1 = nodeno-distance+1;
                  node2 = nodeno;
                  edgeno = (node1)*3-3 - 2*(int)java.lang.Math.floor((double)node1/distance);
                  if (java.lang.Math.ceil((double)lineno/2)+1 == distance)  {
                     edgeno++;
                  }
               }

               if (lineno % 4 == 0) {
                  // where source = nodeno-distance and target = nodeno+1
                  node1 = nodeno-distance;
                  node2 = nodeno+1;
                  edgeno = (node1)*3-1 - 2*(int)java.lang.Math.floor((double)node1/distance);
               }

            }

            // Set value for the calculated edge
            edges.set(edgeno, Constants.edgeUmatValue, f);

            // Check if values are correct, print error if not (also if not in debug mode)
            if (debug) {
               System.out.println("edge " + edgeno + " : " 
                     + edges.getInt(edgeno, Constants.edgeSource) + "(" + node1 + ")"
                     + " - " + edges.getInt(edgeno, Constants.edgeTarget) + "(" + node2 + ")"
                     + " | Value: " + edges.getFloat(edgeno, Constants.edgeUmatValue));
            }
            if ((node1 != edges.getInt(edgeno, Constants.edgeSource)) || (node2 != edges.getInt(edgeno, Constants.edgeTarget))) {
               System.out.println("!!! ERROR in Umatvalue edge from " 
                     + edges.getInt(edgeno, Constants.edgeSource) + "(" + node1 
                     + ") to " + edges.getInt(edgeno, Constants.edgeTarget) + "(" + node2 + ")");
            }

         } // for elements

         lineno++;

      } // for rows
   }


   /**
      Gets next calculation ID.
      If we are working on a database, this is the
      next value of the sequence.
      If we are working on files, 0 is returned.

      @return calculationID ID of calculation.
    */
   public int getNextCalcID () {
      Reader db = Utils.makeReader();
      calcID = Integer.valueOf(db.readOneLine(SingleInfo.nextCalcID)).intValue();
      return calcID;
   }

   /**
      Cleanup.
   */
   public void finalize() {
      reader.finalize();
   }
   
   
}
