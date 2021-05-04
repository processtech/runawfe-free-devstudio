package ru.runa.gpd.util;

import com.google.common.base.Strings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.var.file.FileVariable;

public class EmbeddedFileUtils {

    public static IFile getProcessFile(String fileName) {
        return IOUtils.getFile(fileName);
    }

    public static boolean isProcessFile(String path) {
        return path != null && path.startsWith(IFileDataProvider.PROCESS_FILE_PROTOCOL);
    }

    public static String getProcessFileName(String path) {
        return path.substring(IFileDataProvider.PROCESS_FILE_PROTOCOL.length());
    }

    public static String getProcessFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return IFileDataProvider.PROCESS_FILE_PROTOCOL + fileName;
        }
        return fileName;
    }

    public static boolean isFileVariableClassName(String className) {
        return FileVariable.class.getName().equals(className);
    }

    public static void deleteProcessFile(String path) {
        if (isProcessFile(path)) {
            String fileName = getProcessFileName(path);
            deleteProcessFile(getProcessFile(fileName));
        }
    }

    public static String copyProcessFile(IFolder sourceFolder, String path, String oldName, String newName) {
        String fileName = EmbeddedFileUtils.getProcessFileName(path);
        IFile file = IOUtils.getFile(sourceFolder, fileName);
        if (file.exists()) {
            fileName = fileName.replace(oldName, newName);
            path = EmbeddedFileUtils.getProcessFilePath(fileName);
            IFile newFile = EmbeddedFileUtils.getProcessFile(path);
            try {
                file.copy(newFile.getFullPath(), true, null);
                return path;
            } catch (CoreException e) {
                PluginLogger.logError("Unable to copy file " + file + " from process definition", e);
            }
        }
        return null;
    }

    public static void deleteProcessFile(IFile file) {
        if (file.exists()) {
            try {
                file.delete(true, null);
            } catch (CoreException e) {
                PluginLogger.logError("Unable to delete file " + file + " from process definition", e);
            }
        }
    }

    public static IFile getBotTaskFile(String fileName) {
        return IOUtils.getFile(fileName);
    }

    public static boolean isBotTaskFile(String path) {
        return path != null && path.startsWith(IFileDataProvider.BOT_TASK_FILE_PROTOCOL);
    }

    public static boolean isBotTaskFileName(String fileName, String botTaskName) {
        String botTaskNameWithoutSpaces = generateBotTaskEmbeddedFileName(botTaskName);
        if (fileName.startsWith(botTaskNameWithoutSpaces + BotTaskUtils.EMBEDDED_SUFFIX)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getBotTaskFileName(String path) {
        if (!isBotTaskFile(path)) {
            throw new IllegalArgumentException(path);
        }
        return path.replace(IFileDataProvider.BOT_TASK_FILE_PROTOCOL, "");
    }

    public static String getBotTaskFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return IFileDataProvider.BOT_TASK_FILE_PROTOCOL + fileName;
        }
        return fileName;
    }

    public static void deleteBotTaskFile(String path) {
        if (isBotTaskFile(path)) {
            String fileName = getBotTaskFileName(path);
            deleteBotTaskFile(getBotTaskFile(fileName));
        }
    }

    public static void deleteBotTaskFile(IFile file) {
        if (file.exists()) {
            try {
                file.delete(true, null);
            } catch (CoreException e) {
                PluginLogger.logError("Unable to delete file " + file + " from bot task", e);
            }
        }
    }

    public static String generateEmbeddedFileName(Delegable delegable, String fileExtension) {
        if (delegable instanceof GraphElement) {
            String id = ((GraphElement) delegable).getId();
            return id + ".template." + fileExtension;
        }
        if (delegable instanceof BotTask) {
            String name = ((BotTask) delegable).getName();
            name = generateBotTaskEmbeddedFileName(name);
            return name + BotTaskUtils.EMBEDDED_SUFFIX + "." + fileExtension;
        }
        return null;
    }

    public static String generateBotTaskEmbeddedFileName(String botTaskName) {
        return botTaskName.replace(' ', '_');
    }

}
