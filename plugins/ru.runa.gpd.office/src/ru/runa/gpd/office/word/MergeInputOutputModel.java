package ru.runa.gpd.office.word;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.office.InputOutputModel;
import ru.runa.gpd.office.Messages;

import com.google.common.base.Strings;

public class MergeInputOutputModel extends InputOutputModel {

    private List<String> inputPathList = new ArrayList<String>();
    private List<String> inputVariableList = new ArrayList<String>();;
    private List<String> inputAddBreakList = new ArrayList<>();

    public void serialize(Document document, Element parent) {
        initMultiIn(parent);
        Element output = parent.addElement("output");
        if (!Strings.isNullOrEmpty(outputDir)) {
            output.addAttribute("dir", outputDir);
        }
        if (!Strings.isNullOrEmpty(outputVariable)) {
            output.addAttribute("variable", outputVariable);
        }
        if (!Strings.isNullOrEmpty(outputFilename)) {
            output.addAttribute("fileName", outputFilename);
        }
    }

    public static MergeInputOutputModel deserialize(List<Element> inputList, Element output) {
        MergeInputOutputModel model = new MergeInputOutputModel();
        if (inputList != null) {
            for (Element input : inputList) {
                model.inputPathList.add(input.attributeValue("path"));
                model.inputVariableList.add(input.attributeValue("variable"));
                model.inputAddBreakList.add(input.attributeValue("addBreak", "true"));
            }
        }
        initOutModel(output, model);
        return model;
    }

    public void validate(GraphElement graphElement, List<ValidationError> errors) {
        if (inputPathList.isEmpty() && inputVariableList.isEmpty()) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.in.file.empty")));
        }
        if (Strings.isNullOrEmpty(outputVariable) && Strings.isNullOrEmpty(outputDir)) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.out.file.empty")));
        }
        if (Strings.isNullOrEmpty(outputFilename)) {
            errors.add(ValidationError.createError(graphElement, Messages.getString("model.validation.out.filename.empty")));
        }
    }

    public List<String> getInputPathList() {
        return inputPathList;
    }

    public List<String> getInputVariableList() {
        return inputVariableList;
    }

    public List<String> getInputAddBreakList() {
        return inputAddBreakList;
    }

    private void initMultiIn(Element parent) {
        final int size = Math.max(inputPathList.size(), inputVariableList.size());
        for (int i = 0; i < size; ++i) {
            initIn(parent, i < inputPathList.size() ? inputPathList.get(i) : null, i < inputVariableList.size() ? inputVariableList.get(i) : null,
                    i < inputAddBreakList.size() ? inputAddBreakList.get(i) : null);
        }

    }

    private void initIn(Element parent, String inputPath, String inputVariable, String addBreak) {
        Element input = parent.addElement("input");
        if (!Strings.isNullOrEmpty(inputPath)) {
            input.addAttribute("path", inputPath);
        }
        if (!Strings.isNullOrEmpty(inputVariable)) {
            input.addAttribute("variable", inputVariable);
        }
        if (!Strings.isNullOrEmpty(addBreak)) {
            input.addAttribute("addBreak", addBreak);
        }
    }

    private static void initOutModel(Element output, MergeInputOutputModel model) {
        if (output != null) {
            model.outputFilename = output.attributeValue("fileName");
            model.outputDir = output.attributeValue("dir");
            model.outputVariable = output.attributeValue("variable");
        }
    }

}
