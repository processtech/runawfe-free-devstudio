package ru.runa.gpd.ui.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;

import ru.runa.gpd.Activator;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.SubprocessDefinition;

public class ShowActionsAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        editor.getDefinition().setShowActions(!editor.getDefinition().isShowActions());
        IEditorReference[] editorRefs = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        for (IEditorReference er : editorRefs) {
            IEditorPart ep = er.getEditor(true);
            if (ep instanceof GraphitiProcessEditor) {
                GraphitiProcessEditor gpe = (GraphitiProcessEditor) ep;
                if (gpe.getDefinition() instanceof SubprocessDefinition) {
                    gpe.getDiagramEditorPage().refreshActions();
                    gpe.getDiagramEditorPage().refreshPalette();
                }
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setChecked(editor != null && editor.getDefinition().isShowActions());
        action.setEnabled(editor != null && !(editor.getDefinition() instanceof SubprocessDefinition));
    }

}
