package ru.runa.gpd.extension.handler;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.handler.ParamDef.Presentation;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.util.XmlUtil;

@SuppressWarnings("unchecked")
public class ParamDefConfig {
    public static final String NAME_CONFIG = "config";
    private static final Pattern VARIABLE_REGEXP = Pattern.compile("\\$\\{(.*?[^\\\\])\\}");
    private final String name;
    private final List<ParamDefGroup> groups = new ArrayList<ParamDefGroup>();

    public ParamDefConfig(String name) {
        this.name = name;
    }

    public ParamDefConfig() {
        this(NAME_CONFIG);
    }

    public static ParamDefConfig parse(String xml) {
        return parse(XmlUtil.parseWithoutValidation(xml));
    }

    public static ParamDefConfig parse(Document document) {
        Element rootElement = document.getRootElement();
        return parse(rootElement);
    }

    public static ParamDefConfig parse(Element rootElement) {
        ParamDefConfig config = new ParamDefConfig();
        List<Element> groupElements = rootElement.elements();
        for (Element groupElement : groupElements) {
            ParamDefGroup group = new ParamDefGroup(groupElement);
            List<Element> inputParamElements = groupElement.elements("param");
            for (Element element : inputParamElements) {
                group.getParameters().add(new ParamDef(element));
            }
            config.getGroups().add(group);
        }
        return config;
    }

    public String getName() {
        return name;
    }

    public List<ParamDefGroup> getGroups() {
        return groups;
    }

    /**
     * @param groupName
     * @return group or null if none found
     */
    public ParamDefGroup getGroupByName(String groupName) {
        for (ParamDefGroup group : groups) {
            if (groupName.equals(group.getName())) {
                return group;
            }
        }

        return null;
    }

