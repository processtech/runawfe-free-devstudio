package ru.runa.gpd.office.store.externalstorage;

import java.text.MessageFormat;
import java.util.List;

import org.dom4j.Element;

import com.google.common.base.Strings;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.FilesSupplierMode;
import ru.runa.gpd.office.InputOutputModel;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.util.VariableUtils;

public class ExternalStorageHandlerInputOutputModel extends InputOutputModel {

    public static ExternalStorageHandlerInputOutputModel deserialize(Element input, Element output) {
        ExternalStorageHandlerInputOutputModel model = new ExternalStorageHandlerInputOutputModel();
        if (input != null) {
            model.inputPath = input.attributeValue("path");
            model.inputVariable = input.attributeValue("variable");
        }
        if (output != null) {
            model.outputVariable = output.attributeValue("variable");
        }
        return model;
    }

    @Override
    public void validate(GraphElement graphElement, FilesSupplierMode mode, List<ValidationError> errors) {
        if (mode.isInSupported() && Strings.isNullOrEmpty(inputPath) && Strings.isNullOrEmpty(inputVariable)) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.in.file.empty")));
        }
        if (mode.isOutSupported()) {
            if (Strings.isNullOrEmpty(outputVariable) && Strings.isNullOrEmpty(outputDir)) {
                errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.out.file.empty")));
            }
        }
        if (!Strings.isNullOrEmpty(inputVariable) && !VariableUtils.variableExists(inputVariable, graphElement.getProcessDefinition())) {
            errors.add(ValidationError.createError(graphElement,
                    MessageFormat.format(Messages.getString("model.validation.in.file.variable.dontexist"), inputVariable)));
        }
        if (!Strings.isNullOrEmpty(outputVariable) && !VariableUtils.variableExists(outputVariable, graphElement.getProcessDefinition())) {
            errors.add(ValidationError.createError(graphElement,
                    MessageFormat.format(Messages.getString("model.validation.out.file.variable.dontexist"), outputVariable)));
        }
    }
}
