package ru.runa.gpd.ltk;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;

public class ChangeNode extends TextEditChangeNode {
    private final NodeTypeDefinition definition;
    private final String notation;
    private String label;

    public ChangeNode(TextEditBasedChange change, Object element) {
        super(change);
        if (element != null && element instanceof GraphElement) {
            GraphElement graphElement = (GraphElement) element;
            definition = graphElement.getTypeDefinition();
            notation = graphElement.getProcessDefinition().getLanguage().getNotation();
        } else {
            definition = null;
            notation = null;
        }
    }

    public ChangeNode(TextEditBasedChange change, Object element, String label) {
        this(change, element);
        this.label = label;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        if (definition != null) {
            return definition.getImageDescriptor(notation);
        }
        return super.getImageDescriptor();
    }

    public Image getImage() {
        if (definition != null) {
            return definition.getImage(notation);
        }
        return null;
    }

    @Override
    public String getText() {
        if (label != null) {
            return label;
        }
        return super.getText();
    }
}
