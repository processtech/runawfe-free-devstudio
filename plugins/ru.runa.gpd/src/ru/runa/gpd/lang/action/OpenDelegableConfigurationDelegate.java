package ru.runa.gpd.lang.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.ChangeDelegationConfigurationFeature;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.UndoRedoUtil;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;

public class OpenDelegableConfigurationDelegate extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        Delegable delegable = (Delegable) getSelection();
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable);
        if (newConfig != null) {
            UndoRedoUtil.executeFeature(new ChangeDelegationConfigurationFeature(delegable, newConfig));
            if (delegable instanceof ExclusiveGateway) {
                getActiveDesignerEditor().getDiagramEditorPage().getDiagramBehavior().refreshContent();
                ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                        .getDiagramEditorPage().refreshConnections();
            }
        }
    }

}
