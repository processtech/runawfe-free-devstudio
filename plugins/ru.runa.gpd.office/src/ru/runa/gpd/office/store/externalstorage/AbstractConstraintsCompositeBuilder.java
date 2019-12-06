package ru.runa.gpd.office.store.externalstorage;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.store.StorageConstraintsModel;

abstract class AbstractConstraintsCompositeBuilder extends Composite implements ConstraintsCompositeBuilder {

    protected final StorageConstraintsModel constraintsModel;
    protected final VariableContainer variableContainer;
    protected String variableTypeName;

    public AbstractConstraintsCompositeBuilder(Composite parent, int style, StorageConstraintsModel constraintsModel,
            VariableContainer variableContainer, String variableTypeName) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        this.constraintsModel = constraintsModel;
        this.variableContainer = variableContainer;
        this.variableTypeName = variableTypeName;
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        this.variableTypeName = variableTypeName;
    }

    @Override
    public void clearConstraints() {
        constraintsModel.setVariableName("");
        constraintsModel.setQueryString("");
    }
}