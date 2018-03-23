package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.KeyHandler;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;

public class CustomDiagramBehavior extends DiagramBehavior {
    private KeyHandler keyHandler;

    public CustomDiagramBehavior(DiagramEditor diagramEditor) {
        super(diagramEditor);
    }

    @Override
    protected ContextMenuProvider createContextMenuProvider() {
        return new DiagramContextMenuProvider(getDiagramContainer().getGraphicalViewer(), getDiagramContainer().getActionRegistry(),
                getConfigurationProvider());
    }

    @Override
    protected KeyHandler getCommonKeyHandler() {
        if (keyHandler == null) {
            keyHandler = new DiagramActionBarContributor().createKeyHandler(((DiagramEditor) getDiagramContainer()).getActionRegistry());
        }
        return keyHandler;
    }

}
