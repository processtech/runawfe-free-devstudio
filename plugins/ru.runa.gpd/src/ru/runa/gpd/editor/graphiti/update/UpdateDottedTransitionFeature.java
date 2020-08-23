package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class UpdateDottedTransitionFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        final PictogramElement pe = context.getPictogramElement();
        final DottedTransition transition = (DottedTransition) getBusinessObjectForPictogramElement(pe);
        final Text nameTextGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.NAME);
        if (nameTextGa != null) {
            if (!Objects.equal(nameTextGa.getValue(), transition.getLabel())) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        final PictogramElement pe = context.getPictogramElement();
        final DottedTransition transition = (DottedTransition) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, transition.getLabel());
        return true;
    }

}
