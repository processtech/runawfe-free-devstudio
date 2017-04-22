package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class MultiTaskStateFigure extends TaskStateFigure {
    @Override
    public void setBounds(Rectangle rect) {
        if (rect.height < 4 * GRID_SIZE) {
            rect.height = 4 * GRID_SIZE;
        }
        super.setBounds(rect);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        super.paintFigure(g, dim);
        if (!model.isMinimizedView()) {
            Utils.paintSurroudingBoxes(g, dim);
            g.drawText("*", dim.width - 2 * GRID_SIZE, dim.height - 3 * GRID_SIZE / 2);
        }
    }

    @Override
    protected Rectangle getFrameArea(Rectangle origin) {
        if (!model.isMinimizedView()) {
            return new Rectangle(origin.x + GRID_SIZE, origin.y, origin.width - 2 * GRID_SIZE, origin.height - GRID_SIZE);
        }
        return super.getFrameArea(origin);
    }

    @Override
    protected Rectangle getBox() {
        if (model.isMinimizedView()) {
            return super.getBox();
        } else {
            Rectangle r = super.getBox();
            Rectangle borderRect = r.getCopy();
            borderRect.expand(GRID_SIZE / 2, 0);
            return borderRect;
        }
    }

}
