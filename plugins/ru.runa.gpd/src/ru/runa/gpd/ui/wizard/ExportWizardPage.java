package ru.runa.gpd.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public abstract class ExportWizardPage extends WizardPage {
    protected Text destinationValueText;
    protected Button browseButton;

    public ExportWizardPage(Class<? extends ExportWizardPage> clazz) {
        super(clazz.getSimpleName());
    }

    protected void createDestinationDirectoryGroup(Composite parent) {
        Font font = parent.getFont();
        Composite destinationSelectionGroup = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        destinationSelectionGroup.setLayout(layout);
        destinationSelectionGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        destinationSelectionGroup.setFont(font);
        Label destinationLabel = new Label(destinationSelectionGroup, SWT.NONE);
        destinationLabel.setText(Localization.getString("label.destination.file"));
        destinationLabel.setFont(font);
        destinationValueText = new Text(destinationSelectionGroup, SWT.READ_ONLY | SWT.BORDER);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
        data.widthHint = 250;
        destinationValueText.setLayoutData(data);
        destinationValueText.setFont(font);
        browseButton = new Button(destinationSelectionGroup, SWT.PUSH);
        browseButton.setText(Localization.getString("button.choose"));
        browseButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                onBrowseButtonSelected();
            }

        });
        browseButton.setFont(font);
        setButtonLayoutData(browseButton);

        new Label(parent, SWT.NONE); // vertical spacer
    }

    protected void onBrowseButtonSelected() {

    }

    protected void setDestinationValue(String value) {
        destinationValueText.setText(value);
    }

    protected String getDestinationValue() {
        return destinationValueText.getText();
    }

    protected boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }

}
