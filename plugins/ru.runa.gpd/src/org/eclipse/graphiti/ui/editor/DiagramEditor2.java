package org.eclipse.graphiti.ui.editor;

public class DiagramEditor2 extends DiagramEditor {

    @Override
    protected DefaultPaletteBehavior createPaletteBehaviour() {
        return new DefaultPaletteBehavior2(this);
    }

}
