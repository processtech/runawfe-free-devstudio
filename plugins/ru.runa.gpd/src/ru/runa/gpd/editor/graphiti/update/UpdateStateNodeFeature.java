package ru.runa.gpd.editor.graphiti.update;

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

import com.google.common.base.Objects;

public class UpdateStateNodeFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        PictogramElement pe = context.getPictogramElement();
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode && !(bo.getUiParentContainer() instanceof Swimlane)) {
            String swimlaneName = PropertyUtil.findTextValueRecursive(pe, GaProperty.SWIMLANE_NAME);
            if (!Objects.equal(swimlaneName, ((SwimlanedNode) bo).getSwimlaneLabel())) {
                return Reason.createTrueReason();
            }
        }
        String nodeName = PropertyUtil.findTextValueRecursive(pe, GaProperty.NAME);
        if (!Objects.equal(nodeName, bo.getName())) {
            return Reason.createTrueReason();
        }
        String minimazed = PropertyUtil.getPropertyValue(pe, GaProperty.MINIMIZED_VIEW);
        if (!Objects.equal(minimazed, String.valueOf(bo.isMinimizedView()))) {
            return Reason.createTrueReason();
        }
        String async = PropertyUtil.getPropertyValue(pe, GaProperty.ASYNC);
        if (async != null && !Objects.equal(async, String.valueOf(((Synchronizable) bo).isAsync()))) {
            return Reason.createTrueReason();
        }

        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Node bo = (Node) getBusinessObjectForPictogramElement(pe);
        if (bo instanceof SwimlanedNode) {
            PropertyUtil.setTextValueProperty(pe, GaProperty.SWIMLANE_NAME, ((SwimlanedNode) bo).getSwimlaneLabel());
        }
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, bo.getName());
        String minimazed = PropertyUtil.getPropertyValue(pe, GaProperty.MINIMIZED_VIEW);
        if (!Objects.equal(minimazed, String.valueOf(bo.isMinimizedView()))) {
            PropertyUtil.setPropertyValue(pe, GaProperty.MINIMIZED_VIEW, String.valueOf(bo.isMinimizedView()));
            layoutPictogramElement(pe);
        }
        String async = PropertyUtil.getPropertyValue(pe, GaProperty.ASYNC);
        if (async != null && !Objects.equal(async, String.valueOf(((Synchronizable) bo).isAsync()))) {
            PropertyUtil.setPropertyValue(pe, GaProperty.ASYNC, String.valueOf(((Synchronizable) bo).isAsync()));
            layoutPictogramElement(pe);
        }
        return true;
    }
}
