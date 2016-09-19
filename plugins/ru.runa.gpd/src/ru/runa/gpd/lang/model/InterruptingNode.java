package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

import ru.runa.gpd.Localization;

public abstract class InterruptingNode extends Node {
    private boolean interrupting = true;

    public boolean isInterrupting() {
        return interrupting;
    }

    public void setInterrupting(boolean interrupting) {
        if (this.interrupting != interrupting) {
            boolean old = this.interrupting;
            this.interrupting = interrupting;
            firePropertyChange(PROPERTY_INTERRUPTING, old, interrupting);
        }
    }

    @Override
    protected void populateCustomPropertyDescriptors(List<IPropertyDescriptor> descriptors) {
        super.populateCustomPropertyDescriptors(descriptors);
        if (getParent() instanceof ITimed) {
            descriptors.add(new ComboBoxPropertyDescriptor(PROPERTY_INTERRUPTING, Localization.getString("property.interrupting"),
                    BooleanPropertyComboBoxTransformer.LABELS));
        }
    }

    @Override
    public Object getPropertyValue(Object id) {
        if (PROPERTY_INTERRUPTING.equals(id)) {
            return BooleanPropertyComboBoxTransformer.getPropertyValue(interrupting);
        }
        return super.getPropertyValue(id);
    }

    @Override
    public void setPropertyValue(Object id, Object value) {
        if (PROPERTY_INTERRUPTING.equals(id)) {
            setInterrupting(BooleanPropertyComboBoxTransformer.setPropertyValue(value));
        } else {
            super.setPropertyValue(id, value);
        }
    }

    private static class BooleanPropertyComboBoxTransformer {
        private static String[] LABELS = new String[] { "true", "false" };

        private static Object getPropertyValue(boolean value) {
            if (value) {
                return Integer.valueOf(0);
            } else {
                return Integer.valueOf(1);
            }
        }

        private static boolean setPropertyValue(Object value) {
            if (Integer.valueOf(0).equals(value)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
