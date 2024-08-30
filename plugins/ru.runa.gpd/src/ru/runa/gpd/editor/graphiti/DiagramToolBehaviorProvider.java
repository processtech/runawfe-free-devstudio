package ru.runa.gpd.editor.graphiti;

import java.util.List;
import java.util.Objects;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.ICreateConnectionFeature;
import org.eclipse.graphiti.features.ICreateFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IDoubleClickContext;
import org.eclipse.graphiti.features.context.IPictogramElementContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.features.context.impl.CustomContext;
import org.eclipse.graphiti.features.custom.ICustomFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.tb.ContextButtonEntry;
import org.eclipse.graphiti.tb.DefaultToolBehaviorProvider;
import org.eclipse.graphiti.tb.IContextButtonPadData;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.change.ChangeEventTypeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateDataStoreFeature;
import ru.runa.gpd.editor.graphiti.create.CreateDottedTransitionFeature;
import ru.runa.gpd.editor.graphiti.create.CreateDragAndDropElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateElementFeature;
import ru.runa.gpd.editor.graphiti.create.CreateStartNodeFeature;
import ru.runa.gpd.editor.graphiti.create.CreateSwimlaneFeature;
import ru.runa.gpd.editor.graphiti.create.CreateTransitionFeature;
import ru.runa.gpd.editor.graphiti.update.ChangeStartEventTypeFeature;
import ru.runa.gpd.editor.graphiti.update.OpenSubProcessFeature;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.AbstractEventNode;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;
import ru.runa.gpd.lang.model.bpmn.EventNodeType;
import ru.runa.gpd.lang.model.bpmn.StartEventType;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;
import ru.runa.gpd.settings.PrefConstants;

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
        if (context.getPictogramElements().length == 1) {
            PictogramElement pe = context.getPictogramElements()[0];
            GraphElement element = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(pe);
            if (element == null) {
                return null;
            }
            if (element instanceof Subprocess) {
                return new OpenSubProcessFeature(getFeatureProvider());
            }
            NodeTypeDefinition definition = element.getTypeDefinition();
            if (definition != null && definition.getGraphitiEntry() != null) {
                return definition.getGraphitiEntry().createDoubleClickFeature(getFeatureProvider());
            }
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
        ((ContextButtonEntry) data.getGenericContextButtons().get(0)).setIconId("delete.gif");
        //
        CreateConnectionContext createConnectionContext = new CreateConnectionContext();
        createConnectionContext.setSourcePictogramElement(pe);
        boolean allowTargetNodeCreation = (element instanceof Node) && ((Node) element).canAddLeavingTransition();
        //
        ContainerShape targetContainer;
        if (element.getUiParentContainer() instanceof Swimlane) {
            targetContainer = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(element.getUiParentContainer());
        } else {
            targetContainer = getFeatureProvider().getDiagramTypeProvider().getDiagram();
        }
        CreateContext createContext = new CreateContext();
        createContext.setTargetContainer(targetContainer);
        createContext.putProperty(CreateElementFeature.CONNECTION_PROPERTY, createConnectionContext);

        boolean expandContextButtonPad = Activator.getPrefBoolean(PrefConstants.P_BPMN_EXPAND_CONTEXT_BUTTON_PAD);
        if (allowTargetNodeCreation && !expandContextButtonPad) {
            NodeTypeDefinition taskStateDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
            CreateDragAndDropElementFeature createTaskStateFeature = new CreateDragAndDropElementFeature(createContext);
            createTaskStateFeature.setNodeDefinition(taskStateDefinition);
            createTaskStateFeature.setFeatureProvider(getFeatureProvider());
            ContextButtonEntry createTaskStateButton = new ContextButtonEntry(createTaskStateFeature, createConnectionContext);
            createTaskStateButton.setText(taskStateDefinition.getLabel());
            createTaskStateButton.setIconId(taskStateDefinition.getPaletteIcon());
            createTaskStateButton.addDragAndDropFeature(createTaskStateFeature);
            data.getDomainSpecificContextButtons().add(createTaskStateButton);
        }

        //
        ContextButtonEntry createTransitionButton = new ContextButtonEntry(null, context);
        NodeTypeDefinition transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        createTransitionButton.setText(transitionDefinition.getLabel());
        createTransitionButton.setIconId(transitionDefinition.getPaletteIcon());
        ICreateConnectionFeature[] features = getFeatureProvider().getCreateConnectionFeatures();
        for (ICreateConnectionFeature feature : features) {
            if (feature.isAvailable(createConnectionContext) && feature.canStartConnection(createConnectionContext)
                    && feature instanceof CreateTransitionFeature) {
                createTransitionButton.addDragAndDropFeature(feature);
            }
        }

        //

        if (allowTargetNodeCreation) {
            ContextButtonEntry createElementButton = null;
            if (!expandContextButtonPad) {
                createElementButton = new ContextButtonEntry(null, null);
                createElementButton.setText(Localization.getString("new.element.label"));
                createElementButton.setDescription(Localization.getString("new.element.description"));
                createElementButton.setIconId("elements.png");
                data.getDomainSpecificContextButtons().add(createElementButton);
            }

            for (ICreateFeature feature : getFeatureProvider().getCreateFeatures()) {
                if (feature instanceof CreateSwimlaneFeature || feature instanceof CreateStartNodeFeature
                        || feature instanceof CreateDataStoreFeature) {
                    continue;
                }
                if (feature instanceof CreateElementFeature && feature.canCreate(createContext)) {
                    CreateElementFeature createElementFeature = (CreateElementFeature) feature;
                    NodeTypeDefinition typeDefinition = createElementFeature.getNodeDefinition();
                    CreateDragAndDropElementFeature createDragAndDropElementFeature = new CreateDragAndDropElementFeature(createContext);
                    createDragAndDropElementFeature.setNodeDefinition(typeDefinition);
                    createDragAndDropElementFeature.setFeatureProvider(getFeatureProvider());
                    ContextButtonEntry createButton = new ContextButtonEntry(createDragAndDropElementFeature, createConnectionContext);
                    createButton.setText(typeDefinition.getLabel());
                    createButton.setIconId(typeDefinition.getPaletteIcon());
                    if (expandContextButtonPad) {
                        createButton.addDragAndDropFeature(createDragAndDropElementFeature);
                        data.getDomainSpecificContextButtons().add(createButton);
                    } else {
                        createElementButton.addDragAndDropFeature(createDragAndDropElementFeature);
                        createElementButton.getContextButtonMenuEntries().add(createButton);
                    }
                }
            }
        }
        if (!createTransitionButton.getDragAndDropFeatures().isEmpty()) {
            if (expandContextButtonPad) {
                data.getDomainSpecificContextButtons().add(createTransitionButton);
            } else {
                data.getDomainSpecificContextButtons().add(createTransitionButton.getDragAndDropFeatures().size(), createTransitionButton);
            }
        }
        boolean processIsTriggeredByEvent = false;
        if (element instanceof StartState && element.getProcessDefinition() instanceof SubprocessDefinition) {
            processIsTriggeredByEvent = ((SubprocessDefinition) element.getProcessDefinition()).isTriggeredByEvent();
        }
        if (element instanceof EndTokenState || processIsTriggeredByEvent) {
            ContextButtonEntry changeEventTypeButton = new ContextButtonEntry(null, null);
            changeEventTypeButton.setText(Localization.getString("event.type.label"));
            changeEventTypeButton.setDescription(Localization.getString("event.type.description"));
            changeEventTypeButton.setIconId("wrench.png");
            data.getDomainSpecificContextButtons().add(changeEventTypeButton);
            PictogramElement pes[] = { pe };
            ICustomContext customContext = new CustomContext(pes);
            for (int i = 0; i < StartEventType.LABELS.length; i++) {
                StartEventType et = StartEventType.values()[i];
                boolean timerStartStateExists = element.getProcessDefinition().getChildren(StartState.class).stream()
                        .filter(startState -> !Objects.equals(startState.getId(), element.getId()))
                        .anyMatch(StartState::isStartByTimer);
                if ((et.equals(StartEventType.blank) && processIsTriggeredByEvent) || (et.equals(StartEventType.timer) && timerStartStateExists)) {
                    continue;
                }
                ContextButtonEntry createButton = new ContextButtonEntry(new ChangeStartEventTypeFeature(getFeatureProvider(), et), customContext);
                createButton.setIconId("graph/" + et.getImageName());
                createButton.setText(StartEventType.LABELS[i]);
                changeEventTypeButton.addContextButtonMenuEntry(createButton);
            }
        }

        if (Activator.getPrefBoolean(PrefConstants.P_INTERNAL_STORAGE_FUNCTIONALITY_ENABLED)) {
            ContextButtonEntry createDottedTransitionButton = new ContextButtonEntry(null, context);
            NodeTypeDefinition dottedTransitionDefinition = NodeRegistry.getNodeTypeDefinition(DottedTransition.class);
            createDottedTransitionButton.setText(dottedTransitionDefinition.getLabel());
            createDottedTransitionButton.setIconId(dottedTransitionDefinition.getPaletteIcon());

            for (ICreateConnectionFeature feature : features) {
                if (feature.isAvailable(createConnectionContext) && feature.canStartConnection(createConnectionContext)
                        && feature instanceof CreateDottedTransitionFeature) {
                    createDottedTransitionButton.addDragAndDropFeature(feature);
                }
            }

            if (!createDottedTransitionButton.getDragAndDropFeatures().isEmpty()) {
                if (expandContextButtonPad) {
                    data.getDomainSpecificContextButtons().add(createDottedTransitionButton);
                } else {
                    data.getDomainSpecificContextButtons().add(createDottedTransitionButton.getDragAndDropFeatures().size(),
                            createDottedTransitionButton);
                }
            }
        }

        if (element instanceof AbstractEventNode) {
            ContextButtonEntry changeEventTypeButton = new ContextButtonEntry(null, null);
            changeEventTypeButton.setText(Localization.getString("event.type.label"));
            changeEventTypeButton.setDescription(Localization.getString("event.type.description"));
            changeEventTypeButton.setIconId("wrench.png");
            data.getDomainSpecificContextButtons().add(changeEventTypeButton);
            PictogramElement pes[] = { pe };
            ICustomContext customContext = new CustomContext(pes);
            for (int i = 0; i < EventNodeType.LABELS.length; i++) {
                EventNodeType et = EventNodeType.values()[i];
                ContextButtonEntry createButton = new ContextButtonEntry(new ChangeEventTypeFeature((AbstractEventNode) element, et), customContext);
                createButton.setIconId("graph/" + et.getImageName(element instanceof CatchEventNode, false));
                createButton.setText(EventNodeType.LABELS[i]);
                changeEventTypeButton.addContextButtonMenuEntry(createButton);
            }
        }
        return data;
    }

    @Override
    public String getToolTip(GraphicsAlgorithm ga) {
        PictogramElement pe = ga.getPictogramElement();
        Object bo = getFeatureProvider().getBusinessObjectForPictogramElement(pe);
        if (bo instanceof GraphElement) {
            return ((GraphElement) bo).getTooltip();
        }
        if (ga instanceof Image && PropertyUtil.hasProperty(pe, GaProperty.CLASS, GaProperty.ACTIONS_ICON)) {
            GraphElement ge = (GraphElement) getFeatureProvider().getBusinessObjectForPictogramElement(((Shape) pe).getContainer());
            List<Action> actions = ge.getActions();
            StringBuilder toolTipBuilder = new StringBuilder(TooltipBuilderHelper.SPACE + Localization.getString("pref.extensions.handler")
                    + TooltipBuilderHelper.COLON + TooltipBuilderHelper.SPACE);
            for (Action act : actions) {
                toolTipBuilder.append(TooltipBuilderHelper.SPACE + TooltipBuilderHelper.NEW_LINE + TooltipBuilderHelper.SPACE + act.getLabel());
            }
            return toolTipBuilder.toString();
        }
        return (String) super.getToolTip(ga);
    }

    @Override
    public PictogramElement getSelection(PictogramElement originalPe, PictogramElement[] oldSelection) {
        if (originalPe instanceof ConnectionDecorator) {
            if (PropertyUtil.hasProperty(originalPe, GaProperty.ID, GaProperty.TRANSITION_NUMBER)
                    || PropertyUtil.hasProperty(originalPe, GaProperty.ID, GaProperty.TRANSITION_COLOR_MARKER)) {
                return getDiagramTypeProvider().getDiagram();
            }
        }
        return super.getSelection(originalPe, oldSelection);
    }

}
