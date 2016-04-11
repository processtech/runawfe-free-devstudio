package ru.runa.gpd.editor.gef.figure.uml;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class ForkJoinConnectionAnchor extends ChopboxAnchor implements ReferencedConnectionAnchor {

    public ForkJoinConnectionAnchor(IFigure owner) {
        super(owner);
    }

    @Override
    public Point getLocation(Point reference) {
        Point point = getPerpendicularPoint(reference);
        if (point != null) {
            return point;
        }
        return super.getLocation(reference);
    }

    @Override
    public Point getReferencePoint() {
        return super.getReferencePoint();
    }

    public Point getReferencePoint(Point reference) {
        Point point = getPerpendicularPoint(reference);
        if (point != null) {
            return point;
        }
        return super.getReferencePoint();
    }

    private Point getPerpendicularPoint(Point reference) {
        Rectangle r = Rectangle.SINGLETON;
        r.setBounds(getOwner().getBounds());

        getOwner().translateToAbsolute(r);
        if (reference.x >= r.x && reference.x <= r.x + r.width) {
            int y;
            if (Math.abs(reference.y - r.y) < Math.abs(reference.y - (r.y + r.height))) {
                y = r.y;
            } else {
                y = r.y + r.height;
            }
            return new Point(reference.x, y);
        } else if (reference.y >= r.y && reference.y <= r.y + r.height) {
            int x;
            if (Math.abs(reference.x - r.x) < Math.abs(reference.x - (r.x + r.width))) {
                x = r.x;
            } else {
                x = r.x + r.width;
            }
            return new Point(x, reference.y);
        }
        return null;
    }

}
