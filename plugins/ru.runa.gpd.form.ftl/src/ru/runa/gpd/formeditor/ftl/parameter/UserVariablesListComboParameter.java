package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeConsumer;
import ru.runa.gpd.formeditor.ftl.parameter.interfaces.IParameterChangeCustomer;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class UserVariablesListComboParameter extends ComboParameter implements IParameterChangeCustomer {

    private final List<IParameterChangeConsumer> consumers = Lists.newArrayList();
    private final Map<String, VariableUserType> variables = Maps.newHashMap();
    private final Map<Composite, List<ComboPair>> combos = Maps.newHashMap();
    private final AtomicReference<ProcessDefinition> currentProcess = new AtomicReference<ProcessDefinition>();

    @Override
    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    @Override
    protected List<String> getOptionValues(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    private List<String> getOptions(ComponentParameter parameter) {
        ProcessDefinition current = FormEditor.getCurrent().getProcessDefinition();
        ProcessDefinition reference = currentProcess.getAndSet(current);
        if (current != null && !current.equals(reference)) {
            variables.clear();
        }
        Map<String, Variable> vars = FormEditor.getCurrent().getVariables(parameter.getVariableTypeFilter());
        List<VariableUserType> userTypes = FormEditor.getCurrent().getUserVariablesTypes();
        for (Map.Entry<String, Variable> var : vars.entrySet()) {
            String typeName = VariableUtils.getListVariableComponentFormat(var.getValue());
            if (typeName == null) {
                continue;
            }
            for (VariableUserType type : userTypes) {
                if (!type.getName().equals(typeName)) {
                    continue;
                }
                variables.put(var.getKey(), type);
                break;
            }
        }
        return Lists.newArrayList(variables.keySet());
    }

    private final Combo getByParent(Composite parent, ComponentParameter parameter) {
        Combo result = null;
        try {
            List<ComboPair> pairs = combos.get(parent);
            if (parameter != null) {
                for (ComboPair pair : pairs) {
                    if (!parameter.equals(pair.parameter)) {
                        continue;
                    }
                    result = pair.combo;
                    break;
                }

            } else {
                result = pairs.get(0).combo;
            }
        } catch (Exception e) {

        }
        return result;
    }

    public final VariableUserType getSelectedVariableListGenericType(Composite parent, ComponentParameter parameter) {
        Combo combo = getByParent(parent, parameter);
        if (combo == null && parameter != null) {
            for (List<ComboPair> pairs : combos.values()) {
                for (ComboPair pair : pairs) {
                    if (!parameter.equals(pair.parameter)) {
                        continue;
                    }
                    combo = pair.combo;
                    break;
                }
                if (combo != null) {
                    break;
                }
            }
        }
        if (combo == null || combo.isDisposed() || combo.getText() == null || combo.getText().isEmpty()) {
            return null;
        }
        return variables.get(combo.getText());
    }

    @Override
    public Composite createEditor(Composite parent, final ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        if (!combos.containsKey(parent)) {
            combos.put(parent, new ArrayList<ComboPair>());
        }
        final Combo combo = new Combo(parent, SWT.READ_ONLY);
        for (String variableName : getOptions(parameter)) {
            combo.add(variableName);
        }

        List<ComboPair> pairs = combos.get(parent);
        pairs.add(new ComboPair(parameter, combo));

        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (oldValue != null) {
            combo.setText((String) oldValue);
        }
        combo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                if (listener != null) {
                    listener.propertyChange(new PropertyChangeEvent(combo, PropertyNames.PROPERTY_VALUE, oldValue, combo.getText()));
                }
                for (IParameterChangeConsumer consumer : new ArrayList<IParameterChangeConsumer>(consumers)) {
                    consumer.onParameterChange(UserVariablesListComboParameter.this, parameter);
                }
            }
        });
        return combo;
    }

    @Override
    public void addParameterChangeListener(IParameterChangeConsumer consumer) {
        consumers.add(consumer);
    }

    @Override
    public void removeParameterChangeListener(IParameterChangeConsumer consumer) {
        consumers.remove(consumer);
    }

    private static class ComboPair {

        private final ComponentParameter parameter;
        private final Combo combo;

        private ComboPair(ComponentParameter parameter, Combo combo) {
            this.parameter = parameter;
            this.combo = combo;
        }
    }

}
