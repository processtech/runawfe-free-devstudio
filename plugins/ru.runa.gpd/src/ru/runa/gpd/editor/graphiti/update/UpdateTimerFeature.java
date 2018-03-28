package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;

import ru.runa.gpd.editor.graphiti.GraphUtil;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.Timer;

public class UpdateTimerFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        Timer timer = (Timer) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (timer.getParent() instanceof ITimed) {
            ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
            if (timer.isInterruptingBoundaryEvent() == containerShape.getChildren().get(0).isVisible()) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
        Timer timer = (Timer) getBusinessObjectForPictogramElement(containerShape);
        GraphUtil.createBoundaryEventEllipse(getDiagram(), containerShape.getChildren().get(0), timer, ga.getWidth(), ga.getHeight());
        // TODO this does not updates view immediately, only on diagram saving
        // containerShape.getChildren().get(0).setVisible(!timer.isInterruptingBoundaryEvent());
        layoutPictogramElement(containerShape);
        return true;
    }

}
