package ru.runa.gpd.ui.enhancement;

import ru.runa.gpd.lang.model.ConditionalEventModel;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.StorageAware;

public final class ConditionalEventDelegableAdapters {

    private ConditionalEventDelegableAdapters() {
    }

    @SuppressWarnings("unchecked")
    public static <N extends Node & Delegable & StorageAware>
    ConditionalEventExpressionDelegableAdapter<N> adaptToExpression(Delegable delegable) {
        return new ConditionalEventExpressionDelegableAdapter<>((N) delegable);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Node & Delegable & StorageAware>
    ConditionalEventExpressionDelegableAdapter<N> adaptToExpression(Delegable delegable, ConditionalEventModel model) {
        return new ConditionalEventExpressionDelegableAdapter<>((N) delegable, model);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Node & Delegable & StorageAware>
    ConditionalEventStorageDelegableAdapter<N> adaptToStorage(Delegable delegable) {
        return new ConditionalEventStorageDelegableAdapter<>((N) delegable);
    }

    @SuppressWarnings("unchecked")
    public static <N extends Node & Delegable & StorageAware>
    ConditionalEventStorageDelegableAdapter<N> adaptToStorage(Delegable delegable, ConditionalEventModel model) {
        return new ConditionalEventStorageDelegableAdapter<>((N) delegable, model);
    }
}
