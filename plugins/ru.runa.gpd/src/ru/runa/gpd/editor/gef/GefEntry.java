package ru.runa.gpd.editor.gef;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPart;

import ru.runa.gpd.editor.Entry;
import ru.runa.gpd.editor.gef.figure.GridSupportLayer;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class GefEntry extends Entry {

    public GefEntry(NodeTypeDefinition nodeTypeDefinition, IConfigurationElement element) {
        super(nodeTypeDefinition, element);
    }

    protected Language getLanguage() {
        return Language.JPDL;
    }

    private EditPart createEditPart(String propertyName, GraphElement element) {
        EditPart editPart = createExecutableExtension(propertyName);
        if (editPart != null) {
            editPart.setModel(element);
        }
        return editPart;
    }

    public EditPart createGraphicalEditPart(GraphElement element) {
        return createEditPart("graphicalEditPart", element);
    }

    public EditPart createTreeEditPart(GraphElement element) {
        return createEditPart("treeEditPart", element);
    }

    public <T extends IFigure> T createFigure(ProcessDefinition definition) {
        T figure = createExecutableExtension("figure");
        if (figure instanceof NodeFigure) {
            ((NodeFigure) figure).init();
        }
        if (figure instanceof TransitionFigure) {
            ((TransitionFigure) figure).init();
        }
        if (figure instanceof GridSupportLayer) {
            ((GridSupportLayer) figure).setDefinition(definition);
        }
        return figure;
    }
}
