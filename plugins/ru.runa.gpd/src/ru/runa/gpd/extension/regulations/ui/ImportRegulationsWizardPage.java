package ru.runa.gpd.extension.regulations.ui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Throwables;

public class ImportRegulationsWizardPage extends WizardPage {
    private Composite fileSelectionArea;
    private Text selectedParText;
    private Button selectParButton;
    private File selectedFile;

    public ImportRegulationsWizardPage() {
        super("import regulations");
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));
        Group importGroup = new Group(sashForm, SWT.NONE);
        importGroup.setLayout(new GridLayout(1, false));
        importGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        fileSelectionArea = new Composite(importGroup, SWT.NONE);
        GridData fileSelectionData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        fileSelectionData.heightHint = 30;
        fileSelectionArea.setLayoutData(fileSelectionData);
        GridLayout fileSelectionLayout = new GridLayout();
        fileSelectionLayout.numColumns = 2;
        fileSelectionLayout.makeColumnsEqualWidth = false;
        fileSelectionLayout.marginWidth = 0;
        fileSelectionLayout.marginHeight = 0;
        fileSelectionArea.setLayout(fileSelectionLayout);
        selectedParText = new Text(fileSelectionArea, SWT.READ_ONLY);
        GridData gridData = new GridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL);
        gridData.heightHint = 30;
        selectedParText.setLayoutData(gridData);
        selectParButton = new Button(fileSelectionArea, SWT.PUSH);
        selectParButton.setText(Localization.getString("button.choose"));
        selectParButton.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_END));
        selectParButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog openDialog = new FileDialog(getShell(), SWT.OPEN);
                openDialog.setFilterExtensions(new String[] { "*.par" });
                if (openDialog.open() != null) {
                    selectedFile = new File(openDialog.getFilterPath() + File.separator + openDialog.getFileName());
                    selectedParText.setText(selectedFile.getAbsolutePath());
                }
            }
        });

        setControl(pageControl);
        Shell shell = getShell();
        Rectangle displayRectangle = shell.getDisplay().getPrimaryMonitor().getBounds();
        shell.setSize(selectedParText.getLineHeight() * 40, selectedParText.getLineHeight() * 16);
        shell.setLocation((displayRectangle.width - shell.getBounds().width) / 2, (displayRectangle.height - shell.getBounds().height) / 2);
        shell.setMinimumSize(selectedParText.getLineHeight() * 40, selectedParText.getLineHeight() * 16);
    }

    public boolean performFinish() {
        if (selectedFile != null) {
            try {
                ProcessEditorBase editor = EditorUtils.getCurrentEditor();
                IFile definitionFile = editor.getDefinitionFile();
                ProcessDefinition definition = editor.getDefinition();
                Map<String, byte[]> files = IOUtils.getArchiveFiles(new FileInputStream(selectedFile), true);
                if (!files.containsKey(ParContentProvider.PROCESS_DEFINITION_REGULATIONS_XML_FILE_NAME)) {
                    throw new Exception("regulations.xml not found");
                }
                copy(definitionFile, files, ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
                copy(definitionFile, files, ParContentProvider.PROCESS_DEFINITION_REGULATIONS_XML_FILE_NAME);
            } catch (Exception exception) {
                PluginLogger.logErrorWithoutDialog("import regulations", exception);
                setErrorMessage(Throwables.getRootCause(exception).getMessage());
                return false;
            }
        }
        return true;
    }

    private void copy(IFile definitionFile, Map<String, byte[]> files, String fileName) throws CoreException {
        if (files.containsKey(fileName)) {
            IFile file = IOUtils.getAdjacentFile(definitionFile, fileName);
            byte[] data = files.get(fileName);
            IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(data));
        }
    }

}
