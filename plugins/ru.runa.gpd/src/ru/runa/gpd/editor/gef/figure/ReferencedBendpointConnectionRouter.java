package ru.runa.gpd.editor.gef.figure;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.PrecisionPoint;

import ru.runa.gpd.editor.gef.figure.uml.ReferencedConnectionAnchor;

class ReferencedBendpointConnectionRouter extends BendpointConnectionRouter {
    private static final PrecisionPoint A_POINT = new PrecisionPoint();

    @SuppressWarnings("unchecked")
	@Override
    public void route(Connection conn) {
        PointList points = conn.getPoints();
        points.removeAllPoints();

        List<Bendpoint> bendpoints = (List<Bendpoint>) getConstraint(conn);
        if (bendpoints == null)
            bendpoints = new ArrayList<Bendpoint>();

        Point ref1, ref2;

        if (bendpoints.isEmpty()) {
            if (conn.getTargetAnchor() instanceof ReferencedConnectionAnchor) {
                Point reference = conn.getSourceAnchor().getReferencePoint();
                ref1 = ((ReferencedConnectionAnchor) conn.getTargetAnchor()).getReferencePoint(reference);
            } else {
                ref1 = conn.getTargetAnchor().getReferencePoint();
            }

            if (conn.getSourceAnchor() instanceof ReferencedConnectionAnchor) {
            	Point reference = ref1.getCopy();
                ref2 = ((ReferencedConnectionAnchor) conn.getSourceAnchor()).getReferencePoint(reference);
            } else {
                ref2 = conn.getSourceAnchor().getReferencePoint();
            }
        } else {
            ref1 = new Point(bendpoints.get(0).getLocation());
            conn.translateToAbsolute(ref1);
            ref2 = new Point(bendpoints.get(bendpoints.size() - 1).getLocation());
            conn.translateToAbsolute(ref2);
        }

        A_POINT.setLocation(conn.getSourceAnchor().getLocation(ref1));
        conn.translateToRelative(A_POINT);
        points.addPoint(A_POINT);

        for (int i = 0; i < bendpoints.size(); i++) {
            Bendpoint bp = bendpoints.get(i);
            points.addPoint(bp.getLocation());
        }

        A_POINT.setLocation(conn.getTargetAnchor().getLocation(ref2));
        conn.translateToRelative(A_POINT);
        points.addPoint(A_POINT);
        conn.setPoints(points);
    }
}
