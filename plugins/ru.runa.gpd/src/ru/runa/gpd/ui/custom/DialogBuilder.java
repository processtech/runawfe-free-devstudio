package ru.runa.gpd.ui.custom;

import ru.runa.gpd.ui.dialog.CustomDialog;

public class DialogBuilder {
    private CustomDialog customDialog;

    public DialogBuilder(int dialogType, String message) {
        customDialog = new CustomDialog(dialogType, message);
    }

    public DialogBuilder withTitle(String title) {
        customDialog.setTitle(title);
        return this;
    }

    public DialogBuilder withDetailsArea(String detailsText) {
        customDialog.setDetailArea(detailsText, false);
        return this;
    }

    public DialogBuilder withOpenedDetailsArea(String detailsText) {
        customDialog.setDetailArea(detailsText, true);
        return this;
    }

    public DialogBuilder withActionButton(int actionId, String actionButtonTitle) {
        customDialog.setActionMode(actionId, actionButtonTitle);
        return this;
    }

    public DialogBuilder withCancelButton() {
        customDialog.setCancelButton(true);
        return this;
    }

    public DialogBuilder withoutOkButton() {
        customDialog.setOkButton(false);
        return this;
    }

    public DialogBuilder withDefaultButton(int defaultButton) {
        customDialog.setDefaultButton(defaultButton);
        return this;
    }

    public int andExecute() {
        return customDialog.open();
    }
}
