package ru.runa.gpd.ui.wizard;

import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IAdaptable;
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

public abstract class ImportWizardPage extends WizardPage {
    protected final IContainer initialSelection;
    protected ListViewer projectViewer;

    public ImportWizardPage(Class<? extends ImportWizardPage> clazz, IStructuredSelection selection) {
        super(clazz.getSimpleName());
        this.initialSelection = getInitialSelection(selection);
    }

    protected IContainer getInitialSelection(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) selectedElement;
                return (IContainer) adaptable.getAdapter(IContainer.class);
            }
        }
        return null;
    }

    protected void createProjectsGroup(Composite parent, List<? extends IContainer> data, LabelProvider labelProvider) {
        Group projectListGroup = new Group(parent, SWT.NONE);
        projectListGroup.setLayout(new GridLayout());
        projectListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectListGroup.setText(Localization.getString("label.project"));
        projectViewer = new ListViewer(projectListGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 100;
        projectViewer.getControl().setLayoutData(gridData);
        if (labelProvider != null) {
            projectViewer.setLabelProvider(labelProvider);
        }
        projectViewer.setContentProvider(new ArrayContentProvider());
        projectViewer.setInput(data);
        if (initialSelection != null) {
            projectViewer.setSelection(new StructuredSelection(initialSelection));
        }
    }

    protected IContainer getSelectedProject() {
        IStructuredSelection selectedProject = (IStructuredSelection) projectViewer.getSelection();
        IContainer container = (IContainer) selectedProject.getFirstElement();
        if (container == null) {
            setErrorMessage(Localization.getString("error.selectTargetProject"));
            return null;
        }
        return container;
    }
}
