// Stefanie Wiltrud Kessler, September 2009 - April 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.Action;
import prefuse.data.Graph;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;


/**
@author kesslewd

Display for displaying a SOM.

*/
public class SOMDisplay extends Display {


   /**
      UID for implementing serializable.
   */
   private static final long serialVersionUID = -7672152142137368491L;

   /**
      Action Group for coloring the SOM.
   */
   protected static String colorGroup = "color";

   /**
      Action Group for re-coloring the SOM.
   */
   protected static String recolorGroup = "recolor";

   /**
      Action Group for changing the shapes of the nodes of the SOM.
   */
   protected static String shapeGroup = "shape";

   /**
      Action Group for the layout of the SOM.
   */
   protected static String layoutGroup = "layout";

   /**
      The current field for the nodelabel.
   */
   private String currentLabelField;

   /**
      The calcluation ID of the display.
   */
   private int calcID;

   /**
      Constructor.
      @param graph The SOM to be shown in this display
      @param labelField The initial field used for nodelabels
   */
   public SOMDisplay  (Graph graph, String labelField, int calculationID) {

      // Create a prefuse display with a new visualization.
      super(new Visualization());

      // Set calculation ID
      this.calcID = calculationID;
      
      // Add the graph to the visualization as the data group "graph"
      m_vis.add("graph", graph);

      // Create a default renderer factory with default
      // shape renderer and edge renderer
      DefaultRendererFactory rf = new DefaultRendererFactory();

      // Label nodes with the current field
      if (labelField == null) {
         currentLabelField = Constants.possibleNodeLabels[0];
      } else {
         currentLabelField = labelField;
      }
      LabelRenderer r = new LabelRenderer(currentLabelField);
      rf.add("INGROUP('graph.nodes')", r);

      // Add the renderer factory to the visualization.
      m_vis.setRendererFactory(rf);

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

        run(colorGroup);
        //run(recolorGroup);
        run(layoutGroup);
        run(shapeGroup);
   }


   /**
      Puts the action into the group recolor
      and runs the action group recolor.
      @param action The action for recoloring.
   */
   public void recolor (Action action) {
      m_vis.putAction(recolorGroup, action);
      run(recolorGroup);
   }


   /**
      Puts the action into the group layout
      and runs the action group layout.
      @param action The action for layouting.
   */
   public void relayout (Action action) {
      m_vis.putAction(layoutGroup, action);
      run(layoutGroup);
   }


   /**
      Relabels all nodes according to the selected field.
      @param field The field to be used for labeling.
   */
   public void relabel (String field) {

      // Create a default renderer factory with default
      // shape renderer and edge renderer
      DefaultRendererFactory rf = new DefaultRendererFactory();

      // Set label for nodes according to selected field
      LabelRenderer r = new LabelRenderer(field);
      rf.add("INGROUP('graph.nodes')", r);

      // Add renderer to visualization.
      m_vis.setRendererFactory(rf);
   }

   /**
      Get calculation ID of the calculation that is shown in this display.
      @returns The ID of the calculation shown in this display.
   */
   public int getCalculationID() {
      return calcID;
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
