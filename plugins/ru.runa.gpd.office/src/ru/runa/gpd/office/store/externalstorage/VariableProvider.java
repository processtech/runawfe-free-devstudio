package ru.runa.gpd.office.store.externalstorage;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;

public interface VariableProvider extends VariableContainer {
    Stream<? extends VariableUserType> complexUserTypes(Predicate<? super VariableUserType> predicate);

    VariableUserType getUserType(String name);

    default Stream<String> complexUserTypeNames(Predicate<? super VariableUserType> predicate) {
        return complexUserTypes(predicate).map(VariableUserType::getName);
    }

    default Stream<String> complexUserTypeNames() {
        return complexUserTypeNames(null);
    }

    default String getVariableTypeNameByVariableName(String variableName) throws IllegalArgumentException {
        return complexUserTypeNames(userType -> userType.getName().equals(variableName)).findAny()
                .orElseThrow(() -> new IllegalArgumentException("VariableUserType not found for variable " + variableName));
    }

    default Stream<String> variableNamesAccordingToType(String format) {
        return getVariables(true, false).stream().filter(variable -> variable.getFormatClassName().equals(format)).map(Variable::getName);
    }

    default Optional<Variable> variableByName(String name) {
        return getVariables(true, false).stream().filter(variable -> variable.getName().equals(name)).findAny();
    }

    default Optional<Variable> variableByScriptingName(String scriptingName) {
        return getVariables(true, false).stream().filter(variable -> variable.getScriptingName().equals(scriptingName)).findAny();
    }

}
