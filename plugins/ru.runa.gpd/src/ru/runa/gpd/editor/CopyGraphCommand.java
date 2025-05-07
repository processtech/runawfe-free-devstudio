package ru.runa.gpd.editor;

import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.gef.commands.Command;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.NamedGraphElement;

// JPDL (Gef) реализация вставки после копирования. Для BPMN см. ru.runa.gpd.editor.graphiti.PasteFeature.
public class CopyGraphCommand extends Command {
    private final IPasteGraph pasteGraphDelegate;

    public CopyGraphCommand(ProcessEditorBase targetEditor, IFolder targetFolder) {
        pasteGraphDelegate = new PasteGraphDelegate(targetEditor, targetFolder);
    }


    @Override
    public boolean canExecute() {
        return pasteGraphDelegate.bufferIsValid();
    }

    @Override
    public String getLabel() {
        return Localization.getString("button.paste");
    }

    @Override
    public void execute() {
        pasteGraphDelegate.pasteGraph();
    }

    @Override
    public boolean canUndo() {
        return pasteGraphDelegate.canUndo();
    }

    @Override
    public void undo() {
        pasteGraphDelegate.undo();
    }

    @Override
    public boolean canRedo() {
        return pasteGraphDelegate.canRedo();
    }

    public List<NamedGraphElement> getFilteredElements() {
        return pasteGraphDelegate.getFilteredElements();
    }

}