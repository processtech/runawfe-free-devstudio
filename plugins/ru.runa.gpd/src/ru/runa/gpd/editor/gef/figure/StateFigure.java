package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.lang.model.Node;

public abstract class StateFigure<T extends Node> extends NodeFigure<T> {
    @Override
    public void init() {
        super.init();
        connectionAnchor = new StateAnchor(this);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        Dimension border = dim.getExpanded(-1, -1);
        g.drawRoundRectangle(new Rectangle(new Point(0, 0), border), 20, 10);
    }
}
