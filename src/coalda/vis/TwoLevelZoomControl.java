// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.

package coalda.vis;

import coalda.base.Constants;
import coalda.ui.SOMTabbedPane;

import prefuse.Display;
import prefuse.controls.AbstractZoomControl;
import prefuse.visual.VisualItem;

import java.awt.geom.Point2D;


/**

Implements a two-level zoom.
Up until a given leven zoom is just normal zoom.
If a user zooms beyond this level on a node, 
a new SOM is calculated for the feature vectors
of this node.

@author kesslewd
*/
public class TwoLevelZoomControl extends AbstractZoomControl {


   /**
      Minimal zoom level.
   */
   protected double m_minScale = DEFAULT_MIN_SCALE;

   /**
      Maximal zoom level.
   */
   protected double m_maxScale = 30;

   /**
      Allow zoom while mouse is over items.
   */
   protected boolean m_zoomOverItem = true;

   /**
      Change zoom level.
   */
   public final double CHANGE_VALUE = 3;

   /**
      Debug mode.
   */
   private boolean debug =  false;

   /**
      The display tabbed pane this zoom works on.
   */
   private SOMTabbedPane tab;


   /**
      Constructor with display it works on.
      @param displayPane The display tabbed pane this zoom works on.
   */
   public TwoLevelZoomControl (SOMTabbedPane displayPane) {
      tab = displayPane;
   }


   /**
      Zoom the given display at the given point by the zoom factor,
      in either absolute (item-space) or screen co-ordinates.
      @param display the Display to zoom
      @param p the point to center the zoom upon
      @param zoom the scale factor by which to zoom
      @param abs if true, the point p should be assumed to be in absolute
         coordinates, otherwise it will be treated as screen (pixel) coordinates
      @return a return code indicating the status of the zoom operation.
         One of {@link #ZOOM}, {@link #NO_ZOOM}, {@link #MIN_ZOOM},
         {@link #MAX_ZOOM}.
   */
   protected int zoom (Display display, Point2D p, double zoom, boolean abs) {
      return zoom (display, p, zoom, abs, null);
   }


   /**
      Zoom the given display at the given point by the zoom factor,
      in either absolute (item-space) or screen co-ordinates.
      @param display the Display to zoom
      @param p the point to center the zoom upon
      @param zoom the scale factor by which to zoom
      @param abs if true, the point p should be assumed to be in absolute
         coordinates, otherwise it will be treated as screen (pixel) coordinates
      @param item The item zoomed on.
      @return a return code indicating the status of the zoom operation.
         One of {@link #ZOOM}, {@link #NO_ZOOM}, {@link #MIN_ZOOM},
         {@link #MAX_ZOOM}.
   */
   protected int zoom (Display display, Point2D p, double zoom,
         boolean abs, VisualItem item) {

      // don't zoom if a transformation is in progress
      if ( display.isTranformInProgress() )
         return NO_ZOOM;

      int status = ZOOM;

      if (debug) {
         if (item != null) {
            System.out.println("I'm zooming with " + zoom 
               + "on an item of type " + item.getString(Constants.kind) );
         } else {
            System.out.println("I'm zooming with " + zoom );
         }
      }

      // Turn zoom around to get more intuitive zooming
      // turning mousewheel forward (zoom<1) zooms in
      // turning mousewheel back (zoom>1) zooms out
      double newzoom = zoom;

      if (newzoom<1) {
         newzoom = 1+(1-newzoom);
      } else {
         newzoom = 1-(newzoom-1);
      }
      if (debug) {
         System.out.println("zoom: " + zoom);
         System.out.println("inverted zoom: " + newzoom);
      }

      // Where in total zoom were we before?
      double scale = display.getScale();
      // Have we been in detail view before?
      boolean detailbefor = ( scale > CHANGE_VALUE );
      if (debug) {
         System.out.println("scale: " + scale);
         if (detailbefor) {
            System.out.println("We have been in Detailview!");
         } else {
            System.out.println("We have been in Overview!");
         }
      }

      // Where in total zoom are we now, after zoom?
      double result = scale * newzoom;
      // Are we in detail view afterwards?
      boolean detailafter = ( result > CHANGE_VALUE );
      if (debug) {
         System.out.println("total Zoom: " + result);
         if (detailafter) {
            System.out.println("We are in Detailview!");
         } else {
            System.out.println("We are in Overview!");
         }
      }

      // Check for minimum/maximum scale
      if ( result < m_minScale ) {
         newzoom = m_minScale/scale;
         if (debug) {
            System.out.println("< minScale " + newzoom);
         }
         status = MIN_ZOOM;
      } else if ( result > m_maxScale ) {
         newzoom = m_maxScale/scale;
         if (debug) {
            System.out.println("> MaxScale " + newzoom);
         }
         status = MAX_ZOOM;
      }
      // Check on final zoom value
      if (debug) {
         System.out.println("please zoom with " + newzoom);
      }

      // Check if we have to change the view 
      if (detailbefor && !detailafter) {
         if (debug) {
            System.out.println("We have to change from detail to overview");
         }
         // Do thing in current configuration

      } else if (detailafter && !detailbefor) {
         if (debug) {
            System.out.println("We have to change from overviwe to detail");
         }
         
         // Zoom only if 
         // - we zoom on an item
         // - this item is a map unit (node)
         // - this map unit has associated feature vectors
         // [watch out for sequence (&&), item != null and only then item.X]
         if (item != null 
                  && item.getString(Constants.kind).equals(Constants.nodeKind) 
                  && (item.getInt(Constants.nodeFVNumber) > 0) ) {
            
            // Add a new tab with the new calculation.
            tab.addSOMDisplay(item.getString(Constants.nodeFVectors), item.getString(Constants.nodeKey));
         
         }
         
         // Leave the original display as it was.
         newzoom = 1.0;
         
      } else {
         if (debug) {
            System.out.println("No change");
         }
         
      }

      // Finally DO the zoom
      if ( abs ) {
         display.zoomAbs(p,newzoom);
      } else {
         display.zoom(p,newzoom);
      }

      // Repaint the dispay
      display.repaint();

      return status;
   }


}
