package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;

public class TimeInputDialog extends AbstractDateInputDialog {

    public TimeInputDialog() {
        super(Localization.getString("InputTime"));
    }

    @Override
    protected String getFormatPattern() {
        return "HH:mm";
    }
    
}
