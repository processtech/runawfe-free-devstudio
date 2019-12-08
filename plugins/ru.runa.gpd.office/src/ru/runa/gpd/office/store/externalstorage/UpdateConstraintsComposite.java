package ru.runa.gpd.office.store.externalstorage;

import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.base.Strings;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.wfe.var.UserTypeMap;

public class UpdateConstraintsComposite extends AbstractConstraintsCompositeBuilder {
    private Combo combo;

    public UpdateConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableContainer variableContainer,
            String variableTypeName) {
        super(parent, style, constraintsModel, variableContainer, variableTypeName);
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        combo.removeAll();
        combo.setText("");
        constraintsModel.setVariableName("");
        getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);
    }

    @Override
    public void build() {
        new Label(this, SWT.NONE).setText(Messages.getString("label.UpdateVariable"));
        addUpdateVariableCombo();
    }

    private void addUpdateVariableCombo() {
        combo = new Combo(this, SWT.READ_ONLY); // TODO #1506 Реализовать адаптивный чекбокс
        getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);

        combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String text = combo.getText();
                if (Strings.isNullOrEmpty(text)) {
                    return;
                }
                constraintsModel.setVariableName(text);
            }
        });
        if (constraintsModel.getVariableName() != null) {
            combo.setText(constraintsModel.getVariableName());
        }
    }

    private Iterable<String> getVariableNamesByVariableTypeName(String variableTypeName) {
        return variableContainer.getVariables(false, false, UserTypeMap.class.getName()).stream()
                .filter(variable -> variable.getUserType().getName().equals(variableTypeName)).map(Variable::getName).collect(Collectors.toSet());
    }

}
