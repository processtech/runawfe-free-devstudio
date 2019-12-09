package ru.runa.gpd.editor;

import com.google.common.base.Throwables;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.wizard.ExportParWizardPage.ParExportOperation;
import ru.runa.gpd.util.IOUtils;

public class ProcessSaveHistory {

    private static final String FOLDER_NAME = "processSaveHistory";
    private static final String SAVEPOINT_EXTENSION = ".par";
    private static final DateFormat SAVEPOINT_SUFFIX_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final DateFormat SAVEPOINT_LABEL_FORMAT = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");

    public static boolean isActive() {
        return Activator.getPrefBoolean(PrefConstants.P_PROCESS_SAVE_HISTORY);
    }

    public static void addSavepoint(IFile processDefinitionFile) throws Exception {
        if (isActive()) {
            File historyFolder = new File(Activator.getPreferencesFolder() + File.separator + FOLDER_NAME + File.separator
                    + processDefinitionFile.getParent().getFullPath());
            if (!historyFolder.exists()) {
                historyFolder.mkdirs();
            }
            List<IFile> resourcesToExport = new ArrayList<IFile>();
            for (IResource resource : processDefinitionFile.getParent().members()) {
                if (resource instanceof IFile) {
                    resourcesToExport.add((IFile) resource);
                }
            }
            String outputFileName = historyFolder + File.separator + processDefinitionFile.getParent().getName() + '_'
                    + SAVEPOINT_SUFFIX_FORMAT.format(new Date()) + SAVEPOINT_EXTENSION;
            new ParExportOperation(resourcesToExport, new FileOutputStream(outputFileName)).run(null);
            File[] savepoints = historyFolder.listFiles();
            if (savepoints != null) {
                int savepointNumber = Activator.getDefault().getPreferenceStore().getInt(PrefConstants.P_PROCESS_SAVEPOINT_NUMBER);
                if (savepoints.length > savepointNumber) {
                    Arrays.sort(savepoints);
                    for (int i = 0; i < savepoints.length - savepointNumber; i++) {
                        savepoints[i].delete();
                    }
                }
            }
        }
    }

    public static Map<String, File> getSavepoints(IFolder processDefinitionFolder) {
        Map<String, File> savepoints = new TreeMap<>((k1, k2) -> {
            return k2.compareTo(k1);
        });
        File historyFolder = new File(
                Activator.getPreferencesFolder() + File.separator + FOLDER_NAME + File.separator + processDefinitionFolder.getFullPath());
        if (historyFolder.exists()) {
            for (File savepoint : historyFolder.listFiles()) {
                try {
                    String label = savepoint.getName();
                    label = label.substring(label.lastIndexOf('_') + 1, label.length() - SAVEPOINT_EXTENSION.length());
                    Date date = SAVEPOINT_SUFFIX_FORMAT.parse(label);
                    savepoints.put(SAVEPOINT_LABEL_FORMAT.format(date), savepoint);
                } catch (ParseException e) {
                    PluginLogger.logError("Unable parse savepoint suffix", e);
                }
            }
        }
        return savepoints;
    }

    public static void restore(File archiveFile, IFolder processDefinitionFolder) {
        try {
            for (IResource resource : processDefinitionFolder.members()) {
                resource.delete(true, null);
            }
            ProcessCache.processDefinitionWasDeleted(IOUtils.getProcessDefinitionFile(processDefinitionFolder));
            try (InputStream is = new FileInputStream(archiveFile);) {
                IOUtils.extractArchiveToFolder(is, processDefinitionFolder);
            }
            processDefinitionFolder.refreshLocal(IResource.DEPTH_ONE, null);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static void clear(IFolder processDefinitionFolder) {
        for (File savepoint : getSavepoints(processDefinitionFolder).values()) {
            savepoint.delete();
        }
    }

    private ProcessSaveHistory() {
    }

}
