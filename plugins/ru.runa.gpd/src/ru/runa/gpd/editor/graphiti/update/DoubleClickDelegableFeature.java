package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.change.ChangeDelegationClassNameFeature;
import ru.runa.gpd.editor.graphiti.change.ChangeDelegationConfigurationFeature;
import ru.runa.gpd.editor.graphiti.change.UndoRedoUtil;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.ui.dialog.ChooseHandlerClassDialog;

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
            String newClassName = dialog.openDialog();
            if (newClassName != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UndoRedoUtil.executeFeature(new ChangeDelegationClassNameFeature(delegable, newClassName));
                    }
                });
                if (delegable instanceof ScriptTask) {
                    ((ScriptTask) delegable).resetNameToDefault();
                }
            }
        } else {
            DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
            String newConfig = provider.showConfigurationDialog(delegable);
            if (newConfig != null) {
                delegable.setDelegationConfiguration(newConfig);
                if (delegable instanceof ExclusiveGateway) {
                    getDiagramBehavior().refreshContent();
                    ((GraphitiProcessEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor())
                            .getDiagramEditorPage().refreshConnections();
                }
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UndoRedoUtil.executeFeature(new ChangeDelegationConfigurationFeature(delegable, newConfig));
                    }
                });
            }
        }
    }

}
