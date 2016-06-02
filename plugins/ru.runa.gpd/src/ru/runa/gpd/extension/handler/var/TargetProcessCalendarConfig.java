package ru.runa.gpd.extension.handler.var;

import org.dom4j.Element;

public class TargetProcessCalendarConfig extends CalendarConfig {
    private String processIdVariableName;

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

    @Override
    protected void serializeData(Element rootElement) {
        rootElement.addAttribute("processId", processIdVariableName);
        super.serializeData(rootElement);
    }

    @Override
    protected void deserializeData(Element rootElement) {
        this.processIdVariableName = rootElement.attributeValue("processId");
        super.deserializeData(rootElement);
    }

}
