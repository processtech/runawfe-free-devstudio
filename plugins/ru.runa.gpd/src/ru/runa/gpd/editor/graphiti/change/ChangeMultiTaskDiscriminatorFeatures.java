package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiInstanceDTO;
import ru.runa.gpd.lang.model.MultiTaskState;

public class ChangeMultiTaskDiscriminatorFeatures extends ChangePropertyFeature<MultiTaskState, MultiInstanceDTO> {

    public ChangeMultiTaskDiscriminatorFeatures(MultiTaskState target, MultiInstanceDTO newValue) {
        super(target, new MultiInstanceDTO(target.getMultiinstanceParameters(), target.getVariableMappings()), newValue);
    }

    @Override
    public void execute(ICustomContext context) {
        target.setDiscriminatorUsage(newValue.getDiscriminatorUsage());
        target.setDiscriminatorValue(newValue.getDiscriminatorValue());
        target.setCreationMode(newValue.getCreationMode());
        target.setSwimlane(target.getProcessDefinition().getSwimlaneByName(newValue.getSwimlaneName()));
        target.setDiscriminatorCondition(newValue.getDiscriminatorCondition());
        target.setVariableMappings(newValue.getVariableMappings());
    }

    @Override
    protected void undo(IContext context) {
        target.setDiscriminatorUsage(oldValue.getDiscriminatorUsage());
        target.setDiscriminatorValue(oldValue.getDiscriminatorValue());
        target.setCreationMode(oldValue.getCreationMode());
        target.setSwimlane(target.getProcessDefinition().getSwimlaneByName(oldValue.getSwimlaneName()));
        target.setDiscriminatorCondition(oldValue.getDiscriminatorCondition());
        target.setVariableMappings(oldValue.getVariableMappings());
    }

    @Override
    public String getName() {
        return Localization.getString("Feature.Multiinstance");
    }

}
