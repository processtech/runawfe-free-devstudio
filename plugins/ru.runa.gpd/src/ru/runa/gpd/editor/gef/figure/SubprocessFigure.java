package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;

import ru.runa.gpd.lang.model.Subprocess;

public class SubprocessFigure extends StateFigure<Subprocess> {
    @Override
    public void init() {
        super.init();
        addLabel();
    }

    @Override
    protected String getTooltipMessage() {
        return model.getSubProcessName();
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        super.paintFigure(g, dim);
        // paint subprocess image
        g.drawLine(dim.width - 20, dim.height - 10, dim.width - 10, dim.height - 10);
        g.drawLine(dim.width - 20, dim.height - 10, dim.width - 20, dim.height - 5);
        g.drawLine(dim.width - 15, dim.height - 15, dim.width - 15, dim.height - 5);
        g.drawLine(dim.width - 10, dim.height - 10, dim.width - 10, dim.height - 5);
    }
}
