package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.gef.figure.uml.TimerAnchor;
import ru.runa.gpd.lang.model.ReceiveMessageNode;

public class ReceiveMessageFigure extends MessageNodeFigure {
    private ConnectionAnchor timerConnectionAnchor;

    @Override
    public void init() {
        super.init();
        this.connectionAnchor = new ReceiveMessageNodeAnchor(this);
        addEmptySpace(0, 2 * GRID_SIZE);
        timerConnectionAnchor = new TimerAnchor(this);
    }

    public ConnectionAnchor getTimerConnectionAnchor() {
        return timerConnectionAnchor;
    }

    @Override
    public Dimension getDefaultSize() {
        return super.getDefaultSize().getExpanded(GRID_SIZE, GRID_SIZE);
    }

    protected Rectangle getFrameArea(Rectangle origin) {
        return new Rectangle(origin.x + GRID_SIZE, origin.y, origin.width - GRID_SIZE, origin.height - GRID_SIZE);
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        Rectangle r = super.getClientArea(rect);
        return getFrameArea(r);
    }

    @Override
    protected Rectangle getBox() {
        Rectangle r = getBounds().getCopy();
        return getFrameArea(r);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        int halfHeight = dim.height / 2;
        int xLeft = (int) (halfHeight * Math.tan(Math.PI / 6));
        PointList points = new PointList(5);
        points.addPoint(0, 0);
        points.addPoint(dim.width - 1, 0);
        points.addPoint(dim.width - 1, dim.height - 1);
        points.addPoint(0, dim.height - 1);
        points.addPoint(xLeft, halfHeight);
        g.drawPolygon(points);
        if (((ReceiveMessageNode) model).getTimer() != null) {
            Utils.paintTimer(g, dim);
        }
    }

    static class ReceiveMessageNodeAnchor extends StateAnchor {
        public ReceiveMessageNodeAnchor(IFigure owner) {
            super(owner);
        }

        @Override
        public Point getLocation(Point reference) {
            Rectangle r = Rectangle.SINGLETON;
            r.setBounds(getBox());
            getOwner().translateToAbsolute(r);
            Point ref = r.getCenter().negate().translate(reference);
            if (ref.x < 0) {
                double cutOffAngle = Math.atan((double) r.height / r.width);
                double refAngle = Math.atan((double) ref.y / ref.x);
                if (Math.abs(refAngle) < cutOffAngle) {
                    double p = (r.width - r.height * Math.tan(Math.PI / 6)) / 2;
                    double k1 = (double) ref.y / ref.x;
                    double b1 = 0;
                    double k2 = r.height / (2 * p - r.width);
                    if (ref.y < 0) {
                        k2 = -1 * k2;
                    }
                    double b2 = k2 * p;
                    double dx = (b2 - b1) / (k1 - k2);
                    double dy = dx * k1 + b1;
                    return new Point((int) Math.round(r.getCenter().x + dx), (int) Math.round(r.getCenter().y + dy));
                }
            }
            return super.getLocation(reference);
        }
    }
}
