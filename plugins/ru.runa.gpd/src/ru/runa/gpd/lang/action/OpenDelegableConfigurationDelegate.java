package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        Delegable delegable = (Delegable) getSelection();
        String newConfig = DialogEnhancement.showConfigurationDialog(delegable);
        if (newConfig != null) {
            delegable.setDelegationConfiguration(newConfig);
            if (delegable instanceof ExclusiveGateway) {
                getActiveDesignerEditor().getDiagramEditorPage().getDiagramBehavior().refreshContent();
                ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                        .getDiagramEditorPage().refreshConnections();
            }
        }
    }

}
