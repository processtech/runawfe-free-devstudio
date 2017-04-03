package org.eclipse.graphiti.ui.internal.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.ui.internal.Messages;

public class GFPaletteRoot2 extends GFPaletteRoot {

    public GFPaletteRoot2(IDiagramTypeProvider diagramTypeProvider) {
        super(diagramTypeProvider);
    }

    protected PaletteContainer createModelIndependentTools() {
        PaletteGroup controlGroup = new PaletteGroup(Messages.GraphicsPaletteRoot_0_xmen);
        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();
        {
            // Selection tool
            ToolEntry tool = new GFPanningSelectionToolEntry2();
            entries.add(tool);
            setDefaultEntry(tool);
        }
        {
            // Marquee tool
            ToolEntry tool = new GFMarqueeToolEntry();
            entries.add(tool);
        }
        controlGroup.addAll(entries);
        return controlGroup;
    }

}
