package org.eclipse.graphiti.ui.internal.editor;

import org.eclipse.graphiti.ui.internal.Messages;

public class GFPanningSelectionToolEntry2 extends GFPanningSelectionToolEntry {

    public GFPanningSelectionToolEntry2() {
        this(null);
    }

    public GFPanningSelectionToolEntry2(String label) {
        this(null, label);
    }

    public GFPanningSelectionToolEntry2(String label, String shortDesc) {
        super(label, shortDesc);
        if (label == null || label.length() == 0) {
            setLabel(Messages.GFPanningSelectionToolEntry_Select);
        }
        if (shortDesc == null || shortDesc.length() == 0) {
            setDescription(Messages.GFPanningSelectionToolEntry_0_xmsg);
        }
        setToolClass(GFPanningSelectionTool2.class);
    }

}
