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

import ru.runa.gpd.IPropertyNames;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.ui.VariableListDialog;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;

import com.google.common.base.Joiner;

public class VariableListParameter extends ParameterType {
    private static final String VALUES_DELIM = ",";

    public VariableListParameter() {
        super(true, true);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(ComponentParameter parameter, int propertyId) {
        return new VariableListPropertyDescriptor(propertyId, parameter.getLabel(), FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter()));
    }

    @Override
    public Composite createEditor(Composite parent, final ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            text.setText(Joiner.on(VALUES_DELIM).join((List<String>) oldValue));
            text.setData(oldValue);
        }
        Button selectButton = new Button(composite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        if (listener != null) {
            final List<String> variableNames = FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter());
            selectButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    List<String> value = (List<String>) text.getData();
                    VariableListDialog dialog = new VariableListDialog(variableNames, value);
                    List<String> result = dialog.openDialog();
                    if (result != null) {
                        text.setText(Joiner.on(VALUES_DELIM).join(result));
                        text.setData(result);
                        listener.propertyChange(new PropertyChangeEvent(text, IPropertyNames.PROPERTY_VALUE, oldValue, result));
                    }
                }
            });
        }
        return composite;
    }

}
