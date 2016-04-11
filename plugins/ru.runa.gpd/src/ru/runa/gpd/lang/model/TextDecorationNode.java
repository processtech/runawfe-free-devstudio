package ru.runa.gpd.lang.model;

import ru.runa.gpd.editor.graphiti.UIContainer;

public abstract class TextDecorationNode extends NamedGraphElement {

    protected Node target;
    private UIContainer uiContainer;

    abstract public Node getTarget();

    public void setTarget(Node target) {
        this.target = target;
    }

    public UIContainer getUiContainer() {
        return uiContainer;
    }

    public void setUiContainer(UIContainer uiContainer) {
        this.uiContainer = uiContainer;
    }

}
