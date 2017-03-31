package ru.runa.gpd.editor.gef.part.tree;

import java.beans.PropertyChangeEvent;

import ru.runa.gpd.lang.model.jpdl.Action;

public class ActionTreeEditPart extends ElementTreeEditPart {

    @Override
    protected void refreshVisuals() {
        super.refreshVisuals();
        setWidgetText(getLabel());
    }

    protected String getLabel() {
        return ((Action) getModel()).getLabel();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
        String messageId = evt.getPropertyName();
        if (PROPERTY_CLASS.equals(messageId)) {
            refreshVisuals();
        }
    }

}
