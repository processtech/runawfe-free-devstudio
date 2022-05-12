package ru.runa.gpd.extension.decision;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

public class GroovyDecisionModel extends GroovyModel {
    private final List<IfExpression> ifExpressions = new ArrayList<>();
    private static Pattern IF_PATTERN = Pattern.compile("if \\((.*)\\)");
    private static Pattern RETURN_PATTERN = Pattern.compile("return \"([^\"]*)\";");

    public GroovyDecisionModel() {
    }

    public GroovyDecisionModel(String code, List<Variable> variables) throws Exception {
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        Matcher matcher = IF_PATTERN.matcher(code);
        int startReturnSearch = 0;
        while (matcher.find()) {
            String transition;
            String ifContent = normalizeString(matcher.group(1));
            String[] strings = ifContent.split(" ");
            // tmp
            String firstVariableText = "";
            String operator;
            String secondVariableText = "";
            boolean isOperationDateType = false;
            if (strings.length == 1 || ifContent.indexOf("\"") > 0) {
                // i.e. var1.equals(var2) or var1.contains(var2)
                int start;
                if (ifContent.charAt(0) != '!') {
                    start = 0;
                    if (ifContent.contains("equals")) {
                        operator = "==";
                    } else {
                        operator = "contains";
                    }
                } else {
                    start = 1;
                    operator = "!=";
                }
                firstVariableText = ifContent.substring(start, ifContent.indexOf("."));
                secondVariableText = ifContent.substring(ifContent.indexOf("(") + 1, ifContent.length() - 1);
            } else if (strings.length > 3 && ifContent.contains(" || ") && ifContent.contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar")
                    && ifContent.endsWith(" 0")) {
                // GroovyTypeSupport.DateType
                isOperationDateType = true;
                String ifContentWithoutNullCheck = ifContent.substring(ifContent.lastIndexOf(" || ") + " || ".length());
                String[] stringsWithoutNullCheck = ifContentWithoutNullCheck.split(" ");
                String[] parts = stringsWithoutNullCheck[0].split("\\.compareTo\\(");
                firstVariableText = parts[0].substring(parts[0].lastIndexOf("(") + 1, parts[0].indexOf(")"));
                secondVariableText = parts[1].substring(parts[1].lastIndexOf("(") + 1, parts[1].indexOf(")"));
                operator = stringsWithoutNullCheck[1];
            } else if (strings.length > 3 && ifContent.contains(" || ") && ifContent.contains("BigDecimal") && ifContent.endsWith(" 0")) {
                // GroovyTypeSupport.BigDecimalType
                String ifContentWithoutNullCheck = ifContent.substring(ifContent.lastIndexOf(" || ") + " || ".length());
                String[] stringsWithoutNullCheck = ifContentWithoutNullCheck.split(" ");
                firstVariableText = stringsWithoutNullCheck[2];
                secondVariableText = stringsWithoutNullCheck[6];
                operator = stringsWithoutNullCheck[9];
            } else {
                firstVariableText = strings[0];
                operator = strings[1];
                if (strings.length == 3) {
                    secondVariableText = strings[2];
                } else {
                    for (int i = 2; i < strings.length; i++) {
                        secondVariableText += " " + strings[i];
                    }
                }
            }
            startReturnSearch = matcher.end(1);
            if (returnMatcher.find(startReturnSearch)) {
                transition = returnMatcher.group(1);
                startReturnSearch = returnMatcher.end(1);
            } else {
                throw new RuntimeException("unparsed");
            }
            if (firstVariableText.indexOf(".") > 0 && !isOperationDateType) {
                // Java names doesn't allow use of point in variable name
                firstVariableText = firstVariableText.substring(0, firstVariableText.lastIndexOf("."));
            }
            Variable firstVariable = VariableUtils.getVariableByScriptingName(variables, firstVariableText);
            if (firstVariable == null) {
                // variable deleted
                continue;
            }
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(firstVariable.getJavaClassName());
            Operation operation = Operation.getByOperator(operator, typeSupport);
            if (operation == null) {
                throw new RuntimeException("Operation not found for operator: " + operator);
            }
            Object secondVariable;
            if (secondVariableText.indexOf(".") > 0 && !isOperationDateType) {
                try {
                    Double.parseDouble(secondVariableText);
                } catch (NumberFormatException e) {
                    // Java names doesn't allow use of point in variable name
                    secondVariableText = secondVariableText.substring(0, secondVariableText.lastIndexOf("."));
                }
            }
            Variable variable = VariableUtils.getVariableByScriptingName(variables, secondVariableText);
            if (variable != null) {
                secondVariable = variable;
            } else if (Operation.VOID.equals(secondVariableText) || Operation.NULL.equals(secondVariableText)) {
                secondVariable = "null";
            } else {
                secondVariable = typeSupport.unwrapValue(secondVariableText);
            }
            IfExpression ifExpression = new IfExpression(transition, firstVariable, secondVariable, operation);
            addIfExpression(ifExpression);
        }
        if (returnMatcher.find(startReturnSearch)) {
            String defaultTransition = returnMatcher.group(1);
            IfExpression ifExpression = new IfExpression(defaultTransition);
            addIfExpression(ifExpression);
        }
    }

