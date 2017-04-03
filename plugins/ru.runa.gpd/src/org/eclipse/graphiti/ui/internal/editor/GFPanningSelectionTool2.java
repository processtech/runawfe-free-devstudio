package org.eclipse.graphiti.ui.internal.editor;

import org.eclipse.gef.DragTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.gef.tools.PanningSelectionTool;

public class GFPanningSelectionTool2 extends PanningSelectionTool {

    @Override
    public void setDragTracker(DragTracker newDragTracker) {
        if (newDragTracker != null & newDragTracker instanceof MarqueeDragTracker) {
            GFMarqueeDragTracker tracker = new GFMarqueeDragTracker();
            tracker.setMarqueeBehavior(GFMarqueeSelectionTool.BEHAVIOR_NODES_AND_CONNECTIONS);
            super.setDragTracker(tracker);
        } else {
            super.setDragTracker(newDragTracker);
        }
    }

}
