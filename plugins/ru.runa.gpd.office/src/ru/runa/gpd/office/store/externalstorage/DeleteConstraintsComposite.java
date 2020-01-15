package ru.runa.gpd.office.store.externalstorage;

import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class DeleteConstraintsComposite extends AbstractConstraintsCompositeBuilder {
    private final Label label = new Label(this, SWT.NONE);

    public DeleteConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableProvider variableProvider,
            String variableTypeName) {
        super(parent, style, constraintsModel, variableProvider, variableTypeName);
        label.setText(Messages.getString("label.DeleteVariableNotFound"));
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        super.onChangeVariableTypeName(variableTypeName);
        build();
    }

    @Override
    public void build() {
        final Optional<String> name = variableNamesByVariableTypeName(variableTypeName).findAny();
        if (name.isPresent()) {
            constraintsModel.setVariableName(name.get());
            label.setVisible(false);
        } else {
            label.setVisible(true);
        }
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate(String variableTypeName) {
        return variable -> variable.getUserType().getName().equals(variableTypeName);
    }

}
