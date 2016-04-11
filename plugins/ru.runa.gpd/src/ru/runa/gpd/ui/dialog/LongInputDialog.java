package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;

public class LongInputDialog extends UserInputDialog {

    public LongInputDialog() {
        super(Localization.getString("InputNumber"));
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            Long.parseLong(newValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
