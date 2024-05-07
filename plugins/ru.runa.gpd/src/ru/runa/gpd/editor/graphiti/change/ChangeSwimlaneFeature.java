package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;

public class ChangeSwimlaneFeature extends ChangePropertyFeature<SwimlanedNode, Swimlane> {

    protected ChangeSwimlaneFeature(SwimlanedNode target, Swimlane oldValue, Swimlane newValue) {
        super(target, oldValue, newValue);
    }

    public ChangeSwimlaneFeature(SwimlanedNode target, Swimlane newValue) {
        this(target, target.getSwimlane(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setSwimlane(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setSwimlane(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("ru.runa.wfe.extension.handler.user.AssignSwimlaneActionHandler");
    }

}
