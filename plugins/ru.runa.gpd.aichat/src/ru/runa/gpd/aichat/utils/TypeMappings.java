package ru.runa.gpd.aichat.utils;

import java.util.Map;

public class TypeMappings {
    public static String mapEventType(String eventType) {
        Map<String, String> baseTypes = Map.of(
            "start", "bpmn:StartEvent",
            "end", "bpmn:EndEvent",
            "intermediate-throw", "bpmn:IntermediateThrowEvent",
            "intermediate-catch", "bpmn:IntermediateCatchEvent",
            "boundary", "bpmn:BoundaryEvent"
        );
        return baseTypes.get(eventType);
    }

    public static String mapToHandlerClass(String handlerRaw) {
        if (handlerRaw == null) {
            return null;
        }
        Map<String, String> handlerClasses = Map.of(
                "DocxHandler", "ru.runa.wfe.office.doc.DocxHandler",
                "FormulaActionHandler", "ru.runa.wfe.extension.handler.var.FormulaActionHandler",
                "GetExecutorInfoHandler", "ru.runa.wfe.extension.handler.user.GetExecutorInfoHandler"
        );
        return handlerClasses.get(handlerRaw);
    }

    public static String mapActivityType(String activityType) {
        Map<String, String> activityMap = Map.of(
            "task", "bpmn:Task",
            "userTask", "bpmn:UserTask",
            "serviceTask", "bpmn:ServiceTask",
            "scriptTask", "bpmn:ScriptTask",
            "businessRuleTask", "bpmn:BusinessRuleTask",
            "manualTask", "bpmn:ManualTask",
            "receiveTask", "bpmn:ReceiveTask",
            "sendTask", "bpmn:SendTask",
            "subProcess", "bpmn:SubProcess",
            "callActivity", "bpmn:CallActivity"
        );
        return activityMap.get(activityType);
    }

    public static String mapGatewayType(String gatewayType) {
        Map<String, String> gatewayMap = Map.of(
                "exclusive", "bpmn:ExclusiveGateway",
                "parallel", "bpmn:ParallelGateway",
                "inclusive", "bpmn:InclusiveGateway",
                "eventBased", "bpmn:EventBasedGateway",
                "complex", "bpmn:ComplexGateway"
        );
        return gatewayMap.get(gatewayType);
    }
}
