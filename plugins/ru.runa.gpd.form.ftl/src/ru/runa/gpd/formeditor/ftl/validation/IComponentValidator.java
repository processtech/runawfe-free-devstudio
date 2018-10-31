package ru.runa.gpd.formeditor.ftl.validation;

import java.util.List;

import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;

public interface IComponentValidator {

    List<ValidationError> validate(FormNode formNode, Component component, byte[] formData);

}
