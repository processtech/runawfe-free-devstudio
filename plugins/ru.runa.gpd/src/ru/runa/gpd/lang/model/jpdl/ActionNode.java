package ru.runa.gpd.lang.model.jpdl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Node;

public class ActionNode extends Node implements ActionContainer {
    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        int nodeActionsCount = getNodeActions().size();
        if (nodeActionsCount == 0) {
            errors.add(ValidationError.createLocalizedError(this, "nodeAction.required"));
        }
        if (nodeActionsCount > 1) {
            errors.add(ValidationError.createLocalizedError(this, "nodeAction.single", nodeActionsCount));
        }
    }

    public List<Action> getNodeActions() {
        List<Action> result = new ArrayList<Action>();
        for (ActionImpl action : getChildren(ActionImpl.class)) {
            if (ActionEventType.NODE_ACTION.equals(action.getEventType())) {
                result.add(action);
            }
        }
        return result;
    }
}
