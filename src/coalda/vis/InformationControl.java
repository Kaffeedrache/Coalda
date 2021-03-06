// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;

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
      if ( item.canGetString(Constants.nodeUmatValueMedian) ) {
         ta.append("U-Matrix Value (median): " + item.getString(Constants.nodeUmatValueMedian) + newline);
      }
      
      // Print labeled fvs
      if ( item.canGetString(Constants.nodeAllLabeled) ) {
         ta.append("All labeled (gold standard): " + item.getString(Constants.nodeAllLabeledGold) + newline);
      }
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeLabelGold + name) ) {
            ta.append("Labeled as " + name + " (gold): " + item.getString(Constants.nodeLabelGold + name) + newline);
         }
      }
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeProportionGold + name) ) {
            ta.append("% " + name + "/total (gold): " + item.getString(Constants.nodeProportionGold + name) + newline);
         }
      }
      if ( item.canGetString(Constants.nodeAllLabeledAssigned) ) {
         ta.append("All labeled (assigned): " + item.getString(Constants.nodeAllLabeledAssigned) + newline);
      }
      
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeLabelAssigned + name) ) {
            ta.append("Labeled as " + name + " (assigned): " + item.getString(Constants.nodeLabelAssigned + name) + newline);
         }
      }
      for (int k=0; k<Constants.possibleLabels.length; k++) {
         String name = Constants.possibleLabels[k];
         if ( item.canGetString(Constants.nodeProportionAssigned + name) ) {
            ta.append("% " + name + "/total (assigned): " + item.getString(Constants.nodeProportionAssigned + name) + newline);
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
      if ( item.canGetString(Constants.edgeConnvis) ) {
         ta.append("Connectedness: " + item.getString(Constants.edgeConnvis) + newline);
      }
   }


}
