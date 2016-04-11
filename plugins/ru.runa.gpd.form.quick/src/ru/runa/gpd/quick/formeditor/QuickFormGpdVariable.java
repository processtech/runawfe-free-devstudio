package ru.runa.gpd.quick.formeditor;

import ru.runa.wfe.var.dto.QuickFormVariable;

public class QuickFormGpdVariable extends QuickFormVariable {
    private String validationRule;
    private String formatLabel;

    public String getFormatLabel() {
        return formatLabel;
    }

    public void setFormatLabel(String formatLabel) {
        this.formatLabel = formatLabel;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }
}
