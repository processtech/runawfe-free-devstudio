package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;

public class TransitionCreateCommand extends Command {
    private Node source;
    private Node target;
    private Transition transition;

    @Override
    public boolean canExecute() {
        if (source == null || target == null) {
            return false;
        }
        return true;
    }

    private void createTransition() {
        NodeTypeDefinition nodeTypeDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
        transition = nodeTypeDefinition.createElement(source, false);
        transition.setName(source.getNextTransitionName(nodeTypeDefinition));
        transition.setTarget(target);
    }

    @Override
    public void execute() {
        if (transition == null) {
            createTransition();
        }
        source.addLeavingTransition(transition);
    }

    @Override
    public void undo() {
        source.removeLeavingTransition(transition);
    }

    public void setSource(Node newSource) {
        source = newSource;
    }

    public Node getSource() {
        return source;
    }

    public void setTarget(Node newTarget) {
        target = newTarget;
    }
}
