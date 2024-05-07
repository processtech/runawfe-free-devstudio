package ru.runa.gpd.editor.graphiti.change;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.util.VariableMapping;

public class ChangeVariableMappingsFeature extends ChangePropertyFeature<MessageNode, List<VariableMapping>> {

    public ChangeVariableMappingsFeature(MessageNode target, List<VariableMapping> newValue) {
        super(target, new ArrayList<>(target.getVariableMappings()), newValue);
    }

    @Override
    protected void undo(IContext context) {
        target.setVariableMappings(oldValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setVariableMappings(newValue);
    }

    @Override
    public String getName() {
        return Localization.getString("Subprocess.VariableMappings");
    }

}
