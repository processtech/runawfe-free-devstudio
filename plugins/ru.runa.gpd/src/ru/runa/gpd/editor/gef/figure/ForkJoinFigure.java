package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.editor.gef.figure.uml.ForkJoinConnectionAnchor;

public class ForkJoinFigure extends NodeFigure {
    private static final int MIN_SIZE = 5;

    private static enum EDirection {
        HORIZONTAL,
        VERTICAL;
    }

    private EDirection direction = EDirection.HORIZONTAL;

    @Override
    public void init() {
        super.init();
        connectionAnchor = new ForkJoinConnectionAnchor(this);
    }

    @Override
    public Dimension getDefaultSize() {
        if (direction == EDirection.HORIZONTAL) {
            return new Dimension(GRID_SIZE * 16, MIN_SIZE);
        } else {
            return new Dimension(MIN_SIZE, GRID_SIZE * 16);
        }
    }

    @Override
    public void setBounds(Rectangle rect) {
        if (rect.width < rect.height) {
            rect.width = MIN_SIZE;
            direction = EDirection.VERTICAL;
        } else {
            rect.height = MIN_SIZE;
            direction = EDirection.HORIZONTAL;
        }
        super.setBounds(rect);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        g.setBackgroundColor(ColorConstants.black);
        g.fillRectangle(0, 0, dim.width, dim.height);
    }
}
