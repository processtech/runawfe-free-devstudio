package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class MultiSubprocessFigure extends SubprocessFigure {
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
        Utils.paintSurroudingBoxes(g, dim);
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        Rectangle r = super.getClientArea(rect);
        Rectangle borderRect = r.getCopy();
        borderRect.expand(-GRID_SIZE / 2, 0);
        return borderRect;
    }
}
