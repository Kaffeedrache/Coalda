// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;
import coalda.base.VisualSettings;
import prefuse.action.Action;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.layout.AxisLayout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.data.expression.parser.ExpressionParser;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.render.RendererFactory;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;


/**

Provides actions for the layout, colors, labels of the graph.

@author kesslewd
*/
public class ActionProvider {

   /**
      Action list containing the "static" actions,
      those actions that never change.
      These are: text color, sizes, node shape.
   */
   private static ActionList staticActions;


   /**
      Action list containing all necessary actions
      for the force-directed layout.
      These are: UmatLayout, Edge color umatrix value, repaint.
   */
   private static ActionList layoutForce;
   
   /**
      Action list containing all necessary actions
      for the grid-layout.
      These are: X-axis, Y-axis, edge color umatrix value.
   */
   private static ActionList layoutGrid;
   
   /**
      Action list containing all necessary actions
      for the convis-layout.
      These are: X-axis, Y-axis, edge color connvis value.
   */
   private static ActionList layoutConnVis;
   
   /**
      Action for recoloring the nodes.
   */
   private static DataColorActionPaletteChange recolorAction;


   /**
      Returns the action list containing the "static" actions,
      those actions that never change.
      These are: text color, sizes, node shape.
      There is only one instance of this list globally.
      
      @return List containing all actions that can not be
         changed by user interaction.
   */
   public static ActionList getStaticActions () {
      
      // The first time the method gets called, create all actions 
      if (staticActions == null) {
         
         staticActions = new ActionList();

         // Node text color
         ColorAction text = new ColorAction("graph.nodes",
               VisualItem.TEXTCOLOR, VisualSettings.nodeTextColor);
         staticActions.add(text);
         
         // Node shape
         ShapeAction nodeShape = new ShapeAction 
               ("graph.nodes", prefuse.Constants.SHAPE_HEXAGON );
         staticActions.add(nodeShape);
         
         // Node size
         DataSizeAction nodeSize = new DataSizeAction 
               ("graph.nodes", Constants.nodeFVNumber );
         nodeSize.setIs2DArea(false); // 1D data field
         nodeSize.setMinimumSize(VisualSettings.nodeMinimumSize); // minimum node size = 1
         nodeSize.setMaximumSize(VisualSettings.nodeMaximumSize); // maximum node size = 4
         nodeSize.setScale(1); // logarithmic scale [looks better]
         staticActions.add(nodeSize);

         // Edge size
         //SizeAction edgeThickness = new SizeAction ("graph.edges", 2);
         DataSizeAction edgeThickness = new DataSizeAction ("graph.edges", Constants.edgeUmatValue);
         edgeThickness.setIs2DArea(false); // 1D data field
         edgeThickness.setMinimumSize(VisualSettings.edgeMinimumSize); // minimum thickness = 1
         edgeThickness.setMaximumSize(VisualSettings.edgeMaximumSize); // maximum thickness = 8
         edgeThickness.setScale(0); // linear scale
         staticActions.add(edgeThickness);

      }
      
      return staticActions;
      
   }
   
   
   /**
      Returns a color action for the nodes.
      The action colors all nodes according to the given field.
      The palette VisualSettings.palette is used.
      A highlight-coloring is added for selected nodes
      using the VisualSettings.highlight color.
      There is only one instance of this action. TODO check if this is desired
      
      @return One recolor action.
   */
   public static DataColorAction getRecolorAction (String colorField) {
      if (recolorAction == null) {
         recolorAction = new DataColorActionPaletteChange("graph.nodes", colorField,
               prefuse.Constants.NUMERICAL, VisualItem.FILLCOLOR, VisualSettings.palette);
         recolorAction.add(VisualItem.HIGHLIGHT, VisualSettings.highlightColor);
      }
      return recolorAction;
   }
   
   
   /**
      Returns a label renderer for the nodes.
      The renderer labels all nodes according to the given field.
      If the field is null, no label renderer is added, but the
      default shape renderer will be used.
      A new instance is created every time this method is called.
      
      @return RendererFactory containing a label renderer.
   */
   public static RendererFactory getLabelRenderer (String labelField) {

      // Create a default renderer factory with default
      // shape renderer and edge renderer
      DefaultRendererFactory rf = new DefaultRendererFactory();

      if (labelField != null && !labelField.equals(Constants.none)) {
         // Set label for nodes according to selected field
         LabelRenderer r = new LabelRenderer(labelField);
         rf.add("INGROUP('graph.nodes')", r);
      }
      
      return rf;
   }
   
   
   /**
      Creates the action lists for the different layouts.
      Called by getLayout the first time this is called.
   */
   private static void createLayouts () {

      // --- Edge colors ---
      
      // Color edges according to connvis Constants.edgeConnvis;
      DataColorAction edgesConnVis = new ZeroDataColorAction("graph.edges", Constants.edgeConnvis,
            prefuse.Constants.LOG_SCALE, VisualItem.STROKECOLOR, VisualSettings.paletteConnVis, VisualSettings.zeroColor);

      // Color edges according to umatrix value Constants.edgeUmatValue;
      DataColorActionPaletteChange edgesUmat = new DataColorActionPaletteChange("graph.edges", Constants.edgeUmatValue,
            prefuse.Constants.NUMERICAL, VisualItem.STROKECOLOR, VisualSettings.palette);
     

      // --- Layout action lists ---
      
      // Force-directed layout
      // Spring lenght is based on U-matrix value
      layoutForce = new ActionList();
      ForceDirectedLayout fdl = new UmatLayout("graph");
      layoutForce.add(fdl);
      layoutForce.add(edgesUmat);
      layoutForce.add(new RepaintAction());
      
      // Grid layout
      // Grid ordering according to X/Y-values of nodes
      layoutGrid = new ActionList();
      AxisLayout x_axis = new AxisLayout("graph.nodes", Constants.nodeXValue, 
            prefuse.Constants.X_AXIS, VisiblePredicate.TRUE);
      AxisLayout y_axis = new AxisLayout("graph.nodes", Constants.nodeYValue, 
            prefuse.Constants.Y_AXIS, VisiblePredicate.TRUE);
      layoutGrid.add(x_axis);
      layoutGrid.add(y_axis);
      layoutGrid.add(edgesUmat);
      
      // ConnVis layout
      // Grid with new edges according to ConnVis layout
      // Edges are colored according to ConnVis values
      layoutConnVis = new ActionList();
      layoutConnVis.add(x_axis);
      layoutConnVis.add(y_axis);
      layoutConnVis.add(edgesConnVis);

   }
   
   
   /**
      Returns an action list containing all needed for the
      selected layout method.
      The list affects layout and edge color, but not
      shape, label or color of the nodes.
      There is only one instance of every layout list globally.
      Null is returned, if the layoutMethod is no valid layout.
      
      @return List containing all necessary actions of the layout.
   */
   public static ActionList getLayout (String layoutMethod) {
      
      // First time this method is called create the action lists
      if (layoutGrid == null) {
         createLayouts();
      }

      // Force-directed layout
      if (layoutMethod.equals(Constants.layoutForce)) {
         return layoutForce;
      }

      // Grid layout
      if (layoutMethod.equals(Constants.layoutGrid)) {
         return layoutGrid;
      }

      // ConnVis layout
      if (layoutMethod.equals(Constants.layoutConnVis)) {
         return layoutConnVis;
      } 
      
      // No valid layout
      return null;
      
   }
   
   


