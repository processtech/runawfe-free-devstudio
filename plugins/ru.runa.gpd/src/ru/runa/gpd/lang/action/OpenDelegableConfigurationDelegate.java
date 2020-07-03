package ru.runa.gpd.lang.action;

import com.google.common.collect.Lists;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
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
                            List<String> errors = Lists.newArrayList();
                            DialogEnhancement.checkScriptTaskParametersWithDocxTemplate(delegable, templateFilePath, errors, null);

                            if (delegable instanceof GraphElement && errors.size() > 0) {
                                ProcessDefinition processDefinition = ((GraphElement) delegable).getProcessDefinition();
                                ProcessDefinition mainProcessDefinition = null != processDefinition ? processDefinition.getMainProcessDefinition()
                                        : null;
                                if (null != mainProcessDefinition) {
                                    ProcessDefinitionValidator.logErrors(mainProcessDefinition, errors);
                                }
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

    private boolean isScriptDocxHandlerEnhancement(Delegable delegable) {
        return DialogEnhancement.isOn() && 0 == delegable.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID);
    }

}
