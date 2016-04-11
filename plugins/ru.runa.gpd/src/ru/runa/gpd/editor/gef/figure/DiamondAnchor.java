package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

public class DiamondAnchor extends AbstractConnectionAnchor {

    public DiamondAnchor(IFigure owner) {
        super(owner);
    }

    public Point getLocation(Point reference) {
        Rectangle r = Rectangle.SINGLETON;
        r.setBounds(getOwner().getBounds());
        getOwner().translateToAbsolute(r);
        Point ref = r.getCenter().negate().translate(reference);
        float dx, dy;
        if (Math.abs(ref.x * r.height) == Math.abs(Math.abs(ref.y) * r.width)) {
            dx = ref.x > 0 ? r.width / 4 : -r.width / 4;
            dy = ref.y > 0 ? r.height / 4 : -r.height / 4;
        } else if (ref.x == 0) {
            dx = 0;
            dy = ref.y > 0 ? r.height / 2 : -r.height / 2;
        } else {
            float numerator = ref.x * r.height * r.width / 2;
            float firstDenominator = Math.abs(ref.y) * r.width + ref.x * r.height;
            float secondDenominator = Math.abs(ref.y) * r.width - ref.x * r.height;
            dx = (ref.x > 0) ? numerator / firstDenominator : numerator / secondDenominator;
            dy = dx * ref.y / ref.x;
        }
        return new Point(Math.round(r.getCenter().x + dx), Math.round(r.getCenter().y + dy));
    }

}