// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.


package coalda.ui;


import coalda.base.VisualSettings;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import prefuse.util.ColorLib;


/**

Allows the user to chose the colors of a palette.
The general behaviour is encapsulated in the superclass 
PaletteManagerComponent which cannot be directly instanciated.

General behaviour means, that a label is displayed and for every
color of the palette a 10x10 box with that color. By clicking on 
the box a ColorChoser is opened and a color can be chosen. 
The box changes color accordingly.

For linking the general behaviour with a specific variable,
this component must be subclassed. 

... work in progess

@author kesslewd
*/
public class PaletteManagerComponent extends JPanel implements MouseListener {
   

   /**
      Iimplement serializable.
   */
   protected static final long serialVersionUID = 1L;
   

   /**
      Initial color of the box.
   */
   protected Color[] initialValues;

   /**
      The color the user chose.
   */
   protected Color chosenColor;

   /**
      Label, displayted before the box.
   */
   protected JLabel label;

   /**
      Small box with the color, size 10x10.
   */
   protected JPanel[] colorPanes;
   

   /**
      Name. To be set by the subclasses.
   */
   protected String name = "";
   

   /**
      Hide constructor.
      Only subclasses can be instantiated.
   */   
   private PaletteManagerComponent() {
   }

   
   /**
      MUST be called in the constructor of each subclass.
      Creates the label and the box with the initial color
      as well as mouse click behaviour.
      'initialValue' must be set before calling this method.
   */
   private void init() {

      // This cannot be in the constructor, because a call
      // to the super-constructor must be the first call in a 
      // subclass-constructor. But we need to set 'initialValue'
      // first, so this call has to come afterwards.
      // (ok, there might be a better way around this, but 
      // this solution was quick ;) )
      
      label = new JLabel();
      label.setHorizontalAlignment(JLabel.LEFT);
      add(label);
      
      colorPanes = new JPanel[initialValues.length];
      for (int i=0; i<initialValues.length; i++) {
         colorPanes[i] = new JPanel();      
         colorPanes[i].setBackground(initialValues[i]);
         colorPanes[i].setSize(10, 10);
         colorPanes[i].addMouseListener(this);
         colorPanes[i].setName(i+"");
         add(colorPanes[i]);
      }
      
   }


   /**
      On click let user chose color and change
      the background of the panel.
      Subclasses additionally set the variable
      they want to the corresponding color.
   */
   public void mouseClicked(MouseEvent arg0) {
      JPanel colorPane = (JPanel) arg0.getSource();
      int id = Integer.parseInt(colorPane.getName());
      
     Color newColor = JColorChooser.showDialog(
              this,
              "Choose Color",
              initialValues[id]
           );
     if (newColor != null) {
        chosenColor = newColor;
        colorPane.setBackground(newColor);
        initialValues[id] = newColor;
     }
   }

   /**
      Do nothing on mouse entered.    
   */
   public void mouseEntered(MouseEvent arg0) {
   }

   /**
      Do nothing on mouse exited.    
   */
   public void mouseExited(MouseEvent arg0) {
   }

   /**
      Do nothing on mouse pressed.    
   */
   public void mousePressed(MouseEvent arg0) {
   }

   /**
      Do nothing on mouse released.    
   */
   public void mouseReleased(MouseEvent arg0) {
   }

   /**
      Sets the name.   
   */
   public void setName (String userFriendlyName) {
      name = userFriendlyName;
   }
   
   /**
      Returns the name.   
   */
   public String getName () {
      return name;
   }
   
   /**
      Selects this palette.
   */
   public void select () {
   }
   
   
   // Subclasses for the different color variables we want to set.

   
   // ConnVis palette (1 subclass)
   
   /**
      Changes the colors in the ConnVis palette.
   */
   public static class PaletteManagerComponentConnVis extends PaletteManagerComponent {
      
      private static final long serialVersionUID = 1L;
   
      public PaletteManagerComponentConnVis() {
         int length = VisualSettings.paletteConnVis.length;
         initialValues = new Color[length];
         for (int i=0; i<length; i++) {
            initialValues[i] = ColorLib.getColor(VisualSettings.paletteConnVis[i]);
         }
         super.init();
         label.setText("Palette ConnVis");
         setName("Palette ConnVis");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         JPanel colorPane = (JPanel) arg0.getSource();
         int id = Integer.parseInt(colorPane.getName());
         super.mouseClicked(arg0);
         VisualSettings.paletteConnVis[id] = ColorLib.color(chosenColor);
      }
      
