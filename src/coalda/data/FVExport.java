// Stefanie Wiltrud Kessler, September 2009 - July 2010

package coalda.data;


import java.sql.Connection;
import java.sql.SQLException;

import de.unistuttgart.ais.sukre.database.utils.DBConnectionProvider;


/**

Class to export features from the featurevector table elsewhere.

@author kesslewd
*/
public class FVExport {

   /**
      Copy feature vectors from featurevectors table 
      to featurevectorscalculation table.
      Use this before begin calculation.
      
      @param selectedFeatures true = use, false = not use
      @param whichVectors separated by spaces
    */
   public void copyToCalculate (boolean[] selectedFeatures, String whichVectors) {

      // Which fvs to calculate for
      String whichVectors2 = "";
      if (whichVectors != null && !whichVectors.trim().equals("")) {
          whichVectors2 = "where featurevector_id = any (ARRAY[" + whichVectors.replaceAll(" ", ",")+"])";
      }
      
      // Which features to calculate for
      String whichFeatures = "features";
      
      // See if we have to recalculate features
      boolean recalculate = false;
      for (boolean value : selectedFeatures) {
         // if one value is 'false', we need to recalculate
         if (!value) {
            recalculate = true;
            break;
         }
      }
      
      if (recalculate) {
         System.out.println("Calculating new feature vectors with selected features...");
         
         String sqlstatement = "";
         boolean first = true;
         for (int j=1;j<=selectedFeatures.length;j++) {
            if (selectedFeatures[j-1]) {
               if (!first) {
                  sqlstatement = sqlstatement + ",";
               }
               sqlstatement = sqlstatement + "features[" + j + "]";
               first = false;
            }
         }

         whichFeatures = "array[" + sqlstatement + "]";

      }
      
      
      Connection con = null;
      try {
         con = DBConnectionProvider.getInstanceFromDefault().getConnection();
         
         con.createStatement().execute("delete from featurevectorscalculation;");
         String sql = 
            "insert into featurevectorscalculation (featurevector_id, features)" +  
            "(select featurevector_id, " + whichFeatures + " from featurevectors "
            + whichVectors2
            + " order by featurevector_id);";
         System.out.println(sql);
         con.createStatement().execute(sql);
      } catch (SQLException e) {
         e.printStackTrace();
      } finally {
         if (con != null) {
            try {
               con.close();
            } catch (SQLException e) {
               e.printStackTrace();
            }
         }
      }
   }
   
   
}
