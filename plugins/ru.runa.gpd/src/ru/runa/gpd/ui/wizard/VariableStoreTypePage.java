package ru.runa.gpd.ui.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableStoreType;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

/**
 * @author Alekseev Vitaly
 * @since Jun 21, 2017
 */
public class VariableStoreTypePage extends ContentWizardPage {
    private VariableStoreType storeType = VariableStoreType.DEFAULT;

    public VariableStoreTypePage(Variable variable) {
        if (variable != null && variable.getStoreType() != null) {
            storeType = variable.getStoreType();
        }
    }

    @Override
    protected void createContent(Composite composite) {
        final Combo combo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        for (VariableStoreType storeType : VariableStoreType.values()) {
            combo.add(storeType.getDescription());
        }
        combo.setText(storeType.getDescription());
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                storeType = VariableStoreType.values()[combo.getSelectionIndex()];
            }
        });
    }

    @Override
    protected void verifyContentIsValid() {
    }

    public VariableStoreType getStoreType() {
        return storeType;
    }

}
