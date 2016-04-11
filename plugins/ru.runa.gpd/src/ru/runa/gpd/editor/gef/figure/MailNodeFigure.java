package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;

import ru.runa.gpd.SharedImages;

public class MailNodeFigure extends StateFigure {
    @Override
    public void init() {
        super.init();
        addLabel();
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        super.paintFigure(g, dim);
        g.drawImage(SharedImages.getImage("icons/uml/mail_envelope.gif"), 5, 5);
    }
}
