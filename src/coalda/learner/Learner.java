// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.learner;


import coalda.data.CalculationAccess;

import de.unistuttgart.ais.sukre.refinery.network.model.SOMCalculationModel;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.ISOMPropertiesConstants;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMCalculation;
import de.unistuttgart.ais.sukre.somserver.matlab.calculation.SOMConfiguration;


/**
@author kesslewd

Manages the connection to the SOM Server for the calculation of the SOM.

*/
public class Learner {

   /**
      The configuration of the SOM to calculate.
   */
   private SOMCalculationModel somConfig;


   /**
      Constructor.
      @param configComponent The configuration of the SOM to calculate.
   */
   public Learner (SOMCalculationModel configComponent) {
      somConfig = configComponent;
   }


   /**
      Method calculateForAllFVs.
      Calculates a SOM with all the feature vectors in the database.
      @return The calculation ID of the SOM.
   */
   public int calculateForAllFVs () {
      return calculateForFVs("");
   }


   /**
      Method calculateForFVs.
      Calculates a SOM with the selected feature vectors.
      If passed an empty String, all feature vectors are taken.
      @param Feature vectors to calculate the SOM with.
      @return The calculation ID of the SOM.
   */
   public int calculateForFVs (String fvs) {

      // Two spaces annoy matlab
      String myfvs = "";
      if (fvs != null) {
         myfvs = fvs.trim();
      }

      // Get current config
      SOMConfiguration c = somConfig.getSomConfig();

      // Add selected feature vectors to config.
      if (!myfvs.equals("")) {
         c.put(ISOMPropertiesConstants.CALCULATION_FEATURE_VECTOR_IDS, myfvs);
      }

      // Get calculation ID
      Integer calcID = c.getCalculationID();
      if (calcID == null ) {
         CalculationAccess ca = new CalculationAccess();
         calcID = ca.getNextCalcID();
         c.setCalculationID(calcID.intValue());
      }

      // Let Matlab do the calculation
      SOMCalculation smc = new SOMCalculation(c);
      smc.run();

      return calcID.intValue();
   }

}
