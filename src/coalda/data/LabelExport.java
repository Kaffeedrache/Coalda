// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.


package coalda.data;


import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryResult;
import simpleorm.dataset.SSelectMode;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SLog;
import de.unistuttgart.ais.sukre.database.model.FeatureVectorModel;
import de.unistuttgart.ais.sukre.database.model.LinkModel;
import de.unistuttgart.ais.sukre.database.utils.DBConnectionProvider;


/**

Abstraction between the database and the GUI,
exports the labels the user assigns to feature vectors.

@author kesslewd
*/

public class LabelExport {


   /**
      Debug mode. A lot of things on stdout.
   */
   private boolean debug = false;
   

   /**
      Connection to the DB    
   */
   private SSessionJdbc ses = null;

   
   /**
      Connect to the database.
   */
   public LabelExport () {
      Connection con = null;
      try {
         con = DBConnectionProvider.getInstanceFromDefault().getConnection();
      } catch (SQLException e) {
         e.printStackTrace();
         return;
      }
      ses = SSessionJdbc.getThreadLocalSession();
      SLog.getSessionlessLogger().setLevel(0);
      if (ses == null) {
         ses = SSessionJdbc.open(con, "Label Feature Vectors");
         ses.begin();
      }        
   }
   
   
   
   /**
      Labels the feature vectors
      with the label and confidence value given.
      FV IDs must be separated by spaces.
      See Constants for possible values for
      label and confidence value.

      @param featureVectors The feature vectors to be labeled.
      @param label Which label to assign
      @param confidence Confidence value
   */
   public void labelFVs (String featureVectors, int label, double confidence) {

      // Trim String and create Array of Integers
      String fvIDString = featureVectors.trim();      
      String[] fvs = fvIDString.split(" ");
      Integer[] fvIDs= new Integer[fvs.length];
      for (int i = 0; i < fvs.length; i++) {
         fvIDs[i] = new Integer(Integer.parseInt(fvs[i]));
      }
      
      // Get all links for the featureVectors with the given IDs
      SQuery<LinkModel> lq = new SQuery<LinkModel>(LinkModel.META);
      lq.innerJoin(FeatureVectorModel.LINK)
         .select(SSelectMode.SNONE)
         .in(FeatureVectorModel.ID, (Object[]) fvIDs)
         .ascending(LinkModel.ID);
      SQueryResult<LinkModel> links = ses.query(lq);
      
      // Iterate over all links and set label and confidence value
      Iterator<LinkModel> it = links.iterator();
      while (it.hasNext()) {
         LinkModel current = it.next();
         current.setLabelAssigned(label);
         current.setConfidence(new Double(confidence));
         if (debug) {
            System.out.println("Set label of link " + 
                  current.getID()
                  + " to " + current.getLabelAssigned()
                  + ", confidence " + current.getConfidence() );
         }
         // Check for error
         // TODO float comparison
         if (current.getLabelAssigned().intValue() != label || current.getConfidence().intValue() != confidence) {
            System.out.println("Error in labeling of link " + current.getID());
         }
      }
      // Let changes take effect.
      ses.commit();
      ses.begin();
      
   }
  
   
}
