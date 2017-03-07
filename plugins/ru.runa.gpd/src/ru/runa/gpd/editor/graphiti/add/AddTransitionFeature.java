package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
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

import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.Transition;

import com.google.common.base.Strings;

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
        Polyline polyline = gaService.createPolyline(connection);
        polyline.setLineStyle(LineStyle.SOLID);
        polyline.setLineWidth(1);
        polyline.setStyle(StyleUtil.getStyleForTransition(getDiagram()));
        // create link and wire it
        link(connection, transition);
        // add dynamic text decorator for the reference name
        boolean nameLabelVisible = !Strings.isNullOrEmpty(transition.getLabel());
        createLabel(connection, transition.getLabel(), transition.getLabelLocation(), nameLabelVisible);
        // add static graphical decorators (composition and navigable)
        createArrow(connection);
        boolean exclusive = transition.getSource().isExclusive() && transition.getSource().getLeavingTransitions().size() > 1;
        createExclusiveDiamond(connection, exclusive);
        //createDefaultFlow(connection);
        return connection;
    }

    private void createLabel(Connection connection, String transitionName, org.eclipse.draw2d.geometry.Point location, boolean visible) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, true, 0.5, true);
        Text text = Graphiti.getGaService().createText(connectionDecorator);
        text.setHorizontalAlignment(Orientation.ALIGNMENT_LEFT);
        text.setVerticalAlignment(Orientation.ALIGNMENT_CENTER);
        text.setStyle(StyleUtil.getStyleForText(getDiagram()));
        if (location != null) {
            Graphiti.getGaService().setLocation(text, location.x, location.y);
        } else {
            Graphiti.getGaService().setLocation(text, 10, 0);
        }
        text.setValue(transitionName);
        IDimension textDimension = GraphitiUi.getUiLayoutService().calculateTextSize(transitionName, text.getStyle().getFont());
        Graphiti.getGaService().setSize(text, textDimension.getWidth(), textDimension.getHeight());
        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.NAME));
        connectionDecorator.setVisible(visible);
    }

    private void createArrow(Connection connection) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 1.0, true);
        int xy[] = new int[] { -10, -5, 0, 0, -10, 5, -8, 0 };
        int beforeAfter[] = new int[] { 3, 3, 0, 0, 3, 3, 3, 3 };
        Polygon polyline = Graphiti.getGaCreateService().createPolygon(connectionDecorator, xy, beforeAfter);
        polyline.setLineWidth(1);
        polyline.setStyle(StyleUtil.getStyleForPolygonArrow(getDiagram()));
    }

    private void createExclusiveDiamond(Connection connection, boolean visible) {
        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 0.0, true);
        int xy[] = new int[] { -7, -4, 0, 0, -7, 4, -14, 0 };
        int beforeAfter[] = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
        Polygon polyline = Graphiti.getGaCreateService().createPolygon(connectionDecorator, xy, beforeAfter);
        polyline.setLineWidth(1);
        polyline.setStyle(StyleUtil.getStyleForPolygonDiamond(getDiagram()));
        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.EXCLUSIVE_FLOW));
        connectionDecorator.setVisible(visible);
    }

//    private void createDefaultFlow(Connection connection) {
//        ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 0.03, true);
//        int xy[] = new int[] { -12, -4, -20, -4 };
//        int beforeAfter[] = new int[] { 0, 0, 0, 0 };
//        Polygon polyline = Graphiti.getGaCreateService().createPolygon(connectionDecorator, xy, beforeAfter);
//        polyline.setStyle(StyleUtil.getStyleForPolygonDiamond(getDiagram()));
//        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.DEFAULT_FLOW));
//        connectionDecorator.setVisible(false);
//    }
}
