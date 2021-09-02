package ru.runa.gpd.ui.wizard;

import java.io.File;
import java.text.MessageFormat;
import java.util.function.Consumer;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.IOUtils;

class DefaultFileVariableValueComposite extends Composite {
    private final VariableDefaultValuePage parent;
    private final Consumer<String> defaultValueConsumer;
    private String defaultValue;
    private Button fileButton;

    DefaultFileVariableValueComposite(VariableDefaultValuePage parent, String initialDefaultValue, Consumer<String> defaultValueConsumer) {
        super(parent.getDynaComposite(), SWT.NONE);
        this.parent = parent;
        final GridLayout defaultFileValueLayout = new GridLayout();
        defaultFileValueLayout.marginWidth = 0;
        defaultFileValueLayout.marginHeight = 0;
        defaultFileValueLayout.numColumns = 3;
        setLayout(defaultFileValueLayout);
        setLayoutData(new GridData(GridData.FILL_BOTH));

        this.defaultValue = initialDefaultValue;
        this.defaultValueConsumer = defaultValueConsumer;
        build();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        fileButton.setEnabled(enabled);
    }

    void build() {
        for (Control control : getChildren()) {
            control.dispose();
        }

        fileButton = new Button(this, SWT.NONE);
        fileButton.setText(Localization.getString("button.choose"));

        final Hyperlink exportLink = SwtUtils.createLink(this, Localization.getString("button.export"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                dialog.setText(Localization.getString("button.export"));
                final String processFileName = EmbeddedFileUtils.getProcessFileName(defaultValue);
                final String extension = IOUtils.getExtension(processFileName);
                if (extension != null) {
                    dialog.setFilterExtensions(new String[] { "*." + extension });
                }
                dialog.setFileName(processFileName);

                final String savePath = dialog.open();
                if (savePath == null) {
                    return;
                }

                final IFile processFile = EmbeddedFileUtils.getProcessFile(processFileName);
                File outputFile = new File(savePath);
                if (outputFile.exists()) {
                    final MessageBox fileExistsDialog = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.CANCEL | SWT.ICON_WARNING);
                    fileExistsDialog.setMessage(MessageFormat.format(Localization.getString("ImportProjectWizard.page.override.exist"), savePath));

                    switch (fileExistsDialog.open()) {
                    case SWT.YES:
                        break;
                    case SWT.NO: {
                        final String withoutExtension = IOUtils.getWithoutExtension(savePath);
                        for (int i = 1; (outputFile = new File(withoutExtension + "_" + i + "." + extension)).exists(); i++) {
                        }
                        break;
                    }
                    case SWT.CANCEL:
                        return;
                    }
                }
                outputFile.createNewFile();
                IOUtils.copyFile(processFile.getContents(), outputFile);
            }
        });
        exportLink.setVisible(defaultValue != null);

        final Hyperlink fileNameLink = SwtUtils.createLink(this, defaultValue != null ? EmbeddedFileUtils.getProcessFileName(defaultValue) : "",
                new LoggingHyperlinkAdapter() {
                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        if (defaultValue == null) {
                            return;
                        }

                        IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
                                EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue)), true);
                    }
                });

        fileButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                String filePath = dialog.open();
                if (filePath == null) {
                    return;
                }
                File file = new File(filePath);
                if (file.exists()) {
                    if (IOUtils.looksLikeFormFile(filePath)) {
                        parent.setErrorMessage(Localization.getString("VariableDefaultValuePage.error.ReservedFileName"));
                        return;
                    }
                    IFile newFile = EmbeddedFileUtils.getProcessFile(file.getName());
                    if (newFile.exists()) {
                        parent.setErrorMessage(Localization.getString("VariableDefaultValuePage.error.EmbeddedFileNameAlreadyInUse"));
                        return;
                    }
                    parent.setErrorMessage(null);
                    if (EmbeddedFileUtils.isProcessFile(defaultValue)) {
                        IFile oldFile = EmbeddedFileUtils.getProcessFile(EmbeddedFileUtils.getProcessFileName(defaultValue));
                        if (oldFile != null) {
                            EmbeddedFileUtils.deleteProcessFile(oldFile);
                        }
                    }
                    IOUtils.copyFile(filePath, newFile);
                    defaultValue = EmbeddedFileUtils.getProcessFilePath(newFile.getName());
                    fileNameLink.setText(EmbeddedFileUtils.getProcessFileName(defaultValue));
                    fileNameLink.getParent().pack();

                    defaultValueConsumer.accept(defaultValue);
                    exportLink.setVisible(defaultValue != null);
                }
            }
        });
    }

}
