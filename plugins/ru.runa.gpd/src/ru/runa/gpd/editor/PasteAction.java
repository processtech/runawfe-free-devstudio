package ru.runa.gpd.editor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.ui.actions.SelectionAction;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.DrawAfterPasteCommand;

public class PasteAction extends SelectionAction {
    private final EditorBase editor;

    public PasteAction(EditorBase editor) {
        super(editor);
        this.editor = editor;
        setText(Localization.getString("button.paste"));
    }

    @Override
    public boolean calculateEnabled() {
        return createCommand().canExecute();
    }

    private CopyGraphCommand createCommand() {
        return new CopyGraphCommand(editor, (IFolder) editor.getDefinitionFile().getParent());
    }

    @Override
    public void run() {
        CopyGraphCommand copy = createCommand();
        execute(copy);
        execute(new DrawAfterPasteCommand(copy.getFilteredElements(), editor.getDefinition(), editor.getDiagramEditorPage()));

    }
}
