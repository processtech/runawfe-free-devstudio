package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Timer;

public class UpdateTimerFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        Timer timer = (Timer) getBusinessObjectForPictogramElement(context.getPictogramElement());
        if (timer.isBoundaryEvent()) {
            GraphicsAlgorithm boundaryEllipse = PropertyUtil.findGaRecursiveByName(context.getPictogramElement(), GaProperty.BOUNDARY_ELLIPSE);
            if (timer.isInterruptingBoundaryEvent() == boundaryEllipse.getLineVisible()) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        Timer timer = (Timer) getBusinessObjectForPictogramElement(containerShape);
        GraphicsAlgorithm boundaryEllipse = PropertyUtil.findGaRecursiveByName(containerShape, GaProperty.BOUNDARY_ELLIPSE);
        boundaryEllipse.setLineVisible(!timer.isInterruptingBoundaryEvent());
        layoutPictogramElement(containerShape);
        return true;
    }

}
