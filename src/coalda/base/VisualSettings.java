// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.base;


import prefuse.util.ColorLib;


/**

This class contains a few settings for constants that influence the
visual appearance.

@author kesslewd

*/
public class VisualSettings {

   
   /**
      First, middle and last color for interpolating a palette.
   */
   public static int[] interpolateColorsThree = new int[] { 
      ColorLib.rgb(0,0,255),
      ColorLib.rgb(0,200,0),
      ColorLib.rgb(255,0,0)
   };

   /**
      First and last color for interpolating a palette.
   */
   public static int[] interpolateColors = new int[] { 
      ColorLib.rgb(0,0,255),
      ColorLib.rgb(255,0,0)
   };

   /**
      Default palette.
   */
   public static int[] paletteDefault = new int[] { 
      ColorLib.rgb(0,0,255),
      ColorLib.rgb(0,100,128),
      ColorLib.rgb(0,200,0),
      ColorLib.rgb(128,128,0),
      ColorLib.rgb(255,0,0)
   };
   
   /**
      Palette for the colors of nodes and edges.
   */
   public static int[] palette = paletteDefault;
  
   /**
      Color of highlighted nodes.
   */
   public static int highlightColor = ColorLib.rgb(255,200,125);

   /**
      Color of text inside the nodes.
   */
   public static int nodeTextColor = ColorLib.rgb(255,255,255);

   
   /**
      Palette for the colors of edges with ConnVis.
   */
   public static int[] paletteConnVis = new int[] { 
      ColorLib.rgb(255,200,0),
      ColorLib.rgb(0,200,0),
      ColorLib.rgb(0,0,255),
      ColorLib.rgb(255,0,0) 
   };
   
   /**
      Color for edges with value zero in the ConnVis visualization.
   */
   public static int zeroColor = ColorLib.rgb(255,255,255);
   
   
   /**
      Maximum size for nodes.
   */
   public static int nodeMaximumSize = 3;

   /**
      Minimum size for nodes.
   */
   public static int nodeMinimumSize = 1;

   
   /**
      Maximum size for edges.
   */
   public static int edgeMaximumSize = 2;

   /**
      Minimum size for edges.
   */
   public static int edgeMinimumSize = 2;
   
   
}
