package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Swimlane;

import com.google.common.base.Objects;

public class UpdateSwimlaneFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Swimlane bo = (Swimlane) getBusinessObjectForPictogramElement(pe);
        String name = PropertyUtil.findTextValueRecursive(pe, GaProperty.NAME);
        if (!Objects.equal(name, bo.getName())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Swimlane bo = (Swimlane) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, bo.getName());
        return true;
    }
}
