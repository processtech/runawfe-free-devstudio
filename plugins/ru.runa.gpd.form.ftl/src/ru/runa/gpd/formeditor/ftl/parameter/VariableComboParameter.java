package ru.runa.gpd.formeditor.ftl.parameter;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.wysiwyg.FormEditor;
import ru.runa.gpd.lang.model.PropertyNames;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class VariableComboParameter extends ComboParameter {

    @Override
    protected List<String> getOptionLabels(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    @Override
    protected List<String> getOptionValues(ComponentParameter parameter) {
        return getOptions(parameter);
    }

    private List<String> getOptions(ComponentParameter parameter) {
        return FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter());
    }

    @Override
    public Composite createEditor(Composite parent, ComponentParameter parameter, final Object oldValue, final PropertyChangeListener listener) {
        final Combo combo = new Combo(parent, SWT.READ_ONLY);
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        for (String variableName : FormEditor.getCurrent().getVariableNames(parameter.getVariableTypeFilter())) {
            combo.add(variableName);
        }
        if (oldValue != null) {
            combo.setText((String) oldValue);
        }
        if (listener != null) {
            combo.addSelectionListener(new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    listener.propertyChange(new PropertyChangeEvent(combo, PropertyNames.PROPERTY_VALUE, oldValue, combo.getText()));
                }
            });
        }
        return combo;
    }

}
