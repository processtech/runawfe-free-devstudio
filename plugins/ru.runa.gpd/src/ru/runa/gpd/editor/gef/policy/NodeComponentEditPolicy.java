package ru.runa.gpd.editor.gef.policy;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import ru.runa.gpd.editor.gef.command.NodeDeleteCommand;
import ru.runa.gpd.lang.model.Node;

public class NodeComponentEditPolicy extends ComponentEditPolicy {

    @Override
    protected Command createDeleteCommand(GroupRequest request) {
        NodeDeleteCommand nodeDeleteCommand = new NodeDeleteCommand();
        nodeDeleteCommand.setNode((Node) getHost().getModel());
        return nodeDeleteCommand;
    }

}
