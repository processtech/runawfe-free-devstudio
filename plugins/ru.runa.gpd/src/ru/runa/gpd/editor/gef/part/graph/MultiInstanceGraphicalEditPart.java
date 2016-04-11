package ru.runa.gpd.editor.gef.part.graph;

import ru.runa.gpd.lang.model.MultiSubprocess;

public class MultiInstanceGraphicalEditPart extends SubprocessGraphicalEditPart {
    @Override
    public MultiSubprocess getModel() {
        return (MultiSubprocess) super.getModel();
    }
}
