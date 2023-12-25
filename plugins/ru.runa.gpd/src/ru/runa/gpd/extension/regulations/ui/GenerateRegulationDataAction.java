package ru.runa.gpd.extension.regulations.ui;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GenerateRegulationDataAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            boolean success = RegulationsUtil.validate(processDefinition, true);
            if (success) {
                RegulationsUtil.autoFillRegulationProperties(processDefinition);
                processDefinition.setRegulationGenerated(true);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
    
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        ProcessDefinition processDefinition = getSelection();
        action.setEnabled(processDefinition != null && !processDefinition.isInvalid());
    }

}
