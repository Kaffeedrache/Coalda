// Stefanie Wiltrud Kessler, September 2009 - April 2010
//Project SUKRE
//This software is licensed under the terms of a BSD license.

package coalda.vis;


import java.awt.Component;
import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.VisualItem;


/**
@author kesslewd

Changes the color of selected nodes.

*/
public class SelectionControl implements TupleSetListener {


   /**
      The tabbed pane with the displays this control works on.
   */
   private SOMTabbedPane tab;


   /**
      Constructor.
      @param tabbedPane The tabbed pane with the displays this control works on.
   */
   public SelectionControl (SOMTabbedPane tabbedPane) {
      tab = tabbedPane;
   }


   /**
      Method tupleSetChanged.
      @param ts All selected tuples.
      @param add Tuples that are new in the selection.
      @param rem Tuples that are not in the selection anymore.
   */
   public void tupleSetChanged (TupleSet ts, Tuple[] add, Tuple[] rem) {

      // Set 'highlighted' to false for the tuples that are not in the
      // selection anymore (those in 'rem').
      for ( int i=0; i<rem.length; ++i ) {
          ((VisualItem)rem[i]).setHighlighted(false);
      }

      // If we would have an empty tuple set that would be bad,
      // so we just add one and make it not highlighted
      if ( ts.getTupleCount() == 0 ) {
          ts.addTuple(rem[0]);
          ((VisualItem)rem[0]).setHighlighted(false);
      }

      // Get selected display
      Component component = tab.getSelectedComponent();
      if (component instanceof SOMDisplay) {
         SOMDisplay display = (SOMDisplay) component;
         
         // Get items in focus
         TupleSet focusGroup = display.getVisualization().getGroup(Visualization.FOCUS_ITEMS);
         
         // Set 'highlighted' to true for these items
         Iterator<VisualItem> it = focusGroup.tuples();
         while (it.hasNext()) {
            VisualItem vi = it.next();
            vi.setHighlighted(true);
         }
         
         // Recolor all items
         tab.recolor(display,null);
      
      }

   }


}