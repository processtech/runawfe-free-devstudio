package ru.runa.gpd.editor.graphiti;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.Tool;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.AbstractConnectionCreationTool;
import org.eclipse.gef.tools.CreationTool;
import org.eclipse.graphiti.internal.contextbuttons.IContextButtonPadDeclaration;
import org.eclipse.graphiti.internal.contextbuttons.SpecialContextButtonPadDeclaration;
import org.eclipse.graphiti.internal.contextbuttons.StandardContextButtonPadDeclaration;
import org.eclipse.graphiti.internal.features.context.impl.base.PictogramElementContext;
import org.eclipse.graphiti.internal.pref.GFPreferences;
import org.eclipse.graphiti.internal.services.GraphitiInternal;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.tb.IContextButtonPadData;
import org.eclipse.graphiti.tb.IToolBehaviorProvider;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.internal.IResourceRegistry;
import org.eclipse.graphiti.ui.internal.contextbuttons.ContextButtonManagerForPad;
import org.eclipse.graphiti.ui.internal.contextbuttons.ContextButtonPad;
import org.eclipse.graphiti.ui.internal.parts.IPictogramElementEditPart;
import org.eclipse.swt.SWT;

import ru.runa.gpd.Activator;
import ru.runa.gpd.settings.PrefConstants;

public class CustomContextButtonManagerForPad extends ContextButtonManagerForPad {

    protected static final double MINIMUM_ZOOM_LEVEL = 0.75d;
    private Map<IFigure, EditPart> figure2EditPart = new HashMap<IFigure, EditPart>();
    private IFigure activeFigure;
    private ContextButtonPad activeContextButtonPad;
    private boolean contextButtonShowing;
    private ZoomListener zoomListener = new ZoomListener() {
        @Override
        public void zoomChanged(double newZoom) {
            handleZoomChanged();
        }
    };
    private MouseMotionListener mouseMotionListener = new MouseMotionListener.Stub() {
        @Override
        public void mouseEntered(MouseEvent me) {
            reactOnMouse(me);
        }

        @Override
        public void mouseMoved(MouseEvent me) {
            reactOnMouse(me);
        }

        private void reactOnMouse(MouseEvent me) {
            DiagramBehavior diagramBehavior = getDiagramBehavior();

            if (diagramBehavior.isDirectEditingActive()) {
                return;
            }
            Tool activeTool = diagramBehavior.getEditDomain().getActiveTool();
            if (activeTool instanceof CreationTool || activeTool instanceof AbstractConnectionCreationTool) {
                return;
            }

            if ((me.getState() & SWT.MOD1) != 0) {
                hideContextButtonsInstantly();
                return;
            }

            if (!isContextButtonShowing()) {
                return;
            }

            Object source = me.getSource();
            showContextButtonsInstantly((IFigure) source, me.getLocation());
        }

    };

    private IResourceRegistry resourceRegistry;

    public CustomContextButtonManagerForPad(DiagramBehavior diagramBehavior, IResourceRegistry resourceRegistry) {
        super(diagramBehavior, resourceRegistry);
        this.resourceRegistry = resourceRegistry;

        ZoomManager zoomMgr = (ZoomManager) getDiagramBehavior().getDiagramContainer().getGraphicalViewer().getProperty(ZoomManager.class.toString());
        if (zoomMgr != null) {
            zoomMgr.addZoomListener(zoomListener);
        }

        contextButtonShowing = true;

    }

    @Override
    public DiagramBehavior getDiagramBehavior() {
        return super.getDiagramBehavior();
    }

    private Map<IFigure, EditPart> getFigure2EditPart() {
        return figure2EditPart;
    }

    private void setActive(IFigure activeFigure, ContextButtonPad activeContextButtonPad) {
        this.activeFigure = activeFigure;
        this.activeContextButtonPad = activeContextButtonPad;
    }

    private IFigure getActiveFigure() {
        return activeFigure;
    }

    private ContextButtonPad getActiveContextButtonPad() {
        return activeContextButtonPad;
    }

    @Override
    public void register(GraphicalEditPart graphicalEditPart) {
        getFigure2EditPart().put(graphicalEditPart.getFigure(), graphicalEditPart);

        graphicalEditPart.getFigure().addMouseMotionListener(mouseMotionListener);
    }

