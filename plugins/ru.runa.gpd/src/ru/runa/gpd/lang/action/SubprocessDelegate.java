package ru.runa.gpd.lang.action;

import java.util.Objects;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.editor.graphiti.change.ChangeUsedSubprocessFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.MultiSubprocessDTO;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDTO;
import ru.runa.gpd.ui.dialog.MultiSubprocessDialog;
import ru.runa.gpd.ui.dialog.SubprocessDialog;

public class SubprocessDelegate extends BaseModelActionDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        Subprocess subprocess = getSelection();
        if (subprocess != null) {
            action.setChecked(!subprocess.isEmbedded());
            action.setEnabled(!subprocess.isEmbedded() || subprocess.getEmbeddedSubprocess() == null);
        }
    }

    @Override
    public void run(IAction action) {
        Subprocess subprocess = getSelection();
        openDetails(subprocess);
    }

    public void openDetails(Subprocess subprocess) {
        SubprocessDTO oldSubprocessDTO = (subprocess instanceof MultiSubprocess)
                ? new MultiSubprocessDTO(((MultiSubprocess) subprocess).getDiscriminatorCondition(), subprocess.getVariableMappings(),
                        subprocess.getSubProcessName())
                : new SubprocessDTO(subprocess.getVariableMappings(), subprocess.getSubProcessName());

        SubprocessDialog dialog = (subprocess instanceof MultiSubprocess) ? new MultiSubprocessDialog((MultiSubprocess) subprocess)
                : new SubprocessDialog(subprocess);

        if (dialog.open() != Window.CANCEL) {
            SubprocessDTO newSubprocessDTO = (subprocess instanceof MultiSubprocess)
                    ? new MultiSubprocessDTO(((MultiSubprocessDialog) dialog).getParameters().getDiscriminatorCondition(),
                            dialog.getVariableMappings(true), dialog.getSubprocessName())
                    : new SubprocessDTO(dialog.getVariableMappings(true), dialog.getSubprocessName());
            if (!Objects.equals(newSubprocessDTO, oldSubprocessDTO)) {
                UndoRedoUtil.executeFeature(new ChangeUsedSubprocessFeature(subprocess, newSubprocessDTO));
            }
        }
    }
}
