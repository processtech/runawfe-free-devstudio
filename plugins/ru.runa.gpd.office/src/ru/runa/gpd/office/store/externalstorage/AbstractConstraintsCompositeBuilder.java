package ru.runa.gpd.office.store.externalstorage;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.office.store.QueryType;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.wfe.var.UserTypeMap;

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
        constraintsModel.setVariableName("");
    }

    @Override
    public void clearConstraints() {
        constraintsModel.setVariableName("");
        if (QueryType.INSERT.equals(constraintsModel.getQueryType())) {
            constraintsModel.setQueryString("");
        }
    }

    protected Stream<String> getVariableNamesByVariableTypeName(String variableTypeName) {
        return variableContainer.getVariables(false, false, getTypeNameFilters()).stream().filter(getFilterPredicate()).map(Variable::getName);
    }

    protected String[] getTypeNameFilters() {
        return new String[] { UserTypeMap.class.getName() };
    }

    protected abstract Predicate<? super Variable> getFilterPredicate();
}