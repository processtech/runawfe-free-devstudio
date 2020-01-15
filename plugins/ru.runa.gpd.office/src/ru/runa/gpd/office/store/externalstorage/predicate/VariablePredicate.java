package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import ru.runa.gpd.lang.model.Variable;

class VariablePredicate extends ConstraintsPredicate<Variable, Variable> {

    public VariablePredicate() {
    }

    public VariablePredicate(Variable left, PredicateOperationType type, Variable right) {
        super(left, type, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariablePredicate) {
            return Objects.equals(((VariablePredicate) obj).getLeft(), left) && Objects.equals(((VariablePredicate) obj).getRight(), right)
                    && Objects.equals(((VariablePredicate) obj).getType(), type);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (left != null) {
            builder.append('[');
            builder.append(left.getName());
            builder.append(']');
            builder.append(' ');
        }
        if (type != null) {
            builder.append(type.code);
            builder.append(' ');
        }
        if (right != null) {
            builder.append('@');
            builder.append(right.getScriptingName());
            builder.append(' ');
        }
        return builder.toString();
    }

    @Override
    public List<PredicateOperationType> getApplicableOperationTypes() {
        return Arrays.asList(PredicateOperationType.EQUAL, PredicateOperationType.NOT_EQUAL, PredicateOperationType.GREATER_OR_EQUAL,
                PredicateOperationType.LESS_OR_EQUAL, PredicateOperationType.GREATER, PredicateOperationType.LESS, PredicateOperationType.LIKE);
    }
}
