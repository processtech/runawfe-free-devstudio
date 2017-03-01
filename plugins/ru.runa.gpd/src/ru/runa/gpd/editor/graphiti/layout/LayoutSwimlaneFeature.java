package ru.runa.gpd.editor.graphiti.layout;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.services.Graphiti;

import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.PropertyUtil;
import ru.runa.gpd.lang.model.GraphElement;

import com.google.common.base.Objects;

public class LayoutSwimlaneFeature extends LayoutElementFeature {
    public static final String NAME_RECT = "nameRect";

    private boolean isVerticalLayout(ILayoutContext context) {
        ContainerShape containerShape = (ContainerShape) context.getPictogramElement();
        return Objects.equal("true", PropertyUtil.getPropertyValue(containerShape, GaProperty.SWIMLANE_DISPLAY_VERTICAL));
    }

    @Override
    public Dimension getDefaultSize(GraphElement element, ILayoutContext context) {
        Dimension horizontal = super.getDefaultSize(element, context);
        if (isVerticalLayout(context)) {
            horizontal.transpose();
        }
        return horizontal;
    }

    @Override
    public boolean layout(ILayoutContext context) {
        GraphicsAlgorithm ga = context.getPictogramElement().getGraphicsAlgorithm();
        Dimension bounds = adjustBounds(context);
        GraphicsAlgorithm nameRectangle = PropertyUtil.findGaRecursiveByName(ga, NAME_RECT);
        Dimension nameDimension = bounds.getCopy();
        if (isVerticalLayout(context)) {
            nameDimension.setHeight(2 * GRID_SIZE);
        } else {
            nameDimension.setWidth(2 * GRID_SIZE);
        }
        Graphiti.getGaService().setLocationAndSize(nameRectangle, 0, 0, nameDimension.width, nameDimension.height);
        Text nameText = PropertyUtil.findGaRecursiveByName(ga, GaProperty.NAME);
        Graphiti.getGaService().setLocationAndSize(nameText, 0, 0, nameDimension.width, nameDimension.height);
        return true;
    }
}
