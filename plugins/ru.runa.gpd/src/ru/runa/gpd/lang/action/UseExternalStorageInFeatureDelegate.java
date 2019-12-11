package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class UseExternalStorageInFeatureDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        final ScriptTask scriptTask = getSelection();
        scriptTask.setUseExternalStorageIn(!scriptTask.isUseExternalStorageIn());
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        final ScriptTask scriptTask = getSelection();
        if (scriptTask != null) {
            action.setChecked(scriptTask.isUseExternalStorageIn());
        }
    }

}
