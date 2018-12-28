package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.action.IAction;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;

public class GenerateRegulationDataAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            RegulationsUtil.autoFillRegulationProperties(getActiveDesignerEditor().getDefinition());
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

}
