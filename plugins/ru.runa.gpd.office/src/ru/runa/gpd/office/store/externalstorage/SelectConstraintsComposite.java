package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.InternalStorageOperationHandlerCellEditorProvider.VariableUserTypeInfo;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class SelectConstraintsComposite extends AbstractOperatingVariableComboBasedConstraintsCompositeBuilder {
    private final Consumer<String> resultVariableNameConsumer;
    private final VariableUserTypeInfo variableUserTypeInfo;

    public SelectConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            VariableUserTypeInfo variableUserTypeInfo, Consumer<String> resultVariableNameConsumer) {
        super(parent, style, constraintsModel, variableProvider, variableUserTypeInfo.getVariableTypeName());
        this.variableUserTypeInfo = variableUserTypeInfo;
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
    public void build() {
        if (variableUserTypeInfo.isImmutable()) {
            return;
        }
        super.build();
    }

    @Override
    protected String getComboTitle() {
        return Messages.getString("label.SelectResultVariable");
    }

    @Override
    protected String[] getTypeNameFilters() {
        return new String[] { List.class.getName() };
    }

    @Override
    public void clearConstraints() {
        if (variableUserTypeInfo.isImmutable()) {
            constraintsModel.setVariableName(variableUserTypeInfo.getVariableTypeName());
            return;
        }
        super.clearConstraints();
    }

    private void produceResultVariableName(String variableName) {
        if (resultVariableNameConsumer != null) {
            resultVariableNameConsumer.accept(variableName);
        }
    }

}
