package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;
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
