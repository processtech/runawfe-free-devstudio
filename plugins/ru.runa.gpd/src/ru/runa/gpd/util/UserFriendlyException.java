package ru.runa.gpd.util;

public class UserFriendlyException extends Exception {
    // исключение, текст которого понятен пользователю, и поэтому, например, этот текст
    // можно отобразить в GUI
    private String localizedMessage = null;

    public UserFriendlyException(String message) {
        super(message);
    }

    public UserFriendlyException(String message, Throwable cause) {
        super(message, cause);
    }

    public void setLocalizedMessage(String message) {
        this.localizedMessage = message;
    }

    @Override
    public String getLocalizedMessage() {
        return localizedMessage;
    }
}