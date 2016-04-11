package ru.runa.gpd.wfe;

public interface IConnector {
    public boolean isConfigured();

    public void connect() throws Exception;

    public void disconnect() throws Exception;
}
