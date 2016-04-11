package ru.runa.gpd.ui.dialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import ru.runa.gpd.Localization;

public abstract class AbstractDateInputDialog extends UserInputDialog {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(getFormatPattern());

    public AbstractDateInputDialog(String title) {
        super(title);
    }
    
    protected abstract String getFormatPattern();

    @Override
    protected void postCreation() {
        label.setText(Localization.getString("format") + ": " + dateFormat.toPattern());
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            dateFormat.parse(newValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

}
