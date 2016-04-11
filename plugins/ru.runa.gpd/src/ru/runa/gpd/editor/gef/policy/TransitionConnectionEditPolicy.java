package ru.runa.gpd.editor.gef.policy;

import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ConnectionEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.GroupRequest;

import ru.runa.gpd.editor.gef.command.TransitionDeleteCommand;
import ru.runa.gpd.editor.gef.command.TransitionMoveCommand;
import ru.runa.gpd.lang.model.Transition;

public class TransitionConnectionEditPolicy extends ConnectionEditPolicy {

    @Override
    protected Command getDeleteCommand(GroupRequest request) {
        TransitionDeleteCommand command = new TransitionDeleteCommand();
        command.setTransition((Transition) getHost().getModel());
        return command;
    }

    @Override
    public Command getCommand(Request request) {
        if (REQ_MOVE.equals(request.getType())) {
            Transition transition = (Transition) getHost().getModel();
            if (!request.getExtendedData().containsKey(transition)) {
                request.getExtendedData().put(transition, Boolean.TRUE);
                return new TransitionMoveCommand(transition, (ChangeBoundsRequest) request);
            }
        }
        return super.getCommand(request);
    }

    @Override
    public boolean understandsRequest(Request request) {
        if (REQ_MOVE.equals(request.getType())) {
            return true;
        }
        return super.understandsRequest(request);
    }

}
