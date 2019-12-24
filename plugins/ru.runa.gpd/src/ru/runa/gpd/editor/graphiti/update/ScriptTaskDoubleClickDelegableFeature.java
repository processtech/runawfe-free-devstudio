package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;

import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class ScriptTaskDoubleClickDelegableFeature extends DoubleClickDelegableFeature {

    @Override
    public void execute(ICustomContext context) {
        final ScriptTask scriptTask = (ScriptTask) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        if (!scriptTask.isUseExternalStorageOut() && !scriptTask.isUseExternalStorageIn()) {
            super.execute(context);
            return;
        }

        final DelegableProvider provider = HandlerRegistry.getProvider(scriptTask.getDelegationClassName());
        final String newConfig = provider.showConfigurationDialog(scriptTask);

        if (newConfig != null) {
            scriptTask.setDelegationConfiguration(newConfig);
        }
    }
}