    @Override
    public void deRegister(GraphicalEditPart graphicalEditPart) {
        if (graphicalEditPart.getFigure().equals(getActiveFigure())) {
            hideContextButtonsInstantly();
        }

        getFigure2EditPart().remove(graphicalEditPart.getFigure());
        graphicalEditPart.getFigure().removeMouseMotionListener(mouseMotionListener);
    }

    @Override
    public void hideContextButtonsInstantly() {
        if (getActiveContextButtonPad() != null) {
            synchronized (this) {
                ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getDiagramBehavior().getDiagramContainer()
                        .getGraphicalViewer().getRootEditPart();
                IFigure feedbackLayer = rootEditPart.getLayer(LayerConstants.HANDLE_LAYER);
                feedbackLayer.remove(getActiveContextButtonPad());
                setActive(null, null);
            }
        }
    }

    private boolean replaceContextButtonPad(IFigure figure, Point mouseLocation) {
        if (getActiveFigure() == null) {
            return true;
        }
        if (figure.equals(getActiveFigure())) {
            return false;
        }

        IFigure parent = figure.getParent();
        while (parent != null) {
            if (parent.equals(getActiveFigure())) {
                return true;
            }
            parent = parent.getParent();
        }

        if (getActiveFigure().containsPoint(mouseLocation)) {
            return true;
        }

        if (getActiveContextButtonPad() != null && getActiveContextButtonPad().isMouseInOverlappingArea()) {
            return false;
        }

        return true;
    }

    private void showContextButtonsInstantly(IFigure figure, Point mouse) {
        if (!replaceContextButtonPad(figure, mouse)) {
            return;
        }

        synchronized (this) {
            hideContextButtonsInstantly();

            ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getDiagramBehavior().getDiagramContainer().getGraphicalViewer()
                    .getRootEditPart();
            double zoom = rootEditPart.getZoomManager().getZoom();
            if (zoom < MINIMUM_ZOOM_LEVEL) {
                return;
            }

            IPictogramElementEditPart editPart = (IPictogramElementEditPart) getFigure2EditPart().get(figure);
            PictogramElement pe = editPart.getPictogramElement();
            if (pe instanceof Diagram) {
                return;
            }
            if (!GraphitiInternal.getEmfService().isObjectAlive(pe)) {
                return;
            }
            PictogramElementContext context = new PictogramElementContext(pe);

            IToolBehaviorProvider toolBehaviorProvider = getDiagramBehavior().getDiagramTypeProvider().getCurrentToolBehaviorProvider();
            IContextButtonPadData contextButtonPadData = toolBehaviorProvider.getContextButtonPad(context);
            if (contextButtonPadData == null) {
                return;
            }
            if (contextButtonPadData.getDomainSpecificContextButtons().size() == 0 && contextButtonPadData.getGenericContextButtons().size() == 0
                    && contextButtonPadData.getCollapseContextButton() == null) {
                return;
            }

            if (!contextButtonPadData.getPadLocation().contains(mouse.x, mouse.y)) {
                return;
            }

            int declarationType = GFPreferences.getInstance().getContextButtonPadDeclaration();
            IContextButtonPadDeclaration declaration;
            if (declarationType == 1) {
                declaration = new SpecialContextButtonPadDeclaration(contextButtonPadData);
            } else if (!Activator.getDefault().getPreferenceStore().getBoolean(PrefConstants.P_ELEMENT_EXPANDS_PAD)) {
                declaration = new CustomContextButtonPadDeclaration(contextButtonPadData);
            } else {
                declaration = new StandardContextButtonPadDeclaration(contextButtonPadData);
            }

            EditPart activeEditPart = getFigure2EditPart().get(figure);
            ContextButtonPad contextButtonPad = new ContextButtonPad(this, declaration, zoom, getDiagramBehavior(), activeEditPart, resourceRegistry);
            setActive(figure, contextButtonPad);

            IFigure feedbackLayer = rootEditPart.getLayer(LayerConstants.HANDLE_LAYER);
            feedbackLayer.add(contextButtonPad);
        }
    }

    @Override
    public void setContextButtonShowing(boolean enable) {
        contextButtonShowing = enable;
    }

    private boolean isContextButtonShowing() {
        return contextButtonShowing;
    }

    private void handleZoomChanged() {
        hideContextButtonsInstantly();
    }

}
