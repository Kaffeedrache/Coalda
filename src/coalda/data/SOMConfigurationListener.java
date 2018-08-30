// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.awt.Component;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;
import coalda.base.Constants;
import coalda.base.Utils;
import coalda.data.Reader.SingleInfo;
import coalda.ui.SOMDisplay;
import coalda.ui.SOMTabbedPane;
import de.unistuttgart.ais.sukre.refinery.network.model.AbstractCalculationModelListener;
import de.unistuttgart.ais.sukre.refinery.network.model.SOMCalculationModel;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration;


/**

Makes settings and adjustments just before and after the Matlab SOM server calculates.

@author kesslewd
*/
public class SOMConfigurationListener extends AbstractCalculationModelListener {


   /**
      Calculate for feature vectors of selected nodes only
      or for all feature vectors.
   */
   private boolean calculateForSelected = false;

   /**
      Currently selected features.
   */
   private boolean[] selectedFeatures;

   /**
      The display tab containing the displays
      the user is playing with.
   */
   private static SOMTabbedPane displayTab;

   /**
      Writer of nice fresh feature vectors.
   */
   private FVExport fv = new FVExport();


   /**
      Constructor sets the display tab from the parameter.
      Calculates last FVID from database.
      
      @param display Display Tab where the user sees the visualizations.
   */
   public SOMConfigurationListener (SOMTabbedPane display) {
      displayTab = display;
   }


   /**
      Sets if to calculate for feature vectors of selected nodes only
      or for all feature vectors.
      
      @param selected True if we want only selected, false for all.
   */
   public void setCalculateForSelected (boolean selected) {
      calculateForSelected = selected;
   }


   /**
      Sets the currently selected features that will
      be used for the next calculation.
      
      @param selected True for every feature that is to be included, else false.
   */
   public void setSelectedFeatures (boolean[] selected) {
      selectedFeatures = selected;
   }


   /**
      Called just before Matlab starts the calculation.
      Here preprocessing is done, like selecting the features
      to calculate or for which feature vectors to calculate.
      
      @param model SOM Model.
      @param configuration SOM Configuration.
   */
   public void beforeCalculationStart (SOMCalculationModel model,
         SOMConfiguration configuration) {


      String fvs = ""; // which feature vectors (default : all = "")

      // Get selected map units
      if (calculateForSelected) {

         System.out.println("Selecting feature vectors to do calculation...");

         Component component = displayTab.getSelectedComponent();
         if (component instanceof SOMDisplay) {

            // Get selected nodes and their feature vectors
            SOMDisplay display = (SOMDisplay) component;
            TupleSet ts = display.getVisualization().getGroup(Visualization.FOCUS_ITEMS);
            Iterator<VisualItem> tupleIterator = ts.tuples();

            int nodes = 0;
            while (tupleIterator.hasNext()) {
               VisualItem vi = tupleIterator.next();
               nodes++; 
               if (vi.getInt(Constants.nodeFVNumber) != 0) {
                  fvs = fvs + " " + vi.getString(Constants.nodeFVectors);
               }
            }
            
            fvs = fvs.trim();
               
            // Sort FVs and set featurevectors in model,
            // if there are none, take all fvs.
            if (!fvs.equals("")) {
               fvs = Utils.sortFVs(fvs);
               System.out.println("FVs total: " + fvs);
               model.setFeatureVectorIds(fvs);
            } else {
               System.out.println("Error! No feature vectors selected!");
               System.out.println("Error! Taking all feature vectors!");
               model.setFeatureVectorIds(null);
               // TODO notify user graphically
            }
            
            System.out.println("...done");

         }
      } else {
         // We should calculate with all fvs (null)
         model.setFeatureVectorIds(null);
      }
      
      // Copy feature vectors from featurevectors table 
      // to featurevectorscalculation table.
      fv.copyToCalculate(selectedFeatures, fvs);
      
      System.out.println("Finished with preprocessing, now let's calculate");

   }


   /**
      Called when Matlab has finished the calculation, open Tab.      
      
      @param model Model used for the calculation.
   */
   public void calculationFinished (SOMCalculationModel model) {
      
      // Cannot create global writer because of SimpleORM threading problems
      Writer writer = Utils.makeWriter();
      
      System.out.println("Calculation finished; draw the SOM");
      int calculationId = model.getCalculationId().intValue();
      System.out.println("Calculation id: " + calculationId);
      if (calculationId > 0) {
         writer.setCalcID(calculationId);
         // Add normalization method in database
         writer.writeOneLine(SingleInfo.normalization, model.getSomConfig().getNormalizationMethod().toString());
         // Add selected features in database
         String usedFeaturesString = "";
         for (int i=1; i<=selectedFeatures.length; i++) {
            if (selectedFeatures[i-1]) {
               usedFeaturesString = usedFeaturesString + " " + i;
            }
         }
         writer.writeOneLine(SingleInfo.usedFeatures, usedFeaturesString);
         // Display
         displayTab.addSOMDisplay(calculationId);
      } else {
         System.out.println("Error in calculation !");
         // TODO notify user graphically
      }
   }



}