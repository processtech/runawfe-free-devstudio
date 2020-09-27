package ru.runa.gpd.formeditor.ftl.ui.dialog.projection;

import ru.runa.gpd.formeditor.resources.Messages;

public enum Visibility {
    VISIBLE(Messages.getString("visibility.hide")),
    INVISIBLE(Messages.getString("visibility.show"));

    private final String message;

    private Visibility(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
