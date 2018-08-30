// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import prefuse.Constants;
import prefuse.action.assignment.DataColorAction;
import prefuse.util.ColorMap;
import prefuse.util.MathLib;
import prefuse.visual.VisualItem;


/**

DataColorActionPaletteChange colors the items according to the values of
the data field given (like a DataColorAction), while allowing the
change of the palette used.
Warning, this class is not fully tested!

@author kesslewd
*/
public class DataColorActionPaletteChange extends DataColorAction {

   /**
      The palette used for coloring the items.
   */
   private int[] m_palette;

   /**
      ColorMap for the association of items with colors.
   */
   private ColorMap m_cmap;

   /**
      True if the palette has changed, else false.
   */
   private boolean paletteChanged;

   /**
      Data distribution.
   */
   private double[] m_dist;

   /**
      Number of bins in the palette.
   */
   private int m_bins;


   /**
      Constructor.
     
      @param group the data group to process
      @param dataField the data field to base size assignments on
      @param dataType the data type to use for the data field. One of
         {@link prefuse.Constants#NOMINAL}, {@link prefuse.Constants#ORDINAL},
         or {@link prefuse.Constants#NUMERICAL}, for whether the data field
         represents categories, an ordered sequence, or numerical values.
      @param colorField the color field to assign
      @param palette the color palette to use. See
         {@link prefuse.util.ColorLib} for color palette generators.
   */
   public DataColorActionPaletteChange(String group, String dataField,
         int dataType, String colorField, int[] palette) {
      super(group, dataField, dataType, colorField, palette);
      m_palette = palette;
      paletteChanged = false;
      m_cmap = new ColorMap(null,0,1);
   }
   
   
   /**
      Set a new palette.
      
      @param newPalette Array of prefuse colors to use as color palette.
   */
   public void setPalette (int[] newPalette) {
      
      if (newPalette == null) {
         return;
      }
      
      m_palette = newPalette;
      paletteChanged = true;
      int size = 64;
      int[] palette;
      m_dist = getDistribution();
      m_bins = getBinCount();
      
      // compute distribution and color map
      switch ( super.getDataType() ) {
      case Constants.NOMINAL:
      case Constants.ORDINAL:
          m_dist = getDistribution();
          //size = m_omap.size();
          palette = (m_palette!=null ? m_palette : createPalette(size));
          m_cmap.setColorPalette(palette);
          m_cmap.setMinValue(m_dist[0]);
          m_cmap.setMaxValue(m_dist[1]);
          return;
      case Constants.NUMERICAL:
          m_dist = getDistribution();
          size = m_bins > 0 ? m_bins : size;
          palette = (m_palette!=null ? m_palette : createPalette(size));
          m_cmap.setColorPalette(palette);
          m_cmap.setMinValue(0.0);
          m_cmap.setMaxValue(1.0);
          return;
      }
   }
   
   
   /**
      Assignes delegates color assignments to its parent DataColorAction if the
      palette has not changed and for cascaded rules (selection highlighting p.e.).
      Else the same assignment is done as in DataColorAction, only using the
      new palette.
      @see prefuse.action.assignment.ColorAction#getColor(prefuse.visual.VisualItem)
      
      @param item The item where the color is to be determined.
      @return The number of the color to assign.
   */
   public int getColor(VisualItem item) {
      
      int itemColor = super.getColor(item);
      if (!paletteChanged) {
         return itemColor;
      }
      
      // check for any cascaded rules first
      // Needed for highlight of selected nodes
      Object o = lookup(item);
      if (o != null ) {
         return itemColor;
      }
      
      // otherwise perform data-driven assignment
      switch ( super.getDataType() ) {
      case Constants.NUMERICAL:
          double v = item.getDouble(super.getDataField());
          double f = MathLib.interp(super.getScale(), v, m_dist);
          return m_cmap.getColor(f);
      default:
          //Integer idx = (Integer)m_omap.get(item.get(super.getDataField()));
          //return m_cmap.getColor(idx.doubleValue());
         return 0; // TODO
      }
   }
}
