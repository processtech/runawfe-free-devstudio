package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.editor.graphiti.UIContainer;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;

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
