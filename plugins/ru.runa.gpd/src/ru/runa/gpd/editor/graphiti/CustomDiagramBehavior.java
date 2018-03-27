package ru.runa.gpd.editor.graphiti;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.IDiagramContainerUI;
import org.eclipse.ui.part.MultiPageEditorSite;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.GEFActionBarContributor;

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

    @Override
    protected void initActionRegistry(ZoomManager zoomManager) {
        super.initActionRegistry(zoomManager);
        IDiagramContainerUI diagramContainerUi = getDiagramContainer();
        GEFActionBarContributor.createCustomGEFActions(diagramContainerUi.getActionRegistry(),
                (ProcessEditorBase) ((MultiPageEditorSite) diagramContainerUi.getSite()).getMultiPageEditor(),
                diagramContainerUi.getSelectionActions());
    }

}
