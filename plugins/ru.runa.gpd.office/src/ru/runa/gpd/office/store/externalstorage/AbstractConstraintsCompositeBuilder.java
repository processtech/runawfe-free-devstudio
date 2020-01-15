package ru.runa.gpd.office.store.externalstorage;

import com.google.common.base.Strings;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.store.QueryType;
import ru.runa.gpd.office.store.StorageConstraintsModel;
import ru.runa.wfe.var.UserTypeMap;

abstract class AbstractConstraintsCompositeBuilder extends Composite implements ConstraintsCompositeBuilder {

    protected final StorageConstraintsModel constraintsModel;
    protected final VariableProvider variableProvider;
    protected String variableTypeName;

    public AbstractConstraintsCompositeBuilder(Composite parent, int style, StorageConstraintsModel constraintsModel,
            VariableProvider variableProvider, String variableTypeName) {
        super(parent, style);
        setLayout(new GridLayout(2, false));
        this.constraintsModel = constraintsModel;
        this.variableTypeName = variableTypeName;
        this.variableProvider = variableProvider;
    }

    @Override
    public void onChangeVariableTypeName(String variableTypeName) {
        this.variableTypeName = variableTypeName;
        constraintsModel.setVariableName("");
    }

    @Override
    public void clearConstraints() {
        if (!Strings.isNullOrEmpty(constraintsModel.getVariableName())
                && variableNamesByVariableTypeName(variableTypeName).noneMatch(s -> s.equals(constraintsModel.getVariableName()))) {
            constraintsModel.setVariableName("");
        }
        if (QueryType.INSERT.equals(constraintsModel.getQueryType())) {
            constraintsModel.setQueryString("");
        }
    }

    protected Stream<String> variableNamesByVariableTypeName(String variableTypeName) {
        return variableProvider.getVariables(false, false, getTypeNameFilters()).stream().filter(getFilterPredicate(variableTypeName))
                .map(Variable::getName);
    }

    protected String[] getTypeNameFilters() {
        return new String[] { UserTypeMap.class.getName() };
    }

    protected abstract Predicate<? super Variable> getFilterPredicate(String variableTypeName);
}