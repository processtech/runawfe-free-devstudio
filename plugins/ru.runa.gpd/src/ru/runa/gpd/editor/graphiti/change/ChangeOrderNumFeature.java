package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.lang.model.Transition;

public class ChangeOrderNumFeature extends ChangePropertyFeature<Transition, Object> {

    Transition anotherTransition;

    public ChangeOrderNumFeature(Transition target, Object newValue) {
        super(target, target.getPropertyValue(PropertyNames.PROPERTY_ORDERNUM), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        anotherTransition = target.getSource().getLeavingTransitions().get(Integer.parseInt((String) newValue) - 1);
        target.getSource().swapChildren(target, anotherTransition);
        target.firePropertyChange(PropertyNames.PROPERTY_ORDERNUM, oldValue, newValue);
        anotherTransition.firePropertyChange(PropertyNames.PROPERTY_ORDERNUM, newValue, oldValue);

    }

    @Override
    protected void undo(IContext context) {
        target.getSource().swapChildren(target, anotherTransition);
        target.firePropertyChange(PropertyNames.PROPERTY_ORDERNUM, newValue, oldValue);
        anotherTransition.firePropertyChange(PropertyNames.PROPERTY_ORDERNUM, oldValue, newValue);
    }

    @Override
    public boolean canUndo(IContext context) {
        return super.canUndo(context) && anotherTransition != null;
    }

    @Override
    public String getName() {
        return Localization.getString("Transition.property.orderNum");
    }

}
