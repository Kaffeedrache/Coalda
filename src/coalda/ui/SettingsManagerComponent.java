// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.ui;


import coalda.ui.PrefuseColorChooserComponent.PrefuseColorChoserComponentTextColor;
import coalda.ui.PrefuseColorChooserComponent.PrefuseColorChoserComponentHighlighted;
import coalda.ui.PrefuseColorChooserComponent.PrefuseColorChoserComponentZeroColor;
import coalda.ui.SizeChooserComponent.SizeChoserComponentNodeMin;
import coalda.ui.SizeChooserComponent.SizeChoserComponentNodeMax;
import coalda.ui.SizeChooserComponent.SizeChoserComponentEdgeMin;
import coalda.ui.SizeChooserComponent.SizeChoserComponentEdgeMax;
import coalda.ui.PaletteManagerComponent.PaletteManagerComponentDefault;
import coalda.ui.PaletteManagerComponent.PaletteManagerComponentConnVis;
import coalda.ui.PaletteManagerComponent.PaletteManagerComponentInterpolated;
import coalda.ui.PaletteManagerComponent.PaletteManagerComponentInterpolatedThree;
import coalda.ui.PaletteManagerComponent.PaletteManagerComponentGrayscale;
import coalda.vis.ActionProvider;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;


/**

Allows the user to manage different settings that influence the
behaviour of the graphics such as sizes and colors.

@author kesslewd
*/
public class SettingsManagerComponent implements ActionListener {

   /**
      This component.
   */
   private JFrame frame;

   /**
      The tab whith the displays/visualizations we want to influence.
   */
   private SOMTabbedPane displayTab;

   /**
      Possible palettes to chose from have to be entered here.
   */
   private PaletteManagerComponent[] possiblePalettes = {
         new PaletteManagerComponentDefault(),
         new PaletteManagerComponentGrayscale(),
         new PaletteManagerComponentInterpolated(),
         new PaletteManagerComponentInterpolatedThree()
   };

   /**
      The last palette added - to be removed when a new
      palette is chosen.
   */
   PaletteManagerComponent lastPalette;
   

   /**
      Constructor.
      This component is initially invisible, make it visible using
      'setVisible()' - best at the same time you make everything
      else visible in your GUI.
      
      @param displayTabPane The tab whith the displays/visualizations
          we want to influence.
   */
   public SettingsManagerComponent (SOMTabbedPane displayTabPane) {
      this();
      displayTab = displayTabPane;
   }

   
   /**
      Constructor.
      For this component to be of any use, you have to set the 
      displayTab whith the displays/visualizations you want to 
      influence later on with 'setDisplayTab()'.
      This component is initially invisible, make it visible using
      'setVisible()' - best at the same time you make everything
      else visible in your GUI.
   */
   public SettingsManagerComponent () {

      frame = new JFrame("COALDA - SettingsManager");

      
      // ---- Change sizes ----
      
      JPanel sizeChooser = new JPanel(new MigLayout("fill, wrap 1"));

      sizeChooser.add(new SizeChoserComponentNodeMin(), "grow");
      sizeChooser.add(new SizeChoserComponentNodeMax(), "grow");
      sizeChooser.add(new SizeChoserComponentEdgeMin(), "grow");
      sizeChooser.add(new SizeChoserComponentEdgeMax(), "grow");
      
      
      // ---- Change single colors ----
      
      JPanel colorChooser = new JPanel(new MigLayout("fill, wrap 1"));
      
      colorChooser.add(new PrefuseColorChoserComponentTextColor(), "grow");
      colorChooser.add(new PrefuseColorChoserComponentHighlighted(), "grow");
      colorChooser.add(new PrefuseColorChoserComponentZeroColor(), "grow");
           
      
      // ---- Change palettes ----
      
      JPanel paletteChooser = new JPanel(new MigLayout("fill, wrap 1"));

      // ConnVis palette
      paletteChooser.add(new PaletteManagerComponentConnVis());
      
      JLabel labelPalette = new JLabel("Choose palette for node/edge colors");
      paletteChooser.add(labelPalette);
      
      // Add DropBox with names of all available palettes for coloring nodes 
      String[] possiblePaletteNames = new String[possiblePalettes.length];
      for (int i=0; i<possiblePalettes.length; i++) {
         possiblePaletteNames[i] = possiblePalettes[i].getName();
      }
      JComboBox paletteDropBox = new JComboBox(possiblePaletteNames);
      paletteChooser.add(paletteDropBox);

      // Add listener that displays corresponding PaletteManagementComponent
      // to the selected name. Select first element of array as default and display palette.
      paletteDropBox.addActionListener(new PaletteSelectionListener(paletteChooser));
      paletteDropBox.setSelectedIndex(0);
      lastPalette = possiblePalettes[0];
      paletteChooser.add(lastPalette);
      
      
      // ---- Refresh button ----
      
      JButton button = new JButton("Refresh view");
      button.addActionListener(this);
      
      
      // ---- Put it all together ----
      
      JTabbedPane configTab = new JTabbedPane();
      configTab.add("Sizes", sizeChooser);
      configTab.add("Colors", colorChooser);
      configTab.add("Palettes", paletteChooser);

      JPanel contentPane = new JPanel(new MigLayout("fill, wrap 1"));
      contentPane.add(configTab, "grow");
      contentPane.add(button, "grow");
      
      frame.setContentPane(contentPane);
      frame.pack();
   }
   
   
   /**
      Sets the tab whith the displays/visualizations we want to influence.
   */
   public void setDisplayTab (SOMTabbedPane displayTabPane) {
      displayTab = displayTabPane;
   }
   
   
   /**
      What happens when clic on 'Refresh' button.
      Refresh all action lists and repaint all displays.
   */
   public void actionPerformed(ActionEvent e) {

      // Select palette
      lastPalette.select();
      
      // Update action lists with new information
      ActionProvider.refreshLists();
      
      // Run action lists to make changes visible in all tabs
      int tabNumber = displayTab.getTabCount();
      for (int i=0; i<tabNumber; i++) {
         SOMDisplay display = (SOMDisplay)displayTab.getComponentAt(i);
         display.runAll();
      }
  } 

   
   /**
      Make this component visible (this is not done in the constructor).
   */
   public void setVisible() {
      // Show it to me!
      frame.setVisible(true);
   }

   
   /**
      Listener that takes the selected name and adds the corresponding
      PaletteManagerComponent to the panel given in the constructor.
   */
   public class PaletteSelectionListener implements ActionListener {
      
      /**
         The panel things are added.
      */
      JPanel paletteChooser;

      /**
         Constructor.
         @param panel The panel where new Components should be added.
      */
      public PaletteSelectionListener(JPanel panel) {
         paletteChooser = panel;
      }

      /**
         Get the selected index, get corresponding palette,
         add it to the panel after having removed the element before.
      */
      public void actionPerformed(ActionEvent e) {
         JComboBox cb = (JComboBox)e.getSource();
         int id = cb.getSelectedIndex();
         
         // Remove previous palette and add the new one
         if (lastPalette != null) {
            paletteChooser.remove(lastPalette);
         }
         lastPalette = possiblePalettes[id];
         paletteChooser.add(lastPalette);
         
         // Refresh to make changes visible TODO doesn't seem to work always
         paletteChooser.repaint();
      }
   }

   
}
