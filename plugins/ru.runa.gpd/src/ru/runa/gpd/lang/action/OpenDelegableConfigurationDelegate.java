package ru.runa.gpd.lang.action;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        Delegable delegable = (Delegable) getSelection();
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable,
                isScriptDocxHandlerEnhancement(delegable) ? new DocxDialogEnhancementMode(true, 0) {
                    private String templateFilePath;

                    @Override
                    public void invoke(long flags) {
                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE)) {
                            try {
                                checkTemplate(templateFilePath, delegable);
                            } catch (IOException | CoreException e) {
                                e.printStackTrace();
                            }
                        } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_SET_PROCESS_FILEPATH)) {
                            templateFilePath = this.defaultFileName;
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

    private void checkTemplate(String templateFilePath, Delegable delegable) throws IOException, CoreException {
        if (Strings.isNullOrEmpty(templateFilePath)) {
            return;
        }

        IFile file = EmbeddedFileUtils.getProcessFile(templateFilePath);
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(Localization.getString("OpenDelegableConfigurationDelegate.cantGetFile"));
            return;
        }

        try (InputStream inputStream = file.getContents()) {
            if (null == file || !file.exists()) {
                PluginLogger.logInfo(Localization.getString("OpenDelegableConfigurationDelegate.cantGetInputStream"));
                return;
            }
            Map<String, Integer> variablesMap = DocxDialogEnhancementMode.getVariableNamesFromDocxTemplate(inputStream);
            List<String> errors = Lists.newArrayList();
            List<String> usedVariableList = delegable.getVariableNames(false);
            for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                String variable = entry.getKey();
                ListIterator<String> iterator = usedVariableList.listIterator();
                boolean exists = false;
                while (iterator.hasNext()) {
                    if (iterator.next().compareTo(variable) == 0) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    errors.add(Localization.getString("OpenDelegableConfigurationDelegate.noVariableForDocx") + " : ${" + variable + "}");
                }
            }
            if (delegable instanceof GraphElement) {
                ProcessDefinition processDefinition = ((GraphElement) delegable).getProcessDefinition();
                ProcessDefinition mainProcessDefinition = null != processDefinition ? processDefinition.getMainProcessDefinition() : null;
                if (null != mainProcessDefinition) {
                    ProcessDefinitionValidator.logErrors(mainProcessDefinition, errors);
                }
            }
        }
    }

    private boolean isScriptDocxHandlerEnhancement(Delegable delegable) {
        return DialogEnhancement.isOn() && 0 == delegable.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID);
    }

}
