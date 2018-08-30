// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;


import java.sql.Connection;
import java.sql.SQLException;

import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;
import coalda.data.Writer;
import coalda.data.Reader.SingleInfo;
import de.unistuttgart.ais.sukre.database.model.CalculationModel;
import de.unistuttgart.ais.sukre.database.utils.DBConnectionProvider;


/**

Writes things to the db using SimpleORM.

@author kesslewd

*/
public class WriteToORM extends Writer {

   

   /**
      Connection to the DB    
   */
   private SSessionJdbc ses = null;
   

   /**
      Constructor creates connection to DB
   */
   public WriteToORM() {
      
      // Set Log Level = 0 to avoid annoying outputs.
      // Errors are still shown.
      SLog.getSessionlessLogger().setLevel(0);
      
      // Open a connection
      Connection con = null;
      try {
         con = DBConnectionProvider.getInstanceFromDefault().getConnection();
      } catch (SQLException e) {
         e.printStackTrace();
         return;
      }
      
      // Open a session
      ses = SSessionJdbc.getThreadLocalSession();
      if (ses == null) {
         ses = SSessionJdbc.open(con, "Data Export");
         ses.begin();
      }
      
   }
   
   
   

   /**
      Writes one line of data.
      What data depends on the type of SingleInfo.
      
      @param type Type of information to write.
      @param type The information to write.
   */
   public void writeOneLine(SingleInfo type, String content) {
      CalculationModel c = ses.find(CalculationModel.META, new Integer(calcID));
      switch (type) {
         case normalization:
            c.setNormalization(content);
            break;
         case usedFeatures:
            content = content.trim();
            String[] featuresString = content.split(" ");
            Integer[] features = new Integer[featuresString.length];
            for (int i=0; i<features.length; i++) {
               features[i] = new Integer(Integer.parseInt(featuresString[i]));
            }
            c.setFeatures(features);
            break;
         default: // no valid information to write
      }

      // Let changes take effect.
      ses.commit();
      
      // Begin for next thing we want
      ses.begin();
   
   
   }


}
