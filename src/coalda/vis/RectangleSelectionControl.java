// Stefanie Wiltrud Kessler, September 2009 - July 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.


package coalda.vis;


import  java.lang.Math;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.controls.ControlAdapter;
import prefuse.data.tuple.TupleSet;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.display.PaintListener;
import prefuse.visual.VisualItem;

import coalda.base.*;

// add as a paintListener and ControlListener

// TODO Rectangle while mouse pressed




/**

Listener that allows the user to select multiple nodes contained in a rectangle.

@author kesslewd
*/
public class RectangleSelectionControl extends ControlAdapter 
   implements PaintListener {
   
   boolean debug = true;
   private Point2D startCoord;
   private Point2D endCoord;
   
   public void mousePressed (java.awt.event.MouseEvent e) {
      if (debug)
         System.out.println("mouse pressed");
      Display display = (Display) e.getComponent();
      startCoord = display.getMousePosition();
   }
   
   public void mouseReleased(java.awt.event.MouseEvent e) {
      if (debug)
         System.out.println("mouse released");
      
      // Get current mouse position
      Display display = (Display) e.getComponent();
      endCoord = display.getMousePosition();
      
      // Convert coordinates to display coordinates
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
      
      // Get Focus group and delete current items
      TupleSet focusGroup = vis.getFocusGroup(Visualization.FOCUS_ITEMS);
      focusGroup.clear();

      // Check for every visual item if its bounds are inside the rectangle
      while (it.hasNext()) {
         VisualItem item = it.next();
         
         // Add contained items to focus group
         if (area.contains(item.getBounds())) {
            if (debug) {
               System.out.println("Item enthalten: " + item.getString(Constants.nodeKey) 
                     + " x: " + item.getX() + " y :" + item.getY());
            }
            focusGroup.addTuple(item);
         }
      
      }
      
      // Refresh the display
      // TODO doesn't always work
      display.repaint();

   }

   
   
   // HERE we calculate with screen coordinates!!
   public void postPaint(Display d, Graphics2D g) {
      if (debug)
         System.out.println("nach zeichnen");
      
      if (startCoord != null) {
         System.out.println("nach zeichnen - tu was");
         
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
   
   }

   public void prePaint(Display d, Graphics2D g) {
      // do nothing.
   }

   
   
   
   
   // working post-paint, 
   // only after mouse is released
         //~ if (startCoord != null && endCoord != null) {
         //~ System.out.println("nach zeichnen - tu was");
      
         //~ double x = startCoord.getX();  // getItemBounds?
         //~ double y = startCoord.getY();
         
         //~ double height = Math.abs(startCoord.getY() - endCoord.getY());
         //~ double width = Math.abs(startCoord.getX() - endCoord.getX());

         //~ // Rechteck zeichenn\
         //~ // convert from absolute coord to display coord
         //~ //createRectangular(start, end)
         //~ Rectangle2D drawArea = new Rectangle2D.Double(x, y, width, height);
         
         //~ System.out.println("Selection Rectangle to draw: " + drawArea.getX() + " . " +  drawArea.getY() + " . " +  drawArea.getWidth() + " . " +  drawArea.getHeight());
         
         //~ // draw the area
         //~ g.draw(drawArea);
}
