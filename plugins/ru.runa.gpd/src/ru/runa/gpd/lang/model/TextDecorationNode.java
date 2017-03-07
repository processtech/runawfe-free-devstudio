package ru.runa.gpd.lang.model;

import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Font;

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

    protected Font getFont(Text text) {
        Font font = text.getFont();
        if (font == null) {
            return text.getStyle().getFont();
        }
        return font;
    }

}
