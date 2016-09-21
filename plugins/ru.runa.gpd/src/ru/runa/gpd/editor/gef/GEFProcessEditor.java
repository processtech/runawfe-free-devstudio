package ru.runa.gpd.editor.gef;

import java.beans.PropertyChangeEvent;

import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.ui.parts.GraphicalEditor;

import ru.runa.gpd.IPropertyNames;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.GraphElement;

public class GEFProcessEditor extends ProcessEditorBase {
    public static final String ID = "ru.runa.gpd.GEFDesignerEditor";

    @Override
    protected GraphicalEditor createGraphPage() {
        return new DesignerGraphicalEditorPart(this);
    }

    @Override
    protected void selectGraphElement(GraphElement model) {
        ((DesignerGraphicalEditorPart) graphPage).select(model);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        if (IPropertyNames.PROPERTY_SHOW_ACTIONS.equals(evt.getPropertyName())) {
            ((DesignerGraphicalEditorPart) graphPage).getPaletteRoot().refreshElementsVisibility();
        }
    }
    
    @Override
    protected void updateGridLayerVisibility(boolean enabled) {
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, enabled);
        getGraphicalViewer().setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, enabled);
        refresh();
    }
}
