package ru.runa.gpd.ui.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;
import org.eclipse.ui.internal.wizards.datatransfer.DataTransferMessages;
import org.eclipse.ui.internal.wizards.datatransfer.WizardArchiveFileResourceExportPage1;

import ru.runa.gpd.Localization;

public class ExportProjectWizardPage extends
		WizardArchiveFileResourceExportPage1 {

	private CheckboxTableViewer projectViewer;

	public ExportProjectWizardPage(IStructuredSelection selection) {
		super(selection);
		setTitle(Localization.getString("ExportProjectWizard.page.title"));
		setDescription(Localization.getString("ExportProjectWizard.page.description"));
	}

	@Override
	public void createControl(Composite parent) {

		initializeDialogUnits(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));
		composite.setFont(parent.getFont());

		craeteViewer(composite);

		createButtons(composite);

		createDestinationGroup(composite);

		setPageComplete(false);
		setErrorMessage(null); // should not initially have error message

		setControl(composite);

		giveFocusToDestination();
	}

	public void craeteViewer(Composite parent) {
		projectViewer = CheckboxTableViewer.newCheckList(parent, SWT.BORDER);
		projectViewer.getControl().setLayoutData(
				new GridData(GridData.FILL_BOTH));
		projectViewer.setContentProvider(new ArrayContentProvider());
		projectViewer.setLabelProvider(new ProjectLabelProvider());
		projectViewer.setInput(getInput());
		projectViewer
				.addSelectionChangedListener(new ISelectionChangedListener() {

					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						updatePageCompletion();
					}
				});
	}

	public void createButtons(Composite parent) {
		Font font = parent.getFont();

		// top level group
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setFont(parent.getFont());

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = true;
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
				| GridData.HORIZONTAL_ALIGN_FILL));

		Button selectButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID,
				Localization.getString("ImportProjectWizard.page.select.all"),
				false);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectViewer.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);
		selectButton.setFont(font);
		setButtonLayoutData(selectButton);

		Button deselectButton = createButton(
				buttonComposite,
				IDialogConstants.DESELECT_ALL_ID,
				Localization.getString("ImportProjectWizard.page.deselect.all"),
				false);

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				projectViewer.setAllChecked(false);
			}
		};
		deselectButton.addSelectionListener(listener);
		deselectButton.setFont(font);
		setButtonLayoutData(deselectButton);
	}

	public List<IProject> getInput() {
		List<IProject> input = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isOpen()) {
				input.add(projects[i]);
			}
		}
		return input;
	}

	@Override
	protected String getOutputSuffix() {
		return "*.zip";
	}

	@Override
	protected List getWhiteCheckedResources() {
		return Arrays.asList(projectViewer.getCheckedElements());
	}

	
	@Override
	protected boolean executeExportOperation(ArchiveFileExportOperation op) {
		op.setCreateLeadupStructure(false);
		op.setUseCompression(true);
		op.setUseTarFormat(false);

		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			displayErrorDialog(e.getTargetException());
			return false;
		}

		IStatus status = op.getStatus();
		if (!status.isOK()) {
			ErrorDialog.openError(getContainer().getShell(),Localization.getString("ExportProjectWizard.page.export.problem"), null, status);
			return false;
		}

		return true;
	}
	
	@Override
	protected void handleDestinationBrowseButtonPressed() {
		FileDialog dialog = new FileDialog(getContainer().getShell(), SWT.SAVE | SWT.SHEET);
        dialog.setFilterExtensions(new String[] { "*.zip" }); //$NON-NLS-1$ //$NON-NLS-2$
        dialog.setText(Localization.getString("ExportProjectWizard.page.export.archiv"));
        String currentSourceString = getDestinationValue();
        int lastSeparatorIndex = currentSourceString
                .lastIndexOf(File.separator);
        if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(currentSourceString.substring(0,
                    lastSeparatorIndex));
		}
        String selectedFileName = dialog.open();

        if (selectedFileName != null) {
            setErrorMessage(null);
            setDestinationValue(selectedFileName);
        }
	}
	
	@Override
	protected boolean validateDestinationGroup() {
		String destinationValue = getDestinationValue();
        if (destinationValue.length() == 0) {
            setMessage(destinationEmptyMessage());
            return false;
        }

        String conflictingContainer = getConflictingContainerNameFor(destinationValue);
        if (conflictingContainer == null) {
			// no error message, but warning may exists
			setMessage(null);
		} else {
            setErrorMessage(NLS.bind(Localization.getString("ExportProjectWizard.page.directory.conflict"), conflictingContainer));
            giveFocusToDestination();
            return false;
        }

        return true;
	}
	
	@Override
	protected String getDestinationLabel() {
		return Localization.getString("ExportProjectWizard.page.label.destination");
	}
	
	@Override
	protected String destinationEmptyMessage() {
		return Localization.getString("ExportProjectWizard.page.hint.destination");
	}
	
	@Override
	protected boolean validateSourceGroup() {
		// there must be some resources selected for Export
    	boolean isValid = true;
        List resourcesToExport = getWhiteCheckedResources();
    	if (resourcesToExport.size() == 0){
    		setErrorMessage(Localization.getString("ExportProjectWizard.page.hint.nosource"));
            isValid =  false;
    	} else {
			setErrorMessage(null);
		}
		return isValid;
	}
	
	class ProjectLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			if (element instanceof IProject && element != null) {
				return ((IProject) element).getName();// getFullPath().toString();
			} else {
				return super.getText(element);
			}
		}
	}

}
