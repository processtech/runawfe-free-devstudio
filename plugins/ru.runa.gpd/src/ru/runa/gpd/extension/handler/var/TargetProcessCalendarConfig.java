package ru.runa.gpd.extension.handler.var;

import org.dom4j.Element;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public class TargetProcessCalendarConfig extends CalendarConfig {
    public static final String USE_RESULT_DATE_AS_BASE_DATE_MESSAGE = Localization.getString("property.duration.baseDate.useResultVariable");
    private String processIdVariableName;
    private boolean useResultVariableAsBase;

    public TargetProcessCalendarConfig() {
    }

    public TargetProcessCalendarConfig(String xml) {
        super(xml);
    }

    public String getProcessIdVariableName() {
        return processIdVariableName;
    }

    public void setProcessIdVariableName(String processIdVariableName) {
        this.processIdVariableName = processIdVariableName;
    }

    public boolean isUseResultVariableAsBase() {
        return useResultVariableAsBase;
    }

    public void setUseResultVariableAsBase(boolean useResultVariableAsBase) {
        this.useResultVariableAsBase = useResultVariableAsBase;
    }

    @Override
    protected void serializeData(Element rootElement) {
        rootElement.addAttribute("processId", processIdVariableName);
        super.serializeData(rootElement);
        if (useResultVariableAsBase && getResultVariableName() != null) {
            rootElement.addAttribute("basedOn", getResultVariableName());
        }
    }

    @Override
    protected void deserializeData(Element rootElement) {
        this.processIdVariableName = rootElement.attributeValue("processId");
        super.deserializeData(rootElement);
        this.useResultVariableAsBase = Objects.equal(getResultVariableName(), getBaseVariableName());
    }

}
