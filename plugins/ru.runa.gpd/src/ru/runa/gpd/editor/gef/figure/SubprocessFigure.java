package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.Subprocess;

public class SubprocessFigure extends StateFigure<Subprocess> {
    @Override
    public void init() {
        super.init();
        addLabel();
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = null;
        if (model.isMinimizedView()) {
            tooltip = model.getLabel();
        }
        return tooltip;
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        Dimension border = dim.getExpanded(-1, -1);
        if (model.isMinimizedView()) {
            g.drawRectangle(new Rectangle(new Point(0, 0), border));
            g.drawImage(NodeRegistry.getNodeTypeDefinition(model.getClass()).getImage(Language.JPDL.getNotation()), (getClientArea().width - ICON_WIDTH) / 2, (getClientArea().height - ICON_HEIGHT) / 2);
        } else {
            super.paintFigure(g, dim);
            // paint subprocess image
            g.drawLine(dim.width - 20, dim.height - 10, dim.width - 10, dim.height - 10);
            g.drawLine(dim.width - 20, dim.height - 10, dim.width - 20, dim.height - 5);
            g.drawLine(dim.width - 15, dim.height - 15, dim.width - 15, dim.height - 5);
            g.drawLine(dim.width - 10, dim.height - 10, dim.width - 10, dim.height - 5);
        }
        if (!model.isMinimizedView()) {
            if (model.isAsync()) {
                g.drawImage(SharedImages.getImage("icons/uml/async.png"), dim.width - GRID_SIZE / 2 - 20, dim.height - GRID_SIZE - 20);
            }
        }
    }

    protected Rectangle getFrameArea(Rectangle origin) {
        return origin;
    }

    @Override
    public Rectangle getClientArea(Rectangle rect) {
        Rectangle r = super.getClientArea(rect);
        return getFrameArea(r);
    }

    @Override
    protected Rectangle getBox() {
        Rectangle r = getBounds().getCopy();
        return getFrameArea(r);
    }

    @Override
    public void setBounds(Rectangle rect) {
        int minimizedSize = 3 * GEFConstants.GRID_SIZE;
        if (model.isMinimizedView()) {
            rect.width = minimizedSize;
            rect.height = minimizedSize;
        } else {
            if (rect.width < getDefaultSize().width) {
                rect.width = getDefaultSize().width;
            }
            if (rect.height < getDefaultSize().height) {
                rect.height = getDefaultSize().height;
            }
        }
        super.setBounds(rect);
    }

    @Override
    public void update() {
        super.update();
        if (model.isMinimizedView()) {
            label.setVisible(false);
        } else {
            label.setVisible(true);
        }
    }

}
