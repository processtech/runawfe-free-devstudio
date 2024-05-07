package ru.runa.gpd.lang.model;

import java.util.List;

public interface Delegable extends DelegationConfiguration {

    public String getDelegationType();
    
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters);
    
}
