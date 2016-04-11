package ru.runa.gpd.extension;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.util.XmlUtil;

public class XMLEditorDialog extends DelegableConfigurationDialog {
    private Label errorLabel;
    private boolean validateXSD;

    public XMLEditorDialog(String initialValue) {
        super(initialValue);
    }

    public void setValidateXSD(boolean validateXSD) {
        this.validateXSD = validateXSD;
    }

    @Override
    protected void createDialogHeader(Composite composite) {
        errorLabel = new Label(composite, SWT.NONE);
        errorLabel.setForeground(ColorConstants.red);
        errorLabel.setText("");
        errorLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    @Override
    protected void createDialogFooter(Composite composite) {
        styledText.addLineStyleListener(new XmlHighlightTextStyling());
    }

    private void setErrorLabelText(String text) {
        errorLabel.setText(text);
        errorLabel.pack(true);
    }

    @Override
    protected void okPressed() {
        try {
            String xml = styledText.getText();
            if (validateXSD) {
                XmlUtil.parseWithXSDValidation(xml);
            } else {
                XmlUtil.parseWithoutValidation(xml);
            }
            super.okPressed();
        } catch (Exception e) {
            setErrorLabelText(e.getMessage());
        }
    }
}
