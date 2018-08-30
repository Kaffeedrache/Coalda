// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import prefuse.visual.VisualItem;


/**

ZeroDataColorAction colors the items according to the values of
the data field given (like a DataColorAction), while items with 
value zero get a special color different from the rest.

@author kesslewd
*/
public class ZeroDataColorAction extends DataColorActionPaletteChange {

   /**
      Nodes are colored according to their value in this field. 
   */
   private String m_dataField;

   /**
      Color for the nodes that have value zero in this field.
   */
   private int m_zeroColor;
   
   
   /**
      Create a new ZeroDataColorAction where value zero gets a special color.
     
      @param group the data group to process
      @param dataField the data field to base size assignments on
      @param dataType the data type to use for the data field. One of
         {@link prefuse.Constants#NOMINAL}, {@link prefuse.Constants#ORDINAL},
         or {@link prefuse.Constants#NUMERICAL}, for whether the data field
         represents categories, an ordered sequence, or numerical values.
      @param colorField the color field to assign
      @param palette the color palette to use. See
         {@link prefuse.util.ColorLib} for color palette generators.
      @param zeroColor The color to be used only for the items
         with value zero in the data field.
   */
   public ZeroDataColorAction(String group, String dataField, 
           int dataType, String colorField, int[] palette, int zeroColor)
   {
      super(group, dataField, dataType, colorField, palette);
      m_dataField = dataField;
      m_zeroColor = zeroColor;
   }
   
  
   /**
      Create a new ZeroDataColorAction where value zero gets a special color.
     
      @param group the data group to process
      @param dataField the data field to base size assignments on
      @param dataType the data type to use for the data field. One of
         {@link prefuse.Constants#NOMINAL}, {@link prefuse.Constants#ORDINAL},
         or {@link prefuse.Constants#NUMERICAL}, for whether the data field
         represents categories, an ordered sequence, or numerical values.
      @param colorField the color field to assign
      @param palette the color palette to use. See
         {@link prefuse.util.ColorLib} for color palette generators.
         The first color in the palette will be used only for the items
         with value zero in the data field. The other are assigned
         as in the DataColorAction.
   */
   public ZeroDataColorAction(String group, String dataField, 
           int dataType, String colorField, int[] palette)
   {
      super(group, dataField, dataType, colorField, deleteFirstEntry(palette));
      m_dataField = dataField;
      m_zeroColor = palette[0];
   }
   

   /**
      Assignes the zero-Color to all elements where the value is zero
      and delegates the other color assignments to its parent DataColorAction.
      @see prefuse.action.assignment.ColorAction#getColor(prefuse.visual.VisualItem)
      
      @param item The item where the color is to be determined.
      @return The number of the color to assign.
   */
   public int getColor(VisualItem item) {
      double v = item.getDouble(m_dataField);
      if (v==0) {
         return m_zeroColor;
      } else {
         return super.getColor(item);
      }
      
   }
   
   
   /**
      Deletes the first entry of an array and copies the
      rest into a new array that is returned.
      
      @param array The array where the first entry should be deleted
      @return A new array containing all entries of the parameter but the first
   */
   private static int[] deleteFirstEntry (int[] array) {
      int[] copy = new int[array.length-1];
      System.arraycopy(array,1,copy,0,copy.length);
      return copy;
   }
   
   
   /**
      Sets the color to use for items with value zero.
     
      @param zeroColor The color to be used only for the items
         with value zero in the data field.
   */
   public void setZeroColor(int zeroColor) {
      m_zeroColor = zeroColor;   
   }

}