    public static Set<String> getTransitionNames(String code) {
        Set<String> result = Sets.newHashSet();
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        while (returnMatcher.find()) {
            String transitionName = returnMatcher.group(1);
            result.add(transitionName);
        }
        return result;
    }

    public List<String> getTransitionNames() {
        List<String> transitionNames = new ArrayList<>();
        for (IfExpression ifExpression : ifExpressions) {
            transitionNames.add(ifExpression.getTransition());
        }
        return transitionNames;
    }

    public String getDefaultTransitionName() {
        for (IfExpression ifExpression : ifExpressions) {
            if (ifExpression.isByDefault()) {
                return ifExpression.getTransition();
            }
        }
        return "";
    }

    public void addIfExpression(IfExpression ifExpression) {
        ifExpressions.add(ifExpression);
    }

    public List<IfExpression> getIfExpressions() {
        return ifExpressions;
    }

    public IfExpression getIfExpression(String transitionName) {
        for (IfExpression ifExpression : ifExpressions) {
            if (transitionName.equals(ifExpression.getTransition())) {
                return ifExpression;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        IfExpression defaultIf = null;
        for (IfExpression ifExpression : ifExpressions) {
            if (!ifExpression.isByDefault()) {
                buffer.append(ifExpression.generateCode());
            } else {
                defaultIf = ifExpression;
            }
        }
        if (defaultIf != null) {
            buffer.append("\nreturn \"" + defaultIf.getTransition() + "\";\n");
        }
        return buffer.toString();
    }

    public static class IfExpression {
        private Variable firstVariable;
        private Object secondVariable;
        private final Operation operation;
        private final String transition;
        private boolean byDefault;

        public IfExpression(String transition) {
            this.transition = transition;
            this.byDefault = true;
            this.firstVariable = null;
            this.secondVariable = null;
            this.operation = null;
        }

        public IfExpression(String transition, Variable firstVariable, Object secondVariable, Operation operation) {
            this.transition = transition;
            this.firstVariable = firstVariable;
            this.secondVariable = secondVariable;
            this.operation = operation;
        }

        public String generateCode() {
            return "if ( " + operation.generateCode(firstVariable, secondVariable) + " ) {\n\treturn \"" + transition + "\";\n};\n";
        }

        public Variable getFirstVariable() {
            return firstVariable;
        }

        public boolean isByDefault() {
            return byDefault;
        }

        public String getSecondVariableTextValue() {
            if (secondVariable instanceof Variable) {
                return ((Variable) secondVariable).getScriptingName();
            } else if (secondVariable instanceof String) {
                return (String) secondVariable;
            } else {
                throw new IllegalArgumentException("secondVariable class is " + secondVariable.getClass().getName());
            }
        }

        public Operation getOperation() {
            return operation;
        }

        public String getTransition() {
            return transition;
        }
    }
}
