package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

public class CenteredFlowLayout extends FlowLayout {
	
    @Override
    protected void setBoundsOfChild(IFigure parent, IFigure child, Rectangle rect) {
        Rectangle copy = rect.getCopy();
        copy.y += (parent.getBounds().height - rect.height) / 2;
        super.setBoundsOfChild(parent, child, copy);
    }

}
