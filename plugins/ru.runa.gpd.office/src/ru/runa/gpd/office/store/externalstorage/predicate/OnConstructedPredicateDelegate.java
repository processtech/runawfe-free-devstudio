package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.List;
import java.util.stream.Stream;

public class OnConstructedPredicateDelegate<X, Y> extends ConstraintsPredicate<X, Y> {
    private final Runnable onConstructed;
    private final ConstraintsPredicate<X, Y> delegate;

    public OnConstructedPredicateDelegate(ConstraintsPredicate<X, Y> delegate, Runnable onConstructed) {
        this.delegate = delegate;
        this.onConstructed = onConstructed;
    }

    @SuppressWarnings("unchecked")
    public OnConstructedPredicateDelegate(ExpressionPredicate<?> delegate, Runnable onConstructed) {
        this.delegate = (ConstraintsPredicate<X, Y>) delegate;
        this.onConstructed = onConstructed;
    }

    @Override
    public Stream<String> applicableOperationTypeNames() {
        return delegate.applicableOperationTypeNames();
    }

    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public X getLeft() {
        return delegate.getLeft();
    }

    @Override
    public void setLeft(X left) {
        delegate.setLeft(left);
        produceConstructed();
    }

    @Override
    public PredicateOperationType getType() {
        return delegate.getType();
    }

    @Override
    public void setType(PredicateOperationType type) {
        delegate.setType(type);
        produceConstructed();
    }

    @Override
    public Y getRight() {
        return delegate.getRight();
    }

    @Override
    public void setRight(Y right) {
        delegate.setRight(right);
        produceConstructed();
    }

    public void produceConstructed() {
        if (getLeft() != null && getType() != null && getRight() != null) {
            onConstructed.run();
        }
    }

    public ConstraintsPredicate<X, Y> getDelegate() {
        return delegate;
    }

    @Override
    public List<PredicateOperationType> getApplicableOperationTypes() {
        return delegate.getApplicableOperationTypes();
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    @Override
    public ConstraintsPredicate<?, ?> getParent() {
        return delegate.getParent();
    }

    @Override
    public void setParent(ConstraintsPredicate<?, ?> parent) {
        delegate.setParent(parent);
    }

    @Override
    public boolean isComplete() {
        return delegate.isComplete();
    }

}
