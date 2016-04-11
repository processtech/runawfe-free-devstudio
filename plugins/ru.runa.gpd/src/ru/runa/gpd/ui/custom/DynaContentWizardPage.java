package ru.runa.gpd.ui.custom;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class DynaContentWizardPage extends ContentWizardPage {
    protected Composite dynaComposite;

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            updateContent();
        }
    }

    protected final void updateContent() {
        for (Control control : dynaComposite.getChildren()) {
            control.dispose();
        }
        createDynaContent();
        dynaComposite.layout();
    }

    protected abstract void createDynaContent();
}
