package ru.runa.gpd.ui.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.SubprocessDefinition;

public class ShowActionsAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        editor.getDefinition().setShowActions(!editor.getDefinition().isShowActions());
        List<ProcessEditorBase> openEditors = getOpenedDesignerEditors();
        for (ProcessEditorBase pe : openEditors) {
            if (pe instanceof GraphitiProcessEditor) {
                GraphitiProcessEditor gpe = (GraphitiProcessEditor) pe;
                if (gpe.getDefinition() instanceof SubprocessDefinition) {
                    gpe.getDiagramEditorPage().refreshActions();
                    gpe.getDiagramEditorPage().getDiagramBehavior().refreshPalette();
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
