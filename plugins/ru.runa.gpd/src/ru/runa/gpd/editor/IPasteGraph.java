package ru.runa.gpd.editor;

import java.util.List;
import ru.runa.gpd.editor.graphiti.DiagramEditorPage;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

// Используется для делегатов вставки элементов после копирования 
public interface IPasteGraph {

    void pasteGraph();

    boolean bufferIsValid();

    List<NamedGraphElement> getFilteredElements();

    ProcessDefinition getTargetDefinition();

    DiagramEditorPage getDiagramEditorPage();

    boolean canUndo();

    void undo();

    boolean canRedo();

    void redo();

}