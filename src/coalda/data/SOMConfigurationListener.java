// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import coalda.vis.SOMTabbedPane;

import java.awt.Component;
import java.sql.PreparedStatement;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;

import coalda.base.Constants;
import coalda.base.DBAccess;
import coalda.data.FVImport;
import coalda.vis.SOMDisplay;

import de.unistuttgart.ais.sukre.refinery.network.model.AbstractCalculationModelListener;
import de.unistuttgart.ais.sukre.refinery.network.model.SOMCalculationModel;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration;


/**
@author kesslewd

Makes settings and adjustments just before and after the Matlab SOM server calculates.

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
      ID of feature vector having maximum ID
      of those who have the maximum number
      of features.
   */
   private int originalLastFV;

   /**
      The display tab containing the displays
      the user is playing with.
   */
   private static SOMTabbedPane displayTab;

   /**
      Provider of nice fresh feature vectors.
   */
   private FVImport fv = new FVImport();


   /**
      Constructor.
      Set the display tab from the parameter.
      Calculates last FVID from database.
      
      @param display Display Tab where the user sees the visualizations.
   */
   public SOMConfigurationListener (SOMTabbedPane display) {
      originalLastFV = fv.getMaxFVID();
      displayTab = display;
   }



   /**
      Constructor.
      Set the display tab from the parameter.
      
      @param originalLastFVID Maximum FVID that belongs to a 
         vector that has the original number of features.
      @param display Display Tab where the user sees the visualizations.
   */
   public SOMConfigurationListener (int originalLastFVID, SOMTabbedPane display) {
      originalLastFV = originalLastFVID;
      displayTab = display;
   }


   /**
      Method setCalculateForSelected.
      Sets if to calculate for feature vectors of selected nodes only
      or for all feature vectors.
      
      @param selected True if we want only selected, false for all.
   */
   public void setCalculateForSelected (boolean selected) {
      calculateForSelected = selected;
   }


   /**
      Method setSelectedFeatures.
      Sets the currently selected features that will
      be used for the next calculation.
      
      @param selected True for every feature that is to be included, else false.
   */
   public void setSelectedFeatures (boolean[] selected) {
      selectedFeatures = selected;
   }


   /**
      Method beforeCalculationStart.
      Called just before Matlab starts the calculation.
      Here preprocessing is done, like selecting the features
      to calculate or for which feature vectors to calculate.
      
      @param model SOM Model.
      @param configuration SOM Configuration.
   */
   public void beforeCalculationStart (SOMCalculationModel model,
         SOMConfiguration configuration) {

      //get selected Features & recalculate if needed;
      boolean recalculate = false;
      for (boolean value : selectedFeatures) {
         // if one value is 'false', we need to recalculate
         if (!value) {
            recalculate = true;
            break;
         }
      }

      int offset = 0;
      int biggest = 0;

      if (recalculate && !calculateForSelected) { // TODO now not possible recalculate for selection!

         System.out.println("Calculating new feature vectors with selected features...");

         DBAccess db = new DBAccess();
         String offsetString = db.stringQuery("select max(featurevector_id) from featurevectors;"); // TODO
         offset = Integer.parseInt(offsetString);

         // For which feature vectors to recalculate 
         String sqlstatement = "insert into featurevectors values "
               + "((select max(featurevector_id)+1 from featurevectors), "
               + "(select array["
               ;
         boolean first = true;
         for (int j=0;j<selectedFeatures.length;j++) {
            if (selectedFeatures[j]) {
               if (!first) {
                  sqlstatement = sqlstatement + ",";
               }
               int id = j+1;
               sqlstatement = sqlstatement + "features[" + id + "]";
               first = false;
            }
         }
         sqlstatement = sqlstatement 
               + "] from featurevectors where featurevector_id=?),"
               + "(select link_id from featurevectors where featurevector_id=?));";
         PreparedStatement prepstatement = db.prepareStatement(sqlstatement);

         // Make new feature vectors with subset of features and insert to db
         biggest = offset + originalLastFV;
         for (int i=1; i<=originalLastFV; i++) {
            db.executePreparedStatement(prepstatement, i, i);
         }

         System.out.println("...done.");

      }

      // get selected map units
      if (calculateForSelected) {

         System.out.println("Selecting feature vectors to do calculation...");

         Component component = displayTab.getSelectedComponent();
         if (component instanceof SOMDisplay) {

            // Get selected nodes and their feature vectors
            SOMDisplay display = (SOMDisplay) component;
            TupleSet ts = display.getVisualization().getGroup(Visualization.FOCUS_ITEMS);
            Iterator<VisualItem> tupleIterator = ts.tuples();

            String fvs = "";
            String nodes = "";
            while (tupleIterator.hasNext()) {
               VisualItem vi = tupleIterator.next();
               nodes = nodes + " " + vi.getInt(Constants.nodeKey); 
               if (vi.getInt(Constants.nodeFVNumber) != 0) {
                  fvs = fvs + " " + vi.getString(Constants.nodeFVectors);
               }
            }
            fvs = fvs.trim();

            if (!fvs.equals("")) { // there are fvs
               System.out.println("FVs total: " + fvs);
               model.setFeatureVectorIds(fvs);
            }
            System.out.println("...done");

         }
      } else {
         // We should calculate with all fvs (null)
         model.setFeatureVectorIds(null);

         // if we don't take original vectors, but
         // the recalculated ones, map them
         if (offset != 0) {
            String ids = "";
            for (int i=offset+1; i<biggest; i++) {
               ids = ids + " " + i;
            }
            ids = ids.trim();
            System.out.println("FVs total: " + ids);
            model.setFeatureVectorIds(ids);
         }
      }

      System.out.println("Finished with preprocessing, now let's calculate");

   }


   /**
      Method calculationFinished.
      Called when Matlab has finished the calculation.
      
      @param model Model used for the calculation.
   */
   public void calculationFinished (SOMCalculationModel model) {
      System.out.println("Calculation finished; draw the SOM");
      System.out.println("Calculation id: " + model.getCalculationId());
      displayTab.addSOMDisplay(model.getCalculationId().intValue());
   }



}