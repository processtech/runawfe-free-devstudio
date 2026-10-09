package ru.runa.gpd.aichat.utils;

import ru.runa.gpd.aichat.bpmnelements.Connection;
import ru.runa.gpd.aichat.bpmnelements.Lane;
import ru.runa.gpd.aichat.bpmnelements.Node;
import ru.runa.gpd.aichat.bpmnelements.ProcessContext;
import ru.runa.gpd.aichat.bpmnelements.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BpmnXmlImporter {
    public static ProcessContext importXml(String xml, String variablesXml) {
        Pattern pattern = Pattern.compile("<bpmn:process[^>]+id=\"([^\"]+)\"[^>]*name=\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(xml);
        String processId = matcher.find() ? matcher.group(1) : IdGenerator.generate("Process");
        String name = matcher.find() ? matcher.group(2) : "Imported Process";
        ProcessContext context = new ProcessContext();
        context.id = processId;
        context.name = name;
        context.type = "process";
        context.elements = new HashMap<>();
        context.connections = new HashMap<>();
        context.variables = new HashMap<>();
        context.lanes = new HashMap<>();

        parseVariables(variablesXml, context);
        parseLanes(xml, context);
        parseElements(xml, context);
        parseSequenceFlows(xml, context);
        setLaneForElements(context);
        return context;
    }

    private static void parseLanes(String xml, ProcessContext context) {
        Pattern laneSetPattern = Pattern.compile("<bpmn:laneSet[^>]*>([\\s\\S]*?)</bpmn:laneSet>");
        Matcher laneSetMatcher = laneSetPattern.matcher(xml);
        if (laneSetMatcher.find()) {
            String laneSetContent = laneSetMatcher.group(1);
            Pattern lanePattern = Pattern.compile("<bpmn:lane[^>]*id=\"([^\"]+)\"[^>]*name=\"([^\"]*)\"[^>]*>([\\s\\S]*?)</bpmn:lane>");
            Matcher laneMatcher = lanePattern.matcher(laneSetContent);
            while (laneMatcher.find()) {
                String laneId = laneMatcher.group(1);
                String laneName = laneMatcher.group(2);
                String laneContent = laneMatcher.group(3);

                Lane lane = new Lane();
                lane.id = laneId;
                lane.name = laneName;
                lane.bounds = parseLaneBounds(xml, laneId);
                lane.flowNodeRefs = parseLaneFlowNodeRefs(laneContent);
                lane.initializer = parseLaneInitializer(laneContent, laneName);

                context.lanes.put(laneId, lane);
            }
        }
    }

    private static Lane.Initializer parseLaneInitializer(String laneContent, String laneName) {
        Lane.Initializer initializer = new Lane.Initializer(null, null);
        Pattern propertyPattern = Pattern.compile("<property name=\"config\"><!\\[CDATA\\[([^(]*?)\\(([\\s\\S]*)\\)]]></property>");
        Matcher propertyMatcher = propertyPattern.matcher(laneContent);
        if (propertyMatcher.find()) {
            String initializerType = propertyMatcher.group(1);
            if (initializerType.equals("ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction")) {
                initializer.type = "group";
                initializer.name = propertyMatcher.group(2);
            } else if (initializerType.startsWith("@")) {
                initializer.type = "relation";
                initializer.name = initializerType.substring(1);
                initializer.roleSource = propertyMatcher.group(2);
            }
        }
        return initializer;
    }

    private static List<String> parseLaneFlowNodeRefs(String laneContent) {
        List<String> flowNodeRefs = new ArrayList<>();
        Pattern flowNodeRefPattern = Pattern.compile("<bpmn:flowNodeRef>([^<]+)</bpmn:flowNodeRef>");
        Matcher flowNodeRefMatcher = flowNodeRefPattern.matcher(laneContent);
        while (flowNodeRefMatcher.find()) {
            flowNodeRefs.add(flowNodeRefMatcher.group(1));
        }
        return flowNodeRefs;
    }

    private static Lane.Bounds parseLaneBounds(String xml, String laneId) {
        Pattern laneBoundsPattern = Pattern.compile("<bpmndi:BPMNShape[^>]*bpmnElement=\"" + laneId + "\"[^>]*>([\\s\\S]*?)</bpmndi:BPMNShape>", Pattern.CASE_INSENSITIVE);
        Matcher laneBoundsMatcher = laneBoundsPattern.matcher(xml);
        Lane.Bounds bounds = new Lane.Bounds(20, 20, 800, 150);
        if (laneBoundsMatcher.find()) {
            String boundsContent = laneBoundsMatcher.group(1);
            Pattern dcBoundsPattern = Pattern.compile("<dc:Bounds[^>]*x=\"([^\"]+)\"[^>]*y=\"([^\"]+)\"[^>]*width=\"([^\"]+)\"[^>]*height=\"([^\"]+)\"[^>]*/?>");
            Matcher dcBoundsMatcher = dcBoundsPattern.matcher(boundsContent);
            if (dcBoundsMatcher.find()) {
                bounds = new Lane.Bounds(
                        Integer.parseInt(dcBoundsMatcher.group(1)),
                        Integer.parseInt(dcBoundsMatcher.group(2)),
                        Integer.parseInt(dcBoundsMatcher.group(3)),
                        Integer.parseInt(dcBoundsMatcher.group(4))
                );
            }
        }
        return bounds;
    }

    private static void parseVariables(String variablesXml, ProcessContext context) {
        if (!variablesXml.trim().isEmpty()) {
            Pattern variablesPattern = Pattern.compile("<variables>([\\s\\S]*?)</variables>");
            Matcher variablesMatcher = variablesPattern.matcher(variablesXml);
            if (variablesMatcher.find()) {
                String variablesContent = variablesMatcher.group(1);
                Pattern variablePattern = Pattern.compile("<variable[^>]*name=\"([^\"]+)\"[^>]*scriptingName=\"([^\"]+)\"[^>]*format=\"([^\"]+)\"(?:[^>]*defaultValue=\"([^\"]*)\")?[^>]*/?>");
                Matcher variableMatcher = variablePattern.matcher(variablesContent);
                while (variableMatcher.find()) {
                    String varName = variableMatcher.group(1);
                    String scriptingName = variableMatcher.group(2);
                    String format = variableMatcher.group(3);
                    String defaultValue = variableMatcher.group(4);
                    if (defaultValue == null) {
                        defaultValue = "";
                    }
                    Variable variable = new Variable();
                    variable.name = varName;
                    variable.scriptingName = scriptingName;
                    variable.format = format;
                    variable.defaultValue = defaultValue;
                    context.variables.put(varName, variable);
                }
            }
        }
    }

    private static void setLaneForElements(ProcessContext context) {
        for (final Map.Entry<String, Lane> lane : context.lanes.entrySet()) {
            List<String> flowNodeRefs = lane.getValue().flowNodeRefs;
            flowNodeRefs.forEach(nodeId -> {
                Node node = context.elements.get(nodeId);
                if (node != null) {
                    node.laneId = lane.getKey();
                }
            });
        }
    }

    private static void parseSequenceFlows(String xml, ProcessContext context) {
        Pattern flowPattern = Pattern.compile("<bpmn:sequenceFlow[^>]+id=\"([^\"]+)\"[^>]+sourceRef=\"([^\"]+)\"[^>]+targetRef=\"([^\"]+)\"(?:[^>]*name=\"([^\"]*)\")?[^>]*>");
        Matcher flowMatcher = flowPattern.matcher(xml);
        while (flowMatcher.find()) {
            String flowId = flowMatcher.group(1);
            String sourceRef = flowMatcher.group(2);
            String targetRef = flowMatcher.group(3);
            String label = flowMatcher.group(4);
            Connection connection = new Connection();
            connection.id = flowId;
            connection.type = "bpmn:SequenceFlow";
            connection.source = sourceRef;
            connection.target = targetRef;
            connection.label = label != null ? label : "";
            context.connections.put(flowId, connection);
        }
    }

    private static void parseElements(String xml, ProcessContext context) {
        Pattern elementPattern = Pattern.compile("<bpmn:(\\w+)[^>]*?>");
        Matcher elementMatcher = elementPattern.matcher(xml);
        while (elementMatcher.find()) {
            String fullTag = elementMatcher.group(0);
            String elementType = elementMatcher.group(1);
            if (!elementType.equals("process") &&
                    !elementType.equals("definitions") &&
                    !elementType.equals("sequenceFlow") &&
                    !elementType.contains("Diagram") &&
                    !elementType.equals("laneSet") &&
                    !elementType.equals("lane")
            ) {
                Pattern idPattern = Pattern.compile("id=\"([^\"]+)\"");
                Matcher idMatcher = idPattern.matcher(fullTag);
                if (!idMatcher.find()) {
                    continue;
                }
                String elementId = idMatcher.group(1);

                Pattern namePattern = Pattern.compile("name=\"([^\"]*)\"");
                Matcher nameMatcher = namePattern.matcher(fullTag);
                String elementName = nameMatcher.find() ? nameMatcher.group(1) : "";
                String bpmnType = "bpmn:" + elementType.toUpperCase().charAt(0) + elementType.substring(1);

                Node node = new Node();
                node.id = elementId;
                node.type = bpmnType;
                node.name = elementName;
                node.position = parseElementPosition(xml, elementId);
                node.sizing = MapHelpers.getElementSizing(bpmnType);
                node.properties = new HashMap<>();
                node.laneId = null;
                node.handlerClass = parseHandlerClass(xml, elementId);

                context.elements.put(elementId, node);
            }
        }
    }

    private static String parseHandlerClass(String xml, String elementId) {
        Pattern elementScriptTaskPattern = Pattern.compile("<bpmn:scriptTask id=" + elementId + "[^>]*>([\\s\\S]*?)</bpmn:scriptTask>", Pattern.CASE_INSENSITIVE);
        Matcher elementScriptTaskMatcher = elementScriptTaskPattern.matcher(xml);
        if (elementScriptTaskMatcher.find()) {
            String elementScriptTaskContent = elementScriptTaskMatcher.group(1);
            Pattern extensionElementsPattern = Pattern.compile("<extensionElements>([\\s\\S]*?)</extensionElements>");
            Matcher extensionElementsMatcher = extensionElementsPattern.matcher(elementScriptTaskContent);
            if (extensionElementsMatcher.find()) {
                String extensionElementsContent = extensionElementsMatcher.group(1);
                Pattern propertyHandlerPattern = Pattern.compile("<property name=\"class\" value=\"([\\s\\S]*?)\"/>");
                Matcher propertyHandlerMatcher = propertyHandlerPattern.matcher(extensionElementsContent);
                if (propertyHandlerMatcher.find()) {
                    return propertyHandlerMatcher.group(1);
                }
            }
        }
        return null;
    }

    private static Node.Position parseElementPosition(String xml, String elementId) {
        Pattern elementBoundsPattern = Pattern.compile("<bpmndi:BPMNShape[^>]*bpmnElement=" + elementId + "[^>]*>([\\s\\S]*?)</bpmndi:BPMNShape>", Pattern.CASE_INSENSITIVE);
        Matcher elementBoundsMatcher = elementBoundsPattern.matcher(xml);
        Node.Position position = new Node.Position(100, 100);
        if (elementBoundsMatcher.find()) {
            String boundsContent = elementBoundsMatcher.group(1);
            Pattern dcBoundsPattern = Pattern.compile("<dc:Bounds[^>]*x=\"([^\"]+)\"[^>]*y=\"([^\"]+)\"[^>]*width=\"([^\"]+)\"[^>]*height=\"([^\"]+)\"[^>]*/?>");
            Matcher dcBoundsMatcher = dcBoundsPattern.matcher(boundsContent);
            if (dcBoundsMatcher.find()) {
                position = new Node.Position(
                    Integer.parseInt(dcBoundsMatcher.group(1)),
                    Integer.parseInt(dcBoundsMatcher.group(2))
                );
            }
        }
        return position;
    }
}
