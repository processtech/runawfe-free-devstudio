package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.editor.graphiti.TransitionUtil;
import ru.runa.gpd.lang.model.Transition;

public class UpdateTransitionFeature extends UpdateFeature {
    @Override
    public IReason updateNeeded(IUpdateContext context) {
        // retrieve name from pictogram element
        PictogramElement pe = context.getPictogramElement();
        // retrieve name from business model
        Transition transition = (Transition) getBusinessObjectForPictogramElement(pe);
        GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null
                && defaultFlowGa.getPictogramElement().isVisible() != (TransitionUtil.markDefaultTransition() && transition.isDefaultFlow())) {
            return Reason.createTrueReason("Default flow marker is out of date");
        }
        GraphicsAlgorithm exclusiveFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null && exclusiveFlowGa.getPictogramElement().isVisible() != transition.isExclusiveFlow()) {
            return Reason.createTrueReason("Exclusive flow marker is out of date");
        }
        Text nameTextGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.NAME);
        if (nameTextGa != null) {
            boolean nameLabelVisible = !Strings.isNullOrEmpty(transition.getLabel());
            if (nameTextGa.getPictogramElement().isVisible() != nameLabelVisible) {
                return Reason.createTrueReason();
            }
            if (!Objects.equal(nameTextGa.getValue(), transition.getLabel())) {
                return Reason.createTrueReason("Name is out of date");
            }
        }
        Text numberGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.TRANSITION_NUMBER);
        if (numberGa != null) {
            if (!Objects.equal(numberGa.getValue(), StyleUtil.getTransitionNumber(transition))) {
                return Reason.createTrueReason("Transition number is out of date");
            }
            if (!numberGa.getStyle().getId().endsWith(transition.getColor().name())) {
                return Reason.createTrueReason();
            }
            GraphicsAlgorithm colorMarkerGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.TRANSITION_COLOR_MARKER);
            if (numberGa.getY() != nameTextGa.getY() || numberGa.getX() + numberOffsetX(colorMarkerGa, numberGa) != nameTextGa.getX()) {
                return Reason.createTrueReason();
            }
            if (colorMarkerGa.getY() != nameTextGa.getY() || colorMarkerGa.getX() + colorMarkerGa.getWidth() + 1 != nameTextGa.getX()) {
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
        Transition transition = (Transition) getBusinessObjectForPictogramElement(pe);
        PropertyUtil.setTextValueProperty(pe, GaProperty.NAME, transition.getLabel());
        GraphicsAlgorithm defaultFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.DEFAULT_FLOW);
        if (defaultFlowGa != null) {
            defaultFlowGa.getPictogramElement().setVisible(TransitionUtil.markDefaultTransition() && transition.isDefaultFlow());
        }
        GraphicsAlgorithm exclusiveFlowGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.EXCLUSIVE_FLOW);
        if (exclusiveFlowGa != null) {
            exclusiveFlowGa.getPictogramElement().setVisible(transition.isExclusiveFlow());
        }
        GraphicsAlgorithm nameTextGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.NAME);
        if (nameTextGa != null) {
            boolean nameLabelVisible = !Strings.isNullOrEmpty(transition.getLabel());
            nameTextGa.getPictogramElement().setVisible(nameLabelVisible);
        }
        boolean visible = StyleUtil.isTransitionDecoratorVisible(transition);
        GraphicsAlgorithm colorMarkerGa = PropertyUtil.findGaRecursiveByName(pe, GaProperty.TRANSITION_COLOR_MARKER);
        if (colorMarkerGa != null) {
            colorMarkerGa.setStyle(StyleUtil.getTransitionColorMarkerStyle(getDiagram(), transition, transition.getColor()));
            colorMarkerGa.setWidth(colorMarkerGa.getStyle().getFont().getSize() * 2);
            colorMarkerGa.setHeight((int) (colorMarkerGa.getStyle().getFont().getSize() * 1.75));
            colorMarkerGa.setY(nameTextGa.getY());
            colorMarkerGa.setX(nameTextGa.getX() - colorMarkerGa.getWidth() - 1);
            colorMarkerGa.getPictogramElement().setVisible(visible);
        }
        Text numberGa = (Text) PropertyUtil.findGaRecursiveByName(pe, GaProperty.TRANSITION_NUMBER);
        if (numberGa != null) {
            numberGa.setValue(StyleUtil.getTransitionNumber(transition));
            numberGa.setStyle(StyleUtil.getTransitionColorMarkerStyle(getDiagram(), transition, transition.getColor()));
            numberGa.setY(nameTextGa.getY());
            numberGa.setX(nameTextGa.getX() - numberOffsetX(colorMarkerGa, numberGa));
            numberGa.getPictogramElement().setVisible(visible);
        }
        refreshLater();
        return true;
    }

    private int numberOffsetX(GraphicsAlgorithm colorMarkerGa, Text numberGa) {
        int numberWidth = GraphitiUi.getUiLayoutService().calculateTextSize(numberGa.getValue(), numberGa.getStyle().getFont()).getWidth();
        return numberWidth + (colorMarkerGa.getWidth() - numberWidth) / 2 + 1;
    }

    private void refreshLater() {
        Display.getCurrent().asyncExec(new Runnable() {
            @Override
            public void run() {
                ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                        .getDiagramEditorPage().getDiagramBehavior().refresh();
            }
        });
    }

}
