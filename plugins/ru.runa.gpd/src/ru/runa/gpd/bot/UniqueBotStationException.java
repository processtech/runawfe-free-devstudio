package ru.runa.gpd.bot;

/**
 * Unique bot station exception
 */
public class UniqueBotStationException extends RuntimeException {
    private static final long serialVersionUID = 835875126805713780L;

    public UniqueBotStationException(String message) {
        super(message);
    }
}
