// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.ui;


import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.CalculationImport;
import coalda.learner.Learner;
import coalda.vis.ActionProvider;
import coalda.vis.InformationControl;
import coalda.vis.LabelControl;
import coalda.vis.RectangleSelectionControl;
import coalda.vis.SelectionControl;
import coalda.vis.WheelTwoLevelZoomControl;

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
import prefuse.controls.Control;
import prefuse.controls.FocusControl;
import prefuse.controls.PanControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.tuple.TupleSet;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.ui.UILib;


/**

Tabbed pane, each tab holds a SOMDisplay.

@author kesslewd
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
      Field for coloring the nodes.
   */
   private String currentColorField;

   /**
      Field for labeling the nodes.
   */
   private String currentLabelField;

   /**
      Layout of the graph.
   */
   private String currentLayout;



   // -------------- CONSTRUCTORS --------------


   /**
      Constructor.
   */
   public SOMTabbedPane () {
      super();
      // Set current color, label field and layout to default.
      currentColorField = Constants.possibleNodeColorings[0];
      currentLabelField = Constants.possibleNodeLabels[0];
      currentLayout = Constants.possibleLayouts[0];

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


   // -------------- METHODS --------------


   /**
      Adds a SOM configuration to the learner.
      @param somconf 
   */
   public void addSOMConfig (SOMCalculationModel somconf) {
      learner = new Learner(somconf);
   }


   /**
      Set the size of the displays in the tabs. Used for layout.
      @param tabComponentsSize Size of displays in tabs.
   */
   public void setComponentSize (Dimension tabComponentsSize) {
      componentSize = tabComponentsSize;
   }


   /**
      Set the text area used to print information about the nodes.
      @param textArea Text Area.
   */
   public void setTAFeaturesMU (JTextArea textArea) {
      featuresMU = textArea;
   }


   /**
      Set the component used to print information about 
      the feature vectors and the text.
      @param tmc Text Model Component.
   */
   public void setTextVisualization (TextModelComponent tmc) {
      textVisualization = tmc;
   }

   
   /**
      Set the field used for coloring the nodes.
      @param field Field used for coloring the nodes.
   */
   public void setColorField (String field) {
      currentColorField = field;
   }
   
   
   /**
      Set the field used for labeling the nodes.
      @param field Field used for labeling the nodes.
   */
   public void setLabelField (String field) {
      currentLabelField = field;
   }
   /**      
      Set the method used for the layout of the graph.
      @param method Method used for the layout.

   */
   public void setLayoutMethod (String method) {
      currentLayout = method;
   }


   // -------------- ADD SOM DISPLAY --------------


   /**
      Adds a display to the tabbed pane in a new tab.
      This display contains a new calculation done with the feature
      vectors given as a parameter.
      @param featureVectorIDs The feature vectors to be used in the new calculation.
      @param nodeNumber The ID of the node the feature vectors belong to.
   */
   public void addSOMDisplay (String featureVectorIDs, String nodeNumber) {
      // Used in zoom
      // Sort fvs
      System.out.println(featureVectorIDs);
      String fvs = Utils.sortFVs(featureVectorIDs);
      System.out.println(fvs);
      // Calculate new SOM for the given feature vectors
      int calcID = learner.calculateForFVs(fvs);
      // Add display
      addSOMDisplay(calcID);
   }


   /**
      Adds a display to the tabbed pane in a new tab.
      The display contains the graph of the calculation
      given by the ID.
      @param calculationID ID of the calculation to load.
   */
   public void addSOMDisplay (int calculationID) {
      // Import data
      CalculationImport dataImport = new CalculationImport();
      Graph graph = dataImport.readGraph(calculationID);
      // If the graph is not empty, add display
      if ((graph != null) && (graph.getNodeCount() != 0)) {
         SOMDisplay d = new SOMDisplay(graph,
               currentLabelField, currentColorField, currentLayout, calculationID);
         addSOMDisplay(d, calculationID);
      }
   }


   /**
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
      Adds a display to the tabbed pane in the first tab.
      The display contains a calculation with all feature
      vectors in the database.
   */
   public void addSOMDisplayOverview () {
      int calcID = learner.calculateForAllFVs();
      addSOMDisplay(calcID);
   }


   /**
      Adds a display to the tabbed pane in a new tab.
      @param display The display to be added.
      @param calculationID ID of the calculation to load.
   */
   private void addSOMDisplay (SOMDisplay display, int calculationID) {

      // Add tab with new display
      add("Calculation " + calculationID, display);

      // Perform some initializations for the layouts
      display.initLayout();
      
      // Add action lists to display
      display.addActionList(SOMDisplay.staticActionGroup, ActionProvider.getStaticActions());
      display.addActionList(SOMDisplay.layoutGroup, ActionProvider.getLayout(currentLayout));
      display.addRecolorAction(ActionProvider.getRecolorAction(currentColorField));
      display.relabel(currentLabelField);

      // Add control listeners - standard controls
      display.addControlListener(new ZoomToFitControl()); // Fit visualization to display
      display.addControlListener(new PanControl()); // Drag visualization around
      ToolTipControl ttc = new ToolTipControl // Tooltips
            (new String[] {Constants.nodeKey, Constants.nodeFVNumber, Constants.nodeUmatValue, Constants.edgeKey, Constants.edgeUmatValue});
      display.addControlListener(ttc);
      
      // New controls

      // Two-level zoom with mouse wheel
      display.addControlListener(new WheelTwoLevelZoomControl(this)); 
      
      // Display things on clic
      FocusControl fc = new InformationControl(1, featuresMU, textVisualization); 
      display.addControlListener(fc);
      
      // Label nodes on doubleclic
      FocusControl labeler = new LabelControl(2); 
      display.addControlListener(labeler);
      
      // Highlight selected items
      TupleSet focusGroup = display.getVisualization().getGroup(Visualization.FOCUS_ITEMS); 
      focusGroup.addTupleSetListener(new SelectionControl(this)); 
      
      // Select several nodes with a rectangle
      RectangleSelectionControl rsc = new RectangleSelectionControl();
      display.addPaintListener(rsc);
      display.addControlListener(rsc);
      
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

      // Make this tab the selected tab
      this.setSelectedIndex(this.getTabCount()-1);

   }


   // -------------- BEHAVIOUR --------------



   /**
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
