package ru.runa.gpd.editors;

import com.google.common.base.Throwables;
import java.io.FileOutputStream;
import java.io.IOException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

public class DiffEditor extends TextEditor {

    public static String ID = "ru.runa.gpd.editors.DiffEditor";

    private ColorManager colorManager;

    public DiffEditor() {
        super();
        colorManager = new ColorManager();
        setSourceViewerConfiguration(new DiffEditorConfiguration(colorManager));
    }

    @Override
    public void dispose() {
        colorManager.dispose();
        super.dispose();
    }

    public void exportToFile() {
        Shell shell = PlatformUI.getWorkbench().getModalDialogShellProvider().getShell();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        String selectedDirectoryName = dialog.open();
        if (selectedDirectoryName != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(selectedDirectoryName)) {
                String content = getDocumentProvider().getDocument(getEditorInput()).get();
                fileOutputStream.write(content.getBytes());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }

        }
    }

}
