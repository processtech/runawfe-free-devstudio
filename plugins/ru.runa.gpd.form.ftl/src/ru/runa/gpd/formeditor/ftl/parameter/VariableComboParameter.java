package ru.runa.gpd.formeditor.ftl.parameter;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import ru.runa.gpd.formeditor.ftl.ComboOption;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.formeditor.ftl.util.VariablesCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;

// use VariableFinderParameter
@Deprecated
public class VariableComboParameter extends ComboParameter {

    @Override
    public List<ComboOption> getOptions(Component component, ComponentParameter parameter, ProcessDefinition processDefinition) {
        Map<String, Variable> variables = VariablesCache.getInstance().getVariables(parameter.getVariableTypeFilter(), processDefinition);
        List<String> variableNames = Lists.newArrayList(variables.keySet());
        Collections.sort(variableNames);
        return Lists.transform(variableNames, new Function<String, ComboOption>() {

            @Override
            public ComboOption apply(String string) {
                return new ComboOption(string, string);
            }
        });
    }
}
