package ru.runa.gpd.extension.decision;

import java.util.ArrayList;
import java.util.List;
import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.decision.GroovyTypeSupport.BooleanType;
import ru.runa.gpd.lang.model.Variable;

public class Operation {
    private static final List<Operation> OPERATIONS_LIST = new ArrayList<Operation>();
    public static final String VOID = "void";
    public static final String NULL = "null";
    public static final Operation EQ = new Eq();
    public static final Operation NOT_EQ = new NotEq();

    static void registerOperation(Operation operation) {
        OPERATIONS_LIST.add(operation);
    }

    static class Eq extends Operation {
        private Eq() {
            super(Localization.getString("Groovy.Operation.equals"), "==");
            registerOperation(this);
        }

        @Override
        public String generateCode(Variable variable, Object lexem2) {
            if (NULL.equals(lexem2)) {
                return variable.getScriptingName() + " == " + NULL;
            }
            if (BooleanType.TRUE.equals(lexem2)) {
                return variable.getScriptingName();
            }
            return super.generateCode(variable, lexem2);
        }
    }

    static class NotEq extends Operation {
        private NotEq() {
            super(Localization.getString("Groovy.Operation.notequals"), "!=");
            registerOperation(this);
        }

        @Override
        public String generateCode(Variable variable, Object lexem2) {
            if (NULL.equals(lexem2)) {
                return variable.getScriptingName() + " != " + NULL;
            }
            if (BooleanType.TRUE.equals(lexem2)) {
                return "!" + variable.getScriptingName();
            }
            return super.generateCode(variable, lexem2);
        }
    }

    private String visibleName;
    private String operator;

    public Operation(String visibleName, String operator) {
        this.visibleName = visibleName;
        this.operator = operator;
    }

    public String getVisibleName() {
        return visibleName;
    }

    public String getOperator() {
        return operator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return operator.hashCode() + 37 * visibleName.hashCode();
    }

    public String generateCode(Variable variable, Object lexem2) {
        GroovyTypeSupport typeSupport = GroovyTypeSupport.get(variable.getJavaClassName());
        StringBuffer buffer = new StringBuffer();
        buffer.append(typeSupport.wrap(variable));
        buffer.append(" ");
        buffer.append(getOperator());
        buffer.append(" ");
        buffer.append(typeSupport.wrap(lexem2));
        return buffer.toString();
    }

    public static List<Operation> getAll(GroovyTypeSupport typeSupport) {
        List<Operation> allWithExt = new ArrayList<Operation>();
        allWithExt.addAll(OPERATIONS_LIST);
        if (typeSupport == null) {
            return null;
        }
        List<Operation> extOperations = typeSupport.getTypedOperations();
        if (extOperations != null) {
            allWithExt.removeAll(extOperations);
            allWithExt.addAll(extOperations);
        }
        return allWithExt;
    }

    public static Operation getByName(String name, GroovyTypeSupport typeSupport) {
        for (Operation operation : getAll(typeSupport)) {
            if (operation.getVisibleName().equals(name)) {
                return operation;
            }
        }
        return null;
    }

    public static Operation getByOperator(String operator, GroovyTypeSupport typeSupport) {
        for (Operation operation : getAll(typeSupport)) {
            if (operation.getOperator().equals(operator)) {
                return operation;
            }
        }
        throw new RuntimeException("Operation not found for operator: " + operator);
    }
}
