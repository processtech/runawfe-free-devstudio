package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;

import ru.runa.gpd.extension.businessRule.LogicComposite;
import ru.runa.gpd.lang.model.Variable;

public class GroovyDecisionModel extends GroovyModel {
    private final List<IfExpression> ifExpressions = new ArrayList<>();

    public GroovyDecisionModel() {
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
    	private List<Variable> firstVariables;
        private List<Object> secondVariables;
        private List<String> logicExpressions;
        private final List<Operation> operations;
        private List<int[]> brackets;
        private final String transition;
        private boolean byDefault;


        public IfExpression(String transition, List<Variable> firstVariables, List<Object> secondVariables, List<Operation> operations,
                List<String> logicExpressions, List<int[]> brackets) {
            this.transition = transition;
            this.firstVariables = firstVariables;
            this.secondVariables = secondVariables;
            this.operations = operations;
            this.logicExpressions = logicExpressions;
            this.brackets = brackets;
        }
        
        public static String normalizeReturnString(String returnStr) {
            if (returnStr.contains("\"")) {
                return returnStr.replaceAll("\"", "\\\\\"");
            }
            return returnStr;
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
            buffer.append(" ) {\n\treturn \"" + normalizeReturnString(transition) + "\";\n};\n");
            return buffer.toString();
        }

        public boolean isByDefault() {
            return byDefault;
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

        public List<int[]> getBrackets() {
            return brackets;
        }

        public String getTransition() {
            return transition;
        }
    }
}
