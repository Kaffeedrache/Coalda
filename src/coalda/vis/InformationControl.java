// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;


import java.util.Vector;

import coalda.base.Constants;
import coalda.data.FVImport;

import prefuse.controls.FocusControl;
import prefuse.visual.VisualItem;

import javax.swing.JTextArea;

import de.unistuttgart.ais.sukre.refinery.textvis.model.TextModel;
import de.unistuttgart.ais.sukre.refinery.textvis.ui.TextModelComponent;


/**

Dispays information about a node, the associated feature vectors
and the text, when a user clicks on a node.

@author kesslewd
*/
public class InformationControl extends FocusControl {


   /**
      Text Area where the information about the node is displayed.
   */
   private JTextArea ta;

   /**
      TMC where the feature vectors and the text is displayed.
   */
   private TextModelComponent textVisualization;

   /**
      Character used for line break in Text Area.
   */
   private final static String newline = "\n";


   /**
      Constructor with number of clicks and places to display information.
      @param clicks Number of Clicks to trigger action.
      @param nodeInformationTA TextArea where the information about the clicked node is dispayed.
      @param textVisualizationComponent Component for displaying feature vectors and associated text.
   */
   public InformationControl (int clicks, JTextArea nodeInformationTA, TextModelComponent textVisualizationComponent) {
      super(clicks);
      ta = nodeInformationTA;
      textVisualization = textVisualizationComponent;
   }


   /**
      Called when a user clicks on an item.
      @param item The visual Item the user has clicked on.
      @param e Mouse event that triggered the call.
   */
   public void itemClicked(VisualItem item, java.awt.event.MouseEvent e) {

      // Check for type of visualItem
      if (item.canGetString(Constants.kind)) {
         
         // Node
         if (item.getString(Constants.kind) == Constants.itemKind.node.toString()) {
            nodeClickedTa1(item);
            nodeClickedTextVis(item);
         
         // Edge
         } else if (item.getString(Constants.kind) == Constants.edgeKind) {
            edgeClickedTa1(item);
         }
      }

      // Scroll to the top of text area
      ta.setCaretPosition(0);

      // Just do whatever super does...
      super.itemClicked (item,e);
   }


   /**
      Called when a user clicks on a node, modifies text area.
      Displays information about the node.
      @param item The visual Item (node) the user has clicked on.
   */
   private void nodeClickedTa1 (VisualItem item) {
      
      // Delete previous content
      ta.setText("");
      
      // Print information of node fields
      if ( item.canGetString(Constants.nodeKey) ) {
         ta.append("MU number: " + item.getString(Constants.nodeKey) + newline);
      }
      if ( item.canGetString(Constants.nodeXValue) ) {
         ta.append("X Value: " + item.getString(Constants.nodeXValue) + newline);
      }
      if ( item.canGetString(Constants.nodeYValue) ) {
         ta.append("Y Value: " + item.getString(Constants.nodeYValue) + newline);
      }
      if ( item.canGetString(Constants.nodeFVNumber) ) {
         ta.append("FV number: " + item.getString(Constants.nodeFVNumber) + newline);
      }
      if ( item.canGetString(Constants.nodeUmatValue) ) {
         ta.append("U-Matrix Value: " + item.getString(Constants.nodeUmatValue) + newline);
      }
      
      // Print labeled fvs
      if ( item.canGetString(Constants.nodeCoDisLabel) ) {
         ta.append("All labeled: " + item.getString(Constants.nodeCoDisLabel) + newline);
      }
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeLabel + name) ) {
            ta.append("Labeled as " + name + ": " + item.getString(Constants.nodeLabel + name) + newline);
         }
      }
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeLabel + name) ) {
            ta.append("% " + name + "/total: " + item.getString(Constants.nodeProportion + name) + newline);
         }
      }
      
      // Print weights of nodes
      int i=1;
      while ( item.canGetString(Constants.nodeWeight + i) ) {
         ta.append(Constants.nodeWeight + " " + i + ": " + item.getString(Constants.nodeWeight+i) + newline);
         i++;
      }
      
      // Print associated feature vectors
      if ( item.canGetString(Constants.nodeFVectors) ) {
         String fvs = item.getString(Constants.nodeFVectors);
         ta.append("Assigned Feature Vectors: " + fvs + newline);
      }
      
// TEST
/*      // Print associated feature vectors
      //System.out.println("Here comes the test");
      if ( item.canGetString(Constants.nodeFVectors) ) {
         String fvs = item.getString(Constants.nodeFVectors);
         //System.out.println("vectors: " + fvs);
         FVImport fvimport = new FVImport();
         Vector<String> bla = fvimport.getFVsOfMU(fvs);
         //System.out.println("Features: " + bla.size());
                for (int j=0; j<bla.size(); j++) {
                      ta.append(bla.get(j) + newline);
                    //  System.out.println(bla.get(j));
             }
                            // Print text
      // System.out.println("Now try the text");
       Vector<String> bla2 = fvimport.getTextOfMU(fvs);
      // System.out.println("FeatureVs: " + bla2.size());
                 for ( int k=0; k<bla2.size(); k++ ) {
                 
                    ta.append(bla2.get(k) + newline);
              //      System.out.println(bla2.get(k));
           }
               
      }*/
// TEST END
      
   }


   /**
      Called when a user clicks on a node, modifies text component.
      Displays feature vectors and text.
      @param item The visual Item (node) the user has clicked on.
   */
   private void nodeClickedTextVis (VisualItem item) {
      if ( item.canGetString(Constants.nodeFVectors) ) {
         // Get associated feature vectors, if node has any
         String fvs = item.getString(Constants.nodeFVectors);
         if (fvs != null) {
            // Create text model and send it to visualization
            TextModel tm = new TextModel(fvs);
            textVisualization.setTextModel(tm);
         }
      }
   }


   /**
      Called when a user clicks on an edge, modifies text area.
      Displays information about the edge.
      @param item The visual Item (edge) the user has clicked on.
   */
   private void edgeClickedTa1 (VisualItem item) {
      
      // Delete previous content
      ta.setText("");
      
      // Print information of edge fields
      if ( item.canGetString(Constants.edgeKey) ) {
         ta.append("Edge number: " + item.getString(Constants.edgeKey) + newline);
      }
      if ( item.canGetString(Constants.edgeSource) ) {
         ta.append("Edge Source: " + item.getString(Constants.edgeSource) + newline);
      }
      if ( item.canGetString(Constants.edgeTarget) ) {
         ta.append("Edge Target: " + item.getString(Constants.edgeTarget) + newline);
      }
      if ( item.canGetString(Constants.edgeUmatValue) ) {
         ta.append("U-Matrix Value: " + item.getString(Constants.edgeUmatValue) + newline);
      }
   }


}
