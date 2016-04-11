package ru.runa.gpd.ui.action;

import ru.runa.gpd.ui.view.ValidationErrorsView;

public class OpenValidationErrorsView extends OpenViewBaseAction {
    @Override
    protected String getViewId() {
        return ValidationErrorsView.ID;
    }
}
