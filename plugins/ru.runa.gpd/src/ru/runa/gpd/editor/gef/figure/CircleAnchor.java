package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

import ru.runa.gpd.editor.GEFConstants;

/**
 * drawing flat transitions
 * 
 * @author dofs
 * 
 */
public class CircleAnchor extends EllipseAnchor implements GEFConstants {

    public CircleAnchor(IFigure owner) {
        super(owner);
    }

    @Override
    public Point getReferencePoint() {
        Point ref = getOwner().getBounds().getCenter();
        int o = ref.x % GRID_SIZE;
        if (o != 0) {
            ref.x += (o > GRID_SIZE / 2) ? GRID_SIZE - o : -o;
        }
        o = ref.y % GRID_SIZE;
        if (o != 0) {
            ref.y += (o > GRID_SIZE / 2) ? GRID_SIZE - o : -o;
        }
        getOwner().translateToAbsolute(ref);
        return ref;
    }

    @Override
    public Point getLocation(Point reference) {
        Point p = super.getLocation(reference);
        if (Math.abs(p.x - reference.x) <= 3) {
            p.x = reference.x;
        }
        if (Math.abs(p.y - reference.y) <= 3) {
            p.y = reference.y;
        }
        return p;
    }
}
