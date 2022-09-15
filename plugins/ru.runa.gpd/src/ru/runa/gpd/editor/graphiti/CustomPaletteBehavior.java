package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.ui.editor.DefaultPaletteBehavior;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;

public class CustomPaletteBehavior extends DefaultPaletteBehavior {
    private CustomPaletteRoot paletteRoot;

    public CustomPaletteBehavior(DiagramBehavior diagramBehavior) {
        super(diagramBehavior);
    }

    @Override
    protected CustomPaletteRoot createPaletteRoot() {
        paletteRoot = new CustomPaletteRoot(diagramBehavior.getDiagramTypeProvider());
        return paletteRoot;
    }

    @Override
    public void refreshPalette() {
        paletteRoot.updatePaletteEntries();
    }
}
