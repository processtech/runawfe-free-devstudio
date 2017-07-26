package ru.runa.gpd.editor.graphiti.layout;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;

public class LayoutActionFeature extends LayoutElementFeature {

    @Override
    public boolean layout(ILayoutContext context) {
        ContainerShape actionIcon = (ContainerShape) context.getPictogramElement();
        Action action = (Action) getBusinessObjectForPictogramElement(actionIcon);
        if (action.getParent() instanceof TaskState) {
            List<Action> actions = action.getParent().getActions();
            int index = actions.indexOf(action);
            if (index >= 0) {
                IGaService gaService = Graphiti.getGaService();
                Shape actionsIcon = PropertyUtil.findChildShapeByProperty(actionIcon.getContainer(), GaProperty.CLASS, GaProperty.ACTIONS_ICON);
                Rectangle parentRectangle = action.getParent().getConstraint();
                GraphicsAlgorithm actionImage = actionIcon.getGraphicsAlgorithm();
                int actionLineSize = actions.size() * (actionImage.getWidth() + GRID_SIZE / 4) - GRID_SIZE / 4;
                int y = parentRectangle.height - GRID_SIZE * 5 / 2;
                gaService.setLocation(actionImage, (parentRectangle.width - actionLineSize) / 2 + index * (actionImage.getWidth() + GRID_SIZE / 4), y);
                ProcessDefinition pd = ((GraphElement) getBusinessObjectForPictogramElement(actionIcon)).getProcessDefinition();
                if (actionLineSize < parentRectangle.width - 3 * GRID_SIZE) {
                    actionIcon.setVisible(true);
                    actionsIcon.setVisible(false);
                    Graphiti.getPeService().getProperty(actionIcon, GaProperty.ACTIVE).setValue(GaProperty.TRUE);
                    Graphiti.getPeService().getProperty(actionsIcon, GaProperty.ACTIVE).setValue(GaProperty.FALSE);
                } else {
                    actionIcon.setVisible(false);
                    GraphicsAlgorithm actionsImage = actionsIcon.getGraphicsAlgorithm();
                    gaService.setLocation(actionsImage, (parentRectangle.width - actionsImage.getWidth()) / 2, y);
                    actionsIcon.setVisible(true);
                    Graphiti.getPeService().getProperty(actionIcon, GaProperty.ACTIVE).setValue(GaProperty.FALSE);
                    Graphiti.getPeService().getProperty(actionsIcon, GaProperty.ACTIVE).setValue(GaProperty.TRUE);
                }
                if (!pd.isShowActions()) {
                    actionIcon.setVisible(false);
                    actionsIcon.setVisible(false);
                }
            }
        }
        return true;
    }
}
