package ru.runa.gpd.formeditor.ftl.conv;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.lang.model.Variable;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class VariableTypeSupportHashModel extends SimpleHash {
    private static final long serialVersionUID = 1L;

    protected TemplateModel getTemplateModel(Variable variable) throws TemplateModelException {
        Map<String, String> properties = new HashMap<String, String>();
        String javaClassName = variable.getJavaClassName();
        if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Executor", javaClassName)) {
            addPropertyDescriptor(properties, variable, "id");
            addPropertyDescriptor(properties, variable, "name");
            addPropertyDescriptor(properties, variable, "fullName");
            addPropertyDescriptor(properties, variable, "description");
            addPropertyDescriptor(properties, variable, "version");
        }
        if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Actor", javaClassName)) {
            addPropertyDescriptor(properties, variable, "active");
            addPropertyDescriptor(properties, variable, "code");
            addPropertyDescriptor(properties, variable, "email");
            addPropertyDescriptor(properties, variable, "phone");
            addPropertyDescriptor(properties, variable, "title");
            addPropertyDescriptor(properties, variable, "department");
            // transient fields
            addPropertyDescriptor(properties, variable, "firstName");
            addPropertyDescriptor(properties, variable, "middleName");
            addPropertyDescriptor(properties, variable, "lastName");
        }
        if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.user.Group", javaClassName)) {
            addPropertyDescriptor(properties, variable, "ldapGroupName");
        }
        if (VariableFormatRegistry.isAssignableFrom("ru.runa.wfe.var.FileVariable", javaClassName)) {
            addPropertyDescriptor(properties, variable, "name");
            addPropertyDescriptor(properties, variable, "contentType");
            addPropertyDescriptor(properties, variable, "dataLength");
        }
        if (VariableFormatRegistry.isAssignableFrom(Date.class.getName(), javaClassName)) {
            addPropertyDescriptor(properties, variable, "date");
            addPropertyDescriptor(properties, variable, "day");
            addPropertyDescriptor(properties, variable, "hours");
            addPropertyDescriptor(properties, variable, "minutes");
            addPropertyDescriptor(properties, variable, "month");
            addPropertyDescriptor(properties, variable, "seconds");
            addPropertyDescriptor(properties, variable, "time");
            addPropertyDescriptor(properties, variable, "timezoneOffset");
            addPropertyDescriptor(properties, variable, "year");
        }
        if (properties.size() == 0) {
            return new SimpleScalar("${" + variable.getName() + "}");
        }
        return new SimpleHash(properties);
    }

    private void addPropertyDescriptor(Map<String, String> properties, Variable variable, String name) {
        properties.put(name, "${" + variable.getName() + "." + name + "}");
    }

}
