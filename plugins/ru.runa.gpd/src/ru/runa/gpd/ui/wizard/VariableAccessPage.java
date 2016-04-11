package ru.runa.gpd.ui.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class VariableAccessPage extends ContentWizardPage {
    private boolean publicVisibility;

    public VariableAccessPage(Variable variable) {
        if (variable != null) {
            this.publicVisibility = variable.isPublicVisibility();
        }
    }

    @Override
    protected int getGridLayoutColumns() {
        return 1;
    }

    @Override
    protected void createContent(Composite composite) {
        final Combo combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        combo.setItems(new String[] { Localization.getString("VariableAccessPage.by.permissions"), Localization.getString("VariableAccessPage.public") });
        combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        combo.setText(combo.getItem(publicVisibility ? 1 : 0));
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                publicVisibility = combo.getSelectionIndex() == 1;
            }
        });
    }

    @Override
    protected void verifyContentIsValid() {
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }
}
