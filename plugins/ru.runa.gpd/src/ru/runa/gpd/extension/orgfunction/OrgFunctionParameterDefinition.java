package ru.runa.gpd.extension.orgfunction;

public class OrgFunctionParameterDefinition {
    private final String name;
    private final String type;
    private final boolean multiple;

    public OrgFunctionParameterDefinition(String name, String type, boolean multiple) {
        this.name = name;
        this.type = type;
        this.multiple = multiple;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isMultiple() {
        return multiple;
    }
}
