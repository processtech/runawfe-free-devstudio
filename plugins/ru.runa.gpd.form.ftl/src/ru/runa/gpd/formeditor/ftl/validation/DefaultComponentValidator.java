package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.collect.Lists;

public class DefaultComponentValidator implements IComponentValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component) {
        List<ValidationError> list = Lists.newArrayList();
        for (ComponentParameter parameter : component.getType().getParameters()) {
            list.addAll(parameter.getType().getValidator().validate(formNode, component, parameter));
        }
        return list;
    }

}
