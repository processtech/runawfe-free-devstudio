package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.Transition;

public class UpdateTransitionFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Transition bo = (Transition) getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null && defaultFlowGa.getPictogramElement().isVisible() != bo.isDefaultFlow()) {
            return Reason.createTrueReason();
        }
        GraphicsAlgorithm exclusiveFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null && exclusiveFlowGa.getPictogramElement().isVisible() != bo.isExclusiveFlow()) {
            return Reason.createTrueReason();
        }
        Text nameTextGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.NAME);
        if (nameTextGa != null) {
            boolean nameLabelVisible = !Strings.isNullOrEmpty(bo.getLabel());
            if (nameTextGa.getPictogramElement().isVisible() != nameLabelVisible) {
                return Reason.createTrueReason();
            }
            if (!Objects.equal(nameTextGa.getValue(), bo.getLabel())) {
                return Reason.createTrueReason();
            }
        }
        Text colorMarkerGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.COLOR_MARKER);
        if (colorMarkerGa != null) {
            if (!Objects.equal(colorMarkerGa.getValue(), StyleUtil.getTransitionColorMarker(bo))) {
                return Reason.createTrueReason();
            }
            if (!colorMarkerGa.getStyle().getId().endsWith(bo.getColor().name())) {
                return Reason.createTrueReason();
            }
            if (colorMarkerGa.getY() != nameTextGa.getY() || colorMarkerGa.getX() + colorMarkerOffsetX(colorMarkerGa) != nameTextGa.getX()) {
                return Reason.createTrueReason();
            }
        }
        return Reason.createFalseReason();
    }

    @Override
    public boolean update(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Transition bo = (Transition) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, bo.getLabel());
        GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null) {
            defaultFlowGa.getPictogramElement().setVisible(bo.isDefaultFlow());
        }
        GraphicsAlgorithm exclusiveFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null) {
            exclusiveFlowGa.getPictogramElement().setVisible(bo.isExclusiveFlow());
        }
        GraphicsAlgorithm nameTextGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.NAME);
        if (nameTextGa != null) {
            boolean nameLabelVisible = !Strings.isNullOrEmpty(bo.getLabel());
            nameTextGa.getPictogramElement().setVisible(nameLabelVisible);
        }
        Text colorMarkerGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.COLOR_MARKER);
        if (colorMarkerGa != null) {
            colorMarkerGa.setValue(StyleUtil.getTransitionColorMarker(bo));
            colorMarkerGa.setStyle(StyleUtil.getTransitionColorMarkerStyle(getDiagram(), bo, bo.getColor()));
            colorMarkerGa.setY(nameTextGa.getY());
            colorMarkerGa.setX(nameTextGa.getX() - colorMarkerOffsetX(colorMarkerGa));
        }
        return true;
    }

    private int colorMarkerOffsetX(Text colorMarkerGa) {
        return GraphitiUi.getUiLayoutService().calculateTextSize(colorMarkerGa.getValue(), colorMarkerGa.getStyle().getFont()).getWidth() + 1;
    }

}
