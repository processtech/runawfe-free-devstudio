package ru.runa.gpd.formeditor.ftl.ui;

import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.ui.dialog.StringListDialog;

public class TextListDialog extends StringListDialog {

    public TextListDialog(java.util.List<String> value) {
        super(Messages.getString("TextListDialog.title"), value);
    }

}
