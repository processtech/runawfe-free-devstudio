package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeConsumer;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeCustomer;
import ru.runa.gpd.formeditor.ftl.ui.UserVariableFieldListDialog;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class UserVariableFieldListParameter extends ParameterType {
    private static final String VALUES_DELIM = ",";

    public UserVariableFieldListParameter() {
        super(true, true);
    }

    private List<String> getAttributes(final Composite parent, final UserVariablesListComboParameter varListCombo) {
        final List<String> result = Lists.newArrayList();
        if (null == varListCombo) {
            return result;
        }
        final VariableUserType type = varListCombo.getSelectedVariableListGenericType(parent, null);
        if (null == type) {
            return result;
        }
        final List<Variable> attributes = type.getAttributes();
        for (Variable field : attributes) {
            result.add(field.getName());
        }
        return result;
    }

    private final UserVariablesListComboParameter searchVarListCombo() {
        UserVariablesListComboParameter result = null;
        Component component = FormEditor.getCurrent().getSelectedComponent();
        if (component == null) {
            return result;
        }
        List<ComponentParameter> parameters = component.getType().getParameters();
        for (ComponentParameter tested : parameters) {
            if (!(tested.getType() instanceof UserVariablesListComboParameter)) {
                continue;
            }
            result = (UserVariablesListComboParameter) tested.getType();
            break;
        }
        return result;
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(ComponentParameter parameter, int propertyId) {
        return new UserVariableFieldListPropertyDescriptor(propertyId, parameter.getLabel(), new ArrayList<String>());
    }

    @Override
    public Composite createEditor(Composite parent, final ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        final UserVariablesListComboParameter varListCombo = searchVarListCombo();
        if (varListCombo != null) {
            varListCombo.addParameterChangeListener(new IParameterChangeConsumer() {

                @Override
                public void onParameterChange(IParameterChangeCustomer customer, ComponentParameter parameter) {
                    if (!(customer instanceof UserVariablesListComboParameter)) {
                        return;
                    }
                    if (text.isDisposed()) {
                        customer.removeParameterChangeListener(this);
                        return;
                    }
                    text.setText("");
                }
            });
        }

        if (oldValue != null) {
            text.setText(Joiner.on(VALUES_DELIM).join((List<String>) oldValue));
            text.setData(oldValue);
        }
        Button selectButton = new Button(composite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        if (listener != null) {
            final List<String> attributeNames = getAttributes(parent, varListCombo);
            selectButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    List<String> value = (List<String>) text.getData();
                    UserVariableFieldListDialog dialog = new UserVariableFieldListDialog(attributeNames, value);
                    List<String> result = dialog.openDialog();
                    if (result != null) {
                        text.setText(Joiner.on(VALUES_DELIM).join(result));
                        text.setData(result);
                        listener.propertyChange(new PropertyChangeEvent(text, PropertyNames.PROPERTY_VALUE, oldValue, result));
                    }
                }
            });
        }
        return composite;
    }
}
