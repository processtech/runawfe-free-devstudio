package ru.runa.gpd.lang.model;

import ru.runa.wfe.lang.AsyncCompletionMode;

public interface Synchronizable {
    public boolean isAsync();

    public void setAsync(boolean async);

    public AsyncCompletionMode getAsyncCompletionMode();

    public void setAsyncCompletionMode(AsyncCompletionMode completionMode);
}
