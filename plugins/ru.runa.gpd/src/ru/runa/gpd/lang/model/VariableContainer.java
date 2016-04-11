package ru.runa.gpd.lang.model;

import java.util.List;

public interface VariableContainer {
	
	List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters);

}
