package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.editors.DiffEditor;

public class ExportProcessDiff extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        ((DiffEditor)getActiveEditor()).exportToFile();
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(getActiveEditor() != null && getActiveEditor() instanceof DiffEditor);
    }
}
