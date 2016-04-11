package ru.runa.gpd.ui.custom;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;

// TODO change inheritance to this class in project wizard pages
public abstract class ContentWizardPage extends WizardPage {
    protected ContentWizardPage() {
        super("");
        setTitle(Localization.getString(getClass().getSimpleName() + ".title"));
        setDescription(Localization.getString(getClass().getSimpleName() + ".description"));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        createLayout(composite);
        createContent(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        verifyContentIsValid();
    }

    @Override
    public Composite getControl() {
        return (Composite) super.getControl();
    }

    protected int getGridLayoutColumns() {
        return 2;
    }

    protected void createLayout(Composite composite) {
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = getGridLayoutColumns();
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    protected abstract void createContent(Composite composite);

    protected abstract void verifyContentIsValid();

    @Override
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        setPageComplete(newMessage == null);
    }
}
