package ru.runa.gpd.ui.wizard;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class VariableAccessPage extends ContentWizardPage {
    private boolean publicVisibility;
    private boolean editableInChat;

    public VariableAccessPage(Variable variable) {
        if (variable != null) {
            this.publicVisibility = variable.isPublicVisibility();
            this.editableInChat = variable.isEditableInChat();
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

        if (CommonPreferencePage.isChatFunctionalityEnabled()) {
            final Button editableInChatCheckbox = new Button(composite, SWT.CHECK);
            editableInChatCheckbox.setSelection(editableInChat);
            editableInChatCheckbox.setText(Localization.getString("Variable.property.editableInChat"));
            editableInChatCheckbox.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    editableInChat = editableInChatCheckbox.getSelection();
                }
            });
        }
    }

    @Override
    protected void verifyContentIsValid() {
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public boolean isEditableInChat() {
        return editableInChat;
    }
}
