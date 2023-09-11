package ru.runa.gpd.extension.decision;

import ru.runa.gpd.lang.model.Variable;

public class GroovyValidationModel {
    private final Variable variable1;
    private final Variable variable2;
    private final Operation operation;

    public GroovyValidationModel(Variable var1, Variable var2, Operation operation) {
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
