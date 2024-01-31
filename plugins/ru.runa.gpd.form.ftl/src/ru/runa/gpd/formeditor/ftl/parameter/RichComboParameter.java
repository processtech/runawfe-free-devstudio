package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ui.RichComboDialog;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class RichComboParameter extends ParameterType {
    public static final String VALUE_PREFIX = "value@";

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId,
            ProcessDefinition processDefinition) {
        return new RichComboPropertyDescriptor(propertyId, parameter, getVariableNames(parameter, processDefinition));
    }

    @Override
    public Object createEditor(Composite parent, Component component, final ComponentParameter parameter, final Object oldValue,
            final PropertyChangeListener listener, ProcessDefinition processDefinition) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            String textValue = (String) oldValue;
            if (textValue.startsWith(RichComboParameter.VALUE_PREFIX)) {
                textValue = textValue.substring(RichComboParameter.VALUE_PREFIX.length());
            }
            text.setText(textValue);
        }
        Button selectButton = new Button(composite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        if (listener != null) {
            final List<String> variableNames = this.getVariableNames(parameter, processDefinition);
            selectButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    RichComboDialog dialog = new RichComboDialog(parameter.getVariableTypeFilter(), variableNames, text.getText());
                    String result = dialog.openDialog();
                    if (result != null) {
                        String textValue = result;
                        if (textValue.startsWith(RichComboParameter.VALUE_PREFIX)) {
                            textValue = textValue.substring(RichComboParameter.VALUE_PREFIX.length());
                        }
                        text.setText(textValue);
                        listener.propertyChange(new PropertyChangeEvent(text, PropertyNames.PROPERTY_VALUE, oldValue, result));
                    }
                }
            });
        }
        return composite;
    }
}
