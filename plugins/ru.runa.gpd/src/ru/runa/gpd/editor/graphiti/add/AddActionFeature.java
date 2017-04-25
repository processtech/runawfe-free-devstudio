package ru.runa.gpd.editor.graphiti.add;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Transition;

public class AddActionFeature extends AddElementFeature implements GEFConstants {

    @Override
    public boolean canAdd(IAddContext context) {
        Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
        Object parentConnection = getBusinessObjectForPictogramElement(context.getTargetConnection());
        return parentObject instanceof TaskState || parentConnection instanceof Transition; 
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Action action = (Action) context.getNewObject();
        String iconImageId = "graph/" + action.getTypeDefinition().getIcon();
        Dimension iconSize = getDefaultSystemSize(action, context);
        Object parent = null;
        if (context.getTargetConnection() != null) {
            Connection connection = context.getTargetConnection();
            parent = getBusinessObjectForPictogramElement(connection);
            if (parent instanceof Transition) {
                List<Action> actions = action.getParent().getActions();
                int index = actions.indexOf(action);
                if (index >= 0) {
                    IGaService gaService = Graphiti.getGaService();
                    IPeCreateService peCreateService = Graphiti.getPeCreateService();
                    ConnectionDecorator actionDecorator = peCreateService.createConnectionDecorator(connection, true, (index + 1) * .1, true);
                    actionDecorator.getProperties().add(new GaProperty(GaProperty.CLASS, GaProperty.ACTION_ICON));
                    actionDecorator.getProperties().add(new GaProperty(GaProperty.ACTIVE, GaProperty.TRUE));
                    Image actionIcon = gaService.createImage(actionDecorator, iconImageId);
                    gaService.setLocationAndSize(actionIcon, -iconSize.width / 2, -iconSize.height / 2, iconSize.width, iconSize.height);
                    link(actionDecorator, action);
                    peCreateService.createChopboxAnchor(actionDecorator);
                    layoutPictogramElement(connection);
                    return actionDecorator;
                } else {
                    throw new IllegalStateException("unknown action: " + action);
                }
            }
        } else {
            parent = getBusinessObjectForPictogramElement(context.getTargetContainer());
            if (parent instanceof TaskState) {
                List<Action> actions = action.getParent().getActions();
                if (actions.indexOf(action) >= 0) {
                    ContainerShape targetShape = context.getTargetContainer();
                    IPeCreateService peCreateService = Graphiti.getPeCreateService();
                    ContainerShape actionShape = peCreateService.createContainerShape(targetShape, true);
                    actionShape.getProperties().add(new GaProperty(GaProperty.CLASS, GaProperty.ACTION_ICON));
                    actionShape.getProperties().add(new GaProperty(GaProperty.ACTIVE, GaProperty.TRUE));
                    IGaService gaService = Graphiti.getGaService();
                    Image actionIcon = gaService.createImage(actionShape, iconImageId);
                    gaService.setSize(actionIcon, iconSize.width, iconSize.height);
                    link(actionShape, action);
                    peCreateService.createChopboxAnchor(actionShape);
                    layoutPictogramElement(targetShape);
                    return actionShape;
                } else {
                    throw new IllegalStateException("unknown action: " + action);
                }
            }
        }
        return null;
    }

}
