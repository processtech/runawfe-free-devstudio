package ru.runa.gpd.sync;

public interface Connector {

    public boolean isConfigured();

    public void connect() throws Exception;

    public void disconnect() throws Exception;

}
