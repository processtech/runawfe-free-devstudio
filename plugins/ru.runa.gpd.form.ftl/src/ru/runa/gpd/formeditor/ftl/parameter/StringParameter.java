package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;

public class StringParameter extends ParameterType {

    @Override
    public PropertyDescriptor createPropertyDescriptor(ComponentParameter parameter, int propertyId) {
        return new TextPropertyDescriptor(propertyId, parameter.getLabel());
    }

    @Override
    public Composite createEditor(Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        final Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            text.setText(oldValue.toString());
        }
        if (listener != null) {
            text.addModifyListener(new LoggingModifyTextAdapter() {

                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    listener.propertyChange(new PropertyChangeEvent(text, PropertyNames.PROPERTY_VALUE, oldValue, text.getText()));
                }
            });
        }
        return null;
    }

}
