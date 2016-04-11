package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;

public class WaitStateFigure extends StateFigure {
    @Override
    public void init() {
        super.init();
        addLabel();
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        super.paintFigure(g, dim);
        int offset = 5;
        int diameter = 18;
        int center = offset + diameter / 2;
        g.drawOval(offset, offset, diameter, diameter);
        g.drawLine(center, center, center, center + diameter / 2 - 5);
        g.drawLine(center, center, center + diameter / 2 - 5, center - diameter / 2 + 5);
    }
}
