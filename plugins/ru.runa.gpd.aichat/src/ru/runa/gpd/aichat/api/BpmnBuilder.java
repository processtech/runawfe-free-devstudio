package ru.runa.gpd.aichat.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.runa.gpd.aichat.bpmnelements.Connection;
import ru.runa.gpd.aichat.bpmnelements.FormItem;
import ru.runa.gpd.aichat.bpmnelements.Lane;
import ru.runa.gpd.aichat.bpmnelements.Node;
import ru.runa.gpd.aichat.bpmnelements.ProcessContext;
import ru.runa.gpd.aichat.bpmnelements.Variable;
import ru.runa.gpd.aichat.utils.AutoHorizontalLayout;
import ru.runa.gpd.aichat.utils.BpmnXmlImporter;
import ru.runa.gpd.aichat.utils.IdGenerator;
import ru.runa.gpd.aichat.utils.MapHelpers;
import ru.runa.gpd.aichat.utils.TypeMappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BpmnBuilder implements IBpmnBuilder {
    private static Map<String, ProcessContext> processes = new HashMap<>();
    private ProcessContext currentProcessContext;

    public void newBpmn(JsonNode params) {
        String name = params.get("name").asText();
        String type = params.get("type").asText();
        currentProcessContext = createProcess(name, type);
    }

    public void openBpmn(JsonNode params) {
        String xml = params.get("xml").asText();
        String variablesXml = params.get("variablesXml").asText();
        currentProcessContext = BpmnXmlImporter.importXml(xml, variablesXml);
        processes.put(currentProcessContext.id, currentProcessContext);
    }

    public void addEvent(JsonNode params) {
        String eventType = params.get("eventType").asText();
        String name = params.get("name").asText();
        JsonNode form =  params.has("form") ? params.get("form") : null;
        List<FormItem<String, String>> formList = new ArrayList<>();
        if (form != null) {
            for (JsonNode item: form) {
                item.fields().forEachRemaining(
                        entry -> formList.add(new FormItem<>(entry.getKey(), entry.getValue().asText()))
                );
            }
        }

        String bpmnType = TypeMappings.mapEventType(eventType);
        Node node = new Node();
        node.type = bpmnType;
        node.name = name;
        node.form = formList;
        createElement(node);
    }

    public void addActivity(JsonNode params) {
        String activityType = params.get("activityType").asText();
        String name = params.get("name").asText();
        String bpmnType = TypeMappings.mapActivityType(activityType);
        String handlerRaw = params.has("handler") ? params.get("handler").asText() : null;
        String handlerClass = TypeMappings.mapToHandlerClass(handlerRaw);
        String configRaw = params.has("config") ? params.get("config").asText() : "";
        String variableFileName = params.has("variableFileName") ? params.get("variableFileName").asText() : "";
        String template = params.has("template") ? params.get("template").asText() : "";
        String executor = params.has("executor") ? params.get("executor").asText() : "";
        String format = params.has("format") ? params.get("format").asText() : "";
        String result = params.has("result") ? params.get("result").asText() : "";
        JsonNode form =  params.has("form") ? params.get("form") : null;
        List<FormItem<String, String>> formList = new ArrayList<>();
        if (form != null) {
            for (JsonNode item: form) {
                item.fields().forEachRemaining(
                        entry -> formList.add(new FormItem<>(entry.getKey(), entry.getValue().asText()))
                );
            }
        }

        Node node = new Node();
        node.type = bpmnType;
        node.name = name;
        node.handlerClass = handlerClass;
        node.config = configRaw;
        node.variableFileName = variableFileName;
        node.template = template;
        node.executor = executor;
        node.format = format;
        node.result = result;
        node.form = formList;
        createElement(node);
    }

    public void addGateway(JsonNode params) {
        String gatewayType = params.get("gatewayType").asText();
        String name = params.get("name").asText();
        String bpmnType = TypeMappings.mapGatewayType(gatewayType);

        Node node = new Node();
        node.type = bpmnType;
        node.name = name;
        createElement(node);
    }

    public void connect(JsonNode params) throws Exception {
        final String sourceName = params.get("sourceName").asText();
        String targetName = params.get("targetName").asText();
        String label = params.has("label") ? params.get("label").asText() : null;

        String sourceId = null;
        String targetId = null;

        for (Map.Entry<String, Node> elementEntry: currentProcessContext.elements.entrySet()) {
            if (elementEntry.getValue().name.equals(sourceName)) {
                sourceId = elementEntry.getKey();
            }
            if (elementEntry.getValue().name.equals(targetName)) {
                targetId = elementEntry.getKey();
            }
        }
        if (sourceId == null || targetId == null) {
            throw new Exception("Not found elements with ids: " + sourceId + " or " + targetId);
        }

        String flowId = IdGenerator.generate("Flow");

        Connection connection = new Connection();
        connection.id = flowId;
        connection.source = sourceId;
        connection.target = targetId;
        connection.type = "bpmn:SequenceFlow";
        connection.label = label;

        currentProcessContext.connections.put(flowId, connection);
    }

    public void deleteConnect(JsonNode params) {
        String label = params.get("label").asText();
        currentProcessContext.connections.entrySet()
                .stream()
                .filter(conn -> conn.getValue().label != null && conn.getValue().label.equals(label))
                .map(Map.Entry::getKey)
                .findFirst()
                .ifPresent(connectId -> currentProcessContext.connections.remove(connectId));
    }

    public void addLane(JsonNode params) {
        String name = params.get("name").asText();
        String position = "bottom";
        String initializerType = params.get("initializer_type").asText();
        String initializer = params.get("initializer").asText();
        String roleSource = params.has("role_source") ? params.get("role_source").asText() : null;
        final List<String> flowNodeRefs = new ObjectMapper().convertValue(params.get("flowNodeRefs"), new TypeReference<List<String>>() {});

        List<String> flowNodeRefIds = currentProcessContext.elements.entrySet()
                .stream()
                .filter(elementEntry -> flowNodeRefs.contains(elementEntry.getValue().name))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        final String finalLaneId = IdGenerator.generate("Lane");

        Integer laneX = 20;
        Integer laneY = 20;
        Integer laneWidth = 800;
        Integer laneHeight = 180;

        List<Lane> existingLanes = new ArrayList<>(currentProcessContext.lanes.values());
        Integer maxY = 20;
        for (Lane lane : existingLanes) {
            Integer bottom = lane.bounds.y + lane.bounds.height;
            if (bottom > maxY) {
                maxY = bottom;
            }
        }
        laneY = maxY + 20;
        laneWidth = Math.max(
                laneWidth,
                (!existingLanes.isEmpty() && existingLanes.get(0) != null && existingLanes.get(0).bounds != null) ? existingLanes.get(0).bounds.width : 800
        );

        flowNodeRefIds.forEach(nodeId -> {
            Node node = currentProcessContext.elements.get(nodeId);
            if (node != null) {
                node.laneId = finalLaneId;
            }
        });

        if (currentProcessContext.lanes == null) {
            currentProcessContext.lanes = new HashMap<>();
        }

        Lane lane = new Lane();
        lane.id = finalLaneId;
        lane.name = name;
        lane.flowNodeRefs = flowNodeRefIds;
        lane.initializer = new Lane.Initializer(
                initializer.equals("null") ? null : initializer,
                initializerType.equals("null") ? null : initializerType,
                roleSource
        );
        lane.bounds = new Lane.Bounds(laneX, laneY, laneWidth, laneHeight);

        currentProcessContext.lanes.put(finalLaneId, lane);
    }

    private void deleteNodeAndConnections(String elementId) throws Exception {
        if (currentProcessContext.elements.get(elementId) == null) {
            throw new Exception("Not found element with id: " + elementId + " in process");
        }
        currentProcessContext.elements.remove(elementId);
        currentProcessContext.connections.entrySet()
                .removeIf(conn -> conn.getValue().source.equals(elementId) || conn.getValue().target.equals(elementId));
    }

    public void addVariable(JsonNode params) {
        String name = params.get("name").asText();
        String format = params.get("format").asText();
        String scriptingName = params.has("scriptingName") ? params.get("scriptingName").asText() : null;

        if (currentProcessContext.variables == null) {
            currentProcessContext.variables = new HashMap<>();
        }
        String finalScriptingName = scriptingName == null ? name.replace(" ", "_") : scriptingName.replace(" ", "_");
        String fullFormat = MapHelpers.getRunaFormat(format);

        Variable variable = new Variable();
        variable.name = name;
        variable.scriptingName = finalScriptingName;
        variable.format = fullFormat;

        currentProcessContext.variables.put(name, variable);
    }

    public void deleteElement(JsonNode params) throws Exception {
        String name = params.get("name").asText();
        String elementId = currentProcessContext.elements.entrySet()
                .stream()
                .filter(element -> element.getValue().name.equals(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (elementId != null) {
            deleteNodeAndConnections(elementId);
            return;
        }

        if (!currentProcessContext.lanes.isEmpty()) {
            String laneId = currentProcessContext.lanes.entrySet()
                    .stream()
                    .filter(lane -> lane.getValue().name.equals(name))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (laneId != null) {
                deleteLane(laneId);
                return;
            }
        }

        if (currentProcessContext.variables.get(name) != null) {
            currentProcessContext.variables.remove(name);
            return;
        }

        throw new Exception("Not found element with name: " + name);
    }

    public String getVariablesXml() {
        if (currentProcessContext == null || currentProcessContext.variables == null || currentProcessContext.variables.isEmpty()) {
            return "<variables/>";
        }

        StringBuilder xml = new StringBuilder("<variables>\n");
        for (Variable variable: currentProcessContext.variables.values()) {
            xml.append("  <variable name=\"").append(variable.name).append("\" scriptingName=\"").append(variable.scriptingName).append("\" format=\"").append(variable.format).append("\"");
            if (variable.defaultValue != null) {
                xml.append(" defaultValue=\"").append(variable.defaultValue).append("\"");
            }
            xml.append("/>\n");
        }
        xml.append("</variables>");
        return xml.toString();
    }

    public void autoLayout() {
        String algorithm = "horizontal";
        AutoHorizontalLayout.applyLayout(currentProcessContext);
    }

    public String exportXml() {
        return generateXmlWithElements();
    }

    private void deleteLane(final String laneId) throws Exception {
        if (currentProcessContext.lanes.get(laneId) == null) {
            throw new Exception("Lane with id: " + laneId + " not found");
        }
        currentProcessContext.lanes.remove(laneId);
        currentProcessContext.elements.forEach((key, value) -> {
            if (value.laneId != null && value.laneId.equals(laneId)) {
                value.laneId = null;
            }
        });
    }

    private ProcessContext createProcess(String name, String type) {
        String processId = IdGenerator.generate("Process");
        ProcessContext context = new ProcessContext();
        context.id = processId;
        context.name = name;
        context.type = type;
        context.elements = new HashMap<>();
        context.connections = new HashMap<>();
        context.variables = new HashMap<>();
        context.lanes = new HashMap<>();
        processes.put(processId, context);
        return context;
    }

    private Node createElement(Node nodeDef) {
        String elementId = IdGenerator.generate(nodeDef.type.split(":")[1]);
        Node.Position position = nodeDef.position;
        if (position == null) {
            int elementCount = currentProcessContext.elements.size();
            position = new Node.Position(100 + (elementCount * 50), 200);
        }

        Node node = new Node();
        node.id = elementId;
        node.type = nodeDef.type;
        node.name = nodeDef.name;
        node.handlerClass = nodeDef.handlerClass;
        node.config = nodeDef.config;
        node.variableFileName = nodeDef.variableFileName;
        node.template = nodeDef.template;
        node.position = position;
        node.laneId = nodeDef.laneId;
        node.executor = nodeDef.executor;
        node.format = nodeDef.format;
        node.result = nodeDef.result;
        node.form = nodeDef.form;
        node.properties = nodeDef.properties == null ? new HashMap<>() : nodeDef.properties;

        currentProcessContext.elements.put(elementId, node);
        return node;
    }

    private String generateXmlWithElements() {
        Collection<Node> nodes = currentProcessContext.elements.values();
        Collection<Connection> connections = currentProcessContext.connections.values();
        Collection<Lane> lanes = currentProcessContext.lanes.values();

        final StringBuilder processXml = new StringBuilder();
        final StringBuilder diagramXml = new StringBuilder();
        generateLaneSetXml(lanes, processXml, diagramXml);
        generateElementsXml(nodes, connections, processXml, diagramXml);
        generateConnectionsXml(connections, nodes, processXml, diagramXml);

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "    <bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" \n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "      xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" \n" +
                "      xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" \n" +
                "      xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"\n" +
                "      id=\"Definitions_1\" \n" +
                "      targetNamespace=\"http://bpmn.io/schema/bpmn\">\n" +
                "      <bpmn:process id=\"" + currentProcessContext.id + "\" name=\"" + currentProcessContext.name + "\" isExecutable=\"true\">\n" +
                "    " + processXml +"  </bpmn:process>\n" +
                "      <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
                "        <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"" + currentProcessContext.id + "\">\n" +
                "    " + diagramXml +"    </bpmndi:BPMNPlane>\n" +
                "      </bpmndi:BPMNDiagram>\n" +
                "    </bpmn:definitions>";
    }

    private void generateLaneSetXml(Collection<Lane> lanes, StringBuilder processXml, StringBuilder diagramXml) {
        if (!lanes.isEmpty()) {
            processXml.append("    <bpmn:laneSet id=\"LaneSet_1\">\n");
            lanes.forEach(lane -> {
                processXml.append("      <bpmn:lane id=\"").append(lane.id).append("\" name=\"").append(lane.name).append("\">\n");
                lane.flowNodeRefs.forEach(flowNodeRef -> {
                    processXml.append("        <bpmn:flowNodeRef>").append(flowNodeRef).append("</bpmn:flowNodeRef>\n");
                });
                if (lane.initializer.type != null) {
                    processXml.append("        <extensionElements>\n");
                    if (lane.initializer.type.equals("group")) {
                        processXml.append("        <property name=\"config\"><![CDATA[ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction(").append(lane.initializer.name).append(")]]></property>\n");
                    } else if (lane.initializer.type.equals("relation")) {
                        processXml.append("        <property name=\"config\"><![CDATA[@").append(lane.initializer.name).append("(").append(lane.initializer.roleSource).append(")]]></property>\n");
                    }
                    processXml.append("        </extensionElements>\n");
                }
                processXml.append("      </bpmn:lane>\n");
                diagramXml.append("      <bpmndi:BPMNShape id=\"").append(lane.id).append("_di\" bpmnElement=\"").append(lane.id).append("\" isHorizontal=\"true\">\n")
                        .append("          <dc:Bounds x=\"").append(lane.bounds.x).append("\" y=\"").append(lane.bounds.y).append("\" width=\"").append(lane.bounds.width).append("\" height=\"").append(lane.bounds.height).append("\" />\n")
                        .append("        </bpmndi:BPMNShape>\n");
            });
            processXml.append("    </bpmn:laneSet>\n");
        }
    }

    private String escapeXmlAttribute(String value) {
        if (value == null) return null;
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    private void generateElementsXml(final Collection<Node> nodes, final Collection<Connection> connections, final StringBuilder processXml, final StringBuilder diagramXml) {
        nodes.forEach(node -> {
            String tagName = MapHelpers.getXmlTagName(node.type);
            processXml.append("    <bpmn:").append(tagName).append(" id=\"").append(node.id).append("\" name=\"").append(escapeXmlAttribute(node.name)).append("\">\n");

            if (node.type.equals("bpmn:UserTask") || node.type.equals("bpmn:StartEvent")) {
                if (node.form != null && !node.form.isEmpty()) {
                    processXml.append("      <extensionElements>\n");
                    for (FormItem<String, String> formItem: node.form) {
                        processXml.append("        <property name=\"" + formItem.getName() + "\" value=\"").append(formItem.getValue()).append("\"/>\n");
                    }
                    processXml.append("      </extensionElements>\n");
                }
            }

            if (node.type.equals("bpmn:ScriptTask")) {
                if (node.handlerClass != null && !node.handlerClass.isEmpty()) {
                    processXml.append("      <extensionElements>\n");
                    processXml.append("        <property name=\"class\" value=\"").append(node.handlerClass).append("\"/>\n");
                    if (node.handlerClass.equals("ru.runa.wfe.office.doc.DocxHandler")) {
                        processXml.append("        <property name=\"docxConfig\" variable=\"").append(node.variableFileName).append("\" template=\"").append(node.template).append("\"/>\n");
                    } else if (node.handlerClass.equals("ru.runa.wfe.extension.handler.var.FormulaActionHandler")) {
                        processXml.append("        <property name=\"config\"><![CDATA[").append(node.config).append("]]></property>\n");
                    } else if (node.handlerClass.equals("ru.runa.wfe.extension.handler.user.GetExecutorInfoHandler")) {
                        processXml.append("        <property name=\"config\"><![CDATA[").append("<config>\n" +
                                "  <input>\n" +
                                "    <param name=\"executor\" variable=\"" + node.executor + "\"/>\n" +
                                "    <param name=\"format\" value=\"" + node.format + "\"/>\n" +
                                "  </input>\n" +
                                "  <output>\n" +
                                "    <param name=\"result\" variable=\"" + node.result + "\"/>\n" +
                                "  </output>\n" +
                                "</config>").append("]]></property>\n");
                    }
                    processXml.append("      </extensionElements>\n");
                }
            }

            List<Connection> incoming = connections.stream().filter(conn -> conn.target.equals(node.id)).collect(Collectors.toList());
            List<Connection> outgoing = connections.stream().filter(conn -> conn.source.equals(node.id)).collect(Collectors.toList());

            incoming.forEach(conn -> processXml.append("      <bpmn:incoming>").append(conn.id).append("</bpmn:incoming>\n"));
            outgoing.forEach(conn -> processXml.append("      <bpmn:outgoing>").append(conn.id).append("</bpmn:outgoing>\n"));

            processXml.append("    </bpmn:").append(tagName).append(">\n");

            Node.Sizing sizing = MapHelpers.getElementSizing(node.type);
            Integer x = node.position.x == null ? 100 : node.position.x;
            Integer y = node.position.y == null ? 100 : node.position.y;

            diagramXml.append("      <bpmndi:BPMNShape id=\"").append(node.id).append("_di\" bpmnElement=\"").append(node.id).append("\">\n")
                    .append("          <dc:Bounds x=\"").append(x).append("\" y=\"").append(y).append("\" width=\"").append(sizing.width).append("\" height=\"").append(sizing.height).append("\" />\n")
                    .append("        </bpmndi:BPMNShape>\n");
        });
    }

    private void generateConnectionsXml(final Collection<Connection> connections, final Collection<Node> nodes, final StringBuilder processXml, final StringBuilder diagramXml) {
        connections.forEach(conn -> {
            Optional<Node> sourceElement = nodes.stream().filter(elem -> elem.id.equals(conn.source)).findFirst();
            Optional<Node> targetElement = nodes.stream().filter(elem -> elem.id.equals(conn.target)).findFirst();
            String connLabel = conn.label != null ? " name=\"" + escapeXmlAttribute(conn.label) + "\"" : "";
            processXml.append("    <bpmn:sequenceFlow id=\"").append(conn.id).append("\" sourceRef=\"").append(conn.source).append("\" targetRef=\"").append(conn.target).append("\"").append(connLabel).append(" />\n");

            if (sourceElement.isPresent() && targetElement.isPresent()) {
                Node.Sizing sourceSizing = MapHelpers.getElementSizing(sourceElement.get().type);
                Node.Sizing targetSizing = MapHelpers.getElementSizing(targetElement.get().type);

                int sourceX = sourceElement.get().position.x == null ? 100 : sourceElement.get().position.x;
                int sourceY = sourceElement.get().position.y == null ? 100 : sourceElement.get().position.y;
                int targetX = targetElement.get().position.x == null ? 250 : targetElement.get().position.x;
                int targetY = targetElement.get().position.y == null ? 250 : targetElement.get().position.y;

                int startX = sourceX + sourceSizing.width;
                int startY = sourceY + (sourceSizing.height / 2);
                int endX = targetX;
                int endY = targetY + (targetSizing.height / 2);

                diagramXml.append("      <bpmndi:BPMNEdge id=\"").append(conn.id).append("_di\" bpmnElement=\"").append(conn.id).append("\">\n")
                        .append("          <di:waypoint x=\"").append(startX).append("\" y=\"").append(startY).append("\" />\n")
                        .append("          <di:waypoint x=\"").append(endX).append("\" y=\"").append(endY).append("\" />\n")
                        .append("        </bpmndi:BPMNEdge>\n");
            }
        });
    }
}
