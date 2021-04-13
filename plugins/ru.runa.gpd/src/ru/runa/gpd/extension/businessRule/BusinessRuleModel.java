package ru.runa.gpd.extension.businessRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.extension.decision.GroovyTypeSupport;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.extension.decision.GroovyDecisionModel.IfExpr;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class BusinessRuleModel {
    private final List<IfExpr> ifs = new ArrayList<IfExpr>();
    private static Pattern IF_PATTERN = Pattern.compile("if \\((.*)\\)");
    private static Pattern RETURN_PATTERN = Pattern.compile("return '''(.*?)''';", Pattern.DOTALL);
    private String defaultFunction;

    public BusinessRuleModel() {
    }

    public BusinessRuleModel(String code, List<Variable> variables) throws Exception {
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        Matcher matcher = IF_PATTERN.matcher(code);
        int startReturnSearch = 0;
        while (matcher.find()) {
            String function;
            String ifContent = normalizeString(matcher.group(1));
            String[] strings = ifContent.split(" ");
            // tmp
            String lexem1Text = "";
            String operator;
            String lexem2Text = "";
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
                lexem1Text = ifContent.substring(start, ifContent.indexOf("."));
                lexem2Text = ifContent.substring(ifContent.indexOf("(") + 1, ifContent.length() - 1);
            } else if (strings.length > 3 && ifContent.contains(" || ") && ifContent.contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar")
                    && ifContent.endsWith(" 0")) {
                // GroovyTypeSupport.DateType
                isOperationDateType = true;
                String ifContentWithoutNullCheck = ifContent.substring(ifContent.lastIndexOf(" || ") + " || ".length());
                String[] stringsWithoutNullCheck = ifContentWithoutNullCheck.split(" ");
                String[] parts = stringsWithoutNullCheck[0].split("\\.compareTo\\(");
                lexem1Text = parts[0].substring(parts[0].lastIndexOf("(") + 1, parts[0].indexOf(")"));
                lexem2Text = parts[1].substring(parts[1].lastIndexOf("(") + 1, parts[1].indexOf(")"));
                operator = stringsWithoutNullCheck[1];
            } else if (strings.length > 3 && ifContent.contains(" || ") && ifContent.contains("BigDecimal") && ifContent.endsWith(" 0")) {
                // GroovyTypeSupport.BigDecimalType
                isOperationDateType = false;
                String ifContentWithoutNullCheck = ifContent.substring(ifContent.lastIndexOf(" || ") + " || ".length());
                String[] stringsWithoutNullCheck = ifContentWithoutNullCheck.split(" ");
                lexem1Text = stringsWithoutNullCheck[2];
                lexem2Text = stringsWithoutNullCheck[6];
                operator = stringsWithoutNullCheck[9];
            } else {
                lexem1Text = strings[0];
                operator = strings[1];
                if (strings.length == 3) {
                    lexem2Text = strings[2];
                } else {
                    for (int i = 2; i < strings.length; i++) {
                        lexem2Text += " " + strings[i];
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
            if (lexem1Text.indexOf(".") > 0 && !isOperationDateType) {
                // Java names doesn't allowed use of point in variable name
                lexem1Text = lexem1Text.substring(0, lexem1Text.lastIndexOf("."));
            }
            Variable variable1 = VariableUtils.getVariableByScriptingName(variables, lexem1Text);
            if (variable1 == null) {
                // variable deleted
                continue;
            }
            GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
            Operation operation = Operation.getByOperator(operator, typeSupport);
            if (operation == null) {
                throw new RuntimeException("Operation not found for operator: " + operator);
            }
            Object lexem2;
            if (lexem2Text.indexOf(".") > 0 && !isOperationDateType) {
                try {
                    Double.parseDouble(lexem2Text);
                } catch (NumberFormatException e) {
                    // Java names doesn't allowed use of point in variable name
                    lexem2Text = lexem2Text.substring(0, lexem2Text.lastIndexOf("."));
                }
            }
            Variable variable2 = VariableUtils.getVariableByScriptingName(variables, lexem2Text);
            if (variable2 != null) {
                lexem2 = variable2;
            } else if (Operation.VOID.equals(lexem2Text) || Operation.NULL.equals(lexem2Text)) {
                lexem2 = "null";
            } else {
                lexem2 = typeSupport.unwrapValue(lexem2Text);
            }
            IfExpr ifExpr = new IfExpr(function, variable1, lexem2, operation);
            addIfExpr(ifExpr);
        }
        if (returnMatcher.find(startReturnSearch)) {
            defaultFunction = returnMatcher.group(1);
        }
    }

    public static Set<String> getFunctionName(String code) {
        Set<String> result = Sets.newHashSet();
        Matcher returnMatcher = RETURN_PATTERN.matcher(code);
        while (returnMatcher.find()) {
            String functionName = returnMatcher.group(1);
            result.add(functionName);
        }
        return result;
    }

    public List<String> getFunctionName() {
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
        private Variable variable1;
        private Object lexem2;
        private final Operation operation;
        private final String function;

        public IfExpr(String function, Variable variable, Object lexem2, Operation operation) {
            this.function = function;
            this.variable1 = variable;
            this.lexem2 = lexem2;
            this.operation = operation;
        }

        public String generateCode() {
            return "if ( " + operation.generateCode(variable1, lexem2) + " ) {\n\treturn '''" + function + "''';\n};\n";
        }

        public Variable getVariable1() {
            return variable1;
        }

        public void setVariable1(Variable variable) {
            this.variable1 = variable;
        }

        public Object getLexem2() {
            return lexem2;
        }

        public void setLexem2(Object lexem2) {
            this.lexem2 = lexem2;
        }

        public String getLexem2TextValue() {
            if (lexem2 instanceof Variable) {
                return ((Variable) lexem2).getScriptingName();
            } else if (lexem2 instanceof String) {
                return (String) lexem2;
            } else {
                throw new IllegalArgumentException("lexem2 class is " + lexem2.getClass().getName());
            }
        }

        public Operation getOperation() {
            return operation;
        }

        //// look
        public String getFunction() {
            return function;
        }
    }
}
