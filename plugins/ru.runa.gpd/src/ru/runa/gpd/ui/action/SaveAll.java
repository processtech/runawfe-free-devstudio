package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import ru.runa.gpd.editor.DirtyDependentActions;
import ru.runa.gpd.editor.ProcessEditorBase;

public class SaveAll extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        IEditorPart[] dirtyEditors = getDirtyEditors();
        for (IEditorPart editorPart : dirtyEditors) {
            if (!(editorPart instanceof ProcessEditorBase)) {
                editorPart.doSave(null);
            }
        }
        for (IEditorPart editorPart : dirtyEditors) {
            if (editorPart instanceof ProcessEditorBase) {
                editorPart.doSave(null);
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        DirtyDependentActions.add(this, action);
        action.setEnabled(getDirtyEditors().length > 0);
    }

    private IEditorPart[] getDirtyEditors() {
        return window.getActivePage().getDirtyEditors();
    }

    @Override
    public void dispose() {
        DirtyDependentActions.remove(this);
        super.dispose();
    }
}
