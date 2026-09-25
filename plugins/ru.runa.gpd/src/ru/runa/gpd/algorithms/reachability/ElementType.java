package ru.runa.gpd.algorithms.reachability;

public enum ElementType {
    START(ElementCategory.SLOW),
    TASK(ElementCategory.SLOW),
    SUBPROCESS(ElementCategory.SLOW),
    CATCH_EVENT(ElementCategory.SLOW),
    ASYNC_TASK(ElementCategory.FAST),
    ASYNC_SUBPROCESS(ElementCategory.FAST),
    AUTOMATIC_TASK(ElementCategory.FAST),
    THROW_EVENT(ElementCategory.FAST),
    EXCLUSIVE_GATEWAY(ElementCategory.FAST),
    PARALLEL_GATEWAY(ElementCategory.INSTANT),
    END(ElementCategory.INSTANT),
    END_TOKEN(ElementCategory.INSTANT);

    private final ElementCategory category;

    ElementType(ElementCategory category) {
        this.category = category;
    }

    public ElementCategory getCategory() {
        return category;
    }

    public boolean isInstant() {
        return category == ElementCategory.INSTANT;
    }
}
