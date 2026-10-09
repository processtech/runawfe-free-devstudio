package ru.runa.gpd.aichat.utils;

import ru.runa.gpd.aichat.bpmnelements.Node;

import java.util.HashMap;
import java.util.Map;

public class MapHelpers {
    private static Map<String, String> formatMap = Map.ofEntries(
            Map.entry("Actor", "ru.runa.wfe.var.format.ActorFormat"),
            Map.entry("BigDecimal", "ru.runa.wfe.var.format.BigDecimalFormat"),
            Map.entry("Boolean", "ru.runa.wfe.var.format.BooleanFormat"),
            Map.entry("Date", "ru.runa.wfe.var.format.DateFormat"),
            Map.entry("DateTime", "ru.runa.wfe.var.format.DateTimeFormat"),
            Map.entry("Double", "ru.runa.wfe.var.format.DoubleFormat"),
            Map.entry("Executor", "ru.runa.wfe.var.format.ExecutorFormat"),
            Map.entry("File", "ru.runa.wfe.var.format.FileFormat"),
            Map.entry("FormattedText", "ru.runa.wfe.var.format.FormattedTextFormat"),
            Map.entry("Group", "ru.runa.wfe.var.format.GroupFormat"),
            Map.entry("Hidden", "ru.runa.wfe.var.format.HiddenFormat"),
            Map.entry("List", "ru.runa.wfe.var.format.ListFormat"),
            Map.entry("Long", "ru.runa.wfe.var.format.LongFormat"),
            Map.entry("Map", "ru.runa.wfe.var.format.MapFormat"),
            Map.entry("ProcessId", "ru.runa.wfe.var.format.ProcessIdFormat"),
            Map.entry("String", "ru.runa.wfe.var.format.StringFormat"),
            Map.entry("Text", "ru.runa.wfe.var.format.TextFormat"),
            Map.entry("Time", "ru.runa.wfe.var.format.TimeFormat"),
            Map.entry("UserType", "ru.runa.wfe.var.format.UserTypeFormat")
    );

    private static Map<String, Node.Sizing> sizingMap = Map.ofEntries(
            Map.entry("bpmn:StartEvent", new Node.Sizing(36, 36)),
            Map.entry("bpmn:EndEvent", new Node.Sizing(36, 36)),
            Map.entry("bpmn:Task", new Node.Sizing(100, 80)),
            Map.entry("bpmn:UserTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:ServiceTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:SendTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:ReceiveTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:ScriptTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:BusinessRuleTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:ManualTask", new Node.Sizing(100, 80)),
            Map.entry("bpmn:ExclusiveGateway", new Node.Sizing(50, 50)),
            Map.entry("bpmn:ParallelGateway", new Node.Sizing(50, 50)),
            Map.entry("bpmn:InclusiveGateway", new Node.Sizing(50, 50)),
            Map.entry("bpmn:EventBasedGateway", new Node.Sizing(50, 50))
    );

    private static Map<String, String> typeMap = Map.ofEntries(
            Map.entry("bpmn:StartEvent", "startEvent"),
            Map.entry("bpmn:EndEvent", "endEvent"),
            Map.entry("bpmn:Task", "task"),
            Map.entry("bpmn:UserTask", "userTask"),
            Map.entry("bpmn:ServiceTask", "serviceTask"),
            Map.entry("bpmn:SendTask", "sendTask"),
            Map.entry("bpmn:ReceiveTask", "receiveTask"),
            Map.entry("bpmn:ScriptTask", "scriptTask"),
            Map.entry("bpmn:BusinessRuleTask", "businessRuleTask"),
            Map.entry("bpmn:ManualTask", "manualTask"),
            Map.entry("bpmn:ExclusiveGateway", "exclusiveGateway"),
            Map.entry("bpmn:ParallelGateway", "parallelGateway"),
            Map.entry("bpmn:InclusiveGateway", "inclusiveGateway"),
            Map.entry("bpmn:EventBasedGateway", "eventBasedGateway")
    );

    public static String getXmlTagName(String bpmnType) {
        return typeMap.get(bpmnType) == null ? "task" : typeMap.get(bpmnType);
    }

    public static Node.Sizing getElementSizing(String bpmnType) {
        return sizingMap.get(bpmnType) != null ? sizingMap.get(bpmnType) : new Node.Sizing(100, 80);
    }

    public static String getRunaFormat(String format) {
        return formatMap.get(format);
    }

}
