package ru.runa.gpd.editor.gef.command;

import org.eclipse.draw2d.geometry.Point;

public class TransitionMoveBendpointCommand extends TransitionAbstractBendpointCommand {

    private Point oldBendpoint;

    @Override
    public void execute() {
        oldBendpoint = transition.getBendpoints().get(index);
        transition.setBendpoint(index, bendpoint);
    }

    @Override
    public void undo() {
        transition.setBendpoint(index, oldBendpoint);
    }

}
