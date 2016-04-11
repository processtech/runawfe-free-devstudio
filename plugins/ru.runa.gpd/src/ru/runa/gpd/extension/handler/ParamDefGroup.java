package ru.runa.gpd.extension.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;

public class ParamDefGroup {
    public static final String NAME_INPUT = "input";
    public static final String NAME_OUTPUT = "output";
    
    private final String name;
    private final String label;
    private String help;
    private final List<ParamDef> parameters = new ArrayList<ParamDef>();
    private final Map<String, String> dynaProperties = new HashMap<String, String>();
    
    public ParamDefGroup(String name) {
        this.name = name;
        this.label = name;
    }
    
    public ParamDefGroup(Element element) {
        this.name = element.getName();
        this.label = element.attributeValue("label", this.name);
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
    
    public void setHelp(String help) {
        this.help = help;
    }

    public List<ParamDef> getParameters() {
        return parameters;
    }
    
    public Map<String, String> getDynaProperties() {
        return dynaProperties;
    }
    
}
