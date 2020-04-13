package ru.runa.gpd.editor.graphiti;

import com.google.common.base.Objects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.LayerManager;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IUpdateFeature;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.features.context.impl.UpdateContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.AnchorContainer;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.GEFActionBarContributor;
import ru.runa.gpd.editor.graphiti.update.BOUpdateContext;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;

public class DiagramEditorPage extends DiagramEditor implements PropertyChangeListener {

    private final ProcessEditorBase editor;
    private DiagramCreator diagramCreator;

    public DiagramEditorPage(ProcessEditorBase editor) {
        this.editor = editor;
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        UndoRedoUtil.watch(editor.getDefinition());
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
            if (PropertyNames.NODE_BOUNDS_RESIZED.equals(event.getPropertyName())) {
                LayoutContext layoutContext = new LayoutContext(pe);
                getDiagramTypeProvider().getFeatureProvider().layoutIfPossible(layoutContext);
            }
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
        if (diagramCreator != null) {
            diagramCreator.disposeDiagram();
        }
        UndoRedoUtil.unwatch(editor.getDefinition());
        super.dispose();
    }

    @Override
    protected void setInput(IEditorInput input) {
        diagramCreator = new DiagramCreator(editor.getDefinitionFile());
        input = diagramCreator.createDiagram(null);
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
    protected DiagramBehavior createDiagramBehavior() {
        return new CustomDiagramBehavior(this);
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

    public PictogramElement[] getAllPictogramElementsForBusinessObject(GraphElement model) {
        return getDiagramTypeProvider().getFeatureProvider().getAllPictogramElementsForBusinessObject(model);
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        getDiagramBehavior().refresh();
    }

    public void refreshActions() {
        refreshActions(getDiagramTypeProvider().getDiagram());
    }

    private void refreshActions(Diagram diagram) {
        refreshActions((ContainerShape) diagram);
        for (Connection connection : diagram.getConnections()) {
            for (final ConnectionDecorator decorator : connection.getConnectionDecorators()) {
                if (PropertyUtil.hasProperty(decorator, GaProperty.CLASS, GaProperty.ACTION_ICON)) {
                    TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(decorator);
                    domain.getCommandStack().execute(new RecordingCommand(domain) {
                        @Override
                        protected void doExecute() {
                            decorator.setVisible(editor.getDefinition().isShowActions());
                        }
                    });
                }
            }
        }
        getDiagramBehavior().refresh();
    }

    public void refreshConnections() {
        Diagram diagram = getDiagramTypeProvider().getDiagram();
        for (Connection connection : diagram.getConnections()) {
            Transition transition = (Transition) getDiagramTypeProvider().getFeatureProvider().getBusinessObjectForPictogramElement(connection);
            if (transition != null && transition.getSource() instanceof ExclusiveGateway) {
                ExclusiveGateway eg = (ExclusiveGateway) transition.getSource();
                TransitionUtil.setDefaultFlow(eg, eg.getDelegationConfiguration());
            }
        }
        TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(diagram);
        domain.getCommandStack().execute(new RecordingCommand(domain) {
            @Override
            protected void doExecute() {
                for (Connection connection : diagram.getConnections()) {
                    if (PropertyUtil.findGaRecursiveByName(connection, GaProperty.DEFAULT_FLOW) != null) {
                        UpdateContext updateContext = new UpdateContext(connection);
                        IUpdateFeature updateFeature = getDiagramTypeProvider().getFeatureProvider().getUpdateFeature(updateContext);
                        updateFeature.update(updateContext);
                    }
                }
            }
        });
        getDiagramBehavior().refreshContent();
    }

    private void refreshActions(ContainerShape containerShape) {
        for (final Shape shape : containerShape.getChildren()) {
            if (PropertyUtil.hasProperty(shape, GaProperty.CLASS, GaProperty.ACTION_ICON)) {
                TransactionalEditingDomain domain = TransactionUtil.getEditingDomain(shape);
                domain.getCommandStack().execute(new RecordingCommand(domain) {
                    @Override
                    protected void doExecute() {
                        if (editor.getDefinition().isShowActions()) {
                            shape.setVisible(Graphiti.getPeService().getPropertyValue(shape, GaProperty.ACTIVE).equals(GaProperty.TRUE));
                        } else {
                            shape.setVisible(false);
                        }
                    }
                });
            }
            if (shape instanceof ContainerShape) {
                refreshActions((ContainerShape) shape);
            }
        }
    }

    private void drawElements(Diagram diagram) {
        List<GraphElement> graphElements = getDefinition().getContainerElements(getDefinition());
        drawElements(diagram, graphElements);
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
                if (graphElement instanceof TaskState) {
                    drawActions((ContainerShape) childContainer, graphElement);
                }
                List<GraphElement> children = getDefinition().getContainerElements(graphElement);
                if (childContainer instanceof ContainerShape && children.size() > 0) {
                    drawElements((ContainerShape) childContainer, children);
                }
            }
        }
    }

    public void drawActions(ContainerShape containerShape, GraphElement actionOwner) {
        IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        for (Action action : actionOwner.getActions()) {
            AddContext context = new AddContext(new AreaContext(), action);
            context.setNewObject(action);
            context.setTargetContainer(containerShape);
            IAddFeature addFeature = featureProvider.getAddFeature(context);
            if (addFeature != null) {
                if (addFeature.canAdd(context)) {
                    addFeature.add(context);
                }
            } else {
                System.out.println("Element not supported: " + action);
                continue;
            }
        }
    }

    public void drawActions(Connection containerShape, GraphElement actionOwner) {
        IFeatureProvider featureProvider = getDiagramTypeProvider().getFeatureProvider();
        for (Action action : actionOwner.getActions()) {
            AddContext context = new AddContext(new AreaContext(), action);
            context.setNewObject(action);
            context.setTargetConnection(containerShape);
            IAddFeature addFeature = featureProvider.getAddFeature(context);
            if (addFeature != null) {
                if (addFeature.canAdd(context)) {
                    addFeature.add(context);
                }
            } else {
                System.out.println("Element not supported: " + action);
                continue;
            }
        }
    }

    public void drawTransitions(List<Transition> transitions) {
        for (Transition transition : transitions) {
            Anchor sourceAnchor = null;
            Anchor targetAnchor = null;
            AnchorContainer sourceShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider()
                    .getPictogramElementForBusinessObject(transition.getSource());
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
            AnchorContainer targetShape = (AnchorContainer) getDiagramTypeProvider().getFeatureProvider()
                    .getPictogramElementForBusinessObject(transition.getTarget());
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
            drawActions((Connection) getDiagramTypeProvider().getFeatureProvider().addIfPossible(addContext), transition);
        }
        setPictogramElementForSelection(null);
    }

    @Override
    protected void initializeActionRegistry() {
        super.initializeActionRegistry();
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
        getDiagramBehavior().refresh();
    }
}
