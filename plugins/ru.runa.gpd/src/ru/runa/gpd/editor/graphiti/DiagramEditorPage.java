package ru.runa.gpd.editor.graphiti;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.KeyHandler;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.ProcessEditorContributor;
import ru.runa.gpd.editor.gef.GEFActionBarContributor;
import ru.runa.gpd.editor.graphiti.update.BOUpdateContext;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Transition;

import com.google.common.base.Objects;

public class DiagramEditorPage extends DiagramEditor implements PropertyChangeListener {
    private final ProcessEditorBase editor;
    private KeyHandler keyHandler;

    public DiagramEditorPage(ProcessEditorBase editor) {
        this.editor = editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        editor.getDefinition().setDelegatedListener(this);
    }

    public ProcessDefinition getDefinition() {
        return editor.getDefinition();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        PictogramElement pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(event.getSource());
        // TODO unify event propagation to interested parties
        if (pe != null) {
            BOUpdateContext context = new BOUpdateContext(pe, event.getSource());
            getDiagramTypeProvider().getFeatureProvider().updateIfPossibleAndNeeded(context);
        } else if (event.getSource() instanceof Swimlane && PropertyNames.PROPERTY_NAME.equals(event.getPropertyName())) {
            for (SwimlanedNode swimlanedNode : editor.getDefinition().getChildren(SwimlanedNode.class)) {
                if (Objects.equal(swimlanedNode.getSwimlane(), event.getSource())) {
                    pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(swimlanedNode);
                    if (pe != null) {
                        BOUpdateContext context = new BOUpdateContext(pe, swimlanedNode);
                        getDiagramTypeProvider().getFeatureProvider().updateIfPossibleAndNeeded(context);
                    }
                }
            }
        }
        if (event.getSource() instanceof Node) {
            if (PropertyNames.PROPERTY_TIMER_DELAY.equals(event.getPropertyName())
                    || PropertyNames.NODE_LEAVING_TRANSITION_ADDED.equals(event.getPropertyName())
                    || PropertyNames.NODE_LEAVING_TRANSITION_REMOVED.equals(event.getPropertyName())) {
                updateLeavingTransitions((Node) event.getSource());
            }
        }
    }

    private void updateLeavingTransitions(Node node) {
        for (Transition transition : node.getLeavingTransitions()) {
            PictogramElement pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(transition);
            if (pe != null) {
                BOUpdateContext context = new BOUpdateContext(pe, transition);
                getDiagramTypeProvider().getFeatureProvider().updateIfPossibleAndNeeded(context);
            }
        }
    }

    @Override
    public void dispose() {
        editor.getDefinition().unsetDelegatedListener(this);
        super.dispose();
    }

    @Override
    protected void setInput(IEditorInput input) {
        DiagramCreator creator = new DiagramCreator(editor.getDefinitionFile());
        input = creator.createDiagram(null);
        super.setInput(input);
        importDiagram();
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        updateGridLayerVisibility(editor.getDefinition().isShowGrid());
    }

