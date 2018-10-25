package ru.runa.gpd.extension.regulations.ui;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.jface.action.IAction;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.StartState;

public class GenerateRegulationDataAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            List<String> nodesInSequence = Lists.newArrayList();
            RegulationsUtil.fillRegulationPropertiesWithSequence(null, getActiveDesignerEditor().getDefinition().getFirstChild(StartState.class), nodesInSequence);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

}
