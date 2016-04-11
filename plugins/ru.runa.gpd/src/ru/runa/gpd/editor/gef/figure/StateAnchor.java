package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class StateAnchor extends ChopboxAnchor {

    public StateAnchor(IFigure owner) {
        super(owner);
    }

    @Override
    public Point getReferencePoint() {
        Point ref = getBox().getCenter();
        getOwner().translateToAbsolute(ref);
        return ref;
    }

    @Override
    public Point getLocation(Point reference) {
        Rectangle r = getBox().getCopy();

        getOwner().translateToAbsolute(r);
        float centerX = r.x + 0.5f * r.width;
        float centerY = r.y + 0.5f * r.height;

        if (r.isEmpty() || (reference.x == (int) centerX && reference.y == (int) centerY)) {
            return new Point((int) centerX, (int) centerY); // This avoids divide-by-zero
        }

        float dx = reference.x - centerX;
        float dy = reference.y - centerY;

        // r.width, r.height, dx, and dy are guaranteed to be non-zero.
        float scale = 0.5f / Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);

        dx *= scale;
        dy *= scale;
        centerX += dx;
        centerY += dy;

        return new Point(Math.round(centerX), Math.round(centerY));
    }

    @Override
    protected Rectangle getBox() {
        return ((NodeFigure) getOwner()).getBox();
    }

}
