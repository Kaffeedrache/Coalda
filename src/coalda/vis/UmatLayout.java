// Stefanie Wiltrud Kessler, September 2009 - April 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;


import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.RungeKuttaIntegrator;
import prefuse.visual.EdgeItem;

import coalda.base.Constants;


public class UmatLayout extends ForceDirectedLayout {


static ForceSimulator fsim = new ForceSimulator(new RungeKuttaIntegrator());

/*
	static float gravConstant = -1f; 
	static float minDistance = -1f;
	static float theta = 0.9f;

	static float drag = 0.01f; 
	static float springCoeff = 1E-4f;  
	static float defaultLength = 150f;  //default: 50f

static {
	        fsim.addForce(new NBodyForce(gravConstant, minDistance, theta));
	        fsim.addForce(new DragForce(drag));
	        fsim.addForce(new SpringForce(springCoeff, defaultLength));
}

*/


   public UmatLayout (String group) {
      super(group, false, true); // bounderies = false, runonce = true, 
//	    super(group, fsim, false, true);
      setIterations(200); // 200 is enough with pre-layout, 500 without
   }

//        public UmatLayout (String group,
//		ForceSimulator fsim2, boolean enforceBounds) {
//	    super(group, fsim2, enforceBounds, false);
//	}

   protected float getSpringLength(EdgeItem e) {

      int value = 0;
      if ( e.canGetString(Constants.edgeUmatValue) ) {
         value = e.getInt(Constants.edgeUmatValue);
      }

      // Make differences more visible
      value = value * 20;

      // Avoid zero
      value += 1;

      // Return
      return value;

   }

/*
	// two further possibilities to customize ....
	protected float getMassValue(VisualItem n) {
	    return 1.0f;
	}

	protected float getSpringCoefficient(EdgeItem e) {
	    return -1; 
	}

*/
}
