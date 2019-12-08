package ru.runa.gpd.office.store.externalstorage;

import java.util.function.Predicate;

import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.store.StorageConstraintsModel;

public class UpdateConstraintsComposite extends AbstractOperatingVariableComboBasedConstraintsCompositeBuilder {
    public UpdateConstraintsComposite(Composite parent, int style, StorageConstraintsModel constraintsModel, VariableContainer variableContainer,
            String variableTypeName) {
        super(parent, style, constraintsModel, variableContainer, variableTypeName);
    }

    @Override
    protected Predicate<? super Variable> getFilterPredicate() {
        return variable -> variable.getUserType().getName().equals(variableTypeName);
    }

    @Override
    protected String getComboTitle() {
        return Messages.getString("label.UpdateVariable");
    }

}
