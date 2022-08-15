package ru.runa.gpd.editor;

import org.eclipse.ui.IEditorPart;

public interface ConfigurableTitleEditorPart extends IEditorPart {

    Object getPartNameInput();

    void setPartName(String partName);
}
