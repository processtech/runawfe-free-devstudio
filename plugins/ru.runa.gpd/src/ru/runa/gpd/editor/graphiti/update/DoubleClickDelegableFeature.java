package ru.runa.gpd.editor.graphiti.update;

import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.editor.graphiti.ChangeDelegationClassNameFeature;
import ru.runa.gpd.editor.graphiti.ChangeDelegationConfigurationFeature;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.editor.graphiti.UndoRedoUtil;
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
            String newClassName = dialog.openDialog();
            if (newClassName != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        UndoRedoUtil.executeFeature(new ChangeDelegationClassNameFeature(delegable, newClassName));
                    }
                });
            }
        } else {
            String newConfig = DialogEnhancement.showConfigurationDialog(delegable);
            if (newConfig != null) {
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
