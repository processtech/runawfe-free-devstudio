package ru.runa.gpd.lang.model;

import java.util.List;

public interface IVariableContainer {
	
	List<Variable> getVariables(boolean expandComplexTypes, boolean includeSwimlanes, String... typeClassNameFilters);

}
