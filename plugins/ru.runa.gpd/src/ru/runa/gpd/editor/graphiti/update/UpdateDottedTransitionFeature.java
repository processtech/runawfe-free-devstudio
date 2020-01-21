package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class UpdateDottedTransitionFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        final PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        final DottedTransition transition = (DottedTransition) getBusinessObjectForPictogramElement(pe);
        final GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null && defaultFlowGa.getPictogramElement().isVisible()) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        final PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        final DottedTransition transition = (DottedTransition) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, transition.getLabel());
        GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null) {
            defaultFlowGa.getPictogramElement().setVisible(true);
        }
        return true;
    }

}
