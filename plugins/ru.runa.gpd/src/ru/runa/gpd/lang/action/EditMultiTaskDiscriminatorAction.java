package ru.runa.gpd.lang.action;

import java.util.Objects;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import ru.runa.gpd.editor.graphiti.change.ChangeMultiTaskDiscriminatorFeatures;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.MultiInstanceDTO;
import ru.runa.gpd.ui.dialog.MultiTaskDiscriminatorDialog;

public class EditMultiTaskDiscriminatorAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        MultiTaskState state = getSelection();
        MultiTaskDiscriminatorDialog dialog = new MultiTaskDiscriminatorDialog(state);
        MultiInstanceDTO oldMultiTaskStateDTO = new MultiInstanceDTO(state.getMultiinstanceParameters(), state.getVariableMappings());
        if (dialog.open() == IDialogConstants.OK_ID) {
            MultiInstanceDTO newMultiTaskStateDTO = new MultiInstanceDTO(dialog.getParameters(), dialog.getVariableMappings());
            if (!Objects.equals(newMultiTaskStateDTO, oldMultiTaskStateDTO)) {
                UndoRedoUtil.executeFeature(new ChangeMultiTaskDiscriminatorFeatures(state, newMultiTaskStateDTO));
            }
        }
    }
}
