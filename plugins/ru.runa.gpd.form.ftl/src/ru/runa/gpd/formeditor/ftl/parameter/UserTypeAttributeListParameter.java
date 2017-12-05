package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
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
import ru.runa.gpd.formeditor.ftl.ui.UserTypeAttributeListDialog;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class UserTypeAttributeListParameter extends ParameterType {
    private static final String VALUES_DELIM = ",";

    public UserTypeAttributeListParameter() {
        super(true, true);
    }

    @Override
    public PropertyDescriptor createPropertyDescriptor(Component component, ComponentParameter parameter, int propertyId) {
        return new UserTypeAttributeListPropertyDescriptor(propertyId, parameter.getLabel(), component, this);
    }

    @Override
    public Object createEditor(Composite parent, final Component component, ComponentParameter parameter, final Object oldValue,
            final PropertyChangeListener listener) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(2, false));
        final Text text = new Text(composite, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            text.setText(Joiner.on(VALUES_DELIM).join((List<String>) oldValue));
            text.setData(oldValue);
        }
        text.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                listener.propertyChange(new PropertyChangeEvent(text, PropertyNames.PROPERTY_VALUE, oldValue, text.getData()));
            }

        });
        Button selectButton = new Button(composite, SWT.PUSH);
        selectButton.setText("...");
        selectButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        selectButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                List<String> value = (List<String>) text.getData();
                UserTypeAttributeListDialog dialog = new UserTypeAttributeListDialog(getAttributes(component), value);
                List<String> result = dialog.openDialog();
                if (result != null) {
                    text.setData(result);
                    text.setText(Joiner.on(VALUES_DELIM).join(result));
                }
            }
        });
        return text;
    }

    @Override
    public void updateEditor(Object ui, Component component, ComponentParameter parameter) {
        ((Text) ui).setData(Lists.newArrayList());
        ((Text) ui).setText("");
    }

    protected List<String> getAttributes(Component component) {
        VariableUserType userType = getUserType(component);
        return VariableUtils.getUserTypeExpandedAttributeNames(userType);
    }

    private final VariableUserType getUserType(Component component) {
        for (ComponentParameter componentParameter : component.getType().getParameters()) {
            if (componentParameter.getType() instanceof UserTypeVariableListComboParameter) {
                String variableName = (String) component.getParameterValue(componentParameter);
                if (variableName != null) {
                    Variable variable = getVariables(componentParameter).get(variableName);
                    return getListVariableUserType(variable);
                }
            }
        }
        return null;
    }

}
