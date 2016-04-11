package ru.runa.gpd.settings;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.texteditor.TextEditorPlugin;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.Artifact;

public class EditArtifactDialog extends TrayDialog {
    private final Artifact artifact;
    private Text classNameText;
    private Text labelText;
    private Combo configuratorCombo;
    private boolean nameIsModifiable;

    /**
     * Creates a new dialog.
     *
     * @param parent the shell parent of the dialog
     * @param artifact the artifact to edit
     * @param edit whether this is a new template or an existing being edited
     */
    public EditArtifactDialog(Shell parent, Artifact artifact, boolean edit) {
        super(parent);
        this.artifact = new Artifact(artifact);
        nameIsModifiable = !edit;
        // Localization.getString(nameIsModifiable ? "button.change" : "button.create")
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public void create() {
        super.create();
        updateButtons();
    }

    @Override
    protected Control createDialogArea(Composite ancestor) {
        Composite parent = new Composite(ancestor, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
        layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        parent.setLayout(layout);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        createLabel(parent, Localization.getString("property.name"));
        classNameText = createText(parent);
        classNameText.setText(artifact.getName());
        if (nameIsModifiable) {
            classNameText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    doTextWidgetChanged(e.widget);
                }
            });
            classNameText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    updateButtons();
                }
            });
        }
        classNameText.setEditable(nameIsModifiable);
        //
        createLabel(parent, Localization.getString("property.label"));
        labelText = createText(parent);
        labelText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        //
        createLabel(parent, Localization.getString("Variable.property.format"));
        configuratorCombo = new Combo(parent, SWT.READ_ONLY);
        //            for (int i = 0; i < fContextTypes.length; i++) {
        //                configuratorCombo.add(fContextTypes[i][1]);
        //            }
        Composite composite = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData());
        labelText.setText(artifact.getLabel());
        applyDialogFont(parent);
        return composite;
    }

    private void doTextWidgetChanged(Widget w) {
        if (w == classNameText) {
            updateButtons();
        } else if (w == configuratorCombo) {
        } else if (w == labelText) {
            // oh, nothing
        }
    }

    private static Label createLabel(Composite parent, String name) {
        Label label = new Label(parent, SWT.NULL);
        label.setText(name);
        label.setLayoutData(new GridData());
        return label;
    }

    private static Text createText(Composite parent) {
        Text text = new Text(parent, SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return text;
    }

    private void updateButtons() {
        //        if (fOkButton != null && !fOkButton.isDisposed()) {
        //            boolean valid = classNameText == null || classNameText.getText().trim().length() != 0;
        //            fOkButton.setEnabled(valid);
        //        }
    }

    @Override
    protected void okPressed() {
        String name = classNameText == null ? artifact.getName() : classNameText.getText();
        artifact.setName(name);
        artifact.setLabel(labelText.getText());
        super.okPressed();
    }

    /**
     * Returns the created Artifact.
     */
    public Artifact getArtifact() {
        return artifact;
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        String sectionName = getClass().getName() + "_dialogBounds";
        IDialogSettings settings = TextEditorPlugin.getDefault().getDialogSettings();
        IDialogSettings section = settings.getSection(sectionName);
        if (section == null) {
            section = settings.addNewSection(sectionName);
        }
        return section;
    }
}
