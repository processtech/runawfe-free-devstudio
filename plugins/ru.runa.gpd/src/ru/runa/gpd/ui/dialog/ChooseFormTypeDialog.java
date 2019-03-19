package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.form.FormType;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class ChooseFormTypeDialog extends Dialog {
    public final static String EDITOR_INTERNAL = Localization.getString("ChooseFormTypeDialog.editor.internal");
    public final static String EDITOR_EXTERNAL = Localization.getString("ChooseFormTypeDialog.editor.external");

    private Combo typeCombo;
    private Combo editorTypeCombo;

    private String type;
    private String editorType;

    public ChooseFormTypeDialog() {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
    }

    @Override
    public Control createDialogArea(final Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout());

        Composite composite = new Composite(area, SWT.NONE);
        composite.setLayout(new GridLayout());

        typeCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        GridData typeComboData = new GridData(GridData.FILL_HORIZONTAL);
        typeComboData.minimumWidth = 200;
        typeCombo.setLayoutData(typeComboData);
        for (FormType formType : FormTypeProvider.getRegisteredFormTypes()) {
            if (formType.isCreationAllowed()) {
                typeCombo.add(formType.getName());
            }
        }
        typeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateVisibility();
            }
        });

        Label openInLabel = new Label(composite, SWT.NONE);
        openInLabel.setText(Localization.getString("ChooseFormTypeDialog.openInEditor"));

        editorTypeCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
        editorTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        editorTypeCombo.add(EDITOR_INTERNAL);
        if (Activator.getPrefBoolean(PrefConstants.P_FORM_USE_EXTERNAL_EDITOR)) {
            editorTypeCombo.add(EDITOR_EXTERNAL);
        }
        editorTypeCombo.select(0);
        editorTypeCombo.addSelectionListener(new LoggingSelectionAdapter() {
            
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                updateVisibility();
            }
        });

        autoChoose();

        return area;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        typeCombo.select(0);
        updateVisibility();
        return control;
    }

    private void updateVisibility() {
        String name = typeCombo.getItem(typeCombo.getSelectionIndex());
        type = FormTypeProvider.getFormTypeByName(name).getType();
        editorType = editorTypeCombo.getItem(editorTypeCombo.getSelectionIndex());
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("FormTypeForm.selectType"));
    }

    public String getType() {
        return type;
    }

    public String getEditorType() {
        return editorType;
    }

    private void autoChoose() {
        if (typeCombo.getItems().length == 1 && editorTypeCombo.getItems().length == 1) {
            getShell().setSize(0, 0);
            typeCombo.select(0);
            editorTypeCombo.select(0);
            Display.getDefault().asyncExec(() -> {
                okPressed();
            });
        }
    }

}
