package ru.runa.gpd.lang.model.bpmn;

import ru.runa.gpd.editor.graphiti.UIContainer;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.Font;

public abstract class TextDecorationNode extends GraphElement {

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
