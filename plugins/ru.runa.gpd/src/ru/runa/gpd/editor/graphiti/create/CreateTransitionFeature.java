package ru.runa.gpd.editor.graphiti.create;

import java.util.List;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.CreateContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;

public class CreateTransitionFeature extends CreateAbstractTransitionFeature {
    private NodeTypeDefinition targetNodeDefinition;

    public CreateTransitionFeature() {
        super(Transition.class);
    }

    public void setTargetNodeDefinition(NodeTypeDefinition nodeDefinition) {
        this.targetNodeDefinition = nodeDefinition;
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        if (source instanceof Node) {
            Node sourceNode = (Node) source;
            return sourceNode.canAddLeavingTransition();
        }
        return false;
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        Object target = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (target instanceof Node) {
            return ((Node) target).canAddArrivingTransition((Node) source);
        } else if (target instanceof ProcessDefinition) {
            return true;
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        Object tpe = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (tpe instanceof ProcessDefinition) {
            if (targetNodeDefinition != null) { // Создаем переход вместе с узлом
                CreateConnectionContext createConnectionContext = new CreateConnectionContext();
                createConnectionContext.setSourcePictogramElement(context.getSourcePictogramElement());
                createConnectionContext.setTargetLocation(context.getTargetLocation());
                ContainerShape targetContainer = getFeatureProvider().getDiagramTypeProvider().getDiagram();
                CreateContext createContext = new CreateContext();
                createContext.setTargetContainer(targetContainer);
                createContext.putProperty(CreateElementFeature.CONNECTION_PROPERTY, createConnectionContext);
                CreateDragAndDropElementFeature createDragAndDropElementFeature = new CreateDragAndDropElementFeature(createContext);
                createDragAndDropElementFeature.setNodeDefinition(targetNodeDefinition);
                createDragAndDropElementFeature.setFeatureProvider((DiagramFeatureProvider) getFeatureProvider());
                createDragAndDropElementFeature.create(createConnectionContext);
                targetNodeDefinition = null;
            }
            return null;
        } else { // Node
            Node source = (Node) getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
            // create new business object
            Transition newTransition = transitionDefinition.createElement(source, false);
            newTransition.setTarget((Node) tpe);
            newTransition.setName(source.getNextTransitionName(transitionDefinition));
            source.addLeavingTransition(newTransition);
            // add connection for business object
            Anchor sourceAnchor = context.getSourceAnchor();
            if (sourceAnchor == null) {
                sourceAnchor = getChopboxAnchor(context.getSourcePictogramElement());
            }
            Anchor targetAnchor = context.getTargetAnchor();
            if (targetAnchor == null) {
                targetAnchor = getChopboxAnchor(context.getTargetPictogramElement());
            }
            AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
            addConnectionContext.setNewObject(newTransition);
            return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
        }
    }

    @Override
    public void postUndo(IContext context) {
        Transition transition = (Transition) getTransition(context);
        if (transition != null) {
            transition.getSource().removeLeavingTransition(transition);
            transition.getParent().removeChild(transition);
        }
        // Для Redo
        context.putProperty(CreateElementFeature.CONNECTION_PROPERTY, transition);
    }

    @Override
    public void postRedo(IContext context) {
        Transition transition = (Transition) context.getProperty(CreateElementFeature.CONNECTION_PROPERTY);
        transition.getSource().addLeavingTransition(transition);
    }

    @Override
    public String getName() {
        return Localization.getString("label.element.transition");
    }

    @Override
    protected List<? extends AbstractTransition> getLeavingTransitions(Object source) {
        return ((Node) source).getLeavingTransitions();
    }

}
