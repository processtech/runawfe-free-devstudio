package ru.runa.gpd.validation;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.TimeFormat;

public class ValidationUtil {

    public static List<ValidatorDefinition> getFieldValidatorDefinitions(Variable variable) {
        List<ValidatorDefinition> result = new ArrayList<ValidatorDefinition>();
        for (ValidatorDefinition definition : ValidatorDefinitionRegistry.getValidatorDefinitions().values()) {
            if (!definition.isGlobal() && definition.isApplicable(variable.getJavaClassName())) {
                if (DateFormat.class.getName().equals(variable.getFormat()) && "time".equals(definition.getName())) {
                    continue;
                }
                if (TimeFormat.class.getName().equals(variable.getFormat()) && definition.getName().startsWith("date")) {
                    continue;
                }
                result.add(definition);
            }
        }
        return result;
    }

    public static IFile rewriteValidation(IFile file, FormNode formNode, FormNodeValidation validation) {
        IFile validationFile = IOUtils.getAdjacentFile(file, formNode.getValidationFileName());
        ValidatorParser.writeValidation(validationFile, formNode, validation);
        return validationFile;
    }

}
