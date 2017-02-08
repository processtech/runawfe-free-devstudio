package ru.runa.gpd.validation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ValidatorConfig {
    public static final String GLOBAL_FIELD_ID = "";
    private String type;
    private String message = "";
    private Map<String, String> params = Maps.newHashMap();
    private final List<String> transitionNames = Lists.newArrayList();

    public ValidatorConfig(String validatorType) {
        this.type = validatorType;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public List<String> getTransitionNames() {
        return transitionNames;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
