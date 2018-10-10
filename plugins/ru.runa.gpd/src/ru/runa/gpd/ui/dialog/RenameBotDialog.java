package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.TooManySpacesChecker;

public class RenameBotDialog extends InputDialog {

    public RenameBotDialog(final IFolder botFolder) {
        super(Display.getDefault().getActiveShell(), Localization.getString("RenameBotDialog.update.title"), Localization.getString("property.name")
                + ":", botFolder.getName(), new IInputValidator() {

            @Override
            public String isValid(String value) {
                if (!botFolder.getName().equals(value) && BotCache.getAllBotNames().contains(value)) {
                    return Localization.getString("RenameBotDialog.error.botWithSameNameExists", value);
                }

                return null;
            }
        });
        setBlockOnOpen(true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Control control = super.createDialogArea(parent);
        getText().addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                updateButtons();

            }
        });
        return control;
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(
                !(Strings.isNullOrEmpty(getText().getText()) || BotCache.getAllBotNames().contains(getText().getText()))
                        && TooManySpacesChecker.isValid(getText().getText()));
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("RenameBotDialog.update.title"));
    }
}