   /**
      Refreshes all action lists according to the
      changes the user might have made to 
         - sizes
         - colors
         - palette
   */
   public static void refreshLists () {
       
      // ---- Refresh static actions ----
      
      for (int i=0; i<=staticActions.size(); i++) {
         try {
            Action myaction = staticActions.get(i);
                        
            // Node text
            if (myaction.getClass() == ColorAction.class) {
               ColorAction colorAction = (ColorAction) myaction;
               colorAction.setDefaultColor(VisualSettings.nodeTextColor);
            }
            if (myaction.getClass() == DataSizeAction.class) {
               DataSizeAction dataSizeAction = (DataSizeAction) myaction;
               if (dataSizeAction.getGroup().equals("graph.nodes")) {
                  dataSizeAction.setMinimumSize(VisualSettings.nodeMinimumSize);
                  dataSizeAction.setMaximumSize(VisualSettings.nodeMaximumSize);
               } else if (dataSizeAction.getGroup().equals("graph.edges")) {
                  dataSizeAction.setMinimumSize(VisualSettings.edgeMinimumSize);
                  dataSizeAction.setMaximumSize(VisualSettings.edgeMaximumSize);
               }
            }
         
         } catch (Exception e) {
            // we have reached the end of the array of actions
            break;
         }
      }


      // ---- Refresh layout actions ----
      
      // ConnVis layout
      // - change zeroColor
      for (int i=0; i<=layoutConnVis.size(); i++) {
         try {
            Action myaction = layoutConnVis.get(i);
            
            if (myaction.getClass() == ZeroDataColorAction.class) {
               ZeroDataColorAction zeroDataColorAction = (ZeroDataColorAction) myaction;
               zeroDataColorAction.setZeroColor(VisualSettings.zeroColor);
               // we don't have to change the palette because it is directly working
               // on the ConnVis palette 
            }
         
         } catch (Exception e) {
            // we have reached the end of the array of actions
            break;
         }
      }
     
      // Grid layout
      // - change palette
      for (int i=0; i<=layoutGrid.size(); i++) {
         try {
            Action myaction = layoutGrid.get(i);

            if (myaction.getClass() == DataColorActionPaletteChange.class) {
               DataColorActionPaletteChange pcDataColorAction = (DataColorActionPaletteChange) myaction;
               pcDataColorAction.setPalette(VisualSettings.palette);
            }
         
         } catch (Exception e) {
            // we have reached the end of the array of actions
            break;
         }
      }
     
      // Force directed layout
      // - change palette
      for (int i=0; i<=layoutForce.size(); i++) {
         try {
            Action myaction = layoutForce.get(i);

            if (myaction.getClass() == DataColorActionPaletteChange.class) {
               DataColorActionPaletteChange pcDataColorAction = (DataColorActionPaletteChange) myaction;
               pcDataColorAction.setPalette(VisualSettings.palette);
            }
         
         } catch (Exception e) {
            // we have reached the end of the array of actions
            break;
         }
      }
      

      // ---- Refresh recolor actions ----
      
      // Remove the old highlight action
      // (this removes only the actions added with 'add', not the original action,
      // don't worry, nodes are still colored according to what we specified above)
      recolorAction.clear();
      // Add the highlight action with the new highlight color
      recolorAction.add(VisualItem.HIGHLIGHT, VisualSettings.highlightColor);
      // Set new palette (current palette is always saved in VisualSettings.palette)
      recolorAction.setPalette(VisualSettings.palette);
      
      
   }
   

   
   
   
}
