package ru.runa.gpd.lang.action;

import java.util.List;
import java.util.ListIterator;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        Delegable delegable = (Delegable) getSelection();
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable,
                isScriptDocxHandlerEnhancement(delegable) ? new DocxDialogEnhancementMode(true, 0) {

                    @Override
                    public void reloadBotTaskEditorXmlFromModel(String newConfiguration, String embeddedFileName, Boolean enableReadDocxButton,
                            Boolean enableDocxMode) {

                        int ttt = 0;
                        ttt++;
                        // reloadDialogXmlFromModel(newConfiguration, embeddedFileName, enableReadDocxButton, enableDocxMode);
                    }

                    @Override
                    public void invoke(long flags) {
                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE)) {
                            // String embeddedFileName = "";
                            // IFile file = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getBotTaskFileName(embeddedFileName));

                            PluginLogger.logInfo("varB !!! ");
                            List<String> usedVariableList = delegable.getVariableNames(false);
                            ListIterator<String> iterator = usedVariableList.listIterator();
                            while (iterator.hasNext()) {
                                String variable = iterator.next();
                                PluginLogger.logInfo("varB > " + variable);
                            }
                        }
                    }
                } : null);
        if (newConfig != null) {
            delegable.setDelegationConfiguration(newConfig);
            if (delegable instanceof ExclusiveGateway) {
                getActiveDesignerEditor().getDiagramEditorPage().getDiagramBehavior().refreshContent();
                ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                        .getDiagramEditorPage().refreshConnections();
            }
        }
    }

    private boolean isScriptDocxHandlerEnhancement(Delegable delegable) {
        return DialogEnhancement.isOn() && 0 == delegable.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID);
    }

}
