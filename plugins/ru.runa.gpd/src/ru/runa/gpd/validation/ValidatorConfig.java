package ru.runa.gpd.validation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorConfig {
    public static final String GLOBAL_FIELD_ID = "";
    private final String type;
    private String message = "";
    private final Map<String, String> params = Maps.newHashMap();
    private final List<String> transitionNames = Lists.newArrayList();

    public ValidatorConfig(String validatorType) {
        this.type = validatorType;
    }
    
    public ValidatorConfig(String validatorType, String message, Map<String, String> params) {
        this.type = validatorType;
        this.message = message;
        this.params.putAll(params);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public List<String> getTransitionNames() {
        return transitionNames;
    }

    public String getType() {
        return type;
    }

}