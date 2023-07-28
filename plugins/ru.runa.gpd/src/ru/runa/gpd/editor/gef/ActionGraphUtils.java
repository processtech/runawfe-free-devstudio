package ru.runa.gpd.editor.gef;

import java.util.List;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;

public class ActionGraphUtils {
    public static final int ACTION_SIZE = 12;
    public static final int ACTION_DELIM = 3;

    @SuppressWarnings("unchecked")
    public static Point getActionFigureLocation(IFigure figure, int actionIndex, boolean feedback) {
        int addPixels = feedback ? ACTION_SIZE/2 : ACTION_SIZE;
        if (figure instanceof TransitionFigure) {
            int shift = actionIndex*(ACTION_SIZE+ACTION_DELIM) + addPixels;
            if (((TransitionFigure) figure).hasSourceDecoration()) {
            	shift += 10;
            }
            PointList pointList = ((TransitionFigure) figure).getPoints();
            Point point0 = pointList.getPoint(0);
            Point point1 = pointList.getPoint(1);
            Dimension diff = point1.getDifference(point0);
            Point dist = new Point(diff.width, diff.height);
            double distance = new Point(0, 0).getDistance(dist);
            double scaleFactor = shift / distance;
            Point translation = dist.getScaled(scaleFactor);
            Point location = point0.getTranslated(translation);
            location.x -= ACTION_SIZE/2;
            location.y -= ACTION_SIZE/2;
            return location;
        } else {
            NodeFigure nodeFigure = (NodeFigure) figure;
            List<IFigure> children = nodeFigure.getActionsContainer().getChildren();
            IFigure prevFigure;
            if (children.size() > actionIndex) {
                prevFigure = children.get(children.size() - 1 - actionIndex);
            } else {
                prevFigure = children.get(0);
            }
            Rectangle prevActionBounds = prevFigure.getBounds();
            return new Point(prevActionBounds.x - 5, prevActionBounds.y);
            /*
            // invert visuals
            actionIndex = size - actionIndex;
            int xShift = actionIndex*(ACTION_SIZE+ACTION_DELIM) - addPixels;
            int yShift = 3*ACTION_SIZE/2;
            if (nodeFigure.isBpmnNotation() || nodeFigure instanceof ActionNodeFigure) {
                yShift += GEFConstants.GRID_SIZE/2;
                xShift += GEFConstants.GRID_SIZE/2;
            }
            return new Point(figure.getBounds().x + figure.getSize().width - xShift - 3*ACTION_SIZE/2, figure.getBounds().y + figure.getSize().height - GEFConstants.GRID_SIZE - yShift);
            */
        }
    }
    
    public static boolean areActionsFitInLine(TransitionFigure connection, int actionsSize) {
        PointList pointList = connection.getPoints();
        Point point0 = pointList.getPoint(0);
        Point point1 = pointList.getPoint(1);
        int shift = actionsSize*(ACTION_SIZE+ACTION_DELIM);
        Dimension diff = point1.getDifference(point0);
        Point dist = new Point(diff.width, diff.height);
        double distance = new Point(0, 0).getDistance(dist);
        if (connection.hasSourceDecoration()) {
        	shift += 10;
        }
        return shift <= distance;
    }
}
