package ru.runa.gpd.editor.gef.figure.uml;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.GEFConstants;

public class TimerAnchor extends AbstractConnectionAnchor implements GEFConstants {
    public TimerAnchor(IFigure owner) {
        super(owner);
    }

    @Override
    public Point getReferencePoint() {
        Rectangle bounds = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(bounds);
        return new Point(bounds.x + GRID_SIZE, bounds.y + bounds.height - GRID_SIZE);
    }

    @Override
    public Point getLocation(Point reference) {
        Rectangle bounds = getOwner().getBounds().getCopy();
        getOwner().translateToAbsolute(bounds);
        Point center = getReferencePoint();
        Point ref = center.getCopy().negate().translate(reference);
        if (ref.x == 0) {
            return new Point(reference.x, (ref.y > 0) ? bounds.bottom() : bounds.bottom() - 2 * GRID_SIZE);
        }
        if (ref.y == 0) {
            return new Point((ref.x > 0) ? bounds.x + 2 * GRID_SIZE : bounds.x, reference.y);
        }
        float dx = (ref.x > 0) ? 0.5f : -0.5f;
        float dy = (ref.y > 0) ? 0.5f : -0.5f;
        float k = (float) (ref.y * 2 * GRID_SIZE) / (ref.x * 2 * GRID_SIZE);
        k = k * k;
        return center.translate((int) (2 * GRID_SIZE * dx / Math.sqrt(1 + k)), (int) (2 * GRID_SIZE * dy / Math.sqrt(1 + 1 / k)));
    }
}
