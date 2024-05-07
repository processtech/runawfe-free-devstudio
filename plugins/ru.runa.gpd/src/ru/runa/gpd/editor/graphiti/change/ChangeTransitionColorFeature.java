package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.TransitionColor;

public class ChangeTransitionColorFeature extends ChangePropertyFeature<Transition, TransitionColor> {

    public ChangeTransitionColorFeature(Transition target, TransitionColor newValue) {
        super(target, target.getColor(), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setColor(newValue);

    }

    @Override
    protected void undo(IContext context) {
        target.setColor(oldValue);

    }

    @Override
    public String getName() {
        return Localization.getString("Transition.property.color");
    }

}
