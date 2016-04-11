package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Ellipse;

public abstract class TerminalFigure extends NodeFigure {
    protected final Ellipse ellipse = new Ellipse();

    @Override
    public void init() {
        super.init();
        connectionAnchor = new CircleAnchor(ellipse);
    }

    protected abstract void addEllipse();
}
