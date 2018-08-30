// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;


import coalda.base.Constants;

import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.visual.EdgeItem;


/**

Layouts a graph using force directed layout.

@author kesslewd
*/
public class UmatLayout extends ForceDirectedLayout {

   /**
      Factor used to multiply the U-Matrix value with,
      to make differences in value bigger. 
   */
   private int strechFactor = 50;
   

   /**
      Iterations for the coordinates calculation in the force-directed layout.
      About 200 is enough with pre-layout, 500 without.
   */
   private int iterations = 200;
   

   /**
      Constructor.
      @param group Which visual group to layout.
   */
   public UmatLayout (String group) {
      super(group, false, true); // bounderies = false, runonce = true, 
      setIterations(iterations); 
   }

   
   /**
      Length of the edges, based on U-matrix value.
      @param group Which visual group to layout.
   */
   protected float getSpringLength(EdgeItem e) {

      float value = 0;
      if ( e.canGetString(Constants.edgeUmatValue) ) {
         value = e.getFloat(Constants.edgeUmatValue);
      }

      // Make differences more visible
      value = value * strechFactor;

      // Avoid zero
      value += 1;

      // Return
      return value;

   }

   
   // Further possibilities to customize include:
   // - mass value of nodes (override getMassValue)
   // - spring coefficient of edges (override getSpringCoeficient)
   // - use custom force simulator

}
