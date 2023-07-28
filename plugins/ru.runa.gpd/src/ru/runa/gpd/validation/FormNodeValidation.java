package ru.runa.gpd.validation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormNodeValidation {
    private final List<ValidatorConfig> globalConfigs = Lists.newArrayList();
    private final Map<String, Map<String, ValidatorConfig>> fieldConfigs = Maps.newTreeMap();

    public boolean isEmpty() {
        return globalConfigs.isEmpty() && fieldConfigs.isEmpty();
    }

    public List<ValidatorConfig> getGlobalConfigs() {
        return globalConfigs;
    }

    public void addGlobalConfigs(List<ValidatorConfig> configs) {
        globalConfigs.addAll(configs);
    }

    public Map<String, Map<String, ValidatorConfig>> getFieldConfigs() {
        return fieldConfigs;
    }

    public void addFieldConfigs(String variableName, List<ValidatorConfig> configs) {
        Map<String, ValidatorConfig> map = Maps.newHashMap();
        for (ValidatorConfig validatorConfig : configs) {
            map.put(validatorConfig.getType(), validatorConfig);
        }
        addFieldConfigs(variableName, map);
    }

    public void addFieldEmptyConfigs(String variableName) {
        addFieldConfigs(variableName, new HashMap<String, ValidatorConfig>());
    }

    public void addFieldConfigs(String variableName, Map<String, ValidatorConfig> configs) {
        fieldConfigs.put(variableName, configs);
    }

    public void removeFieldConfigs(String variableName) {
        fieldConfigs.remove(variableName);
    }

    public Collection<String> getVariableNames() {
        return fieldConfigs.keySet();
    }

    public Collection<String> getVariableNamesWithEmptyConfigs() {
        return fieldConfigs.entrySet().stream().filter(e -> e.getValue().isEmpty()).map(e -> e.getKey()).collect(Collectors.toList());
    }

    public Collection<String> getRequiredVariableNames() {
        return fieldConfigs.entrySet().stream().filter(e -> e.getValue().containsKey(ValidatorDefinition.REQUIRED_VALIDATOR_NAME))
                .map(e -> e.getKey()).collect(Collectors.toList());
    }

}
