// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.


package coalda.ui;


import coalda.base.VisualSettings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**

Allows the user to change a size in VisualSettings.
The general behaviour is encapsulated in the superclass 
SizeChoserComponent which cannot be directly instanciated.

General behaviour means, that a label is displayed and text field.

For linking the general behaviour with a specific variable,
this component must be subclassed. The subclasses can define
the initial value of the textfield in the protected field
'initialValue' and the text of the label in 'label'. They have 
access to the value the user chooses in 'chosenValue' and can
set their variable to this value. 

@author kesslewd
*/
public class SizeChooserComponent extends JPanel 
      implements ActionListener, DocumentListener {
   

   /**
      Implement serializable.
   */
   protected static final long serialVersionUID = 1L;
   

   /**
      Initial value of the textfield.
   */
   protected int initialValue;

   /**
      The value the user chose.
   */
   protected int chosenValue;

   /**
      Label, displayted before the textfield.
   */
   protected JLabel label;

   /**
      Textfield
   */
   protected JTextField textField;
   

   /**
      Hide constructor.
      Only subclasses can be instantiated.
   */   
   private SizeChooserComponent() {
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
      textField =  new JTextField(initialValue+"", 3);
      textField.addActionListener(this);
      textField.getDocument().addDocumentListener(this);
      textField.setHorizontalAlignment(JTextField.RIGHT);
      
      add(label);
      add(textField);
   }


   /**
      On enter key change chosenValue to what the user entered.
   */
   public void actionPerformed(ActionEvent evt) {
      JTextField source = (JTextField)evt.getSource();
      String text = source.getText();
      chosenValue = Integer.parseInt(text);
   }


   /**
      Does nothing.
   */
   public void changedUpdate(DocumentEvent e) {
      // Never fired by a JTextField
   }


   /**
      On change in text field content,
      change chosenValue to what the user entered.
   */
   public void insertUpdate(DocumentEvent e) {
      String text = textField.getText();
      chosenValue = Integer.parseInt(text);
   }


   /**
      Does nothing.
   */
   public void removeUpdate(DocumentEvent e) {
   }

   
   // Subclasses for the different size variables we want to set.

   
   /**
      Changes the variable VisualSettings.nodeMinimumSize
      (minimum size of the nodes)
   */
   public static class SizeChoserComponentNodeMin extends SizeChooserComponent {
      
      private static final long serialVersionUID = 1L;

      public SizeChoserComponentNodeMin() {
         initialValue = VisualSettings.nodeMinimumSize;
         super.init();
         label.setText("Node minimum size: ");
      }

      public void actionPerformed(ActionEvent evt) {
         super.actionPerformed(evt);
         VisualSettings.nodeMinimumSize = chosenValue;
      }

      public void insertUpdate(DocumentEvent e) {
         super.insertUpdate(e);
         VisualSettings.nodeMinimumSize = chosenValue;
      }
   }
   
   
   /**
      Changes the variable VisualSettings.nodeMaximumSize 
      (maximum size of the nodes)
   */
   public static class SizeChoserComponentNodeMax extends SizeChooserComponent {
      
      private static final long serialVersionUID = 1L;
   
      public SizeChoserComponentNodeMax() {
         initialValue = VisualSettings.nodeMaximumSize;
         super.init();
         label.setText("Node maximum size: ");
      }
   
      public void actionPerformed(ActionEvent evt) {
         super.actionPerformed(evt);
         VisualSettings.nodeMaximumSize = chosenValue;
      }

      public void insertUpdate(DocumentEvent e) {
         super.insertUpdate(e);
         VisualSettings.nodeMaximumSize = chosenValue;
      }
   }
   
  
   /**
      Changes the variable VisualSettings.edgeMinimumSize
      (minimum size of the edges)
   */
   public static class SizeChoserComponentEdgeMin extends SizeChooserComponent {
      
      private static final long serialVersionUID = 1L;
   
      public SizeChoserComponentEdgeMin() {
         initialValue = VisualSettings.edgeMinimumSize;
         super.init();
         label.setText("Edge minimum size: ");
      }
   
      public void actionPerformed(ActionEvent evt) {
         super.actionPerformed(evt);
         VisualSettings.edgeMinimumSize = chosenValue;
      }

      public void insertUpdate(DocumentEvent e) {
         super.insertUpdate(e);
         VisualSettings.edgeMinimumSize = chosenValue;
      }
   }
   
   
   /**
      Changes the variable VisualSettings.edgeMaximumSize 
      (maximum size of the edges)
   */
   public static class SizeChoserComponentEdgeMax extends SizeChooserComponent {
      
      private static final long serialVersionUID = 1L;
   
      public SizeChoserComponentEdgeMax() {
         initialValue = VisualSettings.edgeMaximumSize;
         super.init();
         label.setText("Edge maximum size: ");
      }
   
      public void actionPerformed(ActionEvent evt) {
         super.actionPerformed(evt);
         VisualSettings.edgeMaximumSize = chosenValue;
      }

      public void insertUpdate(DocumentEvent e) {
         super.insertUpdate(e);
         VisualSettings.edgeMaximumSize = chosenValue;
      }
   }

   
}
