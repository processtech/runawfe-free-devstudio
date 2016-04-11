package ru.runa.gpd.editor.gef.command;

import org.eclipse.gef.commands.Command;

import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Transition;

public class TransitionReconnectCommand extends Command {

    private Transition transition;

    private Node source;

    private Node target;

    private Node old;

    @Override
    public boolean canExecute() {
        if (transition == null || (source == null && target == null)) {
            return false;
        }
        return true;
    }

    @Override
    public void execute() {
        if (source != null) {
            old = transition.getSource();
            transition.getSource().removeLeavingTransition(transition);
            source.addLeavingTransition(transition);
        } else if (target != null) {
            old = transition.getTarget();
            transition.setTarget(target);
        }
    }

    @Override
    public void undo() {
        if (source != null) {
            transition.getSource().removeLeavingTransition(transition);
            old.addLeavingTransition(transition);
        } else if (target != null) {
            transition.setTarget(old);
        }
    }

    public void setSource(Node source) {
        if (!(source instanceof EndState)) {
            this.source = source;
        }
    }

    public void setTarget(Node target) {
        if (!(target instanceof StartState)) {
            this.target = target;
        }
    }

    public void setTransition(Transition transition) {
        this.transition = transition;
    }

}
