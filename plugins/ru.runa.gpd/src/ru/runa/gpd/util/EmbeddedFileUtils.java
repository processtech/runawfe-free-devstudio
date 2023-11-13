package ru.runa.gpd.util;

import com.google.common.base.Strings;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.var.file.FileVariable;

public class EmbeddedFileUtils {

    public static IFile getProcessFile(String fileName) {
        return IOUtils.getFile(fileName);
    }

    public static boolean isProcessFile(String path) {
        return path != null && path.startsWith(FileDataProvider.PROCESS_FILE_PROTOCOL);
    }

    public static String getProcessFileName(String path) {
        return path.substring(FileDataProvider.PROCESS_FILE_PROTOCOL.length());
    }

    public static String getProcessFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return FileDataProvider.PROCESS_FILE_PROTOCOL + fileName;
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

    public static String copyProcessFile(IFolder sourceFolder, Delegable source, String path, IFolder targetFolder, Delegable target)
            throws Exception {
        String sourceFileName = EmbeddedFileUtils.getProcessFileName(path);
        IFile file = IOUtils.getFile(sourceFolder, sourceFileName);
        if (file.exists()) {
            String targetFileName = sourceFileName.replaceAll(((GraphElement) source).getId(), ((GraphElement) target).getId());
            IFile targetFile = IOUtils.getFile(targetFolder, targetFileName);
            file.copy(targetFile.getFullPath(), true, null);
            return EmbeddedFileUtils.getProcessFilePath(targetFileName);
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
        return path != null && path.startsWith(FileDataProvider.BOT_TASK_FILE_PROTOCOL);
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
        return path.replace(FileDataProvider.BOT_TASK_FILE_PROTOCOL, "");
    }

    public static String getBotTaskFilePath(String fileName) {
        if (!Strings.isNullOrEmpty(fileName)) {
            return FileDataProvider.BOT_TASK_FILE_PROTOCOL + fileName;
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
