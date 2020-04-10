package ru.runa.gpd.editor.graphiti;

import java.util.List;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.util.VariableMapping;

public class ChangeVariableMappingsFeature extends ChangePropertyFeature<MessageNode, List<VariableMapping>> {

    public ChangeVariableMappingsFeature(MessageNode target, List<VariableMapping> newValue) {
        super(target, newValue);
    }

    @Override
    public void postUndo(IContext context) {
        target.setVariableMappings(oldValue);
    }

    @Override
    public void postRedo(IContext context) {
        target.setVariableMappings(newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        oldValue = target.getVariableMappings();
        target.setVariableMappings(newValue);
    }

}
