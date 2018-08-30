// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.ui;


import java.util.Iterator;

import coalda.base.Constants;
import coalda.vis.ActionProvider;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.action.assignment.DataColorAction;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.render.RendererFactory;
import prefuse.visual.VisualItem;


/**

Display for displaying a SOM.

@author kesslewd
*/
public class SOMDisplay extends Display {


   /**
      UID for implementing serializable.
   */
   private static final long serialVersionUID = -7672152142137368491L;

   /**
      Action Group containing all actions that 
      never change by user interaction.
   */
   protected static String staticActionGroup = "color";

   /**
      Action Group containing actions for re-coloring the SOM.
   */
   private static String recolorGroup = "recolor";

   /**
      Action used for re-coloring the SOM.
   */
   private DataColorAction recolorAction;

   /**
      Action Group containing actions for the layout of the SOM.
   */
   protected static String layoutGroup = "layout";

   /**
      The calcluation ID of the calculation displayed in this Display.
   */
   private int calcID;
   
   /**
      The current field for the nodelabel.
   */
   private String currentLabelField;

   /**
      The current field for coloring the nodes.
   */
   private String currentColorField;

   /**
      The current layout of the graph.
   */
   private String currentLayout;
   
   
   /**
      Constructor.
      For the labelField the first entry in Constants.possibleNodeLabels is used.
      For the colorField the first entry in Constants.possibleNodeColorings is used.
      For the layout the first entry in Constants.possibleLayouts is used.
      @param graph The SOM to be shown in this display
      @param calculationID The ID of the calculation that is displayed
   */
   public SOMDisplay  (Graph graph, int calculationID) {
      this(graph, null, null, null, calculationID);
   }

   
   /**
      Constructor.
      @param graph The SOM to be shown in this display
      @param labelField The initial field used for nodelabels
      @param colorField The initial field used for coloring the nodes
      @param layout The initial layout of the graph
      @param calculationID The ID of the calculation that is displayed
   */
   public SOMDisplay  (Graph graph, String labelField, String colorField, String layoutMethod, int calculationID) {

      // Create a prefuse display with a new visualization.
      super(new Visualization());

      // Set calculation ID
      calcID = calculationID;
      
      // Set current fields
      // if they are null, set them to default
      if (labelField == null) {
         currentLabelField = Constants.possibleNodeLabels[0];
      } else {
         currentLabelField = labelField;
      }
      if (colorField == null) {
         currentColorField = Constants.possibleNodeColorings[0];
      } else {
         currentColorField = colorField;
      }
      if (layoutMethod == null) {
         currentLayout = Constants.possibleLayouts[0];
      } else {
         currentLayout = layoutMethod;
      }
      
      // Add the graph to the visualization as the data group "graph"
      m_vis.add("graph", graph);
      
   }


   /**
      Adds an action list to the visualization in the given group.
      @param group The group the action is to be added.
      @param action The action to be added.
   */
   public void addActionList (String group, Action action) {
      m_vis.putAction(group, action);
   }

   
   /**
      Adds the action to the visualization in the group recolor.
      @param group The group the action is to be added.
      @param action The action to be added.
   */
   public void addRecolorAction (DataColorAction action) {
      recolorAction = action;
      m_vis.putAction(recolorGroup, action);
   }
   

   /**
      Runs the specified action group.
      @param actionGroup The action group to be run.
   */
   public void run (String actionGroup) {
      try {
         m_vis.run(actionGroup);
      } catch (Exception e) {
         System.out.println("There was an error while running action group " + actionGroup);
         e.printStackTrace();
      }
   }


   /**
      Runs all action groups.
   */
   public void runAll () {
        run(staticActionGroup);
        run(recolorGroup);
        run(layoutGroup);
        repaint();
   }


   /**
      Changes the field for coloring the nodes of a map.
      @param field The field used for recoloring.
   */
   public void recolor (String field) {
      
      // Get field to recolor
      String myfield = field;
      if (myfield == null) {
         myfield = currentColorField;
      } else {
         currentColorField = field;
      }
      // TODO check if field exists
      
      // Set field
      recolorAction.setDataField(myfield);
      
      // Run action and repaint to make changes visible
      run(recolorGroup);
      repaint();
   }

   
   /**
      Puts the action corresponding to the layoutMethod
      into the group layout and runs the action group layout.
      @param layoutMethod The layout to be used.
   */
   public void relayout (String layoutMethod) {
      
      // Get field to recolor
      String myfield = layoutMethod;
      if (myfield == null) {
         myfield = currentLayout;
      } else {
         currentLayout = layoutMethod;
      }
      // TODO check if methd exists
      
      // Check if additional edges should be visible
      // and set them (in)visible if necessary
      boolean visibility = (layoutMethod.equals(Constants.layoutConnVis));
      setAdditionalEdgesVisibility(visibility);

      // Select layout, run layout and repaint
      m_vis.putAction(layoutGroup, ActionProvider.getLayout(layoutMethod));
      run(layoutGroup);
      repaint();
   }


   /**
      Relabels all nodes according to the selected field.
      @param field The field to be used for labeling.
   */
   public void relabel (String field) {

      // Get field to relabel
      String myfield = field;
      if (myfield == null) {
         myfield = currentLabelField;
      } else {
         currentLabelField = field;
      }
      // TODO check if field exists
      
      // Create a default renderer factory with default
      // shape renderer and edge renderer
      RendererFactory rf = ActionProvider.getLabelRenderer(myfield);

      // Add renderer to visualization.
      m_vis.setRendererFactory(rf);
      
      // Repaint to make changes visible
      repaint();
   }

   
   /**
      Get calculation ID of the calculation that is shown in this display.
      @returns The ID of the calculation shown in this display.
   */
   public int getCalculationID() {
      return calcID;
   }
   

   /**
      Performs some initialization necessary for the layouts.
      Sets the additional edges visibility to false.
      Sets initial coordinates for force-directed layout.
      @param visible If the additional edges are visible or not.
   */
   public void initLayout() {
      
      // Hide additional edges of ConnVis layout
      setAdditionalEdgesVisibility(false);

      // Set initial coordinates for force-directed layout
      Visualization vis = getVisualization();
      Iterator iter = vis.visibleItems("graph.nodes");
      while ( iter.hasNext() ) {
          VisualItem item = (VisualItem)iter.next();
          double x = item.getFloat(Constants.nodeXValue);
          double y = item.getFloat(Constants.nodeYValue);
          item.setEndX(x);
          item.setEndY(y);
      }

   }
   
   
   /**
      Sets the additional edges visibility according to parameter.
      @param visible If the additional edges are visible or not.
   */
   private void setAdditionalEdgesVisibility (boolean visible) {
      
      TupleSet allEdges = m_vis.getGroup("graph.edges");
      
      Iterator<VisualItem> it = allEdges.tuples();
      
      while ( it.hasNext() ) {
         VisualItem item = (VisualItem)it.next();
         if (item.getString(Constants.kind).trim().equals(Constants.itemKind.edge2.toString())) {
            item.setVisible(visible);
         }
     }
      
   }
   
   
   /**
      Deletes the display.
   */
   protected void delete() {
      try {
         this.finalize();
      } catch (Throwable e) {
         // Ignore errors.
      }
   }


}
