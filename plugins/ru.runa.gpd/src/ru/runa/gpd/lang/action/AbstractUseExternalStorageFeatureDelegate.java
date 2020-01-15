package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

abstract class AbstractUseExternalStorageFeatureDelegate extends BaseModelActionDelegate {
    private static final String EXTERNAL_STORAGE_HANDLER_CLASS_NAME = "ru.runa.wfe.office.storage.handler.ExternalStorageHandler";

    @Override
    public void run(IAction action) {
        final ScriptTask scriptTask = getSelection();
        final boolean newPropertyValue = changeProperty(scriptTask);

        if (newPropertyValue) {
            scriptTask.setDelegationClassName(EXTERNAL_STORAGE_HANDLER_CLASS_NAME);
        } else {
            scriptTask.setDelegationClassName(null);
            scriptTask.setDelegationConfiguration(null);
        }
    }

    protected abstract boolean changeProperty(ScriptTask scriptTask);

}
