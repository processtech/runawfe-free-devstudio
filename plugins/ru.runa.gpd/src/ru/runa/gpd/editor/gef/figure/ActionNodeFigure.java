package ru.runa.gpd.editor.gef.figure;

public class ActionNodeFigure extends StateFigure {
    @Override
    public void init() {
        super.init();
        addLabel();
        addActionsContainer();
    }
}
