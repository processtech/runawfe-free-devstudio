package ru.runa.gpd.formeditor.wysiwyg;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class ConnectorServletHelper {
    private static File baseDir;
    private static List<String> synchronizations = new ArrayList<String>();

    public static void sync() {
        try {
            if (baseDir == null || !baseDir.exists()) {
                return;
            }
            File formCssFile = new File(WebServerUtils.getEditorDirectory(), ParContentProvider.FORM_CSS_FILE_NAME);
            if (formCssFile.exists()) {
                formCssFile.delete();
            }
            File[] resourceFiles = baseDir.listFiles(new ConnectorServletHelper.FileExtensionFilter());
            for (File file : resourceFiles) {
                if (!file.isDirectory() && !synchronizations.contains(file.getAbsolutePath())) {
                    IOUtils.copyFileToDir(file, WebServerUtils.getEditorDirectory());
                    synchronizations.add(file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            PluginLogger.logError(e);
        }
    }

    public static File getBaseDir() {
        return baseDir;
    }

    public static void setBaseDir(String dir) {
        baseDir = new File(dir);
    }

    static class FileExtensionFilter implements FilenameFilter {
        private final List<String> extensionExceptions = Arrays.asList("ftl", "xml", "quick", "template", "js");

        @Override
        public boolean accept(File dir, String name) {
            int index = name.lastIndexOf(".");
            if (index == -1) {
                return false;
            }
            return !extensionExceptions.contains(name.substring(index + 1));
        }
    }
}
