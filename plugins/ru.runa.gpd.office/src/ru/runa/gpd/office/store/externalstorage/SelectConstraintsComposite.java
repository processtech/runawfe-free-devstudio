package ru.runa.gpd.office.store.externalstorage;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputModel;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.InternalStorageOperationHandlerCellEditorProvider.VariableUserTypeInfo;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class SelectConstraintsComposite extends AbstractOperatingVariableComboBasedConstraintsCompositeBuilder {
    private final VariableUserTypeInfo variableUserTypeInfo;
    private final InputOutputModel inOutModel;
    private final FilesSupplierMode mode;

    public SelectConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            VariableUserTypeInfo variableUserTypeInfo, InternalStorageDataModel dataModel) {
        super(parent, style, constraintsModel, variableProvider, variableUserTypeInfo.getVariableTypeName());
        this.variableUserTypeInfo = variableUserTypeInfo;
        this.inOutModel = dataModel.getInOutModel();
        this.mode = dataModel.getMode();
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        produceResultVariableName(null);
    }

    @Override
    protected void onWidgetSelected(String text) {
        super.onWidgetSelected(text);
        constraintsModel.setVariableName(null);
        produceResultVariableName(text);
    }

    @Override
    public void build() {
        if (variableUserTypeInfo.isImmutable() || !mode.isOutSupported()) {
            return;
        }
        super.build();
    }

    @Override
    protected String getComboTitle() {
        return Messages.getString("label.SelectResultVariable");
    }

    @Override
    protected Stream<String> variableNamesByVariableTypeName(String variableTypeName) {
        Stream<String> listVariablesStream = variableProvider.getVariables(true, false, List.class.getName()).stream()
                .filter(variable -> variable.getFormatComponentClassNames()[0].equals(variableTypeName))
                .map(Variable::getName);
        Stream<String> singleVariablesStream = variableProvider.getVariables(true, false, variableTypeName).stream().map(Variable::getName);
        return Stream.concat(listVariablesStream, singleVariablesStream);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate(String variableTypeName) {
        throw new UnsupportedOperationException("variableNamesByVariableTypeName is overriden here");
    }

    @Override
    public void clearConstraints() {
        constraintsModel.setVariableName(null);
        if (variableUserTypeInfo.isImmutable()) {
            return;
        }
        super.clearConstraints();
    }

    @Override
    protected void addCombo() {
        super.addCombo();
        if (inOutModel.outputVariable != null) {
            combo.setText(inOutModel.outputVariable);
        }
    }

    private void produceResultVariableName(String variableName) {
        inOutModel.outputVariable = variableName;
    }

}
