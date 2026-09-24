package ru.runa.gpd.office.store.externalstorage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import ru.runa.gpd.lang.model.DataFieldVariable;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;

public class SubQueryVariableProvider implements VariableProvider {

    private final VariableProvider delegate;
    private final VariableUserType userType;

    public SubQueryVariableProvider(VariableProvider delegate, VariableUserType userType) {
        this.delegate = delegate;
        this.userType = userType;
    }

    @Override
    public List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters) {
        List<Variable> variables = new ArrayList<>(
                delegate.getVariables(expandComplexTypes, includeSwimlanes, typeClassNameFilters));

        for (Variable attribute : userType.getAttributes()) {
            variables.add(new DataFieldVariable(userType.getName(), attribute));
        }

        return variables;
    }

    @Override
    public VariableUserType getUserType(String name) {
        if (userType.getName().equals(name)) {
            return userType;
        }
        return delegate.getUserType(name);
    }

    @Override
    public Stream<? extends VariableUserType> complexUserTypes(Predicate<? super VariableUserType> predicate) {
        return delegate.complexUserTypes(predicate);
    }
}
