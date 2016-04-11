package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;

import ru.runa.gpd.lang.model.Decision;

public class DecisionFigure extends NodeFigure<Decision> {
    @Override
    public void init() {
        super.init();
        addLabel();
        connectionAnchor = new DiamondAnchor(this);
    }

    @Override
    protected String getTooltipMessage() {
        return model.getDelegationConfiguration();
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        int halfWidth = dim.width / 2;
        int halfHeight = dim.height / 2;
        PointList points = new PointList(4);
        points.addPoint(halfWidth, 0);
        points.addPoint(dim.width - 1, halfHeight);
        points.addPoint(halfWidth, dim.height - 1);
        points.addPoint(0, halfHeight);
        g.drawPolygon(points);
    }
}
