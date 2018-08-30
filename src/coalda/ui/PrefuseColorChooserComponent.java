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

Allows the user to chose a color for a given variable.
The general behaviour is encapsulated in the superclass 
PrefuseColorChooserComponent which cannot be directly instanciated.

General behaviour means, that a label is displayed and a 10x10 box
with a color. By clicking on the box a ColorChoser is opened and
a color can be chosen. The box changes color accordingly.

For linking the general behaviour with a specific variable,
this component must be subclassed. The subclasses can define
the initial color of the box in the protected field
'initialValue' and the text of the label in 'label'. They have 
access to the color the user chooses in 'chosenColor' and can
set their variable to this value. 

@author kesslewd
*/
public class PrefuseColorChooserComponent extends JPanel implements MouseListener {
   

   /**
      Iimplement serializable.
   */
   protected static final long serialVersionUID = 1L;
   

   /**
      Initial color of the box.
   */
   protected Color initialValue;

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
   protected JPanel colorPane;
   

   /**
      Hide constructor.
      Only subclasses can be instantiated.
   */   
   private PrefuseColorChooserComponent() {
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

      chosenColor = initialValue;
      
      label = new JLabel();
      label.setHorizontalAlignment(JLabel.LEFT);
      
      colorPane = new JPanel();      
      colorPane.setBackground(initialValue);
      colorPane.setSize(10, 10);
      colorPane.addMouseListener(this);
      colorPane.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
      
      add(label);
      add(colorPane);
   }


   /**
      On click let user chose color and change
      the background of the panel.
      Subclasses additionally set the variable
      they want to the corresponding color.
   */
   public void mouseClicked(MouseEvent arg0) {
     Color newColor = JColorChooser.showDialog(
              this,
              "Choose Color",
              initialValue
           );
     if (newColor != null) {
        chosenColor = newColor;
        colorPane.setBackground(newColor);
        initialValue = newColor;
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

   
   // Subclasses for the different color variables we want to set.

   
   /**
      Changes the variable VisualSettings.nodeTextColor 
      (color of the text inside the nodes)
   */
   public static class PrefuseColorChoserComponentTextColor extends PrefuseColorChooserComponent {
      
      private static final long serialVersionUID = 1L;

      public PrefuseColorChoserComponentTextColor() {
         initialValue = ColorLib.getColor(VisualSettings.nodeTextColor);
         super.init();
         label.setText("Node text color:");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         super.mouseClicked(arg0);
         VisualSettings.nodeTextColor = ColorLib.color(chosenColor);
      }
   }
   

   /**
      Changes the variable VisualSettings.highlightColor
      (color of highlighted nodes)
   */
   public static class PrefuseColorChoserComponentHighlighted extends PrefuseColorChooserComponent {
      
      private static final long serialVersionUID = 1L;

      public PrefuseColorChoserComponentHighlighted() {
         initialValue = ColorLib.getColor(VisualSettings.highlightColor);
         super.init();
         label.setText("Highlighted node color:");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         super.mouseClicked(arg0);
         VisualSettings.highlightColor = ColorLib.color(chosenColor);
      }
      
   }

   
   /**
      Changes the variable VisualSettings.zeroColor
      (color of edges with value zero in the ConnVis visualization)
   */
   public static class PrefuseColorChoserComponentZeroColor extends PrefuseColorChooserComponent {
      
      private static final long serialVersionUID = 1L;

      public PrefuseColorChoserComponentZeroColor() {
         initialValue = ColorLib.getColor(VisualSettings.zeroColor);
         super.init();
         label.setText("Zero value color:");
      }
      
      public void mouseClicked(MouseEvent arg0) {
         super.mouseClicked(arg0);
         VisualSettings.zeroColor = ColorLib.color(chosenColor);
      }
      
   }
   
}
