package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;

public class DoubleClickDelegableFeature extends DoubleClickElementFeature {

    @Override
    public boolean canExecute(ICustomContext context) {
        return getBusinessObject(context) instanceof Delegable && super.canExecute(context);
    }

    @Override
    public void execute(ICustomContext context) {
        Delegable delegable = (Delegable) getBusinessObject(context);
        if (delegable.getDelegationClassName() == null) {
            ChooseHandlerClassDialog dialog = new ChooseHandlerClassDialog(delegable.getDelegationType(), delegable.getDelegationClassName());
            String className = dialog.openDialog();
            if (className != null) {
                delegable.setDelegationConfiguration(null);
                delegable.setDelegationClassName(className);
            }
        } else {
            String newConfig = DialogEnhancement.showConfigurationDialog(delegable);
            if (newConfig != null) {
                delegable.setDelegationConfiguration(newConfig);
                if (delegable instanceof ExclusiveGateway) {
                    getDiagramBehavior().refreshContent();
                    ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                            .getDiagramEditorPage().refreshConnections();
                }
            }
        }
    }

}
