package ru.runa.gpd.search;

public enum VariableSearchTarget {

    SCRIPT_TASK("SearchVariableDialog.searchTarget.scriptTask"),
    BUSINESS_RULE("SearchVariableDialog.searchTarget.businessRule"),
    TIMER("SearchVariableDialog.searchTarget.timer"),
    EXCLUSIVE_GATEWAY("SearchVariableDialog.searchTarget.exclusiveGateway"),
    MESSAGING_NODE("SearchVariableDialog.searchTarget.messagingNode"),
    FORM_SCRIPT("SearchVariableDialog.searchTarget.formScript"),
    FORM_FILE("SearchVariableDialog.searchTarget.formFile"),
    FORM_VALIDATION("SearchVariableDialog.searchTarget.formValidation"),
    BOT_TASK("SearchVariableDialog.searchTarget.botTask"),
    SWIMLANE("SearchVariableDialog.searchTarget.swimlane"),
    TASK_ROLE("SearchVariableDialog.searchTarget.taskRole");

    private final String label;

    private VariableSearchTarget(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
