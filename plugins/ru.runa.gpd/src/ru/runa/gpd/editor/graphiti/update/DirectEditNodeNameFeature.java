package ru.runa.gpd.editor.graphiti.update;

import java.util.Objects;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDirectEditingContext;
import org.eclipse.graphiti.features.impl.AbstractDirectEditingFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.editor.graphiti.IRedoProtected;
import ru.runa.gpd.lang.model.Node;

public class DirectEditNodeNameFeature extends AbstractDirectEditingFeature implements CustomUndoRedoFeature, IRedoProtected {
    private boolean multiline = false;
    private String undoName;
    private String redoName;

    public DirectEditNodeNameFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public int getEditingType() {
        if (multiline) {
            return TYPE_MULTILINETEXT;
        } else {
            return TYPE_TEXT;
        }
    }

    @Override
    public boolean canDirectEdit(IDirectEditingContext context) {
        GraphicsAlgorithm ga = context.getGraphicsAlgorithm();
        if (ga instanceof MultiText) {
            multiline = true;
            return true;
        } else if (ga instanceof Text) {
            multiline = false;
            return true;
        }
        // direct editing not supported in all other cases
        return false;
    }

    @Override
    public String getInitialValue(IDirectEditingContext context) {
        // return the current name of the EClass
        PictogramElement pe = context.getPictogramElement();
        Node flowElement = (Node) getBusinessObjectForPictogramElement(pe);
        return flowElement.getName();
    }

    @Override
    public String checkValueValid(String value, IDirectEditingContext context) {
        if (multiline == false && value.contains("\n")) {
            return "Line breakes are not allowed."; //$NON-NLS-1$
        }
        // null means, that the value is valid
        return null;
    }

    @Override
    public void setValue(String value, IDirectEditingContext context) {
        // set the new name
        PictogramElement pe = context.getPictogramElement();
        Node node = (Node) getBusinessObjectForPictogramElement(pe);
        if (!Objects.equals(node.getName(), value)) {
            undoName = node.getName();
            node.setName(value);
            setValueChanged();
        }
    }

    @Override
    public boolean canUndo(IContext context) {
        return undoName != null;
    }

    @Override
    public void postUndo(IContext context) {
        if (context instanceof IDirectEditingContext) {
            PictogramElement pe = ((IDirectEditingContext) context).getPictogramElement();
            Node node = (Node) getBusinessObjectForPictogramElement(pe);
            redoName = node.getName();
            node.setName(undoName);
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return redoName != null;
    }

    @Override
    public void postRedo(IContext context) {
        if (context instanceof IDirectEditingContext) {
            PictogramElement pe = ((IDirectEditingContext) context).getPictogramElement();
            Node node = (Node) getBusinessObjectForPictogramElement(pe);
            node.setName(redoName);
        }

    }

    @Override
    public String getName() {
        return Localization.getString("RenameAction.title");
    }

}
