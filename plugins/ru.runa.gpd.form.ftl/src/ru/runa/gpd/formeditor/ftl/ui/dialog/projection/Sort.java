package ru.runa.gpd.formeditor.ftl.ui.dialog.projection;

import java.util.Optional;
import java.util.stream.Stream;
import ru.runa.gpd.formeditor.resources.Messages;

public enum Sort {
    NONE(Messages.getString("sort.none")),
    ASC(Messages.getString("sort.asc")),
    DESC(Messages.getString("sort.desc"));

    private final String message;

    private Sort(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static Stream<String> messages() {
        return Stream.of(NONE, ASC, DESC).map(Sort::getMessage);
    }

    public static Optional<Sort> by(String message) {
        return Stream.of(NONE, ASC, DESC).filter(sort -> sort.getMessage().equals(message)).findAny();
    }
}
