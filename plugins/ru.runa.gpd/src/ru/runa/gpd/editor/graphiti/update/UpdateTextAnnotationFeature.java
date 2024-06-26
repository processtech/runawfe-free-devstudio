package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.bpmn.TextAnnotation;

public class UpdateTextAnnotationFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        TextAnnotation bo = (TextAnnotation) getBusinessObjectForPictogramElement(pe);
        if (textPropertiesDiffer(pe, GaProperty.DESCRIPTION, bo.getDescription())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        TextAnnotation bo = (TextAnnotation) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.DESCRIPTION, bo.getDescription());
        return true;
    }
}
