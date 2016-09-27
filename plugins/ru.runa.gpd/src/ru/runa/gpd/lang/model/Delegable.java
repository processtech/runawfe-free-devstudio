package ru.runa.gpd.lang.model;

import java.util.List;

public interface Delegable {
    public String getDelegationClassName();

    public void setDelegationClassName(String delegateClassName);

    public String getDelegationConfiguration();

    public void setDelegationConfiguration(String configuration);

    public String getDelegationType();
    
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters);
    
}
