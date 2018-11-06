package ru.runa.gpd.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.gpd.extension.VariableFormatRegistry;

public class ValidatorDefinition {
    public static final String REQUIRED_VALIDATOR_NAME = "required";
    public static final String GLOBAL_VALIDATOR_NAME = "expression";
    public static final String EXPRESSION_PARAM_NAME = "expression";
    public static final String GLOBAL_TYPE = "global";
    public static final String FIELD_TYPE = "field";
    private final String name;
    private final String label;
    private final String type;
    private final List<String> applicable = new ArrayList<String>();
    private final String description;
    private final Map<String, Param> params = new HashMap<String, Param>();

    public ValidatorDefinition(String name, String label, String type, String description) {
        this.name = name;
        this.label = label;
        this.type = type;
        this.description = description;
    }

    public void addApplicableType(String applicableType) {
        this.applicable.add(applicableType);
    }

    public ValidatorConfig create() {
        return new ValidatorConfig(name);
    }

    public boolean isGlobal() {
        return GLOBAL_TYPE.equals(type);
    }

    public boolean isDefault() {
        return REQUIRED_VALIDATOR_NAME.equals(name);
    }

    public boolean isApplicable(String className) {
        if (applicable.isEmpty()) {
            return true;
        }
        for (String appClassName : applicable) {
            if (VariableFormatRegistry.isAssignableFrom(appClassName, className)) {
                return true;
            }
        }
        return false;
    }

    public boolean isApplicableEmpty() {
        return applicable.isEmpty();
    }
    
    public String getDescription() {
        return description;
    }

    public void addParam(Param param) {
        params.put(param.getName(), param);
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Map<String, Param> getParams() {
        return params;
    }

    public static class Param {
        public static final String STRING_TYPE = String.class.getName();
        private final String name;
        private final String label;
        private final String type;
        private final boolean required;

        public Param(String name, String label, String type, boolean required) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = required;
        }

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }
    }
}
