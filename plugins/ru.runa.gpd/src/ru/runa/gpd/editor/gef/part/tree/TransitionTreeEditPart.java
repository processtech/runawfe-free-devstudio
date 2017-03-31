package ru.runa.gpd.editor.gef.part.tree;

import java.util.List;

import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.jpdl.Action;

public class TransitionTreeEditPart extends ElementTreeEditPart {

    public Transition getTransition() {
        return (Transition) getModel();
    }

    @Override
    protected List<Action> getModelChildren() {
        return getTransition().getChildren(Action.class);
    }

}
