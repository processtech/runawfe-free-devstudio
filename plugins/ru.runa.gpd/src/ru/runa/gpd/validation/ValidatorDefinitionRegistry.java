package ru.runa.gpd.validation;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.validation.ValidatorDefinition.Param;
import com.google.common.base.Strings;

public class ValidatorDefinitionRegistry {
    private static final Map<String, ValidatorDefinition> definitions = new LinkedHashMap<String, ValidatorDefinition>();

    private static void init() {
        if (definitions.size() > 0) {
            return;
        }
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.validators").getExtensions();
        for (IExtension extension : extensions) {
            IConfigurationElement[] configElements = extension.getConfigurationElements();
            for (IConfigurationElement configElement : configElements) {
                try {
                    ValidatorDefinition definition = new ValidatorDefinition(configElement.getAttribute("name"), configElement.getAttribute("label"),
                            ValidatorDefinition.FIELD_TYPE, configElement.getAttribute("description"));
                    String applicableString = configElement.getAttribute("applicable");
                    if (!Strings.isNullOrEmpty(applicableString)) {
                        for (String string : applicableString.split(",")) {
                            definition.addApplicableType(string.trim());
                        }
                    }
                    IConfigurationElement[] paramElements = configElement.getChildren();
                    for (IConfigurationElement paramElement : paramElements) {
                        String requiredString = paramElement.getAttribute("required");
                        boolean required = Strings.isNullOrEmpty(requiredString) ? false : Boolean.parseBoolean(requiredString);
                        Param param = new Param(paramElement.getAttribute("name"), paramElement.getAttribute("label"),
                                paramElement.getAttribute("type"), required);
                        definition.addParam(param);
                    }
                    definitions.put(definition.getName(), definition);
                } catch (Exception e) {
                    PluginLogger.logError("Error processing 'validators' element", e);
                }
            }
        }
        ValidatorDefinition global = new ValidatorDefinition(ValidatorDefinition.GLOBAL_VALIDATOR_NAME, "Groovy", ValidatorDefinition.GLOBAL_TYPE,
                "Groovy script");
        global.addParam(new Param(ValidatorDefinition.EXPRESSION_PARAM_NAME, ValidatorDefinition.EXPRESSION_PARAM_NAME, Param.STRING_TYPE, true));
        definitions.put(ValidatorDefinition.GLOBAL_VALIDATOR_NAME, global);
    }

    public static Map<String, ValidatorDefinition> getValidatorDefinitions() {
        init();
        return definitions;
    }

    public static ValidatorDefinition getGlobalDefinition() {
        return getDefinition(ValidatorDefinition.GLOBAL_VALIDATOR_NAME);
    }

    public static ValidatorDefinition getDefinition(String type) {
        init();
        return definitions.get(type);
    }
}
