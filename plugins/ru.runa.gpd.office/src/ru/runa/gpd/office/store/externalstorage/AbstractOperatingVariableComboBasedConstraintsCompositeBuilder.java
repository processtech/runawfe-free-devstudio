package ru.runa.gpd.office.store.externalstorage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Strings;

import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.store.StorageConstraintsModel;

abstract class AbstractOperatingVariableComboBasedConstraintsCompositeBuilder extends AbstractConstraintsCompositeBuilder {
    protected Combo combo;

    public AbstractOperatingVariableComboBasedConstraintsCompositeBuilder(Composite parent, int style, StorageConstraintsModel constraintsModel,
            VariableContainer variableContainer, String variableTypeName) {
        super(parent, style, constraintsModel, variableContainer, variableTypeName);
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        combo.removeAll();
        combo.setText("");
        getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);
    }

    @Override
    public void build() {
        new Label(getParent(), SWT.NONE).setText(getComboTitle());
        addCombo();
    }

    protected void addCombo() {
        combo = new Combo(getParent(), SWT.READ_ONLY);
        getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                constraintsModel.setVariableName(text);
                onWidgetSelected(text);
            }
        });
        if (constraintsModel.getVariableName() != null) {
            combo.setText(constraintsModel.getVariableName());
        }
    }

    protected void onWidgetSelected(String text) {
    }

    protected abstract String getComboTitle();

}
