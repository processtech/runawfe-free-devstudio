package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.editor.graphiti.layout.LayoutSwimlaneFeature;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.util.SwimlaneDisplayMode;

public class AddSwimlaneFeature extends AddElementFeature {
    @Override
    public boolean canAdd(IAddContext context) {
        if (context.getNewObject() instanceof Swimlane) {
            Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
            return parentObject instanceof ProcessDefinition;
        }
        return false;
    }

    private boolean isVerticalLayout() {
        return getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.vertical;
    }

    @Override
    public Dimension getDefaultSize(GraphElement element, IAddContext context) {
        Dimension horizontal = super.getDefaultSize(element, context);
        if (isVerticalLayout()) {
            horizontal.transpose();
        }
        return horizontal;
    }

    @Override
    public PictogramElement add(IAddContext context) {
        Swimlane swimlane = (Swimlane) context.getNewObject();
        Dimension bounds = getBounds(context);
        //
        ContainerShape containerShape = Graphiti.getPeCreateService().createContainerShape(context.getTargetContainer(), true);
        containerShape.getProperties().add(new GaProperty(GaProperty.SWIMLANE_DISPLAY_VERTICAL, String.valueOf(isVerticalLayout())));
        Rectangle main = Graphiti.getGaService().createRectangle(containerShape);
        main.setStyle(StyleUtil.getStyleForEvent(getDiagram(), null));
        main.setLineWidth(1);
        Graphiti.getGaService().setLocationAndSize(main, context.getX(), context.getY(), bounds.width, bounds.height);
        //
        Rectangle nameRectangle = Graphiti.getGaService().createRectangle(main);
        nameRectangle.getProperties().add(new GaProperty(GaProperty.ID, LayoutSwimlaneFeature.NAME_RECT));
        nameRectangle.setStyle(StyleUtil.getStyleForEvent(getDiagram(), null));
        // 
        Text nameText = Graphiti.getGaService().createDefaultText(getDiagram(), nameRectangle, swimlane.getName());
        nameText.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        if (getProcessDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.horizontal) {
            nameText.setAngle(270);
        }
        nameText.setHorizontalAlignment(Orientation.ALIGNMENT_CENTER);
        nameText.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        String bpmnName = swimlane.getTypeDefinition().getBpmnElementName();
        nameText.setStyle(StyleUtil.getStyleForText(getDiagram(), bpmnName));
        // 
        link(containerShape, swimlane);
        //
        Graphiti.getPeCreateService().createChopboxAnchor(containerShape);
        layoutPictogramElement(containerShape);
        return containerShape;
    }
}
