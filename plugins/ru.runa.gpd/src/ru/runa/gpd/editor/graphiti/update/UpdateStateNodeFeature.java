package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Synchronizable;

public class UpdateStateNodeFeature extends UpdateFeature {

    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode && !(bo.getUiParentContainer() instanceof Swimlane)) {
            if (textPropertiesDiffer(pe, GaProperty.SWIMLANE_NAME, ((SwimlanedNode) bo).getSwimlaneLabel())) {
                return Reason.createTrueReason();
            }
        }
        if (textPropertiesDiffer(pe, GaProperty.NAME, bo.getName())) {
            return Reason.createTrueReason();
        }
        if (propertiesDiffer(pe, GaProperty.MINIMIZED_VIEW, String.valueOf(bo.isMinimizedView()))) {
            return Reason.createTrueReason();
        }
        if (bo instanceof Synchronizable) {
            if (propertiesDiffer(pe, GaProperty.ASYNC, String.valueOf(((Synchronizable) bo).isAsync()))) {
                return Reason.createTrueReason();
            }
        }
        String tooltip = PropertyUtil.getPropertyValue(pe, GaProperty.TOOLTIP);
        if (!Objects.equal(tooltip, bo.getTooltip())) {
            return Reason.createTrueReason();
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // Update pictogram element properties from business object
        PictogramElement pe = context.getPictogramElement();
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);

        if (bo instanceof SwimlanedNode) {
            String peSwimlaneName = PropertyUtil.findTextValueRecursive(pe, GaProperty.SWIMLANE_NAME);
            if (textPropertiesDiffer(pe, GaProperty.SWIMLANE_NAME, ((SwimlanedNode) bo).getSwimlaneLabel())) {
                PropertyUtil.setTextValueProperty(pe, GaProperty.SWIMLANE_NAME, ((SwimlanedNode) bo).getSwimlaneLabel());
            }
        }
        if (textPropertiesDiffer(pe, GaProperty.NAME, bo.getName())) {
            PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, bo.getName());
        }
        if (propertiesDiffer(pe, GaProperty.MINIMIZED_VIEW, String.valueOf(bo.isMinimizedView()))) {
            PropertyUtil.setPropertyValue(pe, GaProperty.MINIMIZED_VIEW, String.valueOf(bo.isMinimizedView()));
            layoutPictogramElement(pe);
        }
        if (bo instanceof Synchronizable) {
            if (propertiesDiffer(pe, GaProperty.ASYNC, String.valueOf(((Synchronizable) bo).isAsync()))) {
                PropertyUtil.setPropertyValue(pe, GaProperty.ASYNC, String.valueOf(((Synchronizable) bo).isAsync()));
                layoutPictogramElement(pe);
            }
        }
        PropertyUtil.setPropertyValue(pe, GaProperty.TOOLTIP, bo.getTooltip());
        return true;
    }

}
