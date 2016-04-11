package ru.runa.gpd.editor.gef.command;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.Transition;

public abstract class TransitionAbstractBendpointCommand extends Command {

    protected Transition transition;

    protected int index;

    protected Point bendpoint;

    public void setTransitionDecorator(Transition transition) {
        this.transition = transition;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setLocation(int x, int y) {
        bendpoint = new Point(x, y);
    }

}
