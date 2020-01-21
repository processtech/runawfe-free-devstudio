package ru.runa.gpd.editor.graphiti.add;

import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.impl.AbstractAddFeature;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
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
import ru.runa.gpd.editor.graphiti.DiagramFeatureProvider;
import ru.runa.gpd.editor.graphiti.GaProperty;
import ru.runa.gpd.editor.graphiti.StyleUtil;
import ru.runa.gpd.lang.model.bpmn.DottedTransition;

public class AddDottedTransitionFeature extends AbstractAddFeature {
    private DiagramFeatureProvider featureProvider;

    public AddDottedTransitionFeature() {
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
        return (context instanceof IAddConnectionContext && context.getNewObject() instanceof DottedTransition);
    }

    @Override
    public PictogramElement add(IAddContext context) {
        final IAddConnectionContext addConnectionContext = (IAddConnectionContext) context;
        final DottedTransition transition = (DottedTransition) context.getNewObject();
        final Anchor sourceAnchor = addConnectionContext.getSourceAnchor();
        final Anchor targetAnchor = addConnectionContext.getTargetAnchor();
        if (sourceAnchor == null || targetAnchor == null) {
            return null;
        }
        final IPeCreateService peCreateService = Graphiti.getPeCreateService();
        // CONNECTION WITH POLYLINE
        final FreeFormConnection connection = peCreateService.createFreeFormConnection(getDiagram());
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
        final IGaService gaService = Graphiti.getGaService();
        final Polyline polyline = gaService.createPlainPolyline(connection);
        polyline.setStyle(StyleUtil.getTransitionPolylineStyle(getDiagram()));
        // create link and wire it
        link(connection, transition);
        // add static graphical decorators (composition and navigable)
        createArrow(connection);
        createDefaultFlow(connection);
        return connection;
    }

    private void createDefaultFlow(Connection connection) {
        final ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 0.0, true);
        final int xy[] = new int[] { -3, 7, -10, -7 };
        final Polyline polyline = Graphiti.getGaCreateService().createPolyline(connectionDecorator, xy);
        polyline.setStyle(StyleUtil.getTransitionPolylineStyle(getDiagram()));
        connectionDecorator.getProperties().add(new GaProperty(GaProperty.ID, GaProperty.DEFAULT_FLOW));
        connectionDecorator.setVisible(true);
    }

    private void createArrow(Connection connection) {
        final ConnectionDecorator connectionDecorator = Graphiti.getPeCreateService().createConnectionDecorator(connection, false, 1.0, true);
        final int xy[] = new int[] { -10, -5, 0, 0, -10, 5, -8, 0 };
        final int beforeAfter[] = new int[] { 3, 3, 0, 0, 3, 3, 3, 3 };
        final Polygon polyline = Graphiti.getGaCreateService().createPlainPolygon(connectionDecorator, xy, beforeAfter);
        polyline.setStyle(StyleUtil.getTransitionPolylineStyle(getDiagram()));
    }

}
