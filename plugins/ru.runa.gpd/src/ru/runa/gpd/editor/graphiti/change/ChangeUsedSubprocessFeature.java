package ru.runa.gpd.editor.graphiti.change;

import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.MultiSubprocessDTO;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDTO;

public class ChangeUsedSubprocessFeature extends ChangePropertyFeature<Subprocess, SubprocessDTO> {

    public ChangeUsedSubprocessFeature(Subprocess target, SubprocessDTO newValue) {
        super(target,
                (target instanceof MultiSubprocess)
                        ? new MultiSubprocessDTO(((MultiSubprocess) target).getDiscriminatorCondition(), target.getVariableMappings(), target.getSubProcessName())
                        : new SubprocessDTO(target.getVariableMappings(), target.getSubProcessName()),
                newValue);
        if (!(target instanceof MultiSubprocess)) {
            target.setEmbedded(false);
        }
    }

    @Override
    public void execute(ICustomContext context) {
        target.setSubProcessName(newValue.getSubprocessName());
        target.setVariableMappings(newValue.getVariableMappings());
        if (target instanceof MultiSubprocess) {
            ((MultiSubprocess) target).setDiscriminatorCondition(newValue.getDiscriminatorCondition());
        }
    }

    @Override
    protected void undo(IContext context) {
        target.setSubProcessName(oldValue.getSubprocessName());
        target.setVariableMappings(oldValue.getVariableMappings());
        if (target instanceof MultiSubprocess) {
            ((MultiSubprocess) target).setDiscriminatorCondition(oldValue.getDiscriminatorCondition());
        }
    }

    @Override
    public String getName() {
        return Localization.getString("label.action.subprocess");
    }

}
