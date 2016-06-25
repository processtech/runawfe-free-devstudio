package ru.runa.gpd.bot;

/**
 * Unique bot exception
 */
public class UniqueBotException extends RuntimeException {
    private static final long serialVersionUID = -8902988376614757320L;

    public UniqueBotException(String message) {
        super(message);
    }
}
