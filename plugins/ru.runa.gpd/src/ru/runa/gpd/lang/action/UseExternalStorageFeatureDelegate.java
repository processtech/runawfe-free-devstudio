package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class UseExternalStorageFeatureDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        final ScriptTask scriptTask = getSelection();
        scriptTask.setUseExternalStorage(!scriptTask.isUseExternalStorage());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        final ScriptTask scriptTask = getSelection();
        if (scriptTask != null) {
            action.setChecked(scriptTask.isUseExternalStorage());
        }
    }
}
