package ru.runa.gpd.editor.outline;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.util.EventSupport;

public class ElementTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener, PropertyNames {

    private final EventSupport eventSupport = new EventSupport();

    @Override
    public GraphElement getModel() {
        return (GraphElement) super.getModel();
    }

    @Override
    public void activate() {
        if (!isActive()) {
            getModel().addPropertyChangeListener(this);
            super.activate();
        }
    }

    @Override
    public void deactivate() {
        if (isActive()) {
            getModel().removePropertyChangeListener(this);
            super.deactivate();
        }
    }

    @Override
    protected void refreshVisuals() {
        setWidgetText(getModel().getLabel());
        setWidgetImage(getModel().getEntryImage());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String messageId = evt.getPropertyName();
        if (PROPERTY_CHILDREN_CHANGED.equals(messageId)) {
            refreshChildren();
            eventSupport.firePropertyChange(PROPERTY_EDIT_PART_UPDATED, evt.getOldValue(), evt.getNewValue());
        } else if (PROPERTY_NAME.equals(messageId) || PROPERTY_DESCRIPTION.equals(messageId)) {
            refreshVisuals();
            eventSupport.firePropertyChange(PROPERTY_EDIT_PART_UPDATED, null, null);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class key) {
        if (GraphElement.class.isAssignableFrom(key)) {
            GraphElement element = getModel();
            if (key.isAssignableFrom(element.getClass())) {
                return element;
            }
        }
        return super.getAdapter(key);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        eventSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        eventSupport.removePropertyChangeListener(listener);
    }

}
