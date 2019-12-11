package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class SelectConstraintsComposite extends AbstractOperatingVariableComboBasedConstraintsCompositeBuilder {
    private final Consumer<String> resultVariableNameConsumer;

    public SelectConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            String variableTypeName, Consumer<String> resultVariableNameConsumer) {
        super(parent, style, constraintsModel, variableProvider, variableTypeName);
        this.resultVariableNameConsumer = resultVariableNameConsumer;
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        produceResultVariableName(null);
    }

    @Override
    protected void onWidgetSelected(String text) {
        super.onWidgetSelected(text);
        produceResultVariableName(text);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate(String variableTypeName) {
        return variable -> variable.getFormatComponentClassNames()[0].equals(variableTypeName);
    }

    @Override
    protected String getComboTitle() {
        return Messages.getString("label.SelectResultVariable");
    }

    @Override
    protected String[] getTypeNameFilters() {
        return new String[] { List.class.getName() };
    }

    private void produceResultVariableName(String variableName) {
        if (resultVariableNameConsumer != null) {
            resultVariableNameConsumer.accept(variableName);
        }
    }

}
