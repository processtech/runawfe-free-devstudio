package ru.runa.gpd.office.store.externalstorage;

import java.util.function.Predicate;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class DeleteConstraintsComposite extends AbstractConstraintsCompositeBuilder {
    public DeleteConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            String variableTypeName) {
        super(parent, style, constraintsModel, variableProvider, variableTypeName);
        constraintsModel.setVariableName(null);
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        constraintsModel.setVariableName(null);
    }

    @Override
    public void build() {

    }

    @Override
    public void clearConstraints() {
        super.clearConstraints();
        constraintsModel.setVariableName(null);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate(String variableTypeName) {
        return variable -> variable.getUserType().getName().equals(variableTypeName);
    }

}
