// Stefanie Wiltrud Kessler, September 2009 - July 2010
// Project SUKRE
// This software is licensed under the terms of a BSD license.


package coalda.vis;

import coalda.ui.SOMTabbedPane;

import java.awt.Point;
import java.awt.event.MouseWheelEvent;

import prefuse.Display;
import prefuse.visual.VisualItem;


/**

Implements zooming by mouse wheel.

@author kesslewd
*/
public class WheelTwoLevelZoomControl extends TwoLevelZoomControl {


   /**
      Constructor with display it works on.
      @param displayPane The display tabbed pane this zoom works on.
   */
   public WheelTwoLevelZoomControl (SOMTabbedPane displayPane) {
      super(displayPane);
   }


   /**
      Called when a user moves the mouse wheel on an item.
      @param item The visual Item the user has zoomed on.
      @param e MouseWheel event that triggered the call.
   */
   public void itemWheelMoved (VisualItem item, MouseWheelEvent e) {
      Display display = (Display) e.getComponent();
      Point m_point = new Point();
      m_point.x = display.getWidth()/2;
      m_point.y = display.getHeight()/2;
      zoom (display, m_point, 1 + 0.1f * e.getWheelRotation(), false, item);
   }


   /**
      Called when a user moves the mouse wheel not on an item.
      @param e MouseWheel event that triggered the call.
   */
   public void mouseWheelMoved (MouseWheelEvent e) {
      Display display = (Display) e.getComponent();
      Point m_point = new Point();
      m_point.x = display.getWidth()/2;
      m_point.y = display.getHeight()/2;
      zoom(display, m_point, 1 + 0.1f * e.getWheelRotation(), false);
   }


}
