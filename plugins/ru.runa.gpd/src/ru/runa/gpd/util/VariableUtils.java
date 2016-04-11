package ru.runa.gpd.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableContainer;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.wfe.var.format.ListFormat;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class VariableUtils {
    private static final Pattern LIST_ENTRY_PATTERN = Pattern.compile("\\(\\s*([^\\)]+)\\)");
    public static final String CURRENT_PROCESS_ID = "${currentProcessId}";
    public static final String CURRENT_PROCESS_DEFINITION_NAME = "${currentDefinitionName}";
    public static final String CURRENT_NODE_NAME = "${currentNodeName}";
    public static final String CURRENT_NODE_ID = "${currentNodeId}";

    public static final String getListVariableComponentFormat(Variable variable) {
        String formatStr = variable.getFormat();
        if (!formatStr.contains(ListFormat.class.getName())) {
            return null;
        }
        Matcher m = LIST_ENTRY_PATTERN.matcher(formatStr);
        if (!m.find()) {
            return null;
        }
        MatchResult mr = m.toMatchResult();
        if (mr.groupCount() != 1) {
            return null;
        }
        return mr.group(1).trim();
    }

    public static Map<String, Variable> toMap(List<Variable> variables) {
        Map<String, Variable> result = Maps.newHashMapWithExpectedSize(variables.size());
        for (Variable variable : variables) {
            result.put(variable.getName(), variable);
        }
        return result;
    }

    public static boolean isValidScriptingName(String name) {
        return Objects.equal(name, toScriptingName(name));
    }

    public static String toScriptingName(String variableName) {
        char[] chars = variableName.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    chars[i] = '_';
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    chars[i] = '_';
                }
            }
            if ('$' == chars[i]) {
                chars[i] = '_';
            }
        }
        String scriptingName = new String(chars);
        return scriptingName;
    }

    public static String generateNameForScripting(VariableContainer variableContainer, String variableName, Variable excludedVariable) {
        String scriptingName = toScriptingName(variableName);
        if (excludedVariable != null) {
            if (excludedVariable.getScriptingName() == null || Objects.equal(excludedVariable.getScriptingName(), scriptingName)) {
                return scriptingName;
            }
        }
        while (getVariableByScriptingName(variableContainer.getVariables(false, true), scriptingName) != null) {
            scriptingName += "_";
        }
        return scriptingName;
    }

    public static List<String> getVariableNamesForScripting(List<Variable> variables) {
        List<String> result = Lists.newArrayListWithExpectedSize(variables.size());
        for (Variable variable : variables) {
            if (variable.getScriptingName() != null) {
                result.add(variable.getScriptingName());
            } else {
                // this is here due to strange NPE
                PluginLogger.logErrorWithoutDialog("No scriptingName attribute in " + variable.getName());
            }
        }
        return result;
    }

    public static List<String> getVariableNamesForScripting(Delegable delegable, String... typeClassNameFilters) {
        if (delegable instanceof GraphElement) {
            List<Variable> variables = ((GraphElement) delegable).getVariables(true, true, typeClassNameFilters);
            return getVariableNamesForScripting(variables);
        } else {
            List<String> list = delegable.getVariableNames(true, typeClassNameFilters);
            for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
                String string = iterator.next();
                if (!isValidScriptingName(string)) {
                    iterator.remove();
                }
            }
            return list;
        }
    }

    public static List<String> getVariableNames(List<? extends Variable> variables) {
        List<String> result = Lists.newArrayList();
        for (Variable variable : variables) {
            result.add(variable.getName());
        }
        return result;
    }

    /**
     * @return variable or <code>null</code>
     */
    public static Variable getVariableByScriptingName(List<Variable> variables, String name) {
        for (Variable variable : variables) {
            if (Objects.equal(variable.getScriptingName(), name)) {
                return variable;
            }
        }
        return null;
    }

    /**
     * @return variable or <code>null</code>
     */
    public static Variable getVariableByName(VariableContainer variableContainer, String name) {
        List<Variable> variables = variableContainer.getVariables(false, true);
        for (Variable variable : variables) {
            if (Objects.equal(variable.getName(), name)) {
                return variable;
            }
        }
        if (name != null && name.contains(VariableUserType.DELIM)) {
            int index = name.indexOf(VariableUserType.DELIM);
            String complexVariableName = name.substring(0, index);
            Variable complexVariable = getVariableByName(variableContainer, complexVariableName);
            if (complexVariable == null) {
                return null;
            }
            String scriptingName = complexVariable.getScriptingName();
            String attributeName = name.substring(index + 1);
            while (attributeName.contains(VariableUserType.DELIM)) {
                index = attributeName.indexOf(VariableUserType.DELIM);
                complexVariableName = attributeName.substring(0, index);
                complexVariable = getVariableByName(complexVariable.getUserType(), complexVariableName);
                if (complexVariable == null) {
                    return null;
                }
                scriptingName += VariableUserType.DELIM + complexVariable.getScriptingName();
                attributeName = attributeName.substring(index + 1);
            }
            Variable attribute = getVariableByName(complexVariable.getUserType(), attributeName);
            if (attribute != null) {
                scriptingName += VariableUserType.DELIM + attribute.getScriptingName();
                return new Variable(name, scriptingName, attribute);
            }
        }
        return null;
    }

    public static String wrapVariableName(String variableName) {
        return "${" + variableName + "}";
    }

    public static boolean isVariableNameWrapped(String value) {
        return value.length() > 3 && "${".equals(value.substring(0, 2)) && value.endsWith("}");
    }

    public static String unwrapVariableName(String value) {
        if (value.length() > 3) {
            return value.substring(2, value.length() - 1);
        }
        return "";
    }

    private static void searchInVariables(List<Variable> result, VariableUserType searchType, Variable searchAttribute, Variable parent,
            List<Variable> children) {
        for (Variable variable : children) {
            if (variable.getUserType() == null) {
                continue;
            }
            String syntheticName = (parent != null ? (parent.getName() + VariableUserType.DELIM) : "") + variable.getName();
            String syntheticScriptingName = (parent != null ? (parent.getScriptingName() + VariableUserType.DELIM) : "")
                    + variable.getScriptingName();
            if (Objects.equal(variable.getUserType(), searchType)) {
                Variable syntheticVariable = new Variable(syntheticName + VariableUserType.DELIM + searchAttribute.getName(), syntheticScriptingName
                        + VariableUserType.DELIM + searchAttribute.getScriptingName(), variable);
                result.add(syntheticVariable);
            } else {
                Variable syntheticVariable = new Variable(syntheticName, syntheticScriptingName, variable);
                searchInVariables(result, searchType, searchAttribute, syntheticVariable, variable.getUserType().getAttributes());
            }
        }
    }

    public static List<Variable> findVariablesOfTypeWithAttributeExpanded(VariableContainer variableContainer, VariableUserType searchType,
            Variable searchAttribute) {
        List<Variable> result = Lists.newArrayList();
        searchInVariables(result, searchType, searchAttribute, null, variableContainer.getVariables(false, false));
        return result;
    }

    public static List<Variable> expandComplexVariable(Variable superVariable, Variable complexVariable) {
        List<Variable> result = Lists.newArrayList();
        for (Variable attribute : complexVariable.getUserType().getAttributes()) {
            String name = superVariable.getName() + VariableUserType.DELIM + attribute.getName();
            String scriptingName = superVariable.getScriptingName() + VariableUserType.DELIM + attribute.getScriptingName();
            Variable variable = new Variable(name, scriptingName, attribute);
            result.add(variable);
            if (variable.isComplex()) {
                result.addAll(expandComplexVariable(variable, attribute));
            }
        }
        return result;
    }

    public static Set<VariableUserType> getUsedUserTypes(VariableContainer variableContainer, String variableName) {
        String[] nameParts = variableName.split(Pattern.quote(VariableUserType.DELIM));
        Set<VariableUserType> variableUserTypes = Sets.newHashSet();
        for (int i = 0; i < nameParts.length; i++) {
            Variable variablePart = getVariableByName(variableContainer, nameParts[i]);
            VariableUserType variableUserType = variablePart.getUserType();
            if (variableUserType != null) {
                variableUserTypes.add(variableUserType);
                variableContainer = variableUserType;
            }
        }
        return variableUserTypes;
    }

    public static Variable getComplexVariableByExpandedAttribute(VariableContainer variableContainer, String variableName) {
        return getVariableByName(variableContainer, variableName.split(Pattern.quote(VariableUserType.DELIM))[0]);
    }

}
