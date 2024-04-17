package ru.runa.gpd.formeditor.ftl.util;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

public class VariablesCache {
    private static VariablesCache variablesCache = new VariablesCache();
    private int cachedForVariablesCount = -1;
    private final Map<String, Map<String, Variable>> cachedVariables = new HashMap<String, Map<String, Variable>>();

    private VariablesCache() {

    }

    public static VariablesCache getInstance() {
        return variablesCache;
    }

    public synchronized Map<String, Variable> getVariables(String typeClassNameFilter, ProcessDefinition processDefinition) {
        List<Variable> variables = processDefinition.getVariables(true, true);
        if (cachedForVariablesCount != variables.size()) {
            cachedForVariablesCount = variables.size();
            cachedVariables.clear();
        }
        if (!cachedVariables.containsKey(typeClassNameFilter)) {
            // get variables without strong-typing. (all hierarchy)
            if (!Strings.isNullOrEmpty(typeClassNameFilter) && !Object.class.getName().equals(typeClassNameFilter)) {
                variables = processDefinition.getVariables(true, true, typeClassNameFilter);
                cachedVariables.put(typeClassNameFilter, VariableUtils.toMap(variables));
            } else {
                cachedVariables.put(typeClassNameFilter, VariableUtils.toMap(variables));
            }
        }
        return cachedVariables.get(typeClassNameFilter);
    }
}
