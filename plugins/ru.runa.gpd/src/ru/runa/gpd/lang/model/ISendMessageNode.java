package ru.runa.gpd.lang.model;

import ru.runa.gpd.util.Duration;

public interface ISendMessageNode {

    public Duration getTtlDuration();

    public void setTtlDuration(Duration ttlDuration);

}
