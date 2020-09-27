package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.List;
import java.util.stream.Stream;

public abstract class ConstraintsPredicate<X, Y> {
    protected X left;
    protected PredicateOperationType type;
    protected Y right;
    protected ConstraintsPredicate<?, ?> parent;

    public ConstraintsPredicate() {
    }

    public ConstraintsPredicate(X left, PredicateOperationType type, Y right) {
        this.left = left;
        this.type = type;
        this.right = right;
    }

    public ConstraintsPredicate(X left, PredicateOperationType type, Y right, ConstraintsPredicate<?, ?> parent) {
        this(left, type, right);
        this.parent = parent;
    }

    public X getLeft() {
        return left;
    }

    public void setLeft(X left) {
        this.left = left;
    }

    public PredicateOperationType getType() {
        return type;
    }

    public void setType(PredicateOperationType type) {
        this.type = type;
    }

    public Y getRight() {
        return right;
    }

    public void setRight(Y right) {
        this.right = right;
    }

    public Stream<String> applicableOperationTypeNames() {
        return getApplicableOperationTypes().stream().map(type -> type.code);
    }

    public ConstraintsPredicate<?, ?> getParent() {
        return parent;
    }

    public void setParent(ConstraintsPredicate<?, ?> parent) {
        this.parent = parent;
    }

    public abstract List<PredicateOperationType> getApplicableOperationTypes();
    
    public abstract boolean isComplete();
}
