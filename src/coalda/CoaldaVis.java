// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;

import coalda.base.Constants;
import coalda.data.FVImport;
import coalda.data.SOMConfigurationListener;
import coalda.data.export.MapExporter;
import coalda.data.evaluation.SOMEvaluator;
import coalda.vis.SOMDisplay;
import coalda.vis.SOMTabbedPane;

import de.unistuttgart.ais.sukre.refinery.network.model.SOMCalculationModel;
import de.unistuttgart.ais.sukre.refinery.network.ui.SOMConfigurationComponent;
import de.unistuttgart.ais.sukre.refinery.network.ui.SOMConfigurationManagementComponent;
import de.unistuttgart.ais.sukre.refinery.network.ui.SOMExecutionComponent;
import de.unistuttgart.ais.sukre.refinery.textvis.ui.TextModelComponent;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.ISOMPropertiesConstants;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration.Normalization;

// TODO what if calculation ID to load or export does not exist - check!


/**

@author kesslewd

   Usage: coaldaVis [<calcID> [evaluate | export <file>]

   Possible command line arguments:
   calculationID: loads the calculation with this ID on startup.
   evaluate: Evaluates the calculation with the ID in the first parameter
      and prints the result on the command line.
      Exits without starting the visualization.
   export: Exports the calculation with with the ID in the first parameter
      to the file is the third parameter.
      Exits without starting the visualization.
      
   Starting coaldaVis without any command line parameters will
   start the visualization with an empty screen. From there on you can
   use any of the above functions or calculate a new visualization.
      
*/
public class CoaldaVis {


   /**
      Calculation ID to loaded at startup.
      (0 = none)
   */
   private static int calcID = 0;

   /**
      Tabbed Pane for the displays.
   */
   private static SOMTabbedPane displayTab;

   /**
      Leftmost text area (for displaying features of the map unit).
   */
   private static JTextArea featuresMU;

   /**
      Scrollable for leftmost text area
   */
   private static JScrollPane featuresMUScroll;

   /**
      Component for displaying feature vectors and text.
   */
   private static TextModelComponent tmc;

   /**
      Side pane that contains settings.
   */
   private static JPanel sidePane;

   /**
      Model for SOM used for calculations
   */
   private static SOMCalculationModel SOMmodel = new SOMCalculationModel();

   /**
      Do we have to calculate the SOM
      for all fvs or only for those of selected nodes.
   */
   private static boolean[] selectedFeatures;

   /**
      Last FV ID in the database at startup.
   */
   private static int originalLastFVID;

   /**
      Total size of the Window.
   */
   private static Dimension windowSize = new Dimension(1000, 1000);

   /**
      Size of the DisplayTab.
   */
   private static Dimension displaySize = new Dimension((int) (windowSize.width*0.65), (int) (windowSize.height*0.65));

   /**
      Importer for Feature Vectors.
   */
   private static FVImport fv = new FVImport();

   /**
      Where we keep the settings for the SOM and
      where we define what to do just before and after
      a calculation is started.
   */
   private static SOMConfigurationListener somConfigListener;



