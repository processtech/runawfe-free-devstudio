package ru.runa.gpd.aspects;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;

public abstract aspect UserActivity {
    
    private static final String USER_ACTIVITY_FOLDER_NAME = "userActivityHistory";
    private static final String WORKBENCH_FOLDER_NAME = ".workbench";
    private static final String USER_ACTIVITY_LOG_EXTENSION = ".ualog"; // (u)ser (a)ctivity log
    private static final DateFormat USER_ACTIVITY_LOG_NAME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private static final String EDITING_ACTION_LINE_FORMAT = "{0} {1}";
    private static final DateFormat EDITING_ACTION_TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss,SSS");

    private static Map<Object, PrintWriter> logRepository = new HashMap<>();
    private static boolean started = false;

    static boolean isStarted() {
        return started;
    }

    public static void startLogging() {
        File historyFolder = new File(Activator.getPreferencesFolder() + File.separator + USER_ACTIVITY_FOLDER_NAME + File.separator + WORKBENCH_FOLDER_NAME);
        if (!historyFolder.exists()) {
            historyFolder.mkdirs();
        }
        started = true;
        startEditingSession(null); // workbench started
    }
    
    public static void stopLogging() {
        for (Object owner : logRepository.keySet()) {
            stopEditingSession(owner);
        }
        started = false;
    }
    
    static void startEditingSession(Object owner) {
        if (started) {
            PrintWriter pw = null;
            try {
                if (owner == null) { // workbench
                    pw = new PrintWriter(new File(
                            Activator.getPreferencesFolder() + File.separator + USER_ACTIVITY_FOLDER_NAME + File.separator + WORKBENCH_FOLDER_NAME
                                    + File.separator + USER_ACTIVITY_LOG_NAME_FORMAT.format(new Date()) + USER_ACTIVITY_LOG_EXTENSION));
                } else { // ProcessEditorBase
                    IFile definitionFile = ((ProcessEditorBase) owner).getDefinitionFile();
                    File historyFolder = new File(Activator.getPreferencesFolder() + File.separator + USER_ACTIVITY_FOLDER_NAME + File.separator
                            + definitionFile.getParent().getFullPath());
                    if (!historyFolder.exists()) {
                        historyFolder.mkdirs();
                    }
                    pw = new PrintWriter(new File(
                            historyFolder + File.separator + USER_ACTIVITY_LOG_NAME_FORMAT.format(new Date()) + USER_ACTIVITY_LOG_EXTENSION));
                    owner = ((ProcessEditorBase) owner).getDefinition();
                }
            } catch (FileNotFoundException e) {
                PluginLogger.logError(e);
            }
            stopEditingSession(owner);
            logRepository.put(owner, pw);
            log(owner, owner == null ? UserAction.WS_Open.asString() : UserAction.ES_Open.asString());
        }
    }

    static void stopEditingSession(Object owner) {
        log(owner, owner == null ? UserAction.WS_Close.asString() : UserAction.ES_Close.asString());
        PrintWriter pw = logRepository.get(owner);
        if (pw != null) {
            pw.close();
        }
        logRepository.remove(owner);
    }

    static void logWorkbench(String info) {
        log(null, info);
    }

    static void log(Object owner, String info) {
        if (started) {
            PrintWriter pw = logRepository.get(owner);
            if (pw != null) {
                pw.println(MessageFormat.format(EDITING_ACTION_LINE_FORMAT, EDITING_ACTION_TIMESTAMP_FORMAT.format(new Date()), info));
                pw.flush();
            }
        }
    }

    public static Map<String, File> getLogs(IFolder processDefinitionFolder) {
        Map<String, File> uaLogs = new TreeMap<>((k1, k2) -> {
            return k2.compareTo(k1);
        });
        File historyFolder = new File(Activator.getPreferencesFolder() + File.separator + USER_ACTIVITY_FOLDER_NAME + File.separator
                + processDefinitionFolder.getFullPath());
        if (historyFolder.exists()) {
            for (File uaLog : historyFolder.listFiles()) {
                String label = uaLog.getName();
                label = label.substring(0, label.length() - USER_ACTIVITY_LOG_EXTENSION.length());
                uaLogs.put(label, uaLog);
            }
        }
        return uaLogs;
    }

}
