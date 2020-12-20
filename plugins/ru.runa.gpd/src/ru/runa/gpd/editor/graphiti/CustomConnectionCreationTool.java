package ru.runa.gpd.editor.graphiti;

import org.eclipse.graphiti.ui.internal.editor.GFConnectionCreationTool;

public class CustomConnectionCreationTool extends GFConnectionCreationTool {

    @Override
    protected boolean handleCreateConnection() {
        setCurrentCommand(getCommand());
        executeCurrentCommand();
        eraseSourceFeedback();
        return true;
    }
}
