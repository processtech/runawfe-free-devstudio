package ru.runa.gpd.ui.custom;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.dialog.InfoWithDetailsDialog;
import ru.runa.gpd.util.ThrowableCauseExtractor;

public class Dialogs {

    private static int open(int dialogType, String title, String message, String details) {
        InfoWithDetailsDialog dialog = new InfoWithDetailsDialog(dialogType, title, message, details);
        return dialog.open();
    }

    // private static int openWithAction(int dialogType, String title, String message, String actionTitle, String details, boolean showDetails) {
    // InfoWithDetailsActionDialog dialog = new InfoWithDetailsActionDialog(dialogType, title, message, actionTitle, details, showDetails);
    // return dialog.open();
    // }

    public static void warning(String message, String details) {
        open(MessageDialog.WARNING, Localization.getString("message.warning"), message, details);
    }

    public static void warning(String message) {
        warning(message, null);
    }

    public static void error(String message, String details) {
        open(MessageDialog.ERROR, Localization.getString("error"), message, details);
    }

    // public static int errorWithAction(String message, String actionTitle, String details, boolean showDetails) {
    // return openWithAction(MessageDialog.ERROR, Localization.getString("error"), message, actionTitle, details, showDetails);
    // }

    public static void error(String message) {
        error(message, (String) null);
    }

    public static void error(String message, Throwable th) {
        StringWriter writer = new StringWriter();
        if (th != null) {
            ThrowableCauseExtractor causeExtractor = new ThrowableCauseExtractor(th);
            causeExtractor.runWhile();
            if (message == null) {
                message = causeExtractor.cause.getMessage();
            }
            if (message == null) {
                message = causeExtractor.cause.getClass().getName();
            }
            causeExtractor.cause.printStackTrace(new PrintWriter(writer));
        }
        error(message, writer.toString());
    }

    public static void information(String message) {
        open(MessageDialog.INFORMATION, Localization.getString("message.information"), message, null);
    }

    // public static int confirmWithAction(String message, String actionTitle, String details, boolean showDetails) {
    // return openWithAction(MessageDialog.CONFIRM, Localization.getString("message.confirm"), message, actionTitle, details, showDetails);
    // }

    public static boolean confirm(String message, String details) {
        return open(MessageDialog.CONFIRM, Localization.getString("message.confirm"), message, details) == Window.OK;
    }

    public static boolean confirm(String message) {
        return confirm(message, null);
    }

    public static DialogBuilder create(String message) {
        return create(MessageDialog.INFORMATION, message);
    }

    public static DialogBuilder create(int dialogType, String message) {
        return new DialogBuilder(dialogType, message);
    }

    public static void showErrorMessage(String message) {
        Dialogs.create(MessageDialog.ERROR, message).andExecute();
    }
}
