package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class EndTokenStateFigure extends TerminalFigure {
    @Override
    public void init() {
        super.init();
        addEllipse();
        addLabel();
    }

    @Override
    protected void addEllipse() {
        ellipse.setBounds(new Rectangle(3, 3, 16, 16));
        ellipse.setAlpha(0);
        //        IFigure outer = new ImageFigure(SharedImages.getImage("icons/uml/palette/endtoken.png"));//new Ellipse();
        //        outer.setSize(22, 22);
        //        outer.add(ellipse);
        Ellipse outer = new Ellipse();
        outer.setSize(22, 22);
        outer.setAlpha(0);
        outer.setOutline(false);
        outer.add(ellipse);
        GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gridData.horizontalSpan = 2;
        add(outer, gridData);
        //ellipse.add(new ImageFigure(SharedImages.getImage("icons/uml/palette/endtoken.png")));
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        int xShift = (dim.width - 16) / 2;
        int yShift = 3;
        g.drawLine(xShift + 3, yShift + 3, xShift + 13, 13 + yShift);
        g.drawLine(xShift + 13, yShift + 3, xShift + 3, 13 + yShift);
        g.drawOval(xShift, yShift, 16, 16);
    }
}
