// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.data;

import coalda.data.Reader.SingleInfo;

/**

Writes things.

@author kesslewd

*/
public abstract class Writer {
   

   /**
      ID of the calculation that is imported at the moment
   */
   protected int calcID;
   
   /**
      Writes one line of data.
      What data depends on the type of SingleInfo.
      
      @param type Type of information to write.
      @param type The information to write.
   */
   public abstract void writeOneLine(SingleInfo type, String content);

   /**
      Sets the current calculation ID.
      
      @param calculationID A valid calculation ID.
   */
   public void setCalcID(int calculationID) {
      calcID = calculationID;
   }

   
   
   
   

}
