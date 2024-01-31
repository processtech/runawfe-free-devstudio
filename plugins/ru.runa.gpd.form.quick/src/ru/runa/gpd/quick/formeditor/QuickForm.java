package ru.runa.gpd.quick.formeditor;

import java.util.ArrayList;
import java.util.List;

public class QuickForm {
    private String name;
    private String delegationConfiguration = "";
    private final List<QuickFormComponent> variables = new ArrayList<QuickFormComponent>();
    private final List<QuickFormGpdProperty> properties = new ArrayList<QuickFormGpdProperty>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    public void setDelegationConfiguration(String delegationConfiguration) {
        this.delegationConfiguration = delegationConfiguration;
    }

    public List<QuickFormComponent> getVariables() {
        return variables;
    }

    public List<QuickFormGpdProperty> getProperties() {
        return properties;
    }
}
