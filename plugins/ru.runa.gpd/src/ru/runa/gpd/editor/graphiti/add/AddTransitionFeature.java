package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;
import org.eclipse.graphiti.ui.services.GraphitiUi;

import com.google.common.base.Strings;

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.TransitionColor;

public class AddTransitionFeature extends AbstractAddFeature {
    private DiagramFeatureProvider featureProvider;

    public AddTransitionFeature() {
        super(null);
    }

    public void setFeatureProvider(DiagramFeatureProvider featureProvider) {
        this.featureProvider = featureProvider;
    }

    @Override
    public DiagramFeatureProvider getFeatureProvider() {
        return featureProvider;
    }

    @Override
    public boolean canAdd(IAddContext context) {
        return (context instanceof IAddConnectionContext && context.getNewObject() instanceof Transition);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        IAddConnectionContext addConnectionContext = (IAddConnectionContext) context;
        Transition transition = (Transition) context.getNewObject();
        Anchor sourceAnchor = addConnectionContext.getSourceAnchor();
        Anchor targetAnchor = addConnectionContext.getTargetAnchor();
        if (sourceAnchor == null || targetAnchor == null) {
            return null;
        }
        IPeCreateService peCreateService = Graphiti.getPeCreateService();
        // CONNECTION WITH POLYLINE
        FreeFormConnection connection = peCreateService.createFreeFormConnection(getDiagram());
        connection.setStart(sourceAnchor);
        connection.setEnd(targetAnchor);
        sourceAnchor.getOutgoingConnections().add(connection);
        targetAnchor.getIncomingConnections().add(connection);
        for (org.eclipse.draw2d.geometry.Point bendpoint : transition.getBendpoints()) {
            Point point = StylesFactory.eINSTANCE.createPoint();
            point.setX(bendpoint.x);
            point.setY(bendpoint.y);
            connection.getBendpoints().add(point);
        }
        IGaService gaService = Graphiti.getGaService();
        Polyline polyline = gaService.createPlainPolyline(connection);
        polyline.setStyle(StyleUtil.getTransitionPolylineStyle(getDiagram()));
        // create link and wire it
        link(connection, transition);
        // add dynamic text decorator for the reference name
        boolean nameLabelVisible = !Strings.isNullOrEmpty(transition.getLabel());
        createLabel(connection, transition.getLabel(), transition.getLabelLocation(), nameLabelVisible, transition);
        // add static graphical decorators (composition and navigable)
        createArrow(connection);
        boolean exclusive = transition.getSource().isExclusive() && transition.getSource().getLeavingTransitions().size() > 1;
        createExclusiveDiamond(connection, exclusive);
        // createDefaultFlow(connection);
        return connection;
    }

    private void createLabel(Connection connection, String transitionName, org.eclipse.draw2d.geometry.Point location, boolean visible,
            Transition transition) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, true, 0.5, true);
        Text text = Graphiti.getGaService().createText(connectionDecorator);
        text.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
        text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        text.setStyle(StyleUtil.getTextStyle(getDiagram(), transition));
        if (location != null) {
            Graphiti.getGaService().setLocation(text, location.x, location.y);
        } else {
            Graphiti.getGaService().setLocation(text, 10, 0);
        }
        text.setValue(transitionName);
        IDimension textDimension = GraphitiUi.getUiLayoutService().calculateTextSize(transitionName, text.getStyle().getFont());
        Graphiti.getGaService().setSize(text, textDimension.getWidth(), textDimension.getHeight());
        createColorMarker(connection, location, transition);
        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        connectionDecorator.setVisible(visible);
    }

    private void createColorMarker(Connection connection, org.eclipse.draw2d.geometry.Point location, Transition transition) {
        if (transition.getSource() instanceof FormNode) {
            TransitionColor transitionColor = transition.getColor();
            boolean visible = StyleUtil.isTransitionDecoratorVisible(transition);

            ConnectionDecorator colorMarkerDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, true, 0.5, true);
            Ellipse ellipse = Graphiti.getGaService().createEllipse(colorMarkerDecorator);
            ellipse.setStyle(StyleUtil.getTransitionColorMarkerStyle(getDiagram(), transition, transitionColor));
            ellipse.setWidth((int) (ellipse.getStyle().getFont().getSize() * 2));
            ellipse.setHeight((int) (ellipse.getStyle().getFont().getSize() * 1.75));
            ellipse.setTransparency(.75);
            Graphiti.getGaService().setLocation(ellipse, (location == null ? 10 : location.x) - ellipse.getWidth() - 1,
                    location == null ? 0 : location.y);
            colorMarkerDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.TRANSITION_COLOR_MARKER));
            colorMarkerDecorator.setVisible(visible);

            ConnectionDecorator numberDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, true, 0.5, true);
            Text number = Graphiti.getGaService().createText(numberDecorator);
            number.setStyle(StyleUtil.getTransitionColorMarkerStyle(getDiagram(), transition, transitionColor));
            number.setValue(StyleUtil.getTransitionNumber(transition));
            IDimension textDimension = GraphitiUi.getUiLayoutService().calculateTextSize(number.getValue(), number.getStyle().getFont());
            Graphiti.getGaService().setSize(number, textDimension.getWidth(), textDimension.getHeight());
            Graphiti.getGaService().setLocation(number,
                    (location == null ? 10 : location.x) - (ellipse.getWidth() - textDimension.getWidth()) / 2 - textDimension.getWidth() - 1,
                    location == null ? 0 : location.y);
            numberDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.TRANSITION_NUMBER));
            numberDecorator.setVisible(visible);
        }
    }

    private void createArrow(Connection connection) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 1.0, true);
        int xy[] = new int[] { -10, -5, 0, 0, -10, 5, -8, 0 };
        int beforeAfter[] = new int[] { 3, 3, 0, 0, 3, 3, 3, 3 };
        Polygon polyline = Graphiti.getGaCreateService().createPlainPolygon(connectionDecorator, xy, beforeAfter);
        polyline.setStyle(StyleUtil.getTransitionPolylineStyle(getDiagram()));
    }

    private void createExclusiveDiamond(Connection connection, boolean visible) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 0.0, true);
        int xy[] = new int[] { -7, -4, 0, 0, -7, 4, -14, 0 };
        int beforeAfter[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        Polygon polyline = Graphiti.getGaCreateService().createPlainPolygon(connectionDecorator, xy, beforeAfter);
        polyline.setStyle(StyleUtil.getTransitionDiamondPolylineStyle(getDiagram()));
        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.EXCLUSIVE_FLOW));
        connectionDecorator.setVisible(visible);
    }
}
