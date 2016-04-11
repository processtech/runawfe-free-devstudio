package ru.runa.gpd.ui.dialog;

import ru.runa.gpd.Localization;

public class DoubleInputDialog extends UserInputDialog {

    public DoubleInputDialog() {
        super(Localization.getString("InputNumber"));
    }

    @Override
    protected boolean validate(String newValue) {
        try {
            Double.parseDouble(newValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
