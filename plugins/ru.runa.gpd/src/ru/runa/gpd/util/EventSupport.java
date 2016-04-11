package ru.runa.gpd.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class EventSupport {
    private final Object sourceBean;
    private PropertyChangeSupport listeners;

    public EventSupport(Object sourceBean) {
        this.sourceBean = sourceBean;
        initPropertyChangeSupport();
    }

    public EventSupport() {
        this.sourceBean = this;
        initPropertyChangeSupport();
    }

    private void initPropertyChangeSupport() {
        listeners = new PropertyChangeSupport(sourceBean);
    }

    public void firePropertyChange(String propName, Object old, Object newValue) {
        listeners.firePropertyChange(propName, old, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // duplicates
        removePropertyChangeListener(listener);
        listeners.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(listener);
    }

}
