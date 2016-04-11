package ru.runa.gpd.extension.decision;

import java.util.List;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Strings;

public class GroovyValidationModel {
    public static Expr fromCode(String code, List<Variable> variables) {
        if (Strings.isNullOrEmpty(code)) {
            return null;
        }
        String[] strings = code.split(" ");
        // tmp
        String lexem1Text = "";
        String operator;
        String lexem2Text = "";
        boolean isOperationDateType = false;
        if ((strings.length == 1) || (code.indexOf("\"") > 0)) {
            // i.e. var1.equals(var2) or var1.contains(var2)
            int start;
            if (code.charAt(0) != '!') {
                start = 0;
                if (code.contains("equals")) {
                    operator = "==";
                } else {
                    operator = "contains";
                }
            } else {
                start = 1;
                operator = "!=";
            }
            lexem1Text = code.substring(start, code.indexOf("."));
            lexem2Text = code.substring(code.indexOf("(") + 1, code.length() - 1);
        } else if (strings.length > 3 
                && code.contains(" || ") 
                && code.contains("ru.runa.wfe.commons.CalendarUtil.dateToCalendar")
                && code.endsWith(" 0")) {
            // GroovyTypeSupport.DateType
            isOperationDateType = true;
            String codeWithoutNullCheck = code.substring(code.lastIndexOf(" || ") + " || ".length());
            String[] stringsWithoutNullCheck = codeWithoutNullCheck.split(" ");
            String[] parts = stringsWithoutNullCheck[0].split("\\.compareTo\\(");
            lexem1Text = parts[0].substring(parts[0].lastIndexOf("(") + 1, parts[0].indexOf(")"));
            lexem2Text = parts[1].substring(parts[1].lastIndexOf("(") + 1, parts[1].indexOf(")"));
            operator = stringsWithoutNullCheck[1];
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
        if (lexem1Text.indexOf(".") > 0 && !isOperationDateType) {
            // Java names doesn't allowed use of point in variable name
            lexem1Text = lexem1Text.substring(0, lexem1Text.lastIndexOf("."));
        }
        Variable variable1 = VariableUtils.getVariableByScriptingName(variables, lexem1Text);
        if (variable1 == null) {
            // variable deleted
            return null;
        }
        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable1.getJavaClassName());
        Operation operation = Operation.getByOperator(operator, typeSupport);
        if (operation == null) {
            throw new NullPointerException("operation not found for operator: " + operator);
        }
        if (lexem2Text.indexOf(".") > 0 && !isOperationDateType) {
            // Java names doesn't allowed use of point in variable name
            lexem2Text = lexem2Text.substring(0, lexem2Text.lastIndexOf("."));
        }
        Variable variable2 = VariableUtils.getVariableByScriptingName(variables, lexem2Text);
        if (variable2 == null) {
            return null;
        }
        return new Expr(variable1, variable2, operation);
    }

    public static class Expr {
        private final Variable variable1;
        private final Variable variable2;
        private final Operation operation;

        public Expr(Variable var1, Variable var2, Operation operation) {
            this.variable1 = var1;
            this.variable2 = var2;
            this.operation = operation;
        }

        public String generateCode() {
            return operation.generateCode(variable1, variable2);
        }

        public Variable getVariable1() {
            return variable1;
        }

        public Variable getVariable2() {
            return variable2;
        }

        public Operation getOperation() {
            return operation;
        }
    }
}
