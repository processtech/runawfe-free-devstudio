package ru.runa.gpd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ru.runa.gpd.PluginLogger;

/**
 * Helper for problem in executing external processes
 */
public class Streamer extends Thread {
    private InputStream inputStream;

    public Streamer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                PluginLogger.logInfo(line);
            }
        } catch (IOException ioe) {
            PluginLogger.logError(ioe);
        }
    }
}