   /**

   Main method.

   */
   public static void main(String[] args) {


      // -- Use command line parameters if there are any -------------------------

      if (args.length > 0) {
         try {
            calcID = Integer.parseInt(args[0]);
         } catch (NumberFormatException e) {
            System.out.println("CalcID must be an integer - number is ignored");
         }
      }
      if (args.length > 1) {
         if (args[1].trim().equals("evaluate")) {
            if (calcID != 0) {
               System.out.println("---- Evaluating SOM ----");
               SOMEvaluator evi = new SOMEvaluator();
               evi.evaluate(calcID);
               System.exit(0);
            } else {
               System.out.println("Cannot evaluate this calculation ID.");
               System.exit(1);
            }
         }
         if (args[1].trim().equals("export")) {
            if (calcID == 0) {
               System.out.println("Cannot export this calculation ID.");
               System.exit(1);
            }
            if (args.length > 2) {
               String outFile = args[2].trim();
               System.out.println("---- Exporting Data ----");
               System.out.println("Loading...");
               MapExporter exporter = new MapExporter();
               exporter.readAll(calcID);
               System.out.println("Exporting...");
               exporter.exportToTextFile(outFile);
               System.exit(0);
            } else {
               System.out.println("Missing parameter.");
               System.out.println("Usage: coaldaVis <calcID> export <file>");
               System.exit(1);
            }
         }
      }

      // -- Get settings -------------------------

      Constants.getSettings();

      System.out.println("Matlab SOM server name: " + Constants.matlabServer);
      System.out.println("Database location: " + Constants.dbLocation);


      // -- Create components -------------------------

      displayTab = new SOMTabbedPane();
      featuresMU = new JTextArea ("Features of Map Unit\n");
      featuresMUScroll = new JScrollPane(featuresMU);
      tmc = new TextModelComponent();

      // -- Settings for the model -------------------------

      // Get default config as starting point
      SOMConfiguration startConf = SOMmodel.getSomConfig();

      // Set Matlab server and normalization method
      startConf.put(ISOMPropertiesConstants.CONNECTION_SOM_SERVER_NAME, Constants.matlabServer);
      startConf.setNormalizationMethod(Normalization.var);

      // Set new config as the config to use
      SOMmodel.setSomConfig(startConf); 

      // Get maximal feature vector ID in database
      // (to know from where on we have to delete 
      // if we create new feature vectors for recalculation)
      originalLastFVID = fv.getMaxFVID();

      somConfigListener = new SOMConfigurationListener(originalLastFVID, displayTab);
      SOMmodel.addCalculationModelListener(somConfigListener);


      // -- Window layout ------------------------------------------------

      JFrame frame = new JFrame("COALDA - Coreference Annotation of Large Datasets");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setPreferredSize(windowSize);

      // Side pane
      // (cannot create earlier due to needed components)
      sidePane = createPanel();

      // Layout all component in the frame using splitPanes
      JPanel contentPane = new JPanel(new MigLayout("fill, nocache, nogrid"));
      JSplitPane splitAbove = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, displayTab, sidePane);
      JSplitPane splitBelow = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, featuresMUScroll, tmc);
      JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitAbove, splitBelow);
      contentPane.add(split, "grow");
      frame.setContentPane(contentPane);
      frame.pack();

      // set Menubar
      // (cannot create earlier due to needed components)
      JMenuBar menuBar = createMenu();
      frame.setJMenuBar(menuBar);

      // Show it to me!
      frame.setVisible(true);

      // Set divider locations of splits
      splitAbove.setDividerLocation(0.65);
      splitBelow.setDividerLocation(0.3);
      split.setDividerLocation(0.7);


      // -- Settings for the components of the display -------------------------

      // Settings for the tabbed pane with the displays
      displayTab.setTAFeaturesMU(featuresMU);
      displayTab.setTextVisualization(tmc);
      displayTab.addSOMConfig(SOMmodel);
      //displayTab.setComponentSize(displayTab.getSize());
      displayTab.setComponentSize(displaySize); // ARGH!

      // Settings for the text area
      featuresMU.setEditable(false);
      featuresMU.setLineWrap(true);
      featuresMU.setWrapStyleWord(true);
      featuresMUScroll.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      featuresMUScroll.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);


      // -- Load the data ------------------------------------------------

      // If a calculation ID is given, it is loaded.
      // Else the software starts with an empty screen.
      if (calcID != 0) {
         System.out.println("---- Loading Data ----");
         displayTab.addSOMDisplay(calcID);
      }


   }



   /**
   Method createPanel.
   Creates the side pane.
   @return The side pane.
   */
   public static JPanel createPanel () {

      // ---- Tab Features ----
      Box featureBox = new Box(BoxLayout.PAGE_AXIS);
      featureBox.setBorder(BorderFactory.createTitledBorder("Features"));
      featureBox.setAlignmentX(Component.LEFT_ALIGNMENT);

      // If the state of one of the checkboxes is changed, 
      // propagate this change to the config listener.
      ItemListener featureListener = new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            JCheckBox source = (JCheckBox)e.getItemSelectable();
            // Get id of the checkbox. 
            // The label of the checkbox is 
            // Feature_ID. Feature_Definiton
            // So we take the Substring before the . as int.
            // Substract one because ids start at 1, array at 0.
            // WARNING: Should anybody change the labels, 
            // we are DOOMED!
            String label = (String)source.getText();
            String id = label.substring(0, label.indexOf('.'));
            int i = Integer.parseInt(id);
            i--;
            if (e.getStateChange() == ItemEvent.SELECTED) {
               selectedFeatures[i] = true;
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
               selectedFeatures[i] = false;
            }
            somConfigListener.setSelectedFeatures(selectedFeatures);
         }
      };

      // Create a checkbox for every feature in the definition file.
      // Label is the ID of the feature and the name.
      Vector<String> definitions = fv.getFeatureDefinitions();
      int noOfFeatures = 0;
      selectedFeatures = new boolean[definitions.size()];
      for (String feature : definitions) {
         noOfFeatures++;
         // WARNING: Should anybody change the labels, 
         // we are DOOMED!
         JCheckBox feature_i = new JCheckBox(noOfFeatures + ". " + feature, true);
         feature_i.addItemListener(featureListener);
         featureBox.add(feature_i);
         selectedFeatures[noOfFeatures-1] = true;
      }

      // Make feature tab scrollable (good if we have a lot of features)
      JScrollPane featureBoxScroll = new JScrollPane(featureBox);

      // Set initally selected features in config listener
      somConfigListener.setSelectedFeatures(selectedFeatures);


      // ---- Tab SOM Properties ----
      Box somBox = new Box(BoxLayout.PAGE_AXIS); 

      final SOMConfigurationComponent somConf = new SOMConfigurationComponent();
      somBox.add(somConf, "push, w 70%:70%:75%");
      somConf.setCalculationModel(SOMmodel);

      SOMConfigurationManagementComponent somLoader =
            new SOMConfigurationManagementComponent(somConf);
      somBox.add(somLoader, "push, w 25%:30%:30%");


      // ---- Tab Vectors ----
      Box vectorBox = new Box(BoxLayout.PAGE_AXIS);
      vectorBox.setBorder(BorderFactory.createTitledBorder("Recalculate for..."));
      vectorBox.setAlignmentX(Component.LEFT_ALIGNMENT);

      // Create group with two radio buttons.
      // Listener on one of them.
      ButtonGroup group = new ButtonGroup();

      JRadioButton allfvs = new JRadioButton("all fvs", true);
      JRadioButton selectedfvs = new JRadioButton("fvs of selected map units", true);
      vectorBox.add(allfvs, "grow");
      vectorBox.add(selectedfvs, "grow");
      group.add(allfvs);
      group.add(selectedfvs);

      // If the state of one of the radio buttons is changed, 
      // propagate this change to the config listener.
      allfvs.addItemListener(new ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
               // all fvs selected, calc for selected deselected
               somConfigListener.setCalculateForSelected(false);
            }
            if (e.getStateChange() == ItemEvent.DESELECTED) {
            // all fvs deselected, calc for selected true
            somConfigListener.setCalculateForSelected(true);
            }
         }
      });


      // ---- Execution Component (below tabs) ----
      SOMExecutionComponent ex = new SOMExecutionComponent();
      ex.setCalculationModel(SOMmodel);


      // ---- Add everything to the panel ----
      JPanel weights = new JPanel(new MigLayout("fill, wrap 1"));
      JTabbedPane configTab = new JTabbedPane();
      configTab.add("SOM Properties", somBox);
      configTab.add("Vectors", vectorBox);
      configTab.add("Features", featureBoxScroll);
      weights.add(configTab, "grow");
      weights.add(ex, "grow");

      return weights;

   }



   /**
   Method createMenu.
   Creates the menu bar.
   @return The menu bar.
   */
   public static JMenuBar createMenu () {

      // Create the menu bar.
      JMenuBar menuBar = new JMenuBar();

      // === Menu Export ===
      JMenu loadMenu = new JMenu("Data");
      menuBar.add(loadMenu);

      // MenuItem Load
      // Loads the visualization of the calculation in a new tab.
      JMenuItem loadButton = new JMenuItem("Load...");
      loadButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String calculationID = (String) JOptionPane.showInputDialog(
                  displayTab,
                  "Please enter calculation ID to load:",
                  "Load Calculation",
                  JOptionPane.PLAIN_MESSAGE);
            try {
               System.out.println("---- Loading Data ----");
               int calculationID2 = Integer.parseInt(calculationID);
               displayTab.addSOMDisplay(calculationID2);
            } catch (NumberFormatException f) {
               System.out.println("CalcID must be an integer - number is ignored");
               JOptionPane.showMessageDialog(displayTab, "Not a valid calculation ID.");
            }
         }
      });
      loadMenu.add(loadButton);

      // MenuItem Export
      // Exports the map with the calculation ID the user chose
      // to a textfile at the location the user chose.
      JMenuItem exportButton = new JMenuItem("Export...");
      exportButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String calculationID = (String) JOptionPane.showInputDialog(
                  displayTab,
                  "Please enter calculation ID to export:",
                  "Export Calculation",
                  JOptionPane.PLAIN_MESSAGE);
            try {
               System.out.println("---- Exporting Data ----");
               int calculationID2 = Integer.parseInt(calculationID);
               System.out.println("Loading...");
               MapExporter exporter = new MapExporter();
               exporter.readAll(calculationID2);
               String outFile = (String) JOptionPane.showInputDialog(
                  displayTab,
                  "Please enter textfile to export to:",
                  "Export Calculation",
                  JOptionPane.PLAIN_MESSAGE);
               System.out.println("Exporting...");
               exporter.exportToTextFile(outFile);
            } catch (NumberFormatException f) {
               System.out.println("CalcID must be an integer - number is ignored");
               JOptionPane.showMessageDialog(displayTab, "Not a valid calculation ID.");
            }
         }
      });
      loadMenu.add(exportButton);


      // === Menu Recolor ===
      // Recolor nodes with selected field.
      JMenu recolorMenu = new JMenu("Recolor nodes");
      menuBar.add(recolorMenu);
      ButtonGroup recolorGroup = new ButtonGroup();

      // Listener gets name of selected field and recolors
      ActionListener recolorListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem cb = (JRadioButtonMenuItem)e.getSource();
            String field = (String)cb.getText();
            SOMDisplay c = (SOMDisplay)displayTab.getSelectedComponent();
            displayTab.recolor(c, field);
         }
      };

      // Get possible fields from Array
      int length = Constants.possibleNodeColorings.length;
      for (int k=0; k<length;k++) {
         JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(Constants.possibleNodeColorings[k]);
         recolorMenu.add(rbMenuItem);
         recolorGroup.add(rbMenuItem);
         rbMenuItem.addActionListener(recolorListener);
         // Select first entry of array as default
         if (k == 0) {
            rbMenuItem.setSelected(true);
         }
      }

      // We can also color segun weights for features
      // Get number of features
      int noOfFeatures = selectedFeatures.length;
      for (int k=1; k<=noOfFeatures;k++) {
         JRadioButtonMenuItem rbMenuItem = 
               new JRadioButtonMenuItem(Constants.nodeWeight + k);
         recolorMenu.add(rbMenuItem);
         recolorGroup.add(rbMenuItem);
         rbMenuItem.addActionListener(recolorListener);
      }


      // === Menu Relabel  ===
      // Relabel nodes with selected field.
      JMenu relabelMenu = new JMenu("Relabel nodes");
      menuBar.add(relabelMenu);
      ButtonGroup relabelGroup = new ButtonGroup();

      // Listener gets name of selected field and relabels
      ActionListener relabelListener = new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JRadioButtonMenuItem cb = (JRadioButtonMenuItem)e.getSource();
            String field = (String)cb.getText();
            SOMDisplay c = (SOMDisplay)displayTab.getSelectedComponent();
            displayTab.relabel(c, field);
         }
      };

      // Get possible fields from Array
      length = Constants.possibleNodeLabels.length;
      for (int k=0; k<length;k++) {
         JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(Constants.possibleNodeLabels[k]);
         relabelMenu.add(rbMenuItem);
         relabelGroup.add(rbMenuItem);
         rbMenuItem.addActionListener(relabelListener);
         // Select first entry of array as default
         if (k == 0) {
            rbMenuItem.setSelected(true);
         }
      }


      // === Menu Evaluate ===
      // Evaluates a given calculation
      JMenu evalMenu = new JMenu("Evaluate");
      menuBar.add(evalMenu);

      // MenuItem Evaluate
      JMenuItem evalButton = new JMenuItem("Evaluate...");
      evalButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            String calculationIDString = (String)JOptionPane.showInputDialog(
                  displayTab,
                  "Please enter calculation ID to evaluate:",
                  "Evaluate...",
                  JOptionPane.PLAIN_MESSAGE);
            System.out.println("---- Evaluating SOM ----"); 
            try {
               int calculationID = Integer.parseInt(calculationIDString);
               SOMEvaluator evi = new SOMEvaluator();
               evi.evaluate(calculationID);
            } catch (NumberFormatException f) {
               System.out.println("CalcID must be an integer - number is ignored");
               JOptionPane.showMessageDialog(displayTab, "Not a valid calculation ID.");
            }
         }
      });
      evalMenu.add(evalButton);


      // === Menu DB_Clear ===
      // Deletes the entries from the database
      // that we created while recalculating with
      // different features.
      JMenu dbClearMenu = new JMenu("DB");
      menuBar.add(dbClearMenu);

      JMenuItem dbClearButton = new JMenuItem("Clear database");
      dbClearButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            System.out.println("Clear database of temporary fvs we created");
            fv.deleteCreatedFVs();
         }
      });
      dbClearMenu.add(dbClearButton);


      // We are ready
      return menuBar;

   }



   /**
   Method finalize.
   Clean up before we leave.
   */
   public void finalize () {
      System.out.println("Clear database of temporary fvs we created");
      fv.deleteCreatedFVs();
   }


}

