package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

public class UserTypeAttributeListMultipleParameterType extends UserTypeAttributeListParameterType{

    public UserTypeAttributeListMultipleParameterType() {
        super(true);
    }

    @Override
    protected Object convertListTargetValue(List<String> list) {
        return list;
    }
    
}
