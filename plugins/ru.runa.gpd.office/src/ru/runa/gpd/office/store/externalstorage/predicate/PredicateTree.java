package ru.runa.gpd.office.store.externalstorage.predicate;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

public class PredicateTree implements Iterable<ConstraintsPredicate<?, ?>> {
    private ConstraintsPredicate<?, ?> head;

    /**
     * Full count of {@link ExpressionPredicate} and {@link VariablePredicate} in tree
     */
    private int size;

    /**
     * Count of {@link VariablePredictae} in tree
     */
    private int depth;

    @Override
    public Iterator<ConstraintsPredicate<?, ?>> iterator() {
        return fullFlattenedInOrder(size).iterator();
    }

    public void setHead(ConstraintsPredicate<?, ?> head) {
        if (head == null) {
            clear();
            return;
        }

        this.head = head;
        final List<VariablePredicate> flattenedInOrder = flattenedInOrder();
        depth = flattenedInOrder.size();
        size = depth == 1 ? 1 : depth + (depth - 1);
    }

    public int size() {
        return size;
    }

    public int depth() {
        return depth;
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int add(VariablePredicate e, Consumer<ExpressionPredicate<?>> onNewExpression) {
        Preconditions.checkNotNull(e);
        Preconditions.checkNotNull(onNewExpression);

        if (head == null) {
            head = e;
            depth = 1;
            size = 1;
            return 0;
        }

        final ExpressionPredicate<?> expression = new ExpressionPredicate<>(head, PredicateOperationType.AND, e);
        head.setParent(expression);
        e.setParent(expression);
        head = expression;
        onNewExpression.accept(expression);

        depth += 1;
        size += 2;
        return depth - 1;
    }

    @SuppressWarnings("unchecked")
    public void removeVariablePredicateBy(int index) {
        if (head == null || index >= depth || index < 0) {
            throw new IndexOutOfBoundsException();
        }

        final VariablePredicate predicate = flattenedInOrder(depth).get(index);

        // remove if tree contains 1 variable predicate, i.e. [a = 1];
        if (predicate.equals(head)) {
            clear();
            return;
        }

        // remove if tree contains 1 expression predicate, i.e. [a = 1 and b = 2]
        if (depth == 2) {
            final ConstraintsPredicate<?, ?> old = head;
            if (head.getLeft().equals(predicate)) {
                head = (ConstraintsPredicate<?, ?>) head.getRight();
            } else {
                head = (ConstraintsPredicate<?, ?>) head.getLeft();
            }
            old.setLeft(null);
            old.setRight(null);
            head.setParent(null);
        } else if (predicate.getParent().getParent() == null) { // remove from top
            final ConstraintsPredicate<?, ?> old = head;
            head = (ConstraintsPredicate<?, ?>) old.getLeft();
            head.setParent(null);
            old.setLeft(null);
            old.setRight(null);
            predicate.setParent(null);
        } else { // remove from middle
            final ExpressionPredicate<ConstraintsPredicate<?, ?>> parent = (ExpressionPredicate<ConstraintsPredicate<?, ?>>) predicate.getParent();
            ((ExpressionPredicate<ConstraintsPredicate<?, ?>>) parent.getParent())
                    .setLeft(predicate.equals(predicate.getParent().getLeft()) ? parent.getRight() : parent.getLeft());
            parent.setParent(null);
            parent.setLeft(null);
            parent.setRight(null);
            predicate.setParent(null);
        }

        size -= 2;
        depth -= 1;
    }

    public void clear() {
        head = null;
        size = 0;
        depth = 0;
    }

    public ConstraintsPredicate<?, ?> head() {
        return head;
    }

    private List<VariablePredicate> flattenedInOrder() {
        return flattenedInOrder(-1);
    }

    @SuppressWarnings("unchecked")
    private List<VariablePredicate> flattenedInOrder(int capacity) {
        if (head instanceof VariablePredicate) {
            return Collections.singletonList((VariablePredicate) head);
        }

        final List<VariablePredicate> result = capacity == -1 ? new ArrayList<>() : new ArrayList<>(capacity);
        final Queue<ExpressionPredicate<ConstraintsPredicate<?, ?>>> queue = new ArrayDeque<>();
        queue.offer((ExpressionPredicate<ConstraintsPredicate<?, ?>>) head);

        while (queue.peek() != null) {
            final ExpressionPredicate<ConstraintsPredicate<?, ?>> predicate = queue.poll();

            if (predicate.getRight() == null) {
                predicate.setRight(new VariablePredicate(predicate));
            }
            result.add(predicate.getRight());

            if (predicate.getLeft() != null) {
                if (predicate.getLeft() instanceof VariablePredicate) {
                    result.add((VariablePredicate) predicate.getLeft());
                } else {
                    queue.offer((ExpressionPredicate<ConstraintsPredicate<?, ?>>) predicate.getLeft());
                }
            } else {
                predicate.setLeft(new VariablePredicate(predicate));
                result.add((VariablePredicate) predicate.getLeft());
            }
        }

        Collections.reverse(result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<ConstraintsPredicate<?, ?>> fullFlattenedInOrder(int capacity) {
        if (head instanceof VariablePredicate) {
            return Collections.singletonList(head);
        }

        final List<ConstraintsPredicate<?, ?>> result = new ArrayList<>(capacity);
        final Queue<ExpressionPredicate<ConstraintsPredicate<?, ?>>> queue = new ArrayDeque<>();
        queue.offer((ExpressionPredicate<ConstraintsPredicate<?, ?>>) head);

        while (queue.peek() != null) {
            final ConstraintsPredicate<?, ?> predicate = queue.poll();
            if (predicate instanceof VariablePredicate) {
                result.add(predicate);
                continue;
            }

            if (predicate.getRight() != null) {
                result.add((VariablePredicate) predicate.getRight());
            }
            result.add(predicate);
            if (predicate.getLeft() != null) {
                if (predicate.getLeft() instanceof VariablePredicate) {
                    result.add((VariablePredicate) predicate.getLeft());
                } else {
                    queue.offer((ExpressionPredicate<ConstraintsPredicate<?, ?>>) predicate.getLeft());
                }
            }
        }

        Collections.reverse(result);
        return result;
    }

}
