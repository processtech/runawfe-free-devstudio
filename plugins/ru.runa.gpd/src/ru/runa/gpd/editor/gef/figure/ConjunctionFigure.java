package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.Conjunction;

public class ConjunctionFigure extends NodeFigure<Conjunction> {
    @Override
    public void init() {
        super.init();
        addLabel();
        connectionAnchor = new DiamondAnchor(this);
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = null;
        if (model.isMinimizedView()) {
            tooltip = model.getName();
        }
        return tooltip;
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

    @Override
    public void setBounds(Rectangle rect) {
        int minimizedSize = 2 * GEFConstants.GRID_SIZE;
        if (model.isMinimizedView()) {
            rect.width = minimizedSize;
            rect.height = minimizedSize;
        }
        super.setBounds(rect);
    }

    @Override
    public void update() {
        super.update();
        label.setVisible(!model.isMinimizedView());
    }
}
