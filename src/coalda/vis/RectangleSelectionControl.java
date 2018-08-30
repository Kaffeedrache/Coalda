// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.


package coalda.vis;


import  java.lang.Math;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.data.tuple.TupleSet;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualItem;


/**

Listener that allows the user to select multiple nodes contained in a rectangle.
By pressing the control key, the nodes are added to the current focus group.

@author kesslewd
*/
public class RectangleSelectionControl extends ControlAdapter 
   implements PaintListener {
   

   /**
      The tabbed pane with the displays this control works on.
   */
   private boolean debug = false;

   /**
      Button that is used for the rectangle selection.
      Default is RIGHT_MOUSE_BUTTON.
   */
   private int button;

   /**
      Number of clicks to start the rectangle selection.
      Default is one.
   */
   private int clicks = 1;

   /**
      Start coordinate of the rectangle to be drawn.
   */
   private Point2D startCoord;

   /**
      End coordinate of the rectangle to be drawn.
   */
   private Point2D endCoord;

   /**
      Indicates if there is a selection ongoing at the moment.
   */
   private boolean ongoing = false;

   
   /**
      Constructor setting mouse button on right mouse button.
   */
   public RectangleSelectionControl() {
      button = Control.RIGHT_MOUSE_BUTTON;
   }

   /**
      Constructor setting mouse button on custom mouse button.
      
      @param associatedButton Button that starts the rectangle selection.
   */
   public RectangleSelectionControl(int associatedButton) {
      button = associatedButton;
   }
   

   /**
      When mouse is pressed take this as starting coordinate for the rectangle.
   */
   public void mousePressed (java.awt.event.MouseEvent e) {
      if ( UILib.isButtonPressed(e, button) && e.getClickCount() == clicks)  {
         Display display = (Display) e.getComponent();
         startCoord = display.getMousePosition();
         ongoing = true;
      } 
   }

   
   /**
      When the mouse is released, highlight all items in rectangle boundaries.
   */
   public void mouseReleased(java.awt.event.MouseEvent e) {
      
      if ( UILib.isButtonPressed(e, button) && startCoord != null)  {
      
         // Get current mouse position - this is the end position
         Display display = (Display) e.getComponent();
         endCoord = display.getMousePosition();
         
         // Convert coordinates to display coordinates
         // (we have saved them in screen coordinates)
         Point2D startCoord2 = display.getAbsoluteCoordinate(startCoord, null);
         Point2D endCoord2 = display.getAbsoluteCoordinate(endCoord, null);
   
         if (debug) {
            System.out.println("Mouse position start: " + startCoord2.getX() 
                  + "/" + startCoord2.getY());
            System.out.println("Mouse position end: " + endCoord2.getX() 
                  + "/" + endCoord2.getY());
         }
         
         // Get coordinates of selected rectangle (in display coordinates)
         double x = Math.min(startCoord2.getX(), endCoord2.getX());
         double y = Math.min(startCoord2.getY(), endCoord2.getY());
         double height = Math.abs(startCoord2.getY() - endCoord2.getY());
         double width = Math.abs(startCoord2.getX() - endCoord2.getX());
   
         // Create rectangle 
         Rectangle2D area = new Rectangle2D.Double(x, y, width, height);
         
         if (debug) {
            System.out.println("Selection Rectangle: " + area.getX() + " . " 
                  +  area.getY() + " . " +  area.getWidth() + " . " +  area.getHeight());
         }
   
         // Get all visual items
         Visualization vis = display.getVisualization();
         TupleSet ts = vis.getVisualGroup("graph.nodes");
         Iterator<VisualItem> it = ts.tuples();
         
         // Get Focus group.
         // If control key is pressed, the new items are added to the selection.
         // Else the current items are deleted from the focus group.
         TupleSet focusGroup = vis.getFocusGroup(Visualization.FOCUS_ITEMS);
         if (!e.isControlDown()) {
            focusGroup.clear();
         } 
   
         // Check for every visual item if its bounds are inside the rectangle
         while (it.hasNext()) {
            VisualItem item = it.next();
            
            // Add contained items to focus group
            if (area.contains(item.getBounds())) {
               focusGroup.addTuple(item);
            }
         
         }
         
         // Refresh the display to color items
         display.repaint();
         
      }

   }


   /**
      While mouse is dragged with the correct button - repaint.
      This draws the rectangle through the PostPaint method.
   */
   public void mouseDragged(MouseEvent e) {
      // Repaint only if the right button is pressed
      // and there is an ongoing sequence.
      if ( UILib.isButtonPressed(e, button) && ongoing)  {
         Display display = (Display) e.getComponent();
         display.repaint();
      }
   } 
   
   
   /**
      After repainting: Draw the rectangle.
      The two corners are the point where the mouse was
      pressed and the current mouse position.
    */
   public void postPaint(Display d, Graphics2D g) {
            
      // If there is an ongoing sequence that has started
      // (startCoord != null) but is not finished or
      // has just finished - draw.
      if (startCoord != null && ongoing) {
         
         // Get current mouse position
         Point2D current = d.getMousePosition();
         
         // Get coordinates of selected rectangle (in screen coordinates)
         double x = Math.min(startCoord.getX(), current.getX());
         double y = Math.min(startCoord.getY(), current.getY());
         double height = Math.abs(startCoord.getY() - current.getY());
         double width = Math.abs(startCoord.getX() - current.getX());

         // Create Rectangle
         Rectangle2D drawArea = new Rectangle2D.Double(x, y, width, height);
         
         if (debug) {
            System.out.println("Selection Rectangle to draw: " + drawArea.getX() 
                  + " . " +  drawArea.getY() + " . " +  drawArea.getWidth() 
                  + " . " +  drawArea.getHeight());
         }
         
         // Drwa the rectangle on screen
         g.draw(drawArea);
         
      }
      
      // If the sequence has started and ended we don't need
      // to draw any more - reset variables.
      // Repaint one last time to make the rectangle disappear.
      if (startCoord != null && endCoord != null) {
         ongoing = false;
         startCoord = null;
         endCoord = null;
         d.repaint();
      }
      
   }

   
   /**
      Before repainting: Do nothing.
   */
   public void prePaint(Display d, Graphics2D g) {
      // do nothing.
   }


}
