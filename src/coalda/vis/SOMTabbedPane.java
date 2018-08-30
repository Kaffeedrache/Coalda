// Stefanie Wiltrud Kessler, September 2009 - April 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;
import coalda.data.CalculationImport;
import coalda.learner.Learner;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import de.unistuttgart.ais.sukre.refinery.network.model.SOMCalculationModel;
import de.unistuttgart.ais.sukre.refinery.textvis.ui.TextModelComponent;

import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.assignment.ShapeAction;
import prefuse.action.assignment.SizeAction;
import prefuse.action.layout.AxisLayout;
import prefuse.controls.Control;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.VisiblePredicate;


/**
@author kesslewd

Tabbed pane, each tab holds a SOMDisplay.

*/
public class SOMTabbedPane extends JTabbedPane {


   // -------------- FIELDS --------------


   /**
      UID for implementing serializable.
   */
   private static final long serialVersionUID = -8793204046488370566L;

   /**
      Size for the displays in the tabs.
   */
   private Dimension componentSize;

   /**
      Text area for displaying information about the nodes.
   */
   private JTextArea featuresMU;

   /**
      Component used for text visualization.
   */
   private TextModelComponent textVisualization;

   /**
      Learner used for recalculation.
   */
   private Learner learner;

   /**
      Import of calculation data.
   */
   private CalculationImport dataImport = new CalculationImport();

   /**
      Field for coloring the nodes.
   */
   private String currentColorField;

   /**
      Field for labeling the nodes.
   */
   private String currentLabelField;


   /**
      Action List for coloring the SOM.
   */
   private ActionList color;

   /**
      Action List for re-coloring the SOM.
   */
   private ActionList recolor;

   /**
      Action List for changing the shapes and sizes of the nodes of the SOM.
   */
   private ActionList shape;

   /**
      Action List for the layout of the SOM.
   */
   private ActionList layout;

   /**
      Palette for the colors of nodes.
   */
   private int[] palette = new int[] {
      ColorLib.rgb(0,0,255), // red
      ColorLib.rgb(0,100,128), // brown
      ColorLib.rgb(0,200,0), // green
      ColorLib.rgb(128,128,0), // turquoise
      ColorLib.rgb(255,0,0) // blue
   };


   // -------------- CONSTRUCTORS --------------


   /**
      Constructor.
   */
   public SOMTabbedPane () {
      super();
      // Set current color and label field to default.
      currentColorField = Constants.possibleNodeColorings[0];
      currentLabelField = Constants.possibleNodeLabels[0];

      // Create actions to be added to the displays
      createActions();

      // Add control that tabs can be closed by right click.
      addMouseListener(buttonMouseListener);

   }


   /**
      Constructor.
      @param tabComponentsSize Size of displays in tabs.
   */
   public SOMTabbedPane(Dimension tabComponentsSize) {
      this();
      setComponentSize(tabComponentsSize);
   }


   /**
      Method createActions.
      Creates the action lists that are added to the displays.
      Action lists influence, colors, shapes, sizes and layout.
      Called by the constructors.
   */
   private void createActions () {

      // Colors
      color = new ActionList();
      recolor = new ActionList();

      // Filling color of nodes
      DataColorAction fill = new DataColorAction("graph.nodes", currentColorField,
            prefuse.Constants.NUMERICAL, VisualItem.FILLCOLOR, palette);
            fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
      color.add(fill);
      recolor.add(fill);

      // Node text
      ColorAction text = new ColorAction("graph.nodes",
            VisualItem.TEXTCOLOR, ColorLib.rgb(255,255,255));
      color.add(text);

      // Colour of edges
      DataColorAction edges = new DataColorAction("graph.edges", Constants.edgeUmatValue,
            prefuse.Constants.NUMERICAL, VisualItem.STROKECOLOR, palette);
      color.add(edges);


      // Shapes and Sizes
      shape = new ActionList();

      // Shape of nodes
      ShapeAction nodeShape = new ShapeAction ("graph.nodes", prefuse.Constants.SHAPE_HEXAGON );
      shape.add(nodeShape);

      // Size of nodes
      DataSizeAction nodeSize = new DataSizeAction ("graph.nodes", Constants.nodeFVNumber );
      nodeSize.setIs2DArea(false); // 1D data field
      nodeSize.setMinimumSize(1); // minimum node size = 1
      nodeSize.setMaximumSize(3); // maximum node size = 4
      nodeSize.setScale(1); // logarithmic scale [looks better]
      shape.add(nodeSize);

      // Size of edges
      SizeAction edgeThickness = new SizeAction ("graph.edges", 2);
      //~ DataSizeAction edgeThickness = new DataSizeAction ("graph.edges", EdgeUmatValue );
      //~ edgeThickness.setIs2DArea(false); // 1D data field
      //~ edgeThickness.setMinimumSize(1); // minimum thickness = 1
      //~ edgeThickness.setMaximumSize(10); // maximum thickness = 8
      //~ edgeThickness.setScale(0);
      shape.add(edgeThickness);


      // Layout
      layout = new ActionList();

      AxisLayout x_axis = new AxisLayout("graph.nodes", Constants.nodeXValue, 
            prefuse.Constants.X_AXIS, VisiblePredicate.TRUE);
      AxisLayout y_axis = new AxisLayout("graph.nodes", Constants.nodeYValue, 
            prefuse.Constants.Y_AXIS, VisiblePredicate.TRUE);

      layout.add(x_axis);
      layout.add(y_axis);

   }


