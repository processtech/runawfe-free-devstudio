package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

class ExpressionPredicate<X extends ConstraintsPredicate<?, ?>> extends ConstraintsPredicate<X, VariablePredicate> {

    public ExpressionPredicate() {
    }

    public ExpressionPredicate(X left, PredicateOperationType type, VariablePredicate right) {
        super(left, type, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpressionPredicate) {
            return Objects.equals(((ExpressionPredicate<?>) obj).getLeft(), left) && Objects.equals(((ExpressionPredicate<?>) obj).getType(), type)
                    && Objects.equals(((ExpressionPredicate<?>) obj).getRight(), right);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (left != null) {
            builder.append(' ');
            builder.append(left.toString());
            builder.append(' ');
        }
        if (type != null) {
            builder.append(type.code);
            builder.append(' ');
        }
        if (right != null) {
            builder.append(right.toString());
        }
        return builder.toString();
    }

    @Override
    public List<PredicateOperationType> getApplicableOperationTypes() {
        return Arrays.asList(PredicateOperationType.AND, PredicateOperationType.OR);
    }

}
