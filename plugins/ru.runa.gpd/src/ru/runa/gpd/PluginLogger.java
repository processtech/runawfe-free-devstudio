package ru.runa.gpd;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.ui.custom.Dialogs;

public class PluginLogger {

    public static void logInfo(String message) {
        log(createStatus(IStatus.INFO, IStatus.OK, message, null));
    }

    public static void logError(Throwable exception) {
        StringBuilder messageBuffer = new StringBuilder();
        messageBuffer.append(exception.getMessage());
        messageBuffer.append(" (").append(exception.getClass().getName()).append(")");
        logError(messageBuffer.toString(), exception);
    }

    public static void logError(String message, Throwable exception) {
        logErrorWithoutDialog(message, exception);
        if (exception instanceof CoreException) {
            IStatus status = ((CoreException) exception).getStatus();
            // org.eclipse.core.resources
            if ("org.eclipse.core.filesystem".equals(status.getPlugin())) {
                message += "\n" + Localization.getString("error.org.eclipse.core.filesystem");
            }
        }
        if (Display.getCurrent() != null) {
            Dialogs.error(message, exception);
        }
    }

    public static void logErrorWithoutDialog(String message, Throwable exception) {
        log(createStatus(IStatus.ERROR, IStatus.OK, message, exception));
    }

    public static void logErrorWithoutDialog(String message) {
        log(createStatus(IStatus.ERROR, message));
    }

    private static IStatus createStatus(int severity, int code, String message, Throwable exception) {
        return new Status(severity, Activator.getDefault().getBundle().getSymbolicName(), code, message, exception);
    }

    private static IStatus createStatus(int severity, String message) {
        return new Status(severity, Activator.getDefault().getBundle().getSymbolicName(), message);
    }

    private static void log(IStatus status) {
        Activator.getDefault().getLog().log(status);
    }

    public static IStatus createStatus(Throwable exception) {
        return createStatus(IStatus.ERROR, IStatus.OK, exception.getMessage(), exception);
    }

}