      // Does notthing in select() because we already change the 
      // palette that is used for coloring
      public void select () {
      }
   }


   // VisualSettings.palette (4 subclasses)
   
   /**
      Changes the colors in the default palette.
   */
   public static class PaletteManagerComponentDefault extends PaletteManagerComponent {
      
      private static final long serialVersionUID = 1L;

      public PaletteManagerComponentDefault() {
         int length = VisualSettings.paletteDefault.length;
         initialValues = new Color[length];
         for (int i=0; i<length; i++) {
            initialValues[i] = ColorLib.getColor(VisualSettings.paletteDefault[i]);
         }
         super.init();
         label.setText("Default Palette");
         setName("Default palette");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         JPanel colorPane = (JPanel) arg0.getSource();
         int id = Integer.parseInt(colorPane.getName());
         super.mouseClicked(arg0);
         VisualSettings.paletteDefault[id] = ColorLib.color(chosenColor);
      }
      
      public void select () {
         for (int i=0; i<initialValues.length; i++) {
            VisualSettings.palette[i] = ColorLib.color(initialValues[i]);
         }
      }
   }
   
   /**
      Changes the first and last colors for interpolating a palette.
   */
   public static class PaletteManagerComponentInterpolated extends PaletteManagerComponent {
      
      private static final long serialVersionUID = 1L;
   
      public PaletteManagerComponentInterpolated() {
         int length = VisualSettings.interpolateColors.length;
         initialValues = new Color[length];
         for (int i=0; i<length; i++) {
            initialValues[i] = ColorLib.getColor(VisualSettings.interpolateColors[i]);
         }
         super.init();
         label.setText("Interpolate Colors");
         setName("Interpolated palette (2 colors)");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         JPanel colorPane = (JPanel) arg0.getSource();
         int id = Integer.parseInt(colorPane.getName());
         super.mouseClicked(arg0);
         VisualSettings.interpolateColors[id] = ColorLib.color(chosenColor);
      }
      
      public void select () {
         VisualSettings.palette = ColorLib.getInterpolatedPalette(
               VisualSettings.interpolateColors[0],
               VisualSettings.interpolateColors[1]);
      }
   }
   
   /**
      Changes the first, middle and last colors for interpolating a palette.
   */
   public static class PaletteManagerComponentInterpolatedThree extends PaletteManagerComponent {
      
      private static final long serialVersionUID = 1L;
   
      public PaletteManagerComponentInterpolatedThree() {
         int length = VisualSettings.interpolateColorsThree.length;
         initialValues = new Color[length];
         for (int i=0; i<length; i++) {
            initialValues[i] = ColorLib.getColor(VisualSettings.interpolateColorsThree[i]);
         }
         super.init();
         label.setText("Interpolate colors");
         setName("Interpolated palette (3 colors)");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         JPanel colorPane = (JPanel) arg0.getSource();
         int id = Integer.parseInt(colorPane.getName());
         super.mouseClicked(arg0);
         VisualSettings.interpolateColorsThree[id] = ColorLib.color(chosenColor);
      }
      
      public void select () {
         int[] palette1 = ColorLib.getInterpolatedPalette(
               VisualSettings.interpolateColorsThree[0],
               VisualSettings.interpolateColorsThree[1]);
         int[]  palette2 = ColorLib.getInterpolatedPalette(
               VisualSettings.interpolateColorsThree[1],
               VisualSettings.interpolateColorsThree[2]);

         VisualSettings.palette = new int[palette1.length+palette2.length];
            
         System.arraycopy(palette1, 0, VisualSettings.palette, 0, palette1.length); 
         System.arraycopy(palette2, 0, VisualSettings.palette, palette1.length, palette2.length); 
      }
   }

   /**
      Grayscale palette, not editable.
   */
   public static class PaletteManagerComponentGrayscale extends PaletteManagerComponent {
      
      private static final long serialVersionUID = 1L;
   
      public PaletteManagerComponentGrayscale() {
         // Display only 5 colors
         int length = 5;
         int interval = ColorLib.getGrayscalePalette().length / length;
         
         initialValues = new Color[length];
         for (int i=0; i<length; i++) {
            initialValues[i] = ColorLib.getColor(ColorLib.getGrayscalePalette()[i*interval]);
         }
         super.init();
         label.setText("Grayscale");
         setName("Palette Grayscale");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         // Disable editing
      }
      
      public void select () {
         VisualSettings.palette = ColorLib.getGrayscalePalette();
      }
      
   }
   
}
