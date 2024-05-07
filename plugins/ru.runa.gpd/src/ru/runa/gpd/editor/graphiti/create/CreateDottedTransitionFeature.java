package ru.runa.gpd.editor.graphiti.create;

import java.util.List;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.update.DeleteElementFeature;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.bpmn.ConnectableViaDottedTransition;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class CreateDottedTransitionFeature extends CreateAbstractTransitionFeature {

    public CreateDottedTransitionFeature() {
        super(DottedTransition.class);
    }

    @Override
    public boolean canCreate(ICreateConnectionContext context) {
        final Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        final Object target = getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        if (source instanceof ConnectableViaDottedTransition && target instanceof ConnectableViaDottedTransition) {
            return ((ConnectableViaDottedTransition) target).canAddArrivingDottedTransition((ConnectableViaDottedTransition) source);
        }
        return false;
    }

    @Override
    public Connection create(ICreateConnectionContext context) {
        final Node source = (Node) getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        final Node target = (Node) getBusinessObjectForPictogramElement(context.getTargetPictogramElement());
        // create new business object
        DottedTransition newTransition = transitionDefinition.createElement(source, false);
        newTransition.setName(source.getNextTransitionName(transitionDefinition));
        ((ConnectableViaDottedTransition) source).addLeavingDottedTransition(newTransition);
        ((ConnectableViaDottedTransition) target).addArrivingDottedTransition(newTransition);

        // add connection for business object
        Anchor sourceAnchor = context.getSourceAnchor();
        if (sourceAnchor == null) {
            sourceAnchor = getChopboxAnchor(context.getSourcePictogramElement());
        }
        Anchor targetAnchor = context.getTargetAnchor();
        if (targetAnchor == null) {
            targetAnchor = getChopboxAnchor(context.getTargetPictogramElement());
        }
        final AddConnectionContext addConnectionContext = new AddConnectionContext(sourceAnchor, targetAnchor);
        addConnectionContext.setNewObject(newTransition);
        return (Connection) getFeatureProvider().addIfPossible(addConnectionContext);
    }

    @Override
    public boolean canStartConnection(ICreateConnectionContext context) {
        final Object source = getBusinessObjectForPictogramElement(context.getSourcePictogramElement());
        if (source instanceof ConnectableViaDottedTransition) {
            return ((ConnectableViaDottedTransition) source).canAddLeavingDottedTransition();
        }
        return false;
    }

    @Override
    public void postUndo(IContext context) {
        DottedTransition transition = (DottedTransition) getTransition(context);
        if (transition != null) {
            DeleteElementFeature.removeDottedTransition(transition);
            transition.getParent().removeChild(transition);
        }
        // Для Redo
        context.putProperty(CreateElementFeature.CONNECTION_PROPERTY, transition);
    }

    @Override
    public void postRedo(IContext context) {
        DottedTransition transition = (DottedTransition) context.getProperty(CreateElementFeature.CONNECTION_PROPERTY);
        ((ConnectableViaDottedTransition) transition.getSource()).addLeavingDottedTransition(transition);
        ((ConnectableViaDottedTransition) transition.getTarget()).addArrivingDottedTransition(transition);
    }

    @Override
    public String getName() {
        return Localization.getString("label.element.dottedTransition");
    }

    @Override
    protected List<? extends AbstractTransition> getLeavingTransitions(Object source) {
        return ((ConnectableViaDottedTransition) source).getLeavingDottedTransitions();
    }

}
