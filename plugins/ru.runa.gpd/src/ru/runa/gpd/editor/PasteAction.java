package ru.runa.gpd.editor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.ui.actions.SelectionAction;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.CopyGraphAndDrawAfterPasteCommand;

public class PasteAction extends SelectionAction {
    private final ProcessEditorBase editor;

    public PasteAction(ProcessEditorBase editor) {
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
        execute(new CopyGraphAndDrawAfterPasteCommand(editor, (IFolder) editor.getDefinitionFile().getParent()));
    }
}
