package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.tb.ContextButtonEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonPadData;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.create.CreateAnnotationFeature;
import ru.runa.gpd.editor.graphiti.create.CreateElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateStartNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateSwimlaneFeature;
import ru.runa.gpd.editor.graphiti.update.OpenSubProcessFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

public class DiagramToolBehaviorProvider extends DefaultToolBehaviorProvider {
    public DiagramToolBehaviorProvider(IDiagramTypeProvider provider) {
        super(provider);
    }

    @Override
    protected DiagramFeatureProvider getFeatureProvider() {
        return (DiagramFeatureProvider) super.getFeatureProvider();
    }

    @Override
    public ICustomFeature getDoubleClickFeature(IDoubleClickContext context) {
        PictogramElement pe = context.getInnerPictogramElement();
        GraphElement element = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(pe);
        if (element instanceof Subprocess) {
            return new OpenSubProcessFeature(getFeatureProvider());
        }
        return super.getDoubleClickFeature(context);
    }

    @Override
    public IContextButtonPadData getContextButtonPad(IPictogramElementContext context) {
        IContextButtonPadData data = super.getContextButtonPad(context);
        PictogramElement pe = context.getPictogramElement();
        GraphElement element = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(pe);
        if (element == null || element instanceof Swimlane || element instanceof TextDecorationNode) {
            return null;
        }
        setGenericContextButtons(data, pe, CONTEXT_BUTTON_DELETE);
        //
        CreateConnectionContext createConnectionContext = new CreateConnectionContext();
        createConnectionContext.setSourcePictogramElement(pe);
        boolean allowTargetNodeCreation = (element instanceof Node) && ((Node) element).canAddLeavingTransition();
        //
        CreateContext createContext = new CreateContext();
        ContainerShape targetContainer;
        if (element.getParentContainer() instanceof Swimlane) {
            targetContainer = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(element.getParentContainer());
        } else {
            targetContainer = getFeatureProvider().getDiagramTypeProvider().getDiagram();
        }
        createContext.setTargetContainer(targetContainer);
        createContext.putProperty(CreateElementFeature.CONNECTION_PROPERTY, createConnectionContext);
        if (allowTargetNodeCreation) {
            //
            NodeTypeDefinition taskStateDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
            CreateElementFeature createTaskStateFeature = new CreateElementFeature();
            createTaskStateFeature.setNodeDefinition(taskStateDefinition);
            createTaskStateFeature.setFeatureProvider(getFeatureProvider());
            ContextButtonEntry createTaskStateButton = new ContextButtonEntry(createTaskStateFeature, createContext);
            createTaskStateButton.setText(taskStateDefinition.getLabel());
            createTaskStateButton.setIconId(taskStateDefinition.getPaletteIcon());
            data.getDomainSpecificContextButtons().add(createTaskStateButton);
        }
        //
        ContextButtonEntry createTransitionButton = new ContextButtonEntry(null, context);
        NodeTypeDefinition transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        createTransitionButton.setText(transitionDefinition.getLabel());
        createTransitionButton.setIconId(transitionDefinition.getPaletteIcon());
        ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
        for (ICreateConnectionFeature feature : features) {
            if (feature.isAvailable(createConnectionContext) && feature.canStartConnection(createConnectionContext)) {
                createTransitionButton.addDragAndDropFeature(feature);
            }
        }
        if (createTransitionButton.getDragAndDropFeatures().size() > 0) {
            data.getDomainSpecificContextButtons().add(createTransitionButton);
        }
        //
        if (allowTargetNodeCreation) {
            ContextButtonEntry createElementButton = new ContextButtonEntry(null, null);
            createElementButton.setText("new element");
            createElementButton.setDescription("Create a new element");
            createElementButton.setIconId("elements.png");
            data.getDomainSpecificContextButtons().add(createElementButton);
            for (ICreateFeature feature : getFeatureProvider().getCreateFeatures()) {
                if (feature instanceof CreateSwimlaneFeature || feature instanceof CreateAnnotationFeature
                        || feature instanceof CreateStartNodeFeature) {
                    continue;
                }
                if (feature instanceof CreateElementFeature && feature.canCreate(createContext)) {
                    CreateElementFeature createElementFeature = (CreateElementFeature) feature;
                    ContextButtonEntry createButton = new ContextButtonEntry(feature, createContext);
                    NodeTypeDefinition typeDefinition = createElementFeature.getNodeDefinition();
                    createButton.setText(typeDefinition.getLabel());
                    createButton.setIconId(typeDefinition.getPaletteIcon());
                    createElementButton.getContextButtonMenuEntries().add(createButton);
                }
            }
        }
        return data;
    }

    @Override
    public String getToolTip(GraphicsAlgorithm ga) {
        if (ga instanceof Polyline) {
            Object element = getFeatureProvider().getBusinessObjectForPictogramElement(ga.getPictogramElement());
            if (element instanceof Transition) {
                Transition transition = (Transition) element;
                Object orderNum = transition.getPropertyValue(Transition.PROPERTY_ORDERNUM);
                if (orderNum != null) {
                    return Localization.getString("Transition.property.orderNum") + ": " + orderNum;
                }
            }
        }
        Object bo = getFeatureProvider().getBusinessObjectForPictogramElement(ga.getPictogramElement());
        if (bo instanceof Node) {
            Node node = (Node) bo;
            if (node.isMinimizedView()) {
                return node.getLabel();
            }
        }
        return super.getToolTip(ga);
    }
}