   // -------------- METHODS --------------


   /**
      Method addSOMConfig.
      Adds a SOM configuration to the learner.
      @param somconf 
   */
   public void addSOMConfig (SOMCalculationModel somconf) {
      learner = new Learner(somconf);
   }


   /**
      Method setComponentSize.
      Set the size of the displays in the tabs. Used for layout.
      @param tabComponentsSize Size of displays in tabs.
   */
   public void setComponentSize (Dimension tabComponentsSize) {
      componentSize = tabComponentsSize;
   }


   /**
      Method setTAFeaturesMU.
      Set the text area used to print information about the nodes.
      @param textArea Text Area.
   */
   public void setTAFeaturesMU (JTextArea textArea) {
      featuresMU = textArea;
   }


   /**
      Method setTextVisualization.
      Set the component used to print information about 
      the feature vectors and the text.
      @param tmc Text Model Component.
   */
   public void setTextVisualization (TextModelComponent tmc) {
      textVisualization = tmc;
   }


   //  java.lang.IllegalArgumentException: Unknown column name: Weight26
   /**
      Method recolor.
      Recolors all nodes of the given display
      according to the values of the given field.
      @param display The display that contains the visualization to recolor.
      @param field The field to be used to recolor.
   */
   public void recolor (SOMDisplay display, String field) {

      // Get field to recolor
      String myfield = field;
      if (myfield == null) {
         myfield = currentColorField;
      } else {
         currentColorField = field;
      }

      // Create an action that colors depending on the chosen field
      DataColorAction fill = new DataColorAction("graph.nodes", myfield,
                prefuse.Constants.NUMERICAL, VisualItem.FILLCOLOR, palette);
      fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));

      // Recolor using that field
      display.recolor(fill);
      display.repaint();
   }


   /**
      Method relabel.
      Relabels all nodes of the given display
      with the value of the given field.
      @param display The display that contains the visualization to relabel.
      @param field The field to be used to relabel.
   */
   public void relabel (SOMDisplay display, String field) {

      // Get field to recolor
      String myfield = field;
      if (myfield == null) {
         myfield = currentLabelField;
      } else {
         currentLabelField = field;
      }

      // Relabel
      display.relabel(myfield);
      display.repaint();
   }


   // -------------- ADD SOM DISPLAY --------------


   /**
      Method addSOMDisplay.
      Adds a display to the tabbed pane in a new tab.
      This display contains a new calculation done with the feature
      vectors given as a parameter.
      @param featureVectorIDs The feature vectors to be used in the new calculation.
      @param nodeNumber The ID of the node the feature vectors belong to.
   */
   public void addSOMDisplay (String featureVectorIDs, String nodeNumber) {
      // Used in zoom
      // Calculate new SOM for the given feature vectors
      int calcID = learner.calculateForFVs(featureVectorIDs);
      addSOMDisplay(calcID);
   }


   /**
      Method addSOMDisplay.
      Adds a display to the tabbed pane in a new tab.
      The display contains the graph of the calculation
      given by the ID.
      @param calculationID ID of the calculation to load.
   */
   public void addSOMDisplay (int calculationID) {
      Graph graph = dataImport.readAll(calculationID);  
      if ((graph != null) && (graph.getNodeCount() != 0)) {
         SOMDisplay d = new SOMDisplay(graph, currentLabelField, calculationID);
         addSOMDisplay(d, calculationID);
      }
   }


   /**
      Method addSOMDisplay.
      Adds this display to the tabbed pane in a new tab.
      The title of this display will always show 0 as calculation ID!
      Better use addSOMDisplay (int calculationID) or
      addSOMDisplay (SOMDisplay display, int calculationID) .
      @param display The display to be added.
      @deprecated
   */
   public void addSOMDisplay (SOMDisplay display) {
      addSOMDisplay(display, 0);
   }


   /**
      Method addSOMDisplay.
      Adds a display to the tabbed pane in the first tab.
      The display contains a calculation with all feature
      vectors in the database.
   */
   public void addSOMDisplayOverview () {
      int calcID = learner.calculateForAllFVs();
      addSOMDisplay(calcID);
   }


   /**
      Method addSOMDisplay.
      Adds a display to the tabbed pane in a new tab.
      @param display The display to be added.
      @param calculationID ID of the calculation to load.
   */
   private void addSOMDisplay (SOMDisplay display, int calculationID) {

      // Add tab with new display
      add("Calculation " + calculationID, display);

      // Add action lists to display
      display.addActionList(SOMDisplay.colorGroup, color);
      display.addActionList(SOMDisplay.layoutGroup, layout);
      display.addActionList(SOMDisplay.shapeGroup, shape);
      display.addActionList(SOMDisplay.recolorGroup, recolor);

      // Add control listeners

      display.addControlListener(new WheelTwoLevelZoomControl(this)); // zoom with mouse wheel
      display.addControlListener(new ZoomToFitControl()); // Fit visualization to display
      display.addControlListener(new PanControl()); // Drag visualization around
      ToolTipControl ttc = new ToolTipControl // Tooltips
            (new String[] {Constants.nodeKey, Constants.nodeFVNumber, Constants.edgeKey, Constants.edgeUmatValue});
      display.addControlListener(ttc);
      FocusControl fc = new InformationControl(1, featuresMU, textVisualization); // Display things on clic
      display.addControlListener(fc);
      FocusControl labeler = new LabelControl(2); // Display things on doubleclic
      display.addControlListener(labeler);
      TupleSet focusGroup = display.getVisualization().getGroup(Visualization.FOCUS_ITEMS); 
      focusGroup.addTupleSetListener(new SelectionControl(this)); // highlight selected items

      // Zoom to fit
      int width = (int)componentSize.getWidth();
      int height = (int)componentSize.getHeight();
      display.setSize(width, height);
      int m_margin = 30; // margin around the display
      long m_duration = 200; // duration of the animated zoom (0 is instantaneous)
      Rectangle2D bounds = new Rectangle2D.Double (0,0,width,height);
      GraphicsLib.expand(bounds, m_margin + (int)(1/display.getScale()));
      DisplayLib.fitViewToBounds(display, bounds, m_duration);

      // Run all action lists
      display.runAll();

      // Re-color to match current selected field
      recolor(display, null);

      // Make this tab the selected tab
      this.setSelectedIndex(this.getTabCount()-1);

   }


   // -------------- BEHAVIOUR --------------


   /**
      MouseListener buttonMouseListener
      Closes the current tab if its tabselector is clicked with the right mouse button.
   */
   private final static MouseListener buttonMouseListener = new MouseAdapter() {

      private int button = Control.RIGHT_MOUSE_BUTTON;

      public void mouseClicked(MouseEvent e) {
         Component component = e.getComponent();
         if (component instanceof SOMTabbedPane) {
            SOMTabbedPane stb = (SOMTabbedPane) component;
            int tabID = stb.getSelectedIndex();
            if (UILib.isButtonPressed(e, button)) {
            SOMDisplay theTab = (SOMDisplay) stb.getSelectedComponent();
            stb.removeTabAt(tabID);
            theTab.delete();
            // TODO: delete display, cancel listeners, actions, ...
            }
         }
      }

   };


}
