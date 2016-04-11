package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.PrefConstants;

public class DateInputDialog extends AbstractDateInputDialog {

    public DateInputDialog() {
        super(Localization.getString("InputDate"));
    }

    @Override
    protected String getFormatPattern() {
        return Activator.getPrefString(PrefConstants.P_DATE_FORMAT_PATTERN);
    }
    
}
