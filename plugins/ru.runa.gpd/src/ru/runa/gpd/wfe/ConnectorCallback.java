package ru.runa.gpd.wfe;

public interface ConnectorCallback {

    public void onSynchronizationCompleted();

    public void onSynchronizationFailed(Exception e);

}
