package ru.runa.gpd.util;

public class ThrowableCauseExtractor extends SafeLoopRunner {
    public Throwable cause;

    public ThrowableCauseExtractor(Throwable cause) {
        this.cause = cause;
    }

    @Override
    protected boolean condition() {
        return cause.getCause() != null;
    }

    @Override
    protected void loop() {
        cause = cause.getCause();
    }

}
