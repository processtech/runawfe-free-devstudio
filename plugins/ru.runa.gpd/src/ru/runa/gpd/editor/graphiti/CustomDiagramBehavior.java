package ru.runa.gpd.editor.graphiti;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;
import org.eclipse.graphiti.ui.CustomContextButtonManagerForPad;
import org.eclipse.graphiti.ui.editor.DefaultPaletteBehavior;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.editor.IDiagramContainerUI;
import org.eclipse.graphiti.ui.internal.config.IConfigurationProviderInternal;
import org.eclipse.graphiti.ui.internal.util.gef.ScalableRootEditPartAnimated;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.part.MultiPageEditorSite;

import ru.runa.gpd.editor.CopyAction;
import ru.runa.gpd.editor.CustomPasteAction;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.GEFActionBarContributor;

public class CustomDiagramBehavior extends DiagramBehavior {
    private KeyHandler keyHandler;
    private CustomPaletteBehavior paletteBehavior;

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

        ProcessEditorBase editor = (ProcessEditorBase) ((MultiPageEditorSite) diagramContainerUi.getSite()).getMultiPageEditor();
        IAction copyAction = new CopyAction(editor);
        copyAction.setId(ActionFactory.COPY.getId());
        diagramContainerUi.getActionRegistry().registerAction(copyAction);
        diagramContainerUi.getSelectionActions().add(copyAction.getId());

        IAction pasteAction = new CustomPasteAction(editor, getConfigurationProvider());
        pasteAction.setId(ActionFactory.PASTE.getId());
        diagramContainerUi.getActionRegistry().registerAction(pasteAction);
        diagramContainerUi.getSelectionActions().add(pasteAction.getId());

    }

    @Override
    protected void configureGraphicalViewer() {

        ScrollingGraphicalViewer viewer = (ScrollingGraphicalViewer) getDiagramContainer().getGraphicalViewer();

        ScalableRootEditPartAnimated rootEditPart = new ScalableRootEditPartAnimated(viewer, getConfigurationProvider()) {

            @Override
            protected GridLayer createGridLayer() {
                return new org.eclipse.graphiti.ui.internal.util.draw2d.GridLayer((IConfigurationProviderInternal) getConfigurationProvider());
            }

        };

        // configure ZoomManager
        viewer.setRootEditPart(rootEditPart); // support

        // animation of the zoom
        ZoomManager zoomManager = rootEditPart.getZoomManager();
        List<String> zoomLevels = new ArrayList<String>(3);
        zoomLevels.add(ZoomManager.FIT_ALL);
        zoomLevels.add(ZoomManager.FIT_WIDTH);
        zoomLevels.add(ZoomManager.FIT_HEIGHT);
        zoomManager.setZoomLevelContributions(zoomLevels);
        IToolBehaviorProvider toolBehaviorProvider = getConfigurationProvider().getDiagramTypeProvider().getCurrentToolBehaviorProvider();
        zoomManager.setZoomLevels(toolBehaviorProvider.getZoomLevels());

        this.initActionRegistry(zoomManager);

        // set the keyhandler.
        viewer.setKeyHandler((new GraphicalViewerKeyHandler(viewer)).setParent(getCommonKeyHandler()));

        // settings for grid and guides
        Diagram diagram = getConfigurationProvider().getDiagram();

        boolean snapToGrid = diagram.isSnapToGrid();
        int horizontalGridUnit = diagram.getGridUnit();
        int verticalGridUnit = diagram.getVerticalGridUnit();
        if (verticalGridUnit == -1) {
            // No vertical grid unit set (or old diagram before 0.8): use
            // vertical grid unit
            verticalGridUnit = horizontalGridUnit;
        }
        boolean gridVisisble = (horizontalGridUnit > 0) && (verticalGridUnit > 0);

        viewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, new Boolean(gridVisisble));
        viewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, new Boolean(snapToGrid));
        viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(horizontalGridUnit, verticalGridUnit));
        viewer.setProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED, toolBehaviorProvider.isShowGuides());

        // context button manager
        IConfigurationProviderInternal configurationProvider = (IConfigurationProviderInternal) this.getConfigurationProvider();
        configurationProvider.setContextButtonManager(new CustomContextButtonManagerForPad(this, configurationProvider.getResourceRegistry()));
    }

    @Override
    protected void initConfigurationProvider(IDiagramTypeProvider diagramTypeProvider) {
        super.initConfigurationProvider(diagramTypeProvider);
        getEditDomain().setCommandStack(new CustomCommandStack(getConfigurationProvider(), getEditingDomain()));
    }

    @Override
    protected DefaultPaletteBehavior createPaletteBehaviour() {
        paletteBehavior = new CustomPaletteBehavior(this);
        return paletteBehavior;
    }

    @Override
    protected DefaultPaletteBehavior getPaletteBehavior() {
        if (paletteBehavior == null) {
            return super.getPaletteBehavior();
        }
        return paletteBehavior;
    }

}
