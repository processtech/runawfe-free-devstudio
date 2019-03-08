package ru.runa.gpd.validation;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.gpd.validation.ValidatorDefinition.Param;

public class ValidatorParser {
    private static final String VALIDATORS = "validators";
    private static final String MESSAGE = "message";
    private static final String PARAM = "param";
    private static final String TRANSITION_NAME = "on";
    private static final String TRANSITION_CONTEXT = "transition-context";
    private static final String TYPE = "type";
    private static final String NAME = "name";
    private static final String FIELD = "field";
    private static final String FIELD_VALIDATOR = "field-validator";
    private static final String GLOBAL_VALIDATOR = "validator";

    public static FormNodeValidation parseValidation(IFile validationFile) {
        try {
            FormNodeValidation validation = new FormNodeValidation();
            Document doc = XmlUtil.parseWithoutValidation(validationFile.getContents(true));
            List<Element> fieldNodes = doc.getRootElement().elements(FIELD);
            List<Element> globalValidatorElements = doc.getRootElement().elements(GLOBAL_VALIDATOR);
            validation.addGlobalConfigs(parseValidatorConfigs(globalValidatorElements));
            for (Element fieldElement : fieldNodes) {
                String fieldName = fieldElement.attributeValue(NAME);
                List<Element> fieldValidatorElements = fieldElement.elements(FIELD_VALIDATOR);
                validation.addFieldConfigs(fieldName, parseValidatorConfigs(fieldValidatorElements));
            }
            return validation;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static List<ValidatorConfig> parseValidatorConfigs(List<Element> validatorNodes) {
        List<ValidatorConfig> result = Lists.newArrayList();
        for (Element validatorElement : validatorNodes) {
            String validatorType = validatorElement.attributeValue(TYPE);
            ValidatorConfig validatorConfig = new ValidatorConfig(validatorType);
            Element transaitionsElement = validatorElement.element(TRANSITION_CONTEXT);
            if (transaitionsElement != null) {
                List<Element> transitionNameElements = transaitionsElement.elements(TRANSITION_NAME);
                for (Element transitionElement : transitionNameElements) {
                    validatorConfig.getTransitionNames().add(transitionElement.getText());
                }
            }
            List<Element> paramNodes = validatorElement.elements(PARAM);
            for (Element paramElement : paramNodes) {
                String paramName = paramElement.attributeValue(NAME);
                validatorConfig.getParams().put(paramName, paramElement.getText());
            }
            Element messageElement = validatorElement.element(MESSAGE);
            if (messageElement != null) {
                validatorConfig.setMessage(messageElement.getText());
            }
            result.add(validatorConfig);
        }
        return result;
    }

    public static void writeValidation(IFile validationFile, FormNode formNode, FormNodeValidation validation) {
        try {
            if (validation.isEmpty()) {
                if (validationFile.exists()) {
                    validationFile.delete(true, null);
                }
                return;
            }
            Document document = XmlUtil.createDocument(VALIDATORS);
            Element rootElement = document.getRootElement();
            for (ValidatorConfig config : validation.getGlobalConfigs()) {
                ValidatorDefinition definition = ValidatorDefinitionRegistry.getGlobalDefinition();
                Element element = rootElement.addElement(GLOBAL_VALIDATOR);
                writeConfig(definition, formNode, config, element);
            }
            for (Map.Entry<String, Map<String, ValidatorConfig>> entry : validation.getFieldConfigs().entrySet()) {
                Element fieldElement = rootElement.addElement(FIELD);
                fieldElement.addAttribute(NAME, entry.getKey());
                for (ValidatorConfig validatorConfig : entry.getValue().values()) {
                    ValidatorDefinition definition = ValidatorDefinitionRegistry.getDefinition(validatorConfig.getType());
                    Element element = fieldElement.addElement(FIELD_VALIDATOR);
                    if (definition != null) {
                        writeConfig(definition, formNode, validatorConfig, element);
                    } else {
                        PluginLogger.logErrorWithoutDialog(formNode + " validation: validator definition not found: " + validatorConfig.getType());
                    }
                }
            }
            byte[] bytes = XmlUtil.writeXml(document);
            IOUtils.createOrUpdateFile(validationFile, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            PluginLogger.logError("Validation file update error", e);
        }
    }

    private static void writeConfig(ValidatorDefinition definition, FormNode formNode, ValidatorConfig config, Element element) {
        element.addAttribute(TYPE, config.getType());
        if (config.getTransitionNames().size() > 0 && formNode.getLeavingTransitions().size() > 1
                && config.getTransitionNames().size() != formNode.getLeavingTransitions().size()) {
            // don't write all values
            Element transitionsElement = element.addElement(TRANSITION_CONTEXT);
            for (String transitionName : config.getTransitionNames()) {
                Element transitionElement = transitionsElement.addElement(TRANSITION_NAME);
                transitionElement.addCDATA(transitionName);
            }
        }
        if (!Strings.isNullOrEmpty(config.getMessage())) {
            Element messageElement = element.addElement(MESSAGE);
            messageElement.addCDATA(config.getMessage());
        }
        for (String paramName : config.getParams().keySet()) {
            String paramValue = config.getParams().get(paramName);
            if ((paramValue != null) && (paramValue.length() > 0)) {
                Param param = definition.getParams().get(paramName);
                if (param != null) {
                    Element paramElement = element.addElement(PARAM);
                    paramElement.addAttribute(NAME, paramName);
                    paramElement.addCDATA(paramValue);
                } else {
                    PluginLogger.logErrorWithoutDialog(formNode + " validation: parameter '" + paramName
                            + "'not registered in validator definition: " + definition.getName());
                }
            }
        }
    }

}