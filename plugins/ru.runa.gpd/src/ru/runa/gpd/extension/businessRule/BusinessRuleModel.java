package ru.runa.gpd.extension.businessRule;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import ru.runa.gpd.extension.decision.GroovyModel;
import ru.runa.gpd.extension.decision.Operation;
import ru.runa.gpd.lang.model.Variable;

public class BusinessRuleModel extends GroovyModel {
    private final List<IfExpression> ifExpressions = new ArrayList<>();
    private String defaultFunction;

    public BusinessRuleModel() {
    }

    public void addIfExpression(IfExpression ifExpression) {
        ifExpressions.add(ifExpression);
    }

    public List<IfExpression> getIfExpressions() {
        return ifExpressions;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        for (IfExpression ifExpression : ifExpressions) {
            buffer.append(ifExpression.generateCode());
        }
        if (!Strings.isNullOrEmpty(defaultFunction)) {
            buffer.append("\nreturn \"" + normalizeReturnString(defaultFunction) + "\";\n");
        }
        return buffer.toString();
    }

    public static String normalizeReturnString(String returnStr) {
        if (returnStr.contains("\"")) {
            return returnStr.replaceAll("\"", "\\\\\"");
        }
        return returnStr;
    }

    public String getDefaultFunction() {
        return defaultFunction;
    }

    public void setDefaultFunction(String defaultFunction) {
        this.defaultFunction = defaultFunction;
    }

    public static class IfExpression {
        private List<Variable> firstVariables;
        private List<Object> secondVariables;
        private List<String> logicExpressions;
        private final List<Operation> operations;
        private List<int[]> brackets;
        private final String function;

        public IfExpression(String function, List<Variable> firstVariables, List<Object> secondVariables, List<Operation> operations,
                List<String> logicExpressions, List<int[]> brackets) {
            this.function = function;
            this.firstVariables = firstVariables;
            this.secondVariables = secondVariables;
            this.operations = operations;
            this.logicExpressions = logicExpressions;
            this.brackets = brackets;
        }

        public String generateCode() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("if ( ");
            for (int i = 0; i < firstVariables.size(); i++) {

                for (int j = 0; j < brackets.get(i)[0]; j++) {
                    buffer.append("(");
                }
                if (brackets.get(i)[0] != 0) {
                    buffer.append(" ");
                }

                buffer.append(operations.get(i).generateCode(firstVariables.get(i), secondVariables.get(i)));

                if (brackets.get(i)[1] != 0) {
                    buffer.append(" ");
                }
                for (int j = 0; j < brackets.get(i)[1]; j++) {
                    buffer.append(")");
                }

                if (!logicExpressions.get(i).equals(LogicComposite.NULL_LOGIC_EXPRESSION)) {
                    if (logicExpressions.get(i).equals(LogicComposite.OR_LOGIC_EXPRESSION)) {
                        buffer.append(" " + "||" + " ");
                    }
                    if (logicExpressions.get(i).equals(LogicComposite.AND_LOGIC_EXPRESSION)) {
                        buffer.append(" " + "&&" + " ");
                    }
                }
            }
            buffer.append(" ) {\n\treturn \"" + normalizeReturnString(function) + "\";\n};\n");
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

        public List<int[]> getBrackets() {
            return brackets;
        }
    }
}
