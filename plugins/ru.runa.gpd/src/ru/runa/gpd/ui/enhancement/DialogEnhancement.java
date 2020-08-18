package ru.runa.gpd.ui.enhancement;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;

public class DialogEnhancement {

    public static boolean isOn() {
        return dialogEnhancementMode;
    }

    public static Object getConfigurationValue(Delegable delegable, String valueId) {
        Object obj = null;
        try {
            obj = HandlerRegistry.getProvider(delegable.getDelegationClassName()).getConfigurationValue(delegable, valueId);
        } catch (Throwable e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage());
        }
        return obj;
    }

    public static String showConfigurationDialog(Delegable delegable) {
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable,
                DocxDialogEnhancementMode.isScriptDocxHandlerEnhancement(delegable) ? new DocxDialogEnhancementMode(true, 0) {
                    @Override
                    public void invoke(long flags) {
                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE)) {
                            ProcessDefinition processDefinition = ((GraphElement) delegable).getProcessDefinition();
                            ProcessDefinitionValidator.validateDefinition(processDefinition);
                        }
                    }
                } : null);
        return newConfig;
    }

    private static boolean dialogEnhancementMode = true;
}
