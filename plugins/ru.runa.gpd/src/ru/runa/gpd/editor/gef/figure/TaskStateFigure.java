package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import ru.runa.gpd.SharedImages;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.gef.figure.uml.TimerAnchor;
import ru.runa.gpd.lang.model.TaskState;

public class TaskStateFigure extends StateFigure<TaskState> {
    private ConnectionAnchor timerConnectionAnchor;

    @Override
    public void init() {
        super.init();
        addSwimlaneLabel();
        addLabel();
        addActionsContainer();
        timerConnectionAnchor = new TimerAnchor(this);
    }

    @Override
    public Dimension getDefaultSize() {
        return super.getDefaultSize().getExpanded(GRID_SIZE, GRID_SIZE);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        Dimension border = dim.getExpanded(-1, -1);
        if (model.isMinimizedView()) {
            g.drawRectangle(new Rectangle(new Point(0, 0), border));
        } else {
            g.drawRoundRectangle(new Rectangle(new Point(0, 0), border), 20, 10);
        }
        if (!model.isMinimizedView()) {
            if (model.isAsync()) {
                g.drawImage(SharedImages.getImage("icons/uml/async.png"), dim.width - GRID_SIZE / 2 - 20, dim.height - GRID_SIZE - 20);
            }
            if (model.getTimer() != null) {
                Utils.paintTimer(g, dim);
            }
        }
    }

    protected Rectangle getFrameArea(Rectangle origin) {
        if (model.isMinimizedView()) {
            return new Rectangle(origin.x + GRID_SIZE / 2, origin.y + GRID_SIZE / 2, origin.width - GRID_SIZE, origin.height - GRID_SIZE);
        } else {
            return new Rectangle(origin.x + GRID_SIZE, origin.y, origin.width - GRID_SIZE, origin.height - GRID_SIZE);
        }
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

    public ConnectionAnchor getTimerConnectionAnchor() {
        return timerConnectionAnchor;
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = null;
        if (model.isMinimizedView()) {
            tooltip = model.getSwimlaneLabel();
            tooltip += "\n" + model.getName();
        }
        return tooltip;
    }

    @Override
    public void setBounds(Rectangle rect) {
        int minimizedSize = 2 * GEFConstants.GRID_SIZE;
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
            if (swimlaneLabel != null) {
                swimlaneLabel.setVisible(false);
            }
            actionsContainer.setVisible(false);
        } else {
            label.setVisible(true);
            if (swimlaneLabel != null) {
                swimlaneLabel.setVisible(true);
            }
            actionsContainer.setVisible(model.getProcessDefinition().isShowActions());
        }
    }
}