    protected void updateGridLayerVisibility(boolean enabled) {
        if (getGraphicalViewer() != null && getGraphicalViewer().getEditPartRegistry() != null) {
            ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart) getGraphicalViewer().getEditPartRegistry()
                    .get(LayerManager.ID);
            IFigure gridFigure = ((LayerManager) rootEditPart).getLayer(LayerConstants.GRID_LAYER);
            gridFigure.setVisible(enabled);
            // gridFigure.setEnabled(editor.getDefinition().isShowGrid());
        }
    }

    public ProcessEditorBase getEditor() {
        return editor;
    }

    @Override
    protected ContextMenuProvider createContextMenuProvider() {
        return new DiagramContextMenuProvider(getGraphicalViewer(), getActionRegistry(), getDiagramTypeProvider());
    }

    @Override
    protected KeyHandler getCommonKeyHandler() {
        if (keyHandler == null) {
            keyHandler = ((ProcessEditorContributor) getEditor().getEditorSite().getActionBarContributor()).createKeyHandler(getActionRegistry());
        }
        return keyHandler;
    }

    @Override
    public boolean isDirty() {
        return getCommandStack() != null ? getCommandStack().isDirty() : false;
    }

    private void importDiagram() {
        final Diagram diagram = getDiagramTypeProvider().getDiagram();
        getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
            @Override
            protected void doExecute() {
                getDiagramTypeProvider().getFeatureProvider().link(diagram, editor.getDefinition());
                drawElements(diagram);
                drawTransitions(editor.getDefinition().getChildrenRecursive(Transition.class));
                getDefinition().setDirty(false);
            }

            @Override
            public boolean canUndo() {
                return false;
            }
        });
    }

    public void select(GraphElement model) {
        PictogramElement pe = getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(model);
        selectPictogramElements(new PictogramElement[] { pe });
    }

    @Override
    public CommandStack getCommandStack() {
        return super.getCommandStack();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        refresh();
    }

    private void drawElements(ContainerShape parentShape) {
        List<GraphElement> graphElements = getDefinition().getContainerElements(getDefinition());
        drawElements(parentShape, graphElements);
    }

    public void drawElements(ContainerShape parentShape, List<? extends GraphElement> graphElements) {
        IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        for (GraphElement graphElement : graphElements) {
            if (graphElement.getConstraint() == null) {
                continue;
            }
            AddContext context = new AddContext(new AreaContext(), graphElement);
            IAddFeature addFeature = featureProvider.getAddFeature(context);
            if (addFeature == null) {
                System.out.println("Element not supported: " + graphElement);
                continue;
            }
            context.setNewObject(graphElement);
            context.setTargetContainer(parentShape);
            context.setSize(graphElement.getConstraint().width, graphElement.getConstraint().height);
            context.setLocation(graphElement.getConstraint().x, graphElement.getConstraint().y);
            if (addFeature.canAdd(context)) {
                PictogramElement childContainer = addFeature.add(context);
                List<GraphElement> children = getDefinition().getContainerElements(graphElement);
                if (childContainer instanceof ContainerShape && children.size() > 0) {
                    drawElements((ContainerShape) childContainer, children);
                }
            }
        }
    }

    public void drawTransitions(List<Transition> transitions) {
        for (Transition transition : transitions) {
            Anchor sourceAnchor = null;
            Anchor targetAnchor = null;
            AnchorContainer sourceShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(
                    transition.getSource());
            if (sourceShape == null) {
                continue;
            }
            EList<Anchor> anchorList = sourceShape.getAnchors();
            for (Anchor anchor : anchorList) {
                if (anchor instanceof ChopboxAnchor) {
                    sourceAnchor = anchor;
                    break;
                }
            }
            AnchorContainer targetShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider().getPictogramElementForBusinessObject(
                    transition.getTarget());
            if (targetShape == null) {
                continue;
            }
            anchorList = targetShape.getAnchors();
            for (Anchor anchor : anchorList) {
                if (anchor instanceof ChopboxAnchor) {
                    targetAnchor = anchor;
                    break;
                }
            }
            AddConnectionContext addContext = new AddConnectionContext(sourceAnchor, targetAnchor);
            addContext.setNewObject(transition);
            getDiagramTypeProvider().getFeatureProvider().addIfPossible(addContext);
        }
    }

    @Override
    protected void initActionRegistry(ZoomManager zoomManager) {
        super.initActionRegistry(zoomManager);
        GEFActionBarContributor.createCustomGEFActions(getActionRegistry(), editor, getSelectionActions());
    }

    public void applyStyles() {
		getEditingDomain().getCommandStack().execute(new RecordingCommand(getEditingDomain()) {
			@Override
			protected void doExecute() {
				StyleUtil.resetStyles(getDiagramTypeProvider().getDiagram());
			}

			@Override
			public boolean canUndo() {
				return false;
			}
		});
        refresh();
    }
}
