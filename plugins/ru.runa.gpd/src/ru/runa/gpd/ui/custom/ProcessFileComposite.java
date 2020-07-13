package ru.runa.gpd.ui.custom;

import java.io.File;
import java.io.InputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.EventSupport;
import ru.runa.gpd.util.IOUtils;

public abstract class ProcessFileComposite extends Composite {
    private final EventSupport eventSupport = new EventSupport(this);
    protected IFile file;
    private final String defaultFileName;
    private boolean showFileAsNewFirstTime;
    private boolean firstRebuild = true;
    private final DialogEnhancementMode dialogEnhancementMode;

    public ProcessFileComposite(Composite parent, IFile file, DialogEnhancementMode dialogEnhancementMode) {
        super(parent, SWT.NONE);

        if (null != (this.dialogEnhancementMode = dialogEnhancementMode) && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            this.defaultFileName = ((DocxDialogEnhancementMode) dialogEnhancementMode).defaultFileName;
            this.showFileAsNewFirstTime = ((DocxDialogEnhancementMode) dialogEnhancementMode).showFileAsNewFirstTime;
        } else {
            this.defaultFileName = "";
            this.showFileAsNewFirstTime = false;
        }
        this.file = file;
        setLayout(new GridLayout(3, false));
        if (null != dialogEnhancementMode
                && (dialogEnhancementMode.checkBotDocxTemplateEnhancementMode() || dialogEnhancementMode.checkScriptDocxTemplateEnhancementMode())) {
            GridData gridData = new GridData();
            gridData.widthHint = 333;
            setLayoutData(gridData);
        }
        rebuild();
    }

    private void rebuild() {
        for (Control control : getChildren()) {
            control.dispose();
        }

        boolean forceFileExists = false;

        if (firstRebuild) {
            firstRebuild = false;
            forceFileExists = !showFileAsNewFirstTime && null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()
                    && null != defaultFileName && !defaultFileName.isEmpty();

            boolean fileNotExists = null != file && !file.exists();
            if (fileNotExists) {
                forceFileExists = false;
            }
        }

        showFileAsNewFirstTime = false;

        if (!forceFileExists && (null == file || !file.exists())) {
            if (hasTemplate()) {
                SWTUtils.createLink(this, Localization.getString("button.create"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        IOUtils.copyFile(getTemplateInputStream(), getFile());
                        eventSupport.firePropertyChange(PropertyNames.PROPERTY_VALUE, null, getFile().getName());

                        if (null != dialogEnhancementMode && (dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()
                                || dialogEnhancementMode.checkScriptDocxTemplateEnhancementMode())) {
                            IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getFile(), true);
                        }
                        rebuild();
                    }
                });
            }
            SWTUtils.createLink(this, Localization.getString("button.import"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
                    if (getFileExtension() != null) {
                        dialog.setFilterExtensions(new String[] { "*." + getFileExtension() });
                    }
                    String path = dialog.open();
                    if (path == null) {
                        return;
                    }
                    IOUtils.copyFile(path, getFile());
                    eventSupport.firePropertyChange(PropertyNames.PROPERTY_VALUE, null, getFile().getName());
                    rebuild();
                    if (null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                        dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE | DialogEnhancementMode.DOCX_MAKE_DIRTY);
                    }
                }
            });
        } else {
            SWTUtils.createLink(this, Localization.getString("button.change"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), getFile(), true);
                }
            });
            SWTUtils.createLink(this, Localization.getString("button.export"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                    dialog.setText(Localization.getString("button.export"));
                    if (getFileExtension() != null) {
                        dialog.setFilterExtensions(new String[] { "*." + getFileExtension() });
                    }
                    dialog.setFileName(getFile().getName());
                    String path = dialog.open();
                    if (path == null) {
                        return;
                    }
                    IOUtils.copyFile(getFile().getContents(), new File(path));
                }
            });
            if (isDeleteFileOperationSupported()) {
                SWTUtils.createLink(this, Localization.getString("button.delete"), new LoggingHyperlinkAdapter() {

                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        EmbeddedFileUtils.deleteProcessFile(getFile());
                        if (null != dialogEnhancementMode && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
                            dialogEnhancementMode.invoke(DialogEnhancementMode.DOCX_MAKE_DIRTY);
                        }
                        rebuild();
                        eventSupport.firePropertyChange(PropertyNames.PROPERTY_VALUE, getFile().getName(), null);
                    }
                });
            }
        }

        layout(true, true);
    }

    private IFile getFile() {
        if (null == file && dialogEnhancementMode != null && dialogEnhancementMode.checkBotDocxTemplateEnhancementMode()) {
            file = EmbeddedFileUtils.getProcessFile(defaultFileName);
        }
        return file;
    }

    public EventSupport getEventSupport() {
        return eventSupport;
    }

    protected abstract boolean hasTemplate();

    protected abstract InputStream getTemplateInputStream();

    protected abstract String getFileExtension();

    protected abstract boolean isDeleteFileOperationSupported();

}
