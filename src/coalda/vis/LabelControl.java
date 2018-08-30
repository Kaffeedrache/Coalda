// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;
import coalda.data.LabelExport;

import prefuse.Display;
import prefuse.controls.FocusControl;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;

import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;


/**

Allows the user to label all feature vectors of a node
by performing a double click on it.

@author kesslewd
*/
public class LabelControl extends FocusControl {


   /**
      Constructor with number of clicks to trigger action.
      @param clicks Number of Clicks to trigger action.
   */
   public LabelControl (int clicks) {
      super(clicks);
   }


   /**
      Called when a user clicks on an item, does the labeling.
      Values for the labeling are asked from the user in dialogs.
      @param item The visual Item the user has clicked on.
      @param e Mouse event that triggered the call.
   */
   public void itemClicked(VisualItem item, MouseEvent e) {
      
      // Only do something if correct button has been pressed
      // with the correct number of clicks.
      if ( UILib.isButtonPressed(e, super.button) 
            && e.getClickCount() == super.ccount ) {

         // Get source display
         Display display = (Display)e.getSource();

         // Let user chose a label
         String labelResult = (String)JOptionPane.showInputDialog(
                  display,
                  "Please chose label:",
                  "Labeling",
                  JOptionPane.PLAIN_MESSAGE,
                  null,
                  Constants.possibleLabels,
                  Constants.possibleLabels[0]);

         if (labelResult != null) {

            // Let user chose a confidence value
            String labelConfidence = (String)JOptionPane.showInputDialog(
                  display,
                  "Please enter confidence value (0..100):",
                  "Labeling",
                  JOptionPane.PLAIN_MESSAGE);
            double confidence = Float.parseFloat(labelConfidence);

            int label = Constants.possibleLabelValues[2]; // unlabeled = -1

            if (labelResult.equals(Constants.possibleLabels[0])) { // coref = 1
               label = Constants.possibleLabelValues[0];
            }
            if (labelResult.equals(Constants.possibleLabels[1])) { // disref = 0
               label = Constants.possibleLabelValues[1];
            }

            // Get feature vectors associated with this node
            String fvs = item.getString(Constants.nodeFVectors);
            
            // Set label for all these feature vectors
            LabelExport le = new LabelExport();
            le.labelFVs (fvs, label, confidence);

         }

         // Just do whatever super does...
         super.itemClicked (item,e);
         }
      }


}
