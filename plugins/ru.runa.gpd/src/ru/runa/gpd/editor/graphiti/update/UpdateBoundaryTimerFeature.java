package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;

public class UpdateBoundaryTimerFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        if (context.getPictogramElement() instanceof ContainerShape) {
            ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
            Timer timer = (Timer) getBusinessObjectForPictogramElement(containerShape);
            if (!containerShape.getChildren().isEmpty() && timer.getParent() instanceof ITimed
                    && timer.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        Timer timer = (Timer) getBusinessObjectForPictogramElement(containerShape);
        containerShape.getChildren().get(0).setVisible(!timer.isInterruptingBoundaryEvent());
        layoutPictogramElement(containerShape.getChildren().get(0));
        return true;
    }

}
