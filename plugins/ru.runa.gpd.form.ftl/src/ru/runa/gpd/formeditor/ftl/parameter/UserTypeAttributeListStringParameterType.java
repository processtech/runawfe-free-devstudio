package ru.runa.gpd.formeditor.ftl.parameter;

import java.util.List;

public class UserTypeAttributeListStringParameterType extends UserTypeAttributeListParameterType {

    public UserTypeAttributeListStringParameterType() {
        super(false);
    }

    @Override
    protected Object convertListTargetValue(List<String> list) {
        return convertValueToString(list);
    }

}
