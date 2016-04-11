package ru.runa.gpd.editor.gef.command;

import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.lang.model.Transition;

import com.google.common.collect.Lists;

public class TransitionMoveCommand extends Command {

    private final Transition transition;
    private final ChangeBoundsRequest request;
    private List<Point> oldBendpoints;

    public TransitionMoveCommand(Transition transition, ChangeBoundsRequest request) {
        this.transition = transition;
        this.request = request;
    }

    @Override
    public void execute() {
        if (oldBendpoints == null) {
            oldBendpoints = Lists.newArrayList(transition.getBendpoints());
        }
        List<Point> newBendpoints = Lists.newArrayListWithExpectedSize(transition.getBendpoints().size());
        for (Point oldBendpoint : oldBendpoints) {
            int xCount = (int) Math.round((double) request.getMoveDelta().x / GEFConstants.GRID_SIZE);
            int x = oldBendpoint.x + xCount * GEFConstants.GRID_SIZE;
            int yCount = (int) Math.round((double) request.getMoveDelta().y / GEFConstants.GRID_SIZE);
            int y = oldBendpoint.y + yCount * GEFConstants.GRID_SIZE;
            newBendpoints.add(new Point(x, y));
        }
        transition.setBendpoints(newBendpoints);
    }

    @Override
    public void undo() {
        transition.setBendpoints(oldBendpoints);
    }

}
