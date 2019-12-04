package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class ScriptTaskDoubleClickDelegableFeature extends DoubleClickDelegableFeature {

    private static final String EXTERNAL_STORAGE_HANDLER_CLASS_NAME = "ru.runa.wfe.office.storage.handler.ExternalStorageHandler";

    @Override
    public void execute(ICustomContext context) {
        final ScriptTask scriptTask = (ScriptTask) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        if (!scriptTask.isUseExternalStorage()) {
            super.execute(context);
            return;
        }

        scriptTask.setDelegationClassName(EXTERNAL_STORAGE_HANDLER_CLASS_NAME);
        final DelegableProvider provider = HandlerRegistry.getProvider(scriptTask.getDelegationClassName());
        final String newConfig = provider.showConfigurationDialog(scriptTask);

        if (newConfig != null) {
            scriptTask.setDelegationConfiguration(newConfig);
        }
    }
}
