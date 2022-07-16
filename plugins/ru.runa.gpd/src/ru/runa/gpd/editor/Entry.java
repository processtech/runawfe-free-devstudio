package ru.runa.gpd.editor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.preference.IPreferenceStore;
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.gef.figure.GridSupportLayer;
import ru.runa.gpd.editor.gef.figure.NodeFigure;
import ru.runa.gpd.editor.gef.figure.TransitionFigure;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventCapable;
import ru.runa.gpd.settings.LanguageElementPreferenceNode;
import ru.runa.gpd.settings.PrefConstants;

public abstract class Entry implements GEFConstants {

    protected final NodeTypeDefinition nodeTypeDefinition;
    protected final IConfigurationElement element;
    protected final Dimension defaultSystemSize;
    protected final Dimension defaultSize;
    protected final Dimension defaultBoundaryEventSize;
    protected final boolean fixedSize;

    public Entry(NodeTypeDefinition nodeTypeDefinition, IConfigurationElement element) {
        this.nodeTypeDefinition = nodeTypeDefinition;
        this.element = element;
        String attribute;
        this.defaultSystemSize = new Dimension(Integer.parseInt((attribute = element.getAttribute("width")) != null ? attribute : "10"),
                Integer.parseInt((attribute = element.getAttribute("height")) != null ? attribute : "6"));
        Dimension defaultBoundaryEventSystemSize = new Dimension(
                Integer.parseInt((attribute = element.getAttribute("boundaryEventWidth")) != null ? attribute : "2"),
                Integer.parseInt((attribute = element.getAttribute("boundaryEventHeight")) != null ? attribute : "2"));
        this.defaultSize = this.defaultSystemSize.getScaled(GRID_SIZE);
        this.fixedSize = Boolean.parseBoolean(element.getAttribute("fixedSize"));
        this.defaultBoundaryEventSize = defaultBoundaryEventSystemSize.getScaled(GRID_SIZE);
    }

    protected abstract Language getLanguage();

    protected <T> T createExecutableExtension(String propertyName) {
        try {
            if (element == null || element.getAttribute(propertyName) == null) {
                return null;
            }
            return (T) element.createExecutableExtension(propertyName);
        } catch (CoreException e) {
            PluginLogger.logError("Unable to create element '" + this + "'(unable to load property='" + propertyName + "')", e);
            return null;
        }
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

    public Dimension getDefaultSystemSize() {
        return defaultSystemSize;
    }

    public Dimension getDefaultSize(GraphElement graphElement) {
        if (graphElement instanceof IBoundaryEventCapable && ((IBoundaryEventCapable) graphElement).isBoundaryEvent()) {
            return defaultBoundaryEventSize.getCopy();
        }
        if (!fixedSize) {
            IPreferenceStore store = Activator.getDefault().getPreferenceStore();
            String key = LanguageElementPreferenceNode.getId(nodeTypeDefinition, getLanguage()) + '.' + PrefConstants.P_LANGUAGE_NODE_WIDTH;
            if (store.contains(key)) {
                defaultSize.setWidth(GRID_SIZE * store.getInt(key));
            }
            key = LanguageElementPreferenceNode.getId(nodeTypeDefinition, getLanguage()) + '.' + PrefConstants.P_LANGUAGE_NODE_HEIGHT;
            if (store.contains(key)) {
                defaultSize.setHeight(GRID_SIZE * store.getInt(key));
            }
        }
        return defaultSize.getCopy();
    }

    public boolean isFixedSize() {
        return fixedSize;
    }
}
