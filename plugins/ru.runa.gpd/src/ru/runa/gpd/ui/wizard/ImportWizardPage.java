package ru.runa.gpd.ui.wizard;

import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import ru.runa.gpd.Localization;
import ru.runa.gpd.util.IOUtils;

public abstract class ImportWizardPage extends WizardPage {
    protected final IContainer initialSelection;
    protected ListViewer projectViewer;

    public ImportWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName);
        this.initialSelection = (IContainer) IOUtils.getProcessSelectionResource(selection);
    }

    protected void createProjectsGroup(Composite parent) {
        Group projectListGroup = new Group(parent, SWT.NONE);
        projectListGroup.setLayout(new GridLayout());
        projectListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectListGroup.setText(Localization.getString("label.project"));
        createProjectsList(projectListGroup);
    }

    private void createProjectsList(Composite parent) {
        projectViewer = new ListViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        projectViewer.getControl().setLayoutData(gridData);
        projectViewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return IOUtils.getProcessContainerName((IContainer) element);
            }
        });
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setInput(IOUtils.getAllProcessContainers());
        if (initialSelection != null) {
            projectViewer.setSelection(new StructuredSelection(initialSelection));
        }
    }

    protected IContainer getSelectedContainer() throws Exception {
        IStructuredSelection selectedProject = (IStructuredSelection) projectViewer.getSelection();
        IContainer container = (IContainer) selectedProject.getFirstElement();
        if (container == null) {
            throw new Exception(Localization.getString("ImportParWizardPage.error.selectTargetProject"));
        }
        return container;
    }
}
