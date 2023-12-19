package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.Subprocess;
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
        if (subprocess instanceof MultiSubprocess) {
            MultiSubprocess multiSubprocess = (MultiSubprocess) subprocess;
            MultiSubprocessDialog dialog = new MultiSubprocessDialog(multiSubprocess);
            if (dialog.open() != Window.CANCEL) {
                multiSubprocess.setSubProcessName(dialog.getSubprocessName());
                multiSubprocess.setVariableMappings(dialog.getVariableMappings(true));
                multiSubprocess.setDiscriminatorCondition(dialog.getParameters().getDiscriminatorCondition());
            }
        } else {
            SubprocessDialog dialog = new SubprocessDialog(subprocess);
            if (dialog.open() != Window.CANCEL) {
                subprocess.setSubProcessName(dialog.getSubprocessName());
                subprocess.setEmbedded(false);
                subprocess.setVariableMappings(dialog.getVariableMappings(true));
            }
        }
    }
}
