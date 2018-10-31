package ru.runa.gpd.formeditor.ftl.validation;

import java.text.MessageFormat;
import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentParameter;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class DefaultComponentValidator implements IComponentValidator {

    @Override
    public List<ValidationError> validate(FormNode formNode, Component component, byte[] formData) {
        List<ValidationError> list = Lists.newArrayList();
        
        String componentParameters = new String(formData);
        if (!Objects.equal(component.toString(), componentParameters.substring(componentParameters.indexOf('$'), componentParameters.lastIndexOf('<')))) {
            list.add(ValidationError.createError(formNode, MessageFormat.format(Localization.getString("ExcessiveParameters_2"), component)));
        }
        for (ComponentParameter parameter : component.getType().getParameters()) {
            list.addAll(parameter.getType().getValidator().validate(formNode, component, parameter));
        }
        return list;
    }

}
