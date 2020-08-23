package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;

public class ScriptTaskDoubleClickDelegableFeature extends DoubleClickDelegableFeature {

    @Override
    public void execute(ICustomContext context) {
        final ScriptTask scriptTask = (ScriptTask) fp.getBusinessObjectForPictogramElement(context.getInnerPictogramElement());
        if (!scriptTask.isUseExternalStorageOut() && !scriptTask.isUseExternalStorageIn()) {
            super.execute(context);
            return;
        }
        final String newConfig = DialogEnhancement.showConfigurationDialog(scriptTask);
        if (newConfig != null) {
            scriptTask.setDelegationConfiguration(newConfig);
        }
    }
}
