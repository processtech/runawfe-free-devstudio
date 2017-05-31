package ru.runa.gpd.editor.gef.figure;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

import ru.runa.gpd.editor.gef.ActionGraphUtils;
import ru.runa.gpd.lang.model.Action;

public class ActionFigure extends NodeFigure<Action> {
    private boolean multiple = false;

    public static ActionFigure getMultipleFigure() {
        ActionFigure figure = new ActionFigure();
        figure.setMultiple(true);
        return figure;
    }

    public ActionFigure() {
        setSize(ActionGraphUtils.ACTION_SIZE + 1, ActionGraphUtils.ACTION_SIZE + 1);
        setPreferredSize(ActionGraphUtils.ACTION_SIZE + 1, ActionGraphUtils.ACTION_SIZE + 1);
    }

    @Override
    protected String getTooltipMessage() {
        String tooltip = model.toString();
        if (model.getDelegationConfiguration().length() > 0) {
            tooltip += "\n" + model.getDelegationConfiguration();
        }
        return tooltip;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isMultiple() {
        return multiple;
    }

    @Override
    public void setLocation(Point p) {
        super.setLocation(p);
    }

    @Override
    protected void paintFigure(Graphics g, Dimension dim) {
        g.fillOval(1, 1, ActionGraphUtils.ACTION_SIZE - 2, ActionGraphUtils.ACTION_SIZE - 2);
        g.drawOval(1, 1, ActionGraphUtils.ACTION_SIZE - 2, ActionGraphUtils.ACTION_SIZE - 2);
        if (multiple) {
            g.drawLine(1, ActionGraphUtils.ACTION_SIZE / 2, ActionGraphUtils.ACTION_SIZE - 2, ActionGraphUtils.ACTION_SIZE / 2);
            g.drawLine(ActionGraphUtils.ACTION_SIZE / 2, 1, ActionGraphUtils.ACTION_SIZE / 2, ActionGraphUtils.ACTION_SIZE - 2);
        }
    }
}
