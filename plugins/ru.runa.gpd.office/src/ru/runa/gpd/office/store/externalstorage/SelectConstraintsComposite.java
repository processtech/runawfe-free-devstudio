package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Consumer;
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

public class SelectConstraintsComposite extends AbstractConstraintsCompositeBuilder {
    private final Consumer<String> resultVariableNameConsumer;
    private Combo combo;

    public SelectConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableContainer variableContainer,
            String variableTypeName, Consumer<String> resultVariableNameConsumer) {
        super(parent, style, constraintsModel, variableContainer, variableTypeName);
        this.resultVariableNameConsumer = resultVariableNameConsumer;
    }

    @Override
    public void build() {
        new Label(this, SWT.NONE).setText(Messages.getString("label.SelectResultVariable"));
        addSelectResultVariableCombo();
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        combo.removeAll();
        combo.setText("");
        constraintsModel.setVariableName("");
        produceResultVariableName(null);
        getVariableNamesByVariableTypeName(variableTypeName).forEach(combo::add);
    }

    private void addSelectResultVariableCombo() {
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
                produceResultVariableName(text);
            }
        });

        if (constraintsModel.getVariableName() != null) {
            combo.setText(constraintsModel.getVariableName());
        }
    }

    private void produceResultVariableName(String variableName) {
        if (resultVariableNameConsumer != null) {
            resultVariableNameConsumer.accept(variableName);
        }
    }

    private Iterable<String> getVariableNamesByVariableTypeName(String variableTypeName) {
        return variableContainer.getVariables(false, false, List.class.getName()).stream()
                .filter(variable -> variable.getFormatComponentClassNames()[0].equals(variableTypeName)).map(Variable::getName)
                .collect(Collectors.toSet());
    }

}
