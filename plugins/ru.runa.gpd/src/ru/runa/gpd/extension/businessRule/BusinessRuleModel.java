package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.extension.decision.GroovyModel;
import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class BusinessRuleModel implements GroovyModel {
    private final List<IfExpr> ifs = new ArrayList<IfExpr>();
    private static Pattern IF_PATTERN = Pattern.compile("if \\((.*)\\)");
    private static Pattern RETURN_PATTERN = Pattern.compile("return '''(.*?)''';", Pattern.DOTALL);
    private static Pattern LOGIC_PATTERN = Pattern.compile("(\\|\\||&&)");
    private String defaultFunction;

    public BusinessRuleModel() {
    }

    public BusinessRuleModel(String code, List<Variable> variables) throws Exception {
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        Matcher matcher = IF_PATTERN.matcher(code);
        int startReturnSearch = 0;
        while (matcher.find()) {
            Matcher logicMatcher = LOGIC_PATTERN.matcher(matcher.group(1));
            String function = null;
            List<Variable> firstVariables = new ArrayList();
            List<Object> secondVariables = new ArrayList();
            List<Operation> operations = new ArrayList();
            List<String> logicExpressions = new ArrayList();
            logicExpressions.add(BusinessRuleEditorDialog.NULL_LOGIC_EXPRESSION);
            String[] ifc = matcher.group(1).split("(\\|\\||&&)");

            boolean flag = false;
            ArrayList<Integer> indexes = new ArrayList();
            if (matcher.group(1).contains("BigDecimal") || matcher.group(1).contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar")) {
                flag = true;
                for (int i = 0; i < ifc.length; i++) {
                    if (ifc[i].contains("BigDecimal") || ifc[i].contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar")) {
                        indexes.add(i);
                    }
                }
            }

            for (int i = 0; i < ifc.length; i++) {
                if (logicMatcher.find() && (!indexes.contains(i + 1) && !indexes.contains(i + 2))) {
                    if (logicMatcher.group().equals("||")) {
                        logicExpressions.add(BusinessRuleEditorDialog.OR_LOGIC_EXPRESSION);
                    }
                    if (logicMatcher.group().equals("&&")) {
                        logicExpressions.add(BusinessRuleEditorDialog.AND_LOGIC_EXPRESSION);
                    }
                }
            }

            for (int j = 0; j < ifc.length; j++) {
                if (flag && (indexes.contains(j + 1) || indexes.contains(j + 2))) {
                    continue;
                }
                String ifContent = normalizeString(ifc[j]);
                String[] strings = ifContent.split(" ");
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
                } else if (ifContent.contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar") && ifContent.endsWith(" 0")) {
                    // GroovyTypeSupport.DateType
                    isOperationDateType = true;
                    String[] parts = strings[0].split("\\.compareTo\\(");
                    firstVariableText = parts[0].substring(parts[0].lastIndexOf("(") + 1, parts[0].indexOf(")"));
                    secondVariableText = parts[1].substring(parts[1].lastIndexOf("(") + 1, parts[1].indexOf(")"));
                    operator = strings[1];
                } else if (ifContent.contains("BigDecimal") && ifContent.endsWith(" 0")) {
                    // GroovyTypeSupport.BigDecimalType
                    isOperationDateType = false;
                    firstVariableText = strings[2];
                    secondVariableText = strings[6];
                    operator = strings[9];
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
                    function = returnMatcher.group(1);
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
                if (firstVariables.size() == 0) {
                    firstVariables.add(firstVariable);
                    secondVariables.add(secondVariable);
                    operations.add(operation);
                    firstVariables.add(firstVariable);
                    secondVariables.add(secondVariable);
                    operations.add(operation);
                } else {
                    firstVariables.add(firstVariable);
                    secondVariables.add(secondVariable);
                    operations.add(operation);
                }
            }
            logicExpressions.add(BusinessRuleEditorDialog.NULL_LOGIC_EXPRESSION);
            IfExpr ifExpr = new IfExpr(function, firstVariables, secondVariables, operations, logicExpressions);
            addIfExpr(ifExpr);
        }
        if (returnMatcher.find(startReturnSearch)) {
            defaultFunction = returnMatcher.group(1);
        }
    }

    public static Set<String> getFunctionNames(String code) {
        Set<String> result = Sets.newHashSet();
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        while (returnMatcher.find()) {
            String functionName = returnMatcher.group(1);
            result.add(functionName);
        }
        return result;
    }

    public List<String> getFunctionNames() {
        List<String> functionName = new ArrayList<String>();
        for (IfExpr ifExpr : ifs) {
            functionName.add(ifExpr.getFunction());
        }
        return functionName;
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

    public IfExpr getIfExpr(String functionName) {
        for (IfExpr ifExpr : ifs) {
            if (functionName.equals(ifExpr.getFunction())) {
                return ifExpr;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (IfExpr ifExpr : ifs) {
            buffer.append(ifExpr.generateCode());
        }
        if (!Strings.isNullOrEmpty(defaultFunction)) {
            buffer.append("\nreturn '''" + defaultFunction + "''';\n");
        }
        return buffer.toString();
    }

    public String getDefaultFunction() {
        return defaultFunction;
    }

    public void setDefaultFunction(String defaultFunction) {
        this.defaultFunction = defaultFunction;
    }

    public static class IfExpr {
        private List<Variable> firstVariables;
        private List<Object> secondVariables;
        private List<String> logicExpressions;
        private final List<Operation> operations;
        private final String function;

        public IfExpr(String function, List<Variable> firstVariables, List<Object> secondVariables, List<Operation> operations,
                List<String> logicExpressions) {
            this.function = function;
            this.firstVariables = firstVariables;
            this.secondVariables = secondVariables;
            this.operations = operations;
            this.logicExpressions = logicExpressions;
        }

        public String generateCode() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("if ( ");
            for (int i = 1; i < firstVariables.size(); i++) {
                buffer.append(operations.get(i).generateCode(firstVariables.get(i), secondVariables.get(i)));
                if (!logicExpressions.get(i).equals(BusinessRuleEditorDialog.NULL_LOGIC_EXPRESSION)) {
                    if (logicExpressions.get(i).equals(BusinessRuleEditorDialog.OR_LOGIC_EXPRESSION)) {
                        buffer.append(" " + "||" + " ");
                    }
                    if (logicExpressions.get(i).equals(BusinessRuleEditorDialog.AND_LOGIC_EXPRESSION)) {
                        buffer.append(" " + "&&" + " ");
                    }
                }
            }
            buffer.append(" ) {\n\treturn '''" + function + "''';\n};\n");
            return buffer.toString();
        }

        public List<Variable> getFirstVariables() {
            return firstVariables;
        }

        public List<Object> getSecondVariables() {
            return secondVariables;
        }

        public String getSecondVariableTextValue(int index) {
            if (secondVariables.get(index) instanceof Variable) {
                return ((Variable) secondVariables.get(index)).getScriptingName();
            } else if (secondVariables.get(index) instanceof String) {
                return (String) secondVariables.get(index);
            } else {
                throw new IllegalArgumentException("secondVariable class is " + secondVariables.get(index).getClass().getName());
            }
        }

        public List<Operation> getOperations() {
            return operations;
        }

        public List<String> getLogicExpressions() {
            return logicExpressions;
        }

        public String getFunction() {
            return function;
        }
    }
}