    /**
     * Retrieves all founded parameter to variable mappings
     * 
     * @param configuration
     *            param-based xml or <code>null</code> or empty string
     */
    public static Map<String, String> getAllParameters(String configuration) {
        Map<String, String> properties = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(configuration)) {
            return properties;
        }
        Document doc = XmlUtil.parseWithoutValidation(configuration);
        List<Element> groupElements = doc.getRootElement().elements();
        for (Element groupElement : groupElements) {
            List<Element> paramElements = groupElement.elements("param");
            for (Element element : paramElements) {
                String value;
                if (element.attributeValue("variable") != null) {
                    value = element.attributeValue("variable");
                } else {
                    value = element.attributeValue("value");
                }
                String name = element.attributeValue("name");
                properties.put(name, value);
            }
        }
        return properties;
    }

    /**
     * Retrieves all founded parameter to variable mappings based on this
     * definition.
     * 
     * @param configuration
     *            valid param-based xml
     * @return not <code>null</code> parameters (empty parameters on parsing
     *         error)
     */
    public Map<String, String> parseConfiguration(String configuration) {
        Map<String, String> properties = new HashMap<String, String>();
        if (Strings.isNullOrEmpty(configuration)) {
            return properties;
        }
        try {
            Document doc = XmlUtil.parseWithoutValidation(configuration);
            Map<String, String> allProperties = new HashMap<String, String>();
            for (ParamDefGroup group : groups) {
                Element groupElement = doc.getRootElement().element(group.getName());
                if (groupElement != null) {
                    List<Element> pElements = groupElement.elements();
                    for (Element element : pElements) {
                        if ("param".equals(element.getName())) {
                            String value;
                            if (element.attributeValue("variable") != null) {
                                value = element.attributeValue("variable");
                            } else {
                                value = element.attributeValue("value");
                            }
                            String name = element.attributeValue("name");
                            allProperties.put(name, value);
                        } else {
                            allProperties.put(element.getName(), element.getTextTrim());
                        }
                    }
                }
            }
            for (ParamDefGroup group : groups) {
                Element groupElement = doc.getRootElement().element(group.getName());
                if (groupElement != null) {
                    List<Element> pElements = groupElement.elements();
                    for (Element element : pElements) {
                        String name = "param".equals(element.getName()) ? element.attributeValue("name") : element.getName();
                        String value = allProperties.get(name);
                        String fName = fixParamName(name, allProperties);
                        if (fName == null) {
                            group.getDynaProperties().put(name, value);
                        } else {
                            properties.put(fName, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(configuration, e);
        }
        return properties;
    }

    public String fixParamName(String name, Map<String, String> properties) {
        for (ParamDefGroup group : groups) {
            for (ParamDef paramDef : group.getParameters()) {
                String paramName = paramDef.getName();
                if (name.equals(paramName)) {
                    return name;
                }
                paramName = substitute(paramName, properties);
                if (name.equals(paramName)) {
                    return paramDef.getName();
                }
            }
        }
        return null;
    }

    public ParamDef getParamDef(String name) {
        for (ParamDefGroup group : groups) {
            for (ParamDef paramDef : group.getParameters()) {
                String paramName = paramDef.getName();
                if (name.equals(paramName)) {
                    return paramDef;
                }
            }
        }
        return null;
    }

    public boolean validate(Delegable delegable, List<ValidationError> errors) {
        String configuration = delegable.getDelegationConfiguration();
        GraphElement graphElement = ((GraphElement) delegable);
        Map<String, String> props = parseConfiguration(configuration);
        for (ParamDefGroup group : groups) {
            for (ParamDef paramDef : group.getParameters()) {
                String value = props.get(paramDef.getName());
                if (paramDef.isOptional() && !isValid(value)) {
                    continue;
                }
                if (!paramDef.isOptional() && !isValid(value)) {
                    errors.add(ValidationError.createLocalizedError(graphElement, "parambased.requiredParamIsNotSet", paramDef.getLabel()));
                } else if (paramDef.isUseVariable() && paramDef.getPresentation() == Presentation.combo) {
                    String[] filters = paramDef.getFormatFilters().toArray(new String[paramDef.getFormatFilters().size()]);
                    List<String> variableNames = graphElement.getProcessDefinition().getVariableNames(true, filters);
                    if (!variableNames.contains(value)) {
                        errors.add(ValidationError.createLocalizedError(graphElement, "parambased.missedParamVariable", paramDef.getLabel(), value));
                    }
                }
            }
        }
        return true;
    }

    protected boolean isValid(String value) {
        return value != null && value.trim().length() > 0;
    }

    public String toConfiguration(List<String> variableNames, Map<String, String> properties) {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setSuppressDeclaration(true);
        return XmlUtil.toString(toConfigurationXml(variableNames, properties), format);
    }

    public Document toConfigurationXml(List<String> variableNames, Map<String, String> properties) {
        Document doc = DocumentHelper.createDocument();
        doc.add(DocumentHelper.createElement(name));
        Element root = doc.getRootElement();
        Element prevGroupElement = null;
        for (ParamDefGroup group : groups) {
            Element groupElement;
            if (prevGroupElement != null && prevGroupElement.getName().equals(group.getName())) {
                groupElement = prevGroupElement;
            } else {
                groupElement = DocumentHelper.createElement(group.getName());
                root.add(groupElement);
                for (String dName : group.getDynaProperties().keySet()) {
                    String dValue = group.getDynaProperties().get(dName);
                    Element paramElement = DocumentHelper.createElement("param");
                    paramElement.addAttribute("name", dName);
                    paramElement.addAttribute("value", dValue);
                    groupElement.add(paramElement);
                }
            }
            for (ParamDef param : group.getParameters()) {
                String value = properties.get(param.getName());
                if (value == null) {
                    continue;
                }
                String paramName = param.getName();
                paramName = substitute(paramName, properties);
                Element paramElement;
                if (param.getXmlNodeType() == ParamDef.XML_TYPE_ATTR) {
                    paramElement = DocumentHelper.createElement("param");
                    paramElement.addAttribute("name", paramName);
                    if (param.isUseVariable()) {
                        paramElement.addAttribute("variable", value);
                    } else {
                        paramElement.addAttribute("value", value);
                    }
                } else {
                    paramElement = DocumentHelper.createElement(paramName);
                    paramElement.add(DocumentHelper.createText(value));
                }
                groupElement.add(paramElement);
            }
            prevGroupElement = groupElement;
        }
        return doc;
    }

    private String substitute(String value, Map<String, String> properties) {
        Matcher matcher = VARIABLE_REGEXP.matcher(value);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String pName = matcher.group(1);
            String parameter = properties.get(pName);
            if (parameter == null) {
                parameter = "";
            }
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(parameter));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public void writeXml(Branch parent) {
        Element root = parent.addElement("config");
        for (ParamDefGroup group : getGroups()) {
            Element groupParamElement = root.addElement(group.getName());
            for (ParamDef param : group.getParameters()) {
                Element paramElement = groupParamElement.addElement("param");
                paramElement.addAttribute("name", param.getName());
                paramElement.addAttribute("label", param.getLabel());
                if (param.getFormatFilters().size() > 0) {
                    paramElement.addAttribute("formatFilter", param.getFormatFilters().get(0));
                }
                if (param.isOptional()) {
                    paramElement.addAttribute("optional", "true");
                }
                if (!param.isUseVariable()) {
                    paramElement.addAttribute("variable", "false");
                }
            }
        }
    }

    public Set<String> getAllParameterNames(boolean excludeOptional) {
        Set<String> result = Sets.newHashSet();
        for (ParamDefGroup group : getGroups()) {
            for (ParamDef param : group.getParameters()) {
                if (excludeOptional && param.isOptional()) {
                    continue;
                }
                result.add(param.getName());
            }
        }
        return result;
    }
}
