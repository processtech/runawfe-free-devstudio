package ru.runa.gpd.editor.gef;

import java.util.List;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;
import ru.runa.gpd.editor.CopyAction;
import ru.runa.gpd.editor.PasteAction;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.ProcessEditorContributor;
import ru.runa.gpd.editor.SelectAllAction;

public class GEFActionBarContributor extends ProcessEditorContributor {

    public static void createCustomGEFActions(ActionRegistry registry, ProcessEditorBase editor, List<String> selectionActionIds) {
        IAction copyAction = new CopyAction(editor);
        copyAction.setId(ActionFactory.COPY.getId());
        registry.registerAction(copyAction);
        selectionActionIds.add(copyAction.getId());

        IAction pasteAction = new PasteAction(editor);
        pasteAction.setId(ActionFactory.PASTE.getId());
        registry.registerAction(pasteAction);
        selectionActionIds.add(pasteAction.getId());
        // IAction leftAlignmentAction = new AlignmentAction((IWorkbenchPart)
        // editor, PositionConstants.LEFT);
        // registry.registerAction(leftAlignmentAction);
        // selectionActionIds.add(leftAlignmentAction.getId());
        // IAction topAlignmentAction = new AlignmentAction((IWorkbenchPart)
        // editor, PositionConstants.TOP);
        // registry.registerAction(topAlignmentAction);
        // selectionActionIds.add(topAlignmentAction.getId());
        SelectAllAction selectAllAction = new SelectAllAction(editor);
        selectAllAction.setId(ActionFactory.SELECT_ALL.getId());
        registry.registerAction(selectAllAction);
        selectionActionIds.add(selectAllAction.getId());
    }

}
