package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class UpdateScriptTaskNames extends BaseActionDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(editor != null);
    }

    @Override
    public void run(IAction action) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        if (editor != null) {
            editor.getDefinition().getChildren(ScriptTask.class).stream().forEach(s -> s.resetNameToDefault());
        }
    }
}
