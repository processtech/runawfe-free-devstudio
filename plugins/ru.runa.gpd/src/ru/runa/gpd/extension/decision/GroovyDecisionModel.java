package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Sets;

public class GroovyDecisionModel implements GroovyModel {
    private final List<IfExpr> ifs = new ArrayList<IfExpr>();
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
                isOperationDateType = false;
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
                // Java names doesn't allowed use of point in variable name
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
                    // Java names doesn't allowed use of point in variable name
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
            IfExpr ifExpr = new IfExpr(transition, firstVariable, secondVariable, operation);
            addIfExpr(ifExpr);
        }
        if (returnMatcher.find(startReturnSearch)) {
            String defaultTransition = returnMatcher.group(1);
            IfExpr ifExpr = new IfExpr(defaultTransition);
            addIfExpr(ifExpr);
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
        List<String> transitionNames = new ArrayList<String>();
        for (IfExpr ifExpr : ifs) {
            transitionNames.add(ifExpr.getTransition());
        }
        return transitionNames;
    }

    public String getDefaultTransitionName() {
        for (IfExpr ifExpr : ifs) {
            if (ifExpr.isByDefault()) {
                return ifExpr.getTransition();
            }
        }
        return null;
    }

    private static String normalizeString(String str) {
        while (str.charAt(0) == ' ') {
            str = str.substring(1);
        }
        while (str.charAt(str.length() - 1) == ' ') {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    public void addIfExpr(IfExpr ifExpr) {
        ifs.add(ifExpr);
    }

    public List<IfExpr> getIfExprs() {
        return ifs;
    }

    public IfExpr getIfExpr(String transitionName) {
        for (IfExpr ifExpr : ifs) {
            if (transitionName.equals(ifExpr.getTransition())) {
                return ifExpr;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        IfExpr defaultIf = null;
        for (IfExpr ifExpr : ifs) {
            if (!ifExpr.isByDefault()) {
                buffer.append(ifExpr.generateCode());
            } else {
                defaultIf = ifExpr;
            }
        }
        if (defaultIf != null) {
            buffer.append("\nreturn \"" + defaultIf.getTransition() + "\";\n");
        }
        return buffer.toString();
    }

    public static class IfExpr {
        private Variable firstVariable;
        private Object secondVariable;
        private final Operation operation;
        private final String transition;
        private boolean byDefault;

        public IfExpr(String transition) {
            this.transition = transition;
            this.byDefault = true;
            this.firstVariable = null;
            this.secondVariable = null;
            this.operation = null;
        }

        public IfExpr(String transition, Variable firstVariable, Object secondVariable, Operation operation) {
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

        public void setFirstVariable(Variable variable) {
            this.firstVariable = variable;
        }

        public Object getSecondVariable() {
            return secondVariable;
        }

        public void setSecondVariable(Object secondVariable) {
            this.secondVariable = secondVariable;
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
