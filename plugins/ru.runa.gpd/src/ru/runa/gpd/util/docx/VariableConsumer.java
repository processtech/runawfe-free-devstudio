package ru.runa.gpd.util.docx;

import java.util.ArrayList;
import java.util.Map;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDoesNotExistException;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class VariableConsumer implements VariableProvider {

    private Map<String, Integer> variablesMap;
    private boolean scriptParseMode;

    VariableConsumer(Map<String, Integer> variablesMap, boolean scriptParseMode) {
        this.variablesMap = variablesMap;
        this.scriptParseMode = scriptParseMode;
    }

    @Override
    public ParsedProcessDefinition getParsedProcessDefinition() {
    	return null;
    }

    @Override
    public Long getProcessDefinitionId() {
        return null;
    }

    @Override
    public String getProcessDefinitionName() {
        return null;
    }

    @Override
    public Long getProcessId() {
        return null;
    }

    @Override
    public UserType getUserType(String arg0) {
        return null;
    }

    public void parseGroovy(String script) {
        int firstIndex = script.indexOf("(");
        if (firstIndex > 0 && script.endsWith(")")) {
            String variables = script.substring(firstIndex + 1, script.length() - 1);
            String[] variablesArray = variables.split(",");
            for (String variable : variablesArray) {
                variable = variable.trim();
                if (!variable.isEmpty()) {
                    getVariable(variable);
                }
            }
        }

    }

    @Override
    public Object getValue(String arg0) {
        return "";
    }

    @Override
    public <T> T getValue(Class<T> arg0, String arg1) {
        return null;
    }

    @Override
    public Object getValueNotNull(String arg0) throws VariableDoesNotExistException {
        return null;
    }

    @Override
    public <T> T getValueNotNull(Class<T> arg0, String arg1) throws VariableDoesNotExistException {
        return null;
    }

    private String unarrayVariableName(String variable) {
        if (!scriptParseMode) {
            int i1 = variable.indexOf("[");
            int i2 = variable.indexOf("]");

            if (i1 > 0 && i2 > i1) {
                variable = variable.substring(0, i1);
            }
        }

        return variable.replaceAll("\\[.*?\\]", "");
    }

    @Override
    public WfVariable getVariable(String variableName) {
        if (null != IterateBy.identifyByString(null, variableName)) {
            return null;
        }

        variableName = unarrayVariableName(variableName);

        variablesMap.put(variableName, 1);
        return null;
    }

    public WfVariable getVariable(String variableName, boolean loopVarable) {
        if (null != IterateBy.identifyByString(null, variableName)) {
            return null;
        }

        variableName = unarrayVariableName(variableName);

        variablesMap.put(variableName, 1);
        return loopVarable ? new WfVariable(variableName, new ArrayList()) : null;
    }

    @Override
    public WfVariable getVariableNotNull(String arg0) throws VariableDoesNotExistException {
        return null;
    }

    public void add(WfVariable iteratorVariable) {
    }

    public void remove(String iteratorVariableName) {
    }

    public Object getProperty(Object value, String variableName) {
        return null;
    }

}
