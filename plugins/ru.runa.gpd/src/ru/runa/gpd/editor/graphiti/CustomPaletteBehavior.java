package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.graphiti.ui.editor.DefaultPaletteBehavior;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;

public class CustomPaletteBehavior extends DefaultPaletteBehavior {

    public CustomPaletteBehavior(DiagramBehavior diagramBehavior) {
        super(diagramBehavior);
    }

    @Override
    protected PaletteRoot createPaletteRoot() {
        return new CustomPaletteRoot(diagramBehavior.getDiagramTypeProvider());
    }
}
