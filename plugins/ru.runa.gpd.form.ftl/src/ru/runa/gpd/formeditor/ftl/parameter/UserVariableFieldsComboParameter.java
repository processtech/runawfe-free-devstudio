package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeConsumer;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeCustomer;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.collect.Lists;

public class UserVariableFieldsComboParameter extends ComboParameter {

    @Override
    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return getOptions(null);
    }

    @Override
    protected List<String> getOptionValues(ComponentParameter parameter) {
        return getOptions(null);
    }

    private List<String> getOptions(VariableUserType type) {
        List<String> result = Lists.newArrayList();
        if (type == null) {
            return result;
        }
        List<Variable> attributes = type.getAttributes();
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
    public Composite createEditor(final Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        final Combo combo = new Combo(parent, SWT.READ_ONLY);
        final AtomicReference<String> selected = new AtomicReference<String>();
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        UserVariablesListComboParameter varListCombo = searchVarListCombo();
        if (varListCombo != null) {
            varListCombo.addParameterChangeListener(new IParameterChangeConsumer() {

                @Override
                public void onParameterChange(IParameterChangeCustomer customer, ComponentParameter parameter) {
                    if (!(customer instanceof UserVariablesListComboParameter)) {
                        return;
                    }
                    if (combo.isDisposed()) {
                        customer.removeParameterChangeListener(this);
                        return;
                    }
                    rebuildComboSelection(((UserVariablesListComboParameter) customer).getSelectedVariableListGenericType(parent, parameter), combo);
                    combo.setText(selected.get());
                }

            });

            for (String variableFieldName : getOptions(varListCombo.getSelectedVariableListGenericType(parent, null))) {
                combo.add(variableFieldName);
            }
        }

        if (oldValue != null) {
            combo.setText((String) oldValue);
            selected.set((String) oldValue);
        } else {
            selected.set("");
        }
        if (listener != null) {
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    selected.set(combo.getText());
                    listener.propertyChange(new PropertyChangeEvent(combo, PropertyNames.PROPERTY_VALUE, oldValue, combo.getText()));
                }
            });
        }
        return combo;
    }

    private void rebuildComboSelection(VariableUserType type, Combo combo) {
        if (combo.getItemCount() > 0) {
            combo.removeAll();
        }
        for (String variableFieldName : getOptions(type)) {
            combo.add(variableFieldName);
        }
    }
}
