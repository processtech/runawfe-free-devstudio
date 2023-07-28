package ru.runa.gpd.util;

import com.google.common.base.Objects;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

public class EventSupport {
    private final Object source;
    private final Set<PropertyChangeListener> listeners = new HashSet<>();

    public EventSupport() {
        this.source = this;
    }

    public EventSupport(Object source) {
        this.source = source;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        if (Objects.equal(oldValue, newValue)) {
            return;
        }
        PropertyChangeEvent event = new PropertyChangeEvent(source, propertyName, oldValue, newValue);
        firePropertyChange(event);
    }

    protected void firePropertyChange(PropertyChangeEvent event) {
        for (PropertyChangeListener listener : listeners) {
            listener.propertyChange(event);
        }
    }
}
