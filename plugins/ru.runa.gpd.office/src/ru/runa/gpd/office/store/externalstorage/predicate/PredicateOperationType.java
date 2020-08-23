package ru.runa.gpd.office.store.externalstorage.predicate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum PredicateOperationType {
    EQUAL("=="),
    NOT_EQUAL("!="),
    GREATER_OR_EQUAL(">="),
    LESS_OR_EQUAL("<="),
    GREATER(">"),
    LESS("<"),
    LIKE("like"),
    AND("and"),
    OR("or");

    public final String code;

    private PredicateOperationType(String code) {
        this.code = code;
    }

    public static List<String> codes() {
        return Stream.of(values()).map(type -> type.code).collect(Collectors.toList());
    }

    public static Optional<PredicateOperationType> byCode(String code) {
        return Stream.of(values()).filter(type -> type.code.equals(code)).findAny();
    }
}
