package ru.runa.gpd.extension.handler;

import java.util.List;

import org.dom4j.Element;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.BackCompatibilityUtils;

import com.google.common.collect.Lists;

public class ParamDef {
    public static enum Presentation {
        undefined, combo, text, richcombo, checkbox
    }

    public static final int XML_TYPE_ATTR = 1;
    public static final int XML_TYPE_NODE = 2;
    private Presentation presentation = Presentation.undefined;
    private int xmlNodeType = XML_TYPE_ATTR;
    private final String name;
    private final String label;
    private final List<String> formatFilters = Lists.newArrayList();
    private String help;
    private String[] comboItems = {};
    private String defaultValue;
    private boolean useVariable = true;
    private boolean optional = false;

    public ParamDef(Element element) {
        if (element.attributeValue("presentation") != null) {
            this.presentation = Presentation.valueOf(element.attributeValue("presentation"));
        }
        if (element.attributeValue("xmlNodeType") != null) {
            this.xmlNodeType = Integer.parseInt(element.attributeValue("xmlNodeType"));
        }
        this.useVariable = Boolean.parseBoolean(element.attributeValue("variable", "true"));
        this.name = element.attributeValue("name");
        if (element.attributeValue("label.key") != null) {
            this.label = Localization.getString(element.attributeValue("label.key"));
        } else {
            this.label = element.attributeValue("label");
        }
        this.help = element.attributeValue("help");
        if (element.attributeValue("help.key") != null) {
            this.help = Localization.getString(element.attributeValue("help.key"));
        }
        if (element.attributeValue("optional") != null) {
            this.optional = Boolean.parseBoolean(element.attributeValue("optional"));
        }
        if (element.attributeValue("options") != null) {
            this.comboItems = element.attributeValue("options").split(",", -1);
        }
        this.defaultValue = element.attributeValue("defaultValue");
        String formatFilter = element.attributeValue("formatFilter");
        if (formatFilter != null && formatFilter.length() > 0) {
            String[] formats = formatFilter.split(";", -1);
            for (String format : formats) {
                formatFilters.add(BackCompatibilityUtils.getClassName(format));
            }
        }
    }

    public ParamDef(String name, String label) {
        this.name = name;
        this.label = label;
    }

    public Presentation getPresentation() {
        if (Presentation.undefined == presentation) {
            return (useVariable || comboItems.length > 0) ? Presentation.combo : Presentation.text;
        }
        return presentation;
    }

    public int getXmlNodeType() {
        return xmlNodeType;
    }

    public void setXmlNodeType(int xmlNodeType) {
        this.xmlNodeType = xmlNodeType;
    }

    public String[] getComboItems() {
        return comboItems;
    }

    public List<String> getFormatFilters() {
        return formatFilters;
    }

    public String[] getFormatFiltersAsArray() {
        return formatFilters.toArray(new String[formatFilters.size()]);
    }

    public String getHelp() {
        return help;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public boolean isUseVariable() {
        return useVariable;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setComboItems(String[] comboItems) {
        this.comboItems = comboItems;
    }

    public void setUseVariable(boolean useVariable) {
        this.useVariable = useVariable;
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
