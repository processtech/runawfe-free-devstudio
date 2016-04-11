package ru.runa.gpd.editor.gef.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import ru.runa.gpd.editor.gef.command.ActionDeleteCommand;
import ru.runa.gpd.lang.model.Action;

public class ActionComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        ActionDeleteCommand deleteCommand = new ActionDeleteCommand();
        deleteCommand.setAction((Action) getHost().getModel());
        return deleteCommand;
    }

}
