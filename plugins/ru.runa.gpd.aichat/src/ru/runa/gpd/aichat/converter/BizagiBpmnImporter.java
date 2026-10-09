package ru.runa.gpd.aichat.converter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.eclipse.swt.widgets.Display;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.QName;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PrecisionPoint;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
//import ru.runa.gpd.bizagi.dialog.ShowBizagiImportProblemsDialog;
import ru.runa.gpd.aichat.utils.DocxCreator;
import ru.runa.gpd.aichat.utils.SystemPromptReader;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.graphiti.GraphitiEntry;
import ru.runa.gpd.lang.BpmnSerializer;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.ProcessSerializer;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.EventSubprocess;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.bpmn.BusinessRule;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.DataStore;
import ru.runa.gpd.lang.model.bpmn.EndEventType;
//import ru.runa.gpd.lang.model.bpmn.EventBasedGateway;
import ru.runa.gpd.lang.model.bpmn.EventNodeType;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.StartEventType;
import ru.runa.gpd.lang.model.bpmn.TextAnnotation;
import ru.runa.gpd.lang.model.bpmn.ThrowEventNode;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.lang.par.VariablesXmlContentProvider;
import ru.runa.gpd.quick.formeditor.QuickForm;
import ru.runa.gpd.quick.formeditor.QuickFormComponent;
import ru.runa.gpd.quick.formeditor.util.QuickFormXMLUtil;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.TemplateUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Path;

@SuppressWarnings("unchecked")
/**
 * Временное решение. В дальнейшем планируется конвертировать json, который вернул ИИ, сразу в runa bpmn формат.
 */
public class BizagiBpmnImporter implements GEFConstants {

    private static final double SCALE_FACTOR = 2;

    private static final String TARGET_NAMESPACE = "targetNamespace";
    private static final String BPMN_TARGET_NAMESPACE = "http://bpmn.io/schema/bpmn";

    private static final String DEFAULT = "default";
    private static final String ASSOCIATION = "association";
    private static final String Y = "y";
    private static final String X = "x";
    private static final String HEIGHT = "height";
    private static final String WIDTH = "width";
    private static final String BOUNDS = "Bounds";
    private static final String WAYPOINT = "waypoint";
    private static final String TARGET_REF = "targetRef";
    private static final String SOURCE_REF = "sourceRef";
    private static final String ATTACHED_TO_REF = "attachedToRef";
    private static final String DOCUMENTATION = "documentation";
    private static final String LANE = "lane";
    private static final String LANE_SET = "laneSet";
    private static final String IS_HORIZONTAL = "isHorizontal";
    private static final String TRUE = "true";
    private static final String ID = "id";
    private static final String PROCESS = "process";
    private static final String NAME = "name";
    private static final String IS_MAIN_PARTICIPANT = "isMainParticipant";
    private static final String EXTENSION_ELEMENTS = "extensionElements";
    private static final String PARTICIPANT = "participant";
    private static final String COLLABORATION = "collaboration";
    private static final String BPMN_ELEMENT = "bpmnElement";
    private static final String BPMN_PLANE = "BPMNPlane";
    private static final String BPMN_DIAGRAM = "BPMNDiagram";
    private static final String PARALLEL_MULTIPLE = "parallelMultiple";
    private static final String MESSAGE_EVENT_DEFINITION = "messageEventDefinition";
    private static final String ERROR_EVENT_DEFINITION = "errorEventDefinition";
    private static final String TIMER_EVENT_DEFINITION = "timerEventDefinition";
    private static final String TERMINATE_EVENT_DEFINITION = "terminateEventDefinition";
    private static final String BIZAGI_NAMESPACE = "http://www.bizagi.com/bpmn20";
    private static final String BIZAGI_PROPERTY = "BizagiProperty";
    private static final String BIZAGI_PROPERTIES = "BizagiProperties";
    private static final String BIZAGI_EXTENDED_ATTRIBUTE_VALUES = "BizagiExtendedAttributeValues";
    private static final String BIZAGI_EXTENDED_ATTRIBUTE_VALUE = "BizagiExtendedAttributeValue";
    private static final String BIZAGI_EXTENSIONS = "BizagiExtensions";
    private static final String DATA_OBJECT = "dataObject";
    private static final Dimension MINIMIZED_NODE_SIZE = new Dimension(3 * GRID_SIZE, 3 * GRID_SIZE);

    private static Map<String, Element> planeMap = new HashMap<>();
    private static Map<String, GraphElement> geMap = new HashMap<>();
    private static Map<Rectangle, Swimlane> swimlaneBoundMap = new HashMap<>();
    private static Map<String, IFile> idOfVariablesForTasks = new HashMap<>();
    private static Set<Subprocess> embeddedSubprocesses = new HashSet<>();
    private static Set<String> defaultIds = new HashSet<>();
    private static List<String> bizagiIrrelevantImportedIds;
    private static Map<String, String> bizagiIrrelevantImportedElements;
    private static List<String> bizagiIrrelevantImportedProcessNames;

    public static IFolder go(IContainer dstFolder, String srcFileName, boolean showSwimlanes, boolean ignoreBendPoints, String variablesXml) throws Exception {
        String processName = "";
        IFolder createdFolder = null;
        Map<String, List<Swimlane>> nodeToLaneMap = new HashMap<>();
        Map<String, Swimlane> laneIdToSwimlaneMap = new HashMap<>();
        bizagiIrrelevantImportedIds = new ArrayList<>();
        bizagiIrrelevantImportedElements = new HashMap<>();
        bizagiIrrelevantImportedProcessNames = new ArrayList<>();
        try (InputStream is = new FileInputStream(new File(srcFileName))) {
            Document bpmnDocument = XmlUtil.parseWithoutValidation(is);
            Element definitionsElement = bpmnDocument.getRootElement();
            scaleFactor = (BPMN_TARGET_NAMESPACE.equals(definitionsElement.attributeValue(TARGET_NAMESPACE)) ? 1 : SCALE_FACTOR);
            planeMap.clear();
            for (Element bpmnDiagram : (List<Element>) definitionsElement.elements(BPMN_DIAGRAM)) {
                Element bpmnPlane = bpmnDiagram.element(BPMN_PLANE);
                if (bpmnPlane != null) {
                    for (Element element : (List<Element>) bpmnPlane.elements()) {
                        planeMap.put(element.attributeValue(BPMN_ELEMENT), element);
                    }
                }
            }
            Element processElement = null;
            SwimlaneDisplayMode swimlaneDisplayMode = SwimlaneDisplayMode.none;
            Element collaborationElement = definitionsElement.element(COLLABORATION);
            if (collaborationElement != null) {
                processName = collaborationElement.attributeValue(NAME);
                if (processName == null) {
                    processName = collaborationElement.attributeValue("id");
                }

                List<Element> participants = collaborationElement.elements(PARTICIPANT);
                nextParticipant: for (Element participant : (List<Element>) collaborationElement.elements(PARTICIPANT)) {
                    if (Strings.isNullOrEmpty(processName)) {
                        processName = participant.attributeValue(NAME);
                    }

                    Element participantShape = planeMap.get(participant.attributeValue(ID));
                    if (participantShape != null) {
                        swimlaneDisplayMode = showSwimlanes
                                ? (TRUE.equals(participantShape.attributeValue(IS_HORIZONTAL)) ? SwimlaneDisplayMode.horizontal
                                : SwimlaneDisplayMode.vertical)
                                : SwimlaneDisplayMode.none;
                    }

                    Element extensionElements = participant.element(EXTENSION_ELEMENTS);
                    if (extensionElements != null) {
                        Element bizagiExtensionElements = extensionElements.element(QName.get(BIZAGI_EXTENSIONS, BIZAGI_NAMESPACE));
                        if (bizagiExtensionElements != null) {
                            Element bizagiProperties = bizagiExtensionElements.element(QName.get(BIZAGI_PROPERTIES, BIZAGI_NAMESPACE));
                            if (bizagiProperties != null) {
                                List<Element> properties = bizagiProperties.elements(QName.get(BIZAGI_PROPERTY, BIZAGI_NAMESPACE));
                                for (Element property : properties) {
                                    if (IS_MAIN_PARTICIPANT.equals(property.attributeValue(NAME))) {
                                        continue nextParticipant;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            for (Element process : (List<Element>) definitionsElement.elements(PROCESS)) {
                processElement = process;
                processName = process.attributeValue(NAME);
                if (processName == null) {
                    processName = process.attributeValue(ID);
                }
                IFolder dst = (IFolder) dstFolder;
                IFile processDefFile = dst.getFile("processdefinition.xml");
                if (processDefFile.exists()) {
                    createdFolder = dst;
                    processName = dst.getName();

                    ProcessDefinition existingDefinition = ProcessCache.getProcessDefinition(processDefFile);
                    for (Swimlane swimlane : existingDefinition.getSwimlanes()) {
                        existingDefinition.removeChild(swimlane);
                    }

                    try {
                        for (IResource resource : dst.members()) {
                            if (resource instanceof IFile) {
                                String fileName = resource.getName();
                                if (!fileName.equals("aichat.txt") && !fileName.equals("dialog_history.json") && !fileName.equals("current_variables_xml.txt") && !fileName.equals("current_bpmn_xml.txt")) {
                                    resource.delete(true, null);
                                    //PluginLogger.logInfo("Deleted file: " + fileName);
                                } else {
                                    //PluginLogger.logInfo("Preserved file: " + fileName);
                                }
                            }
                        }
                    } catch (Exception e) {
                        PluginLogger.logError("Failed to delete files in folder", e);
                    }
                } else {
                    createdFolder = IOUtils.getProcessFolder(dstFolder, processName);
                    createdFolder.create(true, true, null);
                }
                IFile definitionFile = IOUtils.getProcessDefinitionFile(createdFolder);
                if (definitionFile.exists()) {
                    definitionFile.delete(true, null);
                }
                Map<String, String> properties = Maps.newHashMap();
                properties.put(BpmnSerializer.SHOW_SWIMLANE, swimlaneDisplayMode.name());
                properties.put(ProcessSerializer.ACCESS_TYPE, ProcessDefinitionAccessType.Process.name());
                Document document = Language.BPMN.getSerializer().getInitialProcessDefinitionDocument(createdFolder.getName(), properties);
                byte[] bytes = XmlUtil.writeXml(document);
                definitionFile.create(new ByteArrayInputStream(bytes), true, null);
                ProcessDefinition definition = ProcessCache.getProcessDefinition(definitionFile);
                definition.setName(processName);
                definition.setLanguage(Language.BPMN);
                swimlaneBoundMap.clear();
                Element laneSet = processElement.element(LANE_SET);
                if (laneSet != null) {
                    for (Element lane : (List<Element>) laneSet.elements(LANE)) {
                        Swimlane swimlane = new Swimlane();
                        String laneId = lane.attributeValue(ID);
                        String laneName = lane.attributeValue(NAME);
                        swimlane.setName(Strings.isNullOrEmpty(laneName) ? laneId : laneName);

                        Element extensionElements = lane.element("extensionElements");
                        if (extensionElements != null) {
                            List<Element> laneProperties = extensionElements.elements("property");
                            for (Element property: laneProperties) {
                                String propName = property.attributeValue("name");
                                if ("config".equals(propName)) {
                                    String propContent = property.getText();
                                    if (propContent != null && !propContent.isEmpty()) {
                                        swimlane.setDelegationClassName("ru.runa.wfe.extension.assign.DefaultAssignmentHandler");
                                        swimlane.setDelegationConfiguration(propContent);
                                        if (propContent.startsWith("@")) {
                                            swimlane.setEditorPath("SwimlaneElement.RelationLabel");
                                        }
                                        //PluginLogger.logInfo("Loaded propContent for lane " + laneName + ": " + propContent);
                                    }
                                }
                            }

                        }

                        Rectangle bounds = bounds(laneId);
                        if (showSwimlanes) {
                            swimlane.setConstraint(bounds);
                        }
                        definition.addChild(swimlane);
                        swimlaneBoundMap.put(bounds, swimlane);
                        laneIdToSwimlaneMap.put(laneId, swimlane);

                        List<Element> flowNodeRefs = lane.elements("flowNodeRef");
                        for (Element flowNodeRef : flowNodeRefs) {
                            String nodeId = flowNodeRef.getText();
                            if (!nodeToLaneMap.containsKey(nodeId)) {
                                nodeToLaneMap.put(nodeId, new ArrayList<>());
                            }
                            nodeToLaneMap.get(nodeId).add(swimlane);
                            //PluginLogger.logInfo("Node " + nodeId + " assigned to lane " + laneName);
                        }
                    }
                    //PluginLogger.logInfo("=== nodeToLaneMap contents (" + nodeToLaneMap.size() + " entries) ===");
                    for (Map.Entry<String, List<Swimlane>> entry : nodeToLaneMap.entrySet()) {
                        String laneName = entry.getValue().isEmpty() ? "empty" : entry.getValue().get(0).getName();
                        //PluginLogger.logInfo("  Node ID: " + entry.getKey() + " -> Lane: " + laneName);
                    }
                }
                geMap.clear();
                embeddedSubprocesses.clear();
                defaultIds.clear();
                List<Element> sequenceFlows = new ArrayList<>();
                List<Element> elements = processElement.elements();
                int n = -1;

                for (Element element : elements) {
                    try {
                        String elementName = element.getName();
                        String id = element.attributeValue(ID);
                        String name = (Strings.isNullOrEmpty(element.attributeValue(NAME))) ? "N" + n++ : element.attributeValue(NAME);
                        String documentation = element.elementText(DOCUMENTATION);
                        defaultIds.add(element.attributeValue(DEFAULT));
                        switch (elementName) {
                        case "startEvent": {
                            StartState start = new StartState();
                            start.setName(name);
                            start.setDescription(documentation);
                            setConstraint(start, id);

                            Swimlane swimlane = getSwimlaneByNodeId(id, nodeToLaneMap, start, showSwimlanes);
                            start.setSwimlane(swimlane);
                            if (showSwimlanes && swimlane != null) {
                                start.setUiParentContainer(swimlane);
                                start.getConstraint().translate(swimlane.getConstraint().getTopLeft().negate());
                            }
                            definition.addChild(start);
                            geMap.put(id, start);

                            if (element.element("messageEventDefinition") != null && element.element("signalEventDefinition") != null) {
                                if (element.attribute(PARALLEL_MULTIPLE) != null) {
                                    addBizagiIrrelevantImportedId(start.getId(), "parallelMultipleEventDefinition", processName);
                                } else {
                                    addBizagiIrrelevantImportedId(start.getId(), "multipleEventDefinition", processName);
                                }
                            } else if (element.element("messageEventDefinition") != null) {
                                start.setEventType(StartEventType.message);
                            } else if (element.element("signalEventDefinition") != null) {
                                start.setEventType(StartEventType.signal);
                            } else if (element.element("conditionalEventDefinition") != null) {
                                addBizagiIrrelevantImportedId(start.getId(), "conditionalEventDefinition", processName);
                            } else if (element.element("timerEventDefinition") != null) {
                                start.setEventType(StartEventType.timer);
                            }

                            QuickForm quickForm = new QuickForm();
                            Element extensionElements = element.element(EXTENSION_ELEMENTS);
                            if (extensionElements != null) {
                                List<Element> userTaskProperties = extensionElements.elements("property");
                                for (Element property : userTaskProperties) {
                                    String propName = property.attributeValue("name");
                                    String propParam = property.attributeValue("value");
                                    QuickFormComponent quickFormComponent = new QuickFormComponent();
                                    quickFormComponent.setTagName(propName);
                                    if (propName.equals("DisplayVariable")) {
                                        quickFormComponent.setParams(Arrays.<Object>asList(propParam, true));
                                    } else {
                                        quickFormComponent.setParams(Collections.<Object>singletonList(propParam));
                                    }
                                    quickForm.getVariables().add(quickFormComponent);
                                }
                                createQuickForm(start, definition, quickForm);
                            }

                            //PluginLogger.logInfo("    getSwimlaneByNodeId returned: " + (swimlane != null ? swimlane.getName() : "NULL"));
                            break;
                        }
                        case "sendTask": {
                            ScriptTask task = new ScriptTask();
                            task.setName(name);
                            task.setDescription(documentation);
                            task.setConstraint(bounds(id));
                            task.setDelegationClassName("ru.runa.wfe.extension.handler.SendEmailActionHandler");
                            adjustMinimized(task);
                            definition.addChild(task);
                            geMap.put(id, task);
                            break;
                        }
                        case "serviceTask":
                        case "task":
                        case "manualTask":
                        case "receiveTask":
                        case "userTask": {
                            TaskState task = new TaskState();
                            task.setName(name);
                            task.setDescription(documentation);

                            NodeTypeDefinition typeDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
                            GraphitiEntry entry = typeDefinition.getGraphitiEntry();
                            Dimension defaultSize = entry.getDefaultSize(task);

                            Rectangle bounds = bounds(id);
                            bounds.width = defaultSize.width;
                            bounds.height = defaultSize.height;
                            task.setConstraint(bounds);

                            //task.setConstraint(bounds(id));
                            adjustMinimized(task);
                            Swimlane swimlane = getSwimlaneByNodeId(id, nodeToLaneMap, task, showSwimlanes);
                            task.setSwimlane(swimlane);
                            if (showSwimlanes && swimlane != null) {
                                task.setUiParentContainer(swimlane);
                                task.getConstraint().translate(swimlane.getConstraint().getTopLeft().negate());
                            }
                            definition.addChild(task);
                            geMap.put(id, task);

                            QuickForm quickForm = new QuickForm();
                            Element extensionElements = element.element(EXTENSION_ELEMENTS);
                            if (extensionElements != null) {
                                List<Element> userTaskProperties = extensionElements.elements("property");
                                for (Element property : userTaskProperties) {
                                    String propName = property.attributeValue("name");
                                    String propParam = property.attributeValue("value");
                                    QuickFormComponent quickFormComponent = new QuickFormComponent();
                                    quickFormComponent.setTagName(propName);
                                    if (propName.equals("DisplayVariable")) {
                                        quickFormComponent.setParams(Arrays.<Object>asList(propParam, "true"));
                                    } else {
                                        quickFormComponent.setParams(Collections.<Object>singletonList(propParam));
                                    }
                                    quickForm.getVariables().add(quickFormComponent);
                                }
                                createQuickForm(task, definition, quickForm);
                            }


                            break;
                        }
                        case "exclusiveGateway": {
                            ExclusiveGateway eg = new ExclusiveGateway();
                            eg.setName(name);
                            eg.setDescription(documentation);

                            NodeTypeDefinition typeDefinition = NodeRegistry.getNodeTypeDefinition(ExclusiveGateway.class);
                            GraphitiEntry entry = typeDefinition.getGraphitiEntry();
                            Dimension defaultSize = entry.getDefaultSize(eg);

                            Rectangle bounds = bounds(id);
                            bounds.width = defaultSize.width;
                            bounds.height = defaultSize.height;
                            eg.setConstraint(bounds);


                            //setConstraint(eg, id);
                            definition.addChild(eg);
                            geMap.put(id, eg);
                            break;
                        }
//                        case "eventBasedGateway": {
//                            EventBasedGateway eg = new EventBasedGateway();
//                            eg.setName(name);
//                            eg.setDescription(documentation);
//                            setConstraint(eg, id);
//                            definition.addChild(eg);
//                            geMap.put(id, eg);
//                            break;
//                        }
                        case "inclusiveGateway": {
                            ExclusiveGateway eg = new ExclusiveGateway();
                            eg.setName(name);
                            eg.setDescription(documentation);
                            setConstraint(eg, id);
                            definition.addChild(eg);
                            geMap.put(id, eg);

                            addBizagiIrrelevantImportedId(eg.getId(), "inclusiveGateway", processName);
                            break;
                        }
                        case "complexGateway": {
                            ParallelGateway pg = new ParallelGateway();
                            pg.setName(name);
                            pg.setDescription(documentation);
                            setConstraint(pg, id);
                            definition.addChild(pg);
                            geMap.put(id, pg);
                            addBizagiIrrelevantImportedId(pg.getId(), "complexGateway", processName);
                            break;
                        }
                        case "parallelGateway": {
                            ParallelGateway pg = new ParallelGateway();
                            pg.setName(name);
                            pg.setDescription(documentation);

                            NodeTypeDefinition typeDefinition = NodeRegistry.getNodeTypeDefinition(ParallelGateway.class);
                            GraphitiEntry entry = typeDefinition.getGraphitiEntry();
                            Dimension defaultSize = entry.getDefaultSize(pg);

                            Rectangle bounds = bounds(id);
                            bounds.width = defaultSize.width;
                            bounds.height = defaultSize.height;
                            pg.setConstraint(bounds);

                            //setConstraint(pg, id);
                            definition.addChild(pg);
                            geMap.put(id, pg);
                            break;
                        }
                        case "businessRule": {
                            BusinessRule pg = new BusinessRule();
                            pg.setName(name);
                            pg.setDescription(documentation);
                            setConstraint(pg, id);
                            definition.addChild(pg);
                            geMap.put(id, pg);
                            break;
                        }
                        case "eventSubprocess": { // Event subprocess
                            EventSubprocess sp = null;
                            sp = new EventSubprocess();
                            sp.setEmbedded(true);
                            sp.setName(name);
                            sp.setSubProcessName(name);
                            sp.setDescription(documentation);
                            sp.setConstraint(bounds(id));
                            adjustMinimized(sp);
                            definition.addChild(sp);
                            geMap.put(id, sp);
                            embeddedSubprocesses.add(sp);
                            break;
                        }
                        case "subProcess": { // Embedded subprocess
                            Subprocess sp = new Subprocess();
                            sp.setEmbedded(true);
                            sp.setName(name);
                            sp.setSubProcessName(name);
                            sp.setDescription(documentation);
                            sp.setConstraint(bounds(id));
                            adjustMinimized(sp);
                            definition.addChild(sp);
                            geMap.put(id, sp);
                            embeddedSubprocesses.add(sp);
                            break;
                        }
                        case "callActivity": { // External subprocess
                            Subprocess sp = new Subprocess();
                            sp.setEmbedded(false);
                            sp.setName(name);
                            sp.setSubProcessName(name);
                            sp.setDescription(documentation);
                            sp.setConstraint(bounds(id));
                            adjustMinimized(sp);
                            definition.addChild(sp);
                            geMap.put(id, sp);
                            break;
                        }
                        case "adHocSubProcess": {
                            Subprocess sp = new Subprocess();
                            sp.setEmbedded(false);
                            sp.setName(name);
                            sp.setSubProcessName(name);
                            sp.setDescription("?" + elementName + "?" + (Strings.isNullOrEmpty(documentation) ? "" : documentation));
                            sp.setConstraint(bounds(id));
                            adjustMinimized(sp);
                            definition.addChild(sp);
                            geMap.put(id, sp);
                            break;
                        }
                        case "endEvent": {
                            if (element.element(TERMINATE_EVENT_DEFINITION) == null) {
                                EndTokenState end = new EndTokenState();
                                end.setName(name);
                                end.setDescription(documentation);
                                setConstraint(end, id);
                                definition.addChild(end);
                                geMap.put(id, end);

//                                if (element.element("errorEventDefinition") != null && element.element("messageEventDefinition") != null) {
//                                    end.setEventType(EndEventType.blank);
//                                    addBizagiIrrelevantImportedId(end.getId(), "multipleEventDefinition", processName);
//                                } else if (element.element("messageEventDefinition") != null) {
//                                    end.setEventType(EndEventType.message);
//                                } else if (element.element("signalEventDefinition") != null) {
//                                    end.setEventType(EndEventType.signal);
//                                } else if (element.element("compensateEventDefinition") != null) {
//                                    addBizagiIrrelevantImportedId(end.getId(), "compensateEventDefinition", processName);
//                                } else if (element.element("escalationEventDefinition") != null) {
//                                    addBizagiIrrelevantImportedId(end.getId(), "escalationEventDefinition", processName);
//                                } else if (element.element("errorEventDefinition") != null) {
//                                    end.setEventType(EndEventType.error);
//                                } else if (element.element("cancelEventDefinition") != null) {
//                                    end.setEventType(EndEventType.cancel);
//                                }

                                break;
                            } else {
                                EndState end = new EndState();
                                end.setName(name);
                                end.setDescription(documentation);
                                setConstraint(end, id);
                                definition.addChild(end);
                                geMap.put(id, end);
                                break;
                            }
                        }
                        case "intermediateCatchEvent": {
                            if (element.element(TIMER_EVENT_DEFINITION) != null) {
                                Timer timer = new Timer();
                                timer.setName(name);
                                timer.setDescription(documentation);
                                setConstraint(timer, id);
                                definition.addChild(timer);
                                geMap.put(id, timer);
                                break;
                            } else {
                                CatchEventNode catchEventNode = new CatchEventNode();
                                catchEventNode.setName(name);
                                catchEventNode.setDescription(documentation);
                                setConstraint(catchEventNode, id);
                                definition.addChild(catchEventNode);
                                geMap.put(id, catchEventNode);

                                if (element.element("errorEventDefinition") != null) {
                                    if (element.attribute(PARALLEL_MULTIPLE) != null) {
                                        addBizagiIrrelevantImportedId(catchEventNode.getId(), "parallelMultipleEventDefinition", processName);
                                    } else {
                                        addBizagiIrrelevantImportedId(catchEventNode.getId(), "multipleEventDefinition", processName);
                                    }
                                } else if (element.element("messageEventDefinition") != null) {
                                    catchEventNode.setEventNodeType(EventNodeType.message);
                                } else if (element.element("signalEventDefinition") != null) {
                                    catchEventNode.setEventNodeType(EventNodeType.signal);
                                } else if (element.element("linkEventDefinition") != null) {
                                    addBizagiIrrelevantImportedId(catchEventNode.getId(), "linkEventDefinition", processName);
                                } else if (element.element("conditionalEventDefinition") != null) {
                                    addBizagiIrrelevantImportedId(catchEventNode.getId(), "conditionalEventDefinition", processName);
                                }
                                break;
                            }

                        }
                        case "intermediateThrowEvent": {
                            ThrowEventNode event = new ThrowEventNode();
                            event.setName(name);
                            event.setDescription(documentation);
                            setConstraint(event, id);
                            definition.addChild(event);
                            geMap.put(id, event);

                            if (element.element("compensateEventDefinition") != null) {
                                addBizagiIrrelevantImportedId(event.getId(), "compensateEventDefinition", processName);
                            } else if (element.element("escalationEventDefinition") != null) {
                                addBizagiIrrelevantImportedId(event.getId(), "escalationEventDefinition", processName);
                            } else if (element.element("messageEventDefinition") != null) {
                                continue;
                            } else {
                                addBizagiIrrelevantImportedId(event.getId(), "Should be none", processName);
                            }

                            break;
                        }
                        case "boundaryEvent": {
                            CatchEventNode event = new CatchEventNode();
                            event.setName(name);
                            event.setDescription(documentation);
                            setConstraint(event, id);
                            String attachedToRefId = element.attributeValue(ATTACHED_TO_REF);
                            GraphElement parent = geMap.get(attachedToRefId);
                            parent.addChild(event);
                            event.setParent(parent);
                            event.setUiParentContainer(parent);
                            if (element.element(ERROR_EVENT_DEFINITION) != null) {
                                event.setEventNodeType(EventNodeType.error);
                            } else if (element.element(MESSAGE_EVENT_DEFINITION) != null) {
                                event.setEventNodeType(EventNodeType.message);
                            }
                            geMap.put(id, event);
                            break;
                        }
                        case "scriptTask": {
                            ScriptTask task = new ScriptTask();
                            task.setName(name);
                            task.setDescription(documentation);

                            NodeTypeDefinition typeDefinition = NodeRegistry.getNodeTypeDefinition(TaskState.class);
                            GraphitiEntry entry = typeDefinition.getGraphitiEntry();
                            Dimension defaultSize = entry.getDefaultSize(task);

                            Rectangle bounds = bounds(id);
                            bounds.width = defaultSize.width;
                            bounds.height = defaultSize.height;
                            task.setConstraint(bounds);

                            adjustMinimized(task);
                            definition.addChild(task);
                            //task.setConstraint(bounds(id));
                            //task.setDelegationClassName("ru.runa.wfe.extension.handler.SendEmailActionHandler");
                            Element extensionElements = element.element(EXTENSION_ELEMENTS);
                            if (extensionElements != null) {
                                List<Element> scriptTaskProperties = extensionElements.elements("property");
                                for (Element property : scriptTaskProperties) {
                                    String propName = property.attributeValue("name");
                                    if ("docxConfig".equals(propName)) {
                                        String template = property.attributeValue("template");
                                        String variable = property.attributeValue("variable");
                                        String configContent =
                                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                                        "<config strict=\"false\">\n" +
                                                        "  <input path=\"processfile://" + task.getId() + ".template.docx\"/>\n" +
                                                        "  <output variable=\"" + variable +"\" fileName=\""+ variable +".docx\"/>\n" +
                                                        "</config>";
                                        task.setDelegationConfiguration(configContent);
                                        byte[] docxContent = DocxCreator.create(template);
                                        IFile file = dstFolder.getFile(new Path(task.getId() + ".template.docx"));
                                        InputStream inputStream = new ByteArrayInputStream(docxContent);
                                        if (!file.exists()) {
                                            file.create(inputStream, true, null);
                                        } else {
                                            file.setContents(inputStream, true, false, null);
                                        }
                                    }
                                    if ("class".equals(propName)) {
                                        String handlerClass = property.attributeValue("value");
                                        if (handlerClass != null && !handlerClass.isEmpty()) {
                                            task.setDelegationClassName(handlerClass);
                                            //PluginLogger.logInfo("Loaded handler class for scriptTask " + name + ": " + handlerClass);
                                        }
                                        //break;
                                    }
                                    if ("config".equals(propName)) {
                                        String propContent = property.getText();
                                        if (propContent != null && !propContent.isEmpty()) {
                                            task.setDelegationConfiguration(propContent);
                                        }
                                    }
                                }
                            }

                            addBizagiIrrelevantImportedId(task.getId(), "selectConfiguration", processName);
                            geMap.put(id, task);
                            break;
                        }
                        case "businessRuleTask": {
                            BusinessRule businessRule = new BusinessRule();
                            businessRule.setName(name);
                            businessRule.setDescription(documentation);
                            businessRule.setConstraint(bounds(id));
                            adjustMinimized(businessRule);
                            definition.addChild(businessRule);
                            geMap.put(id, businessRule);
                            break;
                        }
                        case "textAnnotation": {
                            TextAnnotation annotation = new TextAnnotation();
                            annotation.setConstraint(bounds(id));
                            annotation.setDescription(element.elementText("text"));
                            definition.addChild(annotation);
                            geMap.put(id, annotation);
                            break;
                        }
                        case "sequenceFlow": {
                            sequenceFlows.add(element);
                            break;
                        }
                        case "dataStoreReference": {
                            DataStore store = new DataStore();
                            store.setName(name);
                            store.setDescription(documentation);
                            store.setConstraint(bounds(id));
                            adjustMinimized(store);
                            definition.addChild(store);
                            geMap.put(id, store);
                            break;
                        }
                        case DOCUMENTATION:
                        case ASSOCIATION:
                        case EXTENSION_ELEMENTS:
                        case LANE_SET:
                        case DATA_OBJECT: {
                            // Do nothing
                            break;
                        }
                        // Undefined elements
                        default: {
                            if (planeMap.containsKey(id)) {
                                TextAnnotation annotation = new TextAnnotation();
                                annotation.setConstraint(bounds(id));
                                annotation.setDescription("?" + elementName + "? - " + name);
                                definition.addChild(annotation);
                                geMap.put(id, annotation);
                            }
                        }
                        }
                    } catch (Exception e) {
                        PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
                    }
                }
                for (Element element : sequenceFlows) {
                    try {
                        String id = element.attributeValue(ID);
                        String name = element.attributeValue(NAME);
                        String sourceRef = element.attributeValue(SOURCE_REF);
                        String targetRef = element.attributeValue(TARGET_REF);
                        GraphElement target = geMap.get(targetRef);
                        if (target instanceof Node) {
                            NodeTypeDefinition transitionDefinition = NodeRegistry.getNodeTypeDefinition(Transition.class);
                            Node source = (Node) geMap.get(sourceRef);
                            Transition transition = transitionDefinition.createElement(source, false);
                            transition.setName(Strings.isNullOrEmpty(name) ? source.getNextTransitionName(transitionDefinition) : name);
                            transition.setTarget((Node) target);
                            if (defaultIds.contains(id)) {
                                transition.setDefaultFlow(true);
                                source.setDelegationConfiguration("return \"" + transition.getName() + "\";");
                            }
                            if (transition.getName() == null) {
                                PluginLogger.logInfo(
                                        "NULL transition name: source="
                                                + source.getName()
                                                + ", target="
                                                + ((Node) target).getName()
                                );
                            }
                            for (Transition t : source.getLeavingTransitions()) {
                                if (t.getName() == null) {
                                    PluginLogger.logInfo(
                                            "Existing transition with NULL name from "
                                                    + source.getName()
                                    );
                                }
                            }
                            source.addLeavingTransition(transition);
                            if (!ignoreBendPoints) {
                                Rectangle sourceBounds = source.getConstraint().getCopy();
                                Rectangle targetBounds = target.getConstraint().getCopy();
                                if (showSwimlanes) {
                                    if (source.getUiParentContainer() != null) {
                                        sourceBounds.translate(source.getUiParentContainer().getConstraint().getTopLeft());
                                    }
                                    if (target.getUiParentContainer() != null) {
                                        targetBounds.translate(target.getUiParentContainer().getConstraint().getTopLeft());
                                    }
                                }
                                transition.setBendpoints(Collections.emptyList());
                            }
                        }
                    } catch (Exception e) {
                        PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
                    }
                }

                Element varElement = definitionsElement.element(COLLABORATION);
                if (varElement != null) {
                    varElement = varElement.element(EXTENSION_ELEMENTS);
                    if (varElement != null) {
                        varElement = varElement.element(QName.get(BIZAGI_EXTENSIONS, BIZAGI_NAMESPACE));
                        if (varElement != null) {
                            varElement = varElement.element(QName.get("BizagiExtendedAttributeDefinitions", BIZAGI_NAMESPACE));
                            if (varElement != null) {
                                for (Element element : (List<Element>) varElement
                                        .elements(QName.get("BizagiExtendedAttributeDefinition", BIZAGI_NAMESPACE))) {
                                    String type = element.attributeValue("Type");
                                    String name = element.element(QName.get("Name", BIZAGI_NAMESPACE)).getText();
                                    String description = element.element(QName.get("Description", BIZAGI_NAMESPACE)).getText();
                                    Variable variable = new Variable();
                                    variable.setName(name);
                                    variable.setDescription(description);

                                    switch (type) {
                                    case "Text": {
                                        variable.setFormat("ru.runa.wfe.var.format.StringFormat");
                                        break;
                                    }
                                    case "LongText": {
                                        variable.setFormat("ru.runa.wfe.var.format.TextFormat");
                                        break;
                                    }
                                    case "Date": {
                                        variable.setFormat("ru.runa.wfe.var.format.DateFormat");
                                        break;
                                    }
                                    case "Number": {
                                        variable.setFormat("ru.runa.wfe.var.format.DoubleFormat");
                                        break;
                                    }
                                    case "FileLinked": {
                                        variable.setFormat("ru.runa.wfe.var.format.FileFormat");
                                        break;
                                    }
                                    case "Table": {
                                        VariableUserType userType = new VariableUserType();
                                        userType.setName("Table_" + name);
                                        //userType.setStorageEnabled(true, true);
                                        variable.setName(name);
                                        variable.setDescription(description);
                                        variable.setUserType(userType);
                                        variable.setFormat("ru.runa.wfe.var.format.UserTypeFormat");
                                        varElement = element.element(QName.get("TableColumns", BIZAGI_NAMESPACE));
                                        for (Element tableElement : (List<Element>) varElement
                                                .elements(QName.get("ColumnAttribute", BIZAGI_NAMESPACE))) {
                                            Variable userTypeVariable = new Variable();
                                            type = tableElement.attributeValue("Type");
                                            name = tableElement.element(QName.get("Name", BIZAGI_NAMESPACE)).getText();
                                            description = tableElement.element(QName.get("Description", BIZAGI_NAMESPACE)).getText();
                                            userTypeVariable.setName(name);
                                            switch (type) {
                                            case "Text": {
                                                userTypeVariable.setFormat("ru.runa.wfe.var.format.StringFormat");
                                                break;
                                            }
                                            case "LongText": {
                                                userTypeVariable.setFormat("ru.runa.wfe.var.format.TextFormat");
                                                break;
                                            }
                                            case "Date": {
                                                userTypeVariable.setFormat("ru.runa.wfe.var.format.DateFormat");
                                                break;
                                            }
                                            case "Number": {
                                                userTypeVariable.setFormat("ru.runa.wfe.var.format.DoubleFormat");
                                                break;
                                            }
                                            default: {
                                                userTypeVariable.setFormat("ru.runa.wfe.var.format.StringFormat");
                                            }
                                            }
                                            userTypeVariable.setDescription(description);
                                            userType.addAttribute(userTypeVariable);
                                        }
                                        definition.addVariableUserType(userType);
                                        break;
                                    }
                                    default: {
                                        variable.setFormat("ru.runa.wfe.var.format.StringFormat");
                                    }
                                    }
                                    definition.addChild(variable);
                                }
                            }
                        }
                    }
                }

                IFolder folder = (IFolder) definition.getFile().getParent();
                IResource[] members = folder.members();
                List<GraphElement> graphElements = definition.getChildren(GraphElement.class);
                for (IResource member: members) {
                    if (member.getName().endsWith(".quick") || member.getName().endsWith(".js") || member.getName().endsWith(".validation.xml")) {
                        String memberName = member.getName().split("\\.")[0];
                        //PluginLogger.logInfo(memberName);
                        boolean existsElement = graphElements.stream().anyMatch(graphElement -> graphElement.getId().equals(memberName));
                        //boolean existsElement = geMap.containsKey(memberName);
                        if (existsElement) {
                            continue;
                        }
                        member.delete(true, null);
                    }
                    if (member.getName().endsWith(".template.docx")) {
                        String memberName = member.getName().split("\\.")[0];
                        boolean existsElement = graphElements.stream().anyMatch(graphElement -> graphElement.getId().equals(memberName));
                        //boolean existsElement = geMap.containsKey(memberName);
                        if (existsElement) {
                            continue;
                        }
                        member.delete(true, null);
                    }
                }

                applyVariables(definition, variablesXml);

                ParContentProvider.saveFormsXml(definition);
                WorkspaceOperations.saveProcessDefinition(definition);
                ProcessCache.newProcessDefinitionWasCreated(definitionFile);

                Display.getDefault().asyncExec(() -> {
                    try {
                        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IEditorPart existingEditor = page.findEditor(new FileEditorInput(definitionFile));
                        if (existingEditor != null) {
                            page.closeEditor(existingEditor, false);
                        }
                        IEditorPart newEditor = WorkspaceOperations.openProcessDefinition(definitionFile);
                        if (newEditor != null) {
                            newEditor.doSave(null);
                        }
                    } catch (Exception e) {
                        PluginLogger.logError("Error refreshing editor", e);
                    }
                });

                //WorkspaceOperations.openProcessDefinition(definitionFile);
                createEmbeddedSubprocesses();

            }
        }
//        if (bizagiIrrelevantImportedIds.size() != 0) {
//            ShowBizagiImportProblemsDialog dialog = new ShowBizagiImportProblemsDialog(bizagiIrrelevantImportedIds, bizagiIrrelevantImportedElements,
//                    bizagiIrrelevantImportedProcessNames);
//            dialog.openDialog();
//        }
        return createdFolder;
    }

    private static void applyVariables(ProcessDefinition definition, String variablesXml) {
        if (Strings.isNullOrEmpty(variablesXml)) {
            return;
        }
        try {
            Document doc = XmlUtil.parseWithoutValidation(
                    new ByteArrayInputStream(variablesXml.getBytes(StandardCharsets.UTF_8)));
            Element root = doc.getRootElement();
            if (root == null || !"variables".equals(root.getName())) {
                PluginLogger.logError("applyVariables: unexpected root element: "
                        + (root == null ? "null" : root.getName()), new Exception());
                return;
            }

            for (Element varElement : (List<Element>) root.elements("variable")) {
                String name = varElement.attributeValue("name");
                if (Strings.isNullOrEmpty(name)) {
                    continue;
                }
                String scriptingName = varElement.attributeValue("scriptingName");
                String format = varElement.attributeValue("format");
                boolean swimlane = TRUE.equals(varElement.attributeValue("swimlane"));
                String editor = varElement.attributeValue("editor");
                String defaultValue = varElement.attributeValue("defaultValue");

                if (swimlane) {
                    Swimlane existing = definition.getSwimlaneByName(name);
                    if (existing != null) {
                        if (!Strings.isNullOrEmpty(editor)) {
                            existing.setEditorPath(editor);
                        }
                    } else {
                        Swimlane swimlaneObj = new Swimlane();
                        swimlaneObj.setName(name);
                        if (!Strings.isNullOrEmpty(editor)) {
                            swimlaneObj.setEditorPath(editor);
                        }
                        definition.addChild(swimlaneObj);
                    }
                    continue;
                }

                boolean exists = false;
                for (Variable v : definition.getVariables(true, true)) {
                    if (name.equals(v.getName())) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    continue;
                }
                Variable variable = new Variable();
                variable.setName(name);
                variable.setScriptingName(Strings.isNullOrEmpty(scriptingName) ? name : scriptingName);
                variable.setFormat(Strings.isNullOrEmpty(format)
                        ? "ru.runa.wfe.var.format.StringFormat"
                        : format);
                if (defaultValue != null) {
                    variable.setDefaultValue(defaultValue);
                }
                definition.addChild(variable);
            }
        } catch (Exception e) {
            PluginLogger.logError("Failed to apply variables.xml", e);
        }
    }

    private static void createQuickForm(
            FormNode task,
            ProcessDefinition definition,
            QuickForm quickForm
    ) throws Exception {

        String formFileName = task.getId() + ".quick";

        task.setFormType("quick");
        task.setFormFileName(formFileName);
        task.setTemplateFileName(
                "table2ColumnsProcessLayout.ftl.template"
        );
        task.setUseJSValidation(false);

        IFolder folder = (IFolder) definition.getFile().getParent();
        String replacement =
                "<properties>\n" +
                        "  <property>\n" +
                        "    <name>ProcessName</name>\n" +
                        "    <value></value>\n" +
                        "  </property>\n" +
                        "  <property>\n" +
                        "    <name>Title</name>\n" +
                        "    <value></value>\n" +
                        "  </property>\n" +
                        "  <property>\n" +
                        "    <name>Warning</name>\n" +
                        "    <value></value>\n" +
                        "  </property>\n" +
                        "</properties>";

        byte[] xml = QuickFormXMLUtil.convertQuickFormToXML(
                folder,
                quickForm,
                task.getTemplateFileName()
        );
        String xmlString = new String(xml, StandardCharsets.UTF_8);
        xmlString = xmlString.replace("<properties/>", replacement);
        xml = xmlString.getBytes(StandardCharsets.UTF_8);

        IFile formFile = IOUtils.getAdjacentFile(
                definition.getFile(),
                formFileName
        );

        IOUtils.createOrUpdateFile(
                formFile,
                new ByteArrayInputStream(xml)
        );

        IFile jsFile = IOUtils.getAdjacentFile(
                definition.getFile(),
                task.getScriptFileName()
        );

        if (!jsFile.exists()) {
            IOUtils.createFile(
                    jsFile,
                    TemplateUtils.getFormTemplateAsStream()
            );
        }

        IFile validationFile = IOUtils.getAdjacentFile(definition.getFile(), task.getValidationFileName());
        if (!validationFile.exists()) {
            StringBuilder sb = new StringBuilder();
            for (QuickFormComponent formComponent: quickForm.getVariables()) {
                if (formComponent.getTagName().equals("InputVariable")) {
                    for (Object param: formComponent.getParams()) {
                        sb.append("  <field name=\"" + param +"\"/>\n");
                    }
                }
            }
            if (sb.length() != 0) {
                StringBuilder validationXml = new StringBuilder();
                validationXml.append("<validators>\n");
                validationXml.append(sb);
                validationXml.append("</validators>\n");
                IOUtils.createFile(
                        validationFile,
                        new ByteArrayInputStream(validationXml.toString().getBytes(StandardCharsets.UTF_8))
                );
            }
        }

        IFile templateFile = IOUtils.getFile(folder,"table2ColumnsProcessLayout.ftl.template");
        if (!templateFile.exists()) {
            Bundle formQuickBundle = FrameworkUtil.getBundle(QuickForm.class);
            String table2TemplateContent = readFile("template/table2ColumnsProcessLayout.ftl.template", formQuickBundle);
            byte[] bytes = table2TemplateContent.getBytes(StandardCharsets.UTF_8);
            IOUtils.createFile(templateFile, new ByteArrayInputStream(bytes));
        }
    }

    private static String readFile(String fileName, Bundle bundle) throws Exception {
        if (bundle == null) {
            Exception exception = new RuntimeException("Failed to get bundle");
            PluginLogger.logError(exception);
            throw exception;
        }

        URL url = bundle.getEntry(fileName);
        if (url == null) {
            Exception exception = new RuntimeException("Failed to get url with " + fileName);
            PluginLogger.logError(exception);
            throw exception;
        }
        String content;
        try {
            try (InputStream is = url.openStream()) {
                content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toString();
    }

    private static Swimlane getSwimlaneByNodeId(
            String nodeId,
            Map<String, List<Swimlane>> nodeToLaneMap,
            GraphElement element,
            boolean showSwimlanes
    ) {
//        if (!showSwimlanes) {
//            return null;
//        }

        List<Swimlane> swimlanes = nodeToLaneMap.get(nodeId);
        if (swimlanes != null && !swimlanes.isEmpty()) {
            return swimlanes.get(0);
        }

        if (element != null) {
            Rectangle constraint = element.getConstraint();
            if (constraint != null) {
                Point center = constraint.getCenter();
                for (Map.Entry<Rectangle, Swimlane> entry : swimlaneBoundMap.entrySet()) {
                    if (entry.getKey().contains(center)) {
                        PluginLogger.logInfo("Using coordinate-based fallback for node " + nodeId);
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }

    public static void addBizagiIrrelevantImportedId(String bizagiIrrelevantImportedId, String elementName, String processName) {
        bizagiIrrelevantImportedIds.add(bizagiIrrelevantImportedId);
        bizagiIrrelevantImportedElements.put(bizagiIrrelevantImportedId, elementName);
        bizagiIrrelevantImportedProcessNames.add(processName);
    }

//    private static void createBotTaskFromBizagi(String botStationName, String name, String documentation, String id, Element element,
//            ProcessDefinition definition, boolean showSwimlanes, String handler) {
//
//        String taskHandler = "";
//        if (handler.equals("sendTask")) {
//            taskHandler = "ru.runa.wf.logic.bot.EmailTaskHandler";
//        } else if (handler.equals("serviceTask")) {
//            taskHandler = "ru.runa.wf.logic.bot.WebServiceTaskHandler";
//        }
//
//        IFile botTaskFile = WorkspaceOperations.createNewBotTask(botStationName, "Bizagi bot", name, taskHandler);
//
//        Element extensionElements = element.element(EXTENSION_ELEMENTS);
//        if (extensionElements != null) {
//            Element bizagiExtensionElements = extensionElements.element(QName.get(BIZAGI_EXTENSIONS, BIZAGI_NAMESPACE));
//            if (bizagiExtensionElements != null) {
//                Element bizageExtendedAttributes = bizagiExtensionElements.element(QName.get(BIZAGI_EXTENDED_ATTRIBUTE_VALUES, BIZAGI_NAMESPACE));
//                if (bizageExtendedAttributes != null) {
//                    List<Element> attributeValues = bizageExtendedAttributes.elements(QName.get(BIZAGI_EXTENDED_ATTRIBUTE_VALUE, BIZAGI_NAMESPACE));
//                    for (Element attributeValue : attributeValues) {
//                        idOfVariablesForTasks.put(attributeValue.attributeValue("Id"), botTaskFile);
//                    }
//                }
//            }
//        }
//
//        TaskState task = new TaskState();
//        task.setName(name);
//        task.setDescription(documentation);
//        task.setConstraint(bounds(id));
//        adjustMinimized(task);
//        Swimlane swimlane = swimlane(task);
//        task.setSwimlane(swimlane);
//        if (showSwimlanes && swimlane != null) {
//            task.setUiParentContainer(swimlane);
//            task.getConstraint().translate(swimlane.getConstraint().getTopLeft().negate());
//        }
//        definition.addChild(task);
//        geMap.put(id, task);
//    }

    private static void createEmbeddedSubprocesses() {
        for (Subprocess sp : embeddedSubprocesses) {
            // TODO something like ru.runa.gpd.ui.wizard.NewProcessDefinitionWizard.CreateEmbeddedSubprocessOperation
        }
    }

    private static void setConstraint(GraphElement ge, String id) {
        NodeTypeDefinition typeDefinition = NodeRegistry.getNodeTypeDefinition(ge.getClass());
        GraphitiEntry entry = typeDefinition.getGraphitiEntry();
        Dimension defaultSize = entry.getDefaultSize(ge);
        Rectangle bounds = bounds(id);
        if (bounds.width < defaultSize.width || entry.isFixedSize()) {
            bounds.x -= (defaultSize.width - bounds.width) / 2;
            bounds.width = defaultSize.width;
        }
        if (bounds.height < defaultSize.height || entry.isFixedSize()) {
            bounds.y -= (defaultSize.height - bounds.height) / 2;
            bounds.height = defaultSize.height;
        }
        ge.setConstraint(bounds);
    }

    private static double scaleFactor = 1;

    private static Rectangle bounds(String id) {
        Element shape = planeMap.get(id);
        if (shape != null) {
            Element bounds = shape.element(BOUNDS);
            if (bounds != null) {
                return new PrecisionRectangle(Double.parseDouble(bounds.attributeValue(X)), Double.parseDouble(bounds.attributeValue(Y)),
                        Double.parseDouble(bounds.attributeValue(WIDTH)), Double.parseDouble(bounds.attributeValue(HEIGHT))).scale(scaleFactor);
            }
        }
        throw new IllegalStateException("id: " + id + " does not exist");
    }

    private static void adjustMinimized(Node node) {
        if (node.getConstraint().getSize().equals(MINIMIZED_NODE_SIZE)) {
            node.setMinimizedView(true);
        }
    }

    private static List<Point> waypoints(String id, Rectangle sourceBounds, Rectangle targetBounds) {
        Element shape = planeMap.get(id);
        if (shape != null) {
            List<Point> bendPoints = Lists.newArrayList();
            List<Element> waypoints = shape.elements(WAYPOINT);
            if (waypoints != null) {
                for (Element waypoint : waypoints) {
                    Point point = new PrecisionPoint(Double.parseDouble(waypoint.attributeValue(X)), Double.parseDouble(waypoint.attributeValue(Y)))
                            .scale(scaleFactor);
                    if (!sourceBounds.contains(point.translate(-1, -1)) && !targetBounds.contains(point.translate(-1, -1))) {
                        bendPoints.add(point);
                    }
                }
            }
            return bendPoints;
        }
        throw new IllegalStateException("id: " + id + " does not exist");
    }

    private static Swimlane swimlane(SwimlanedNode node) {
        for (Map.Entry<Rectangle, Swimlane> entry : swimlaneBoundMap.entrySet()) {
            if (entry.getKey().contains(node.getConstraint().getTopLeft())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private BizagiBpmnImporter() {
        // All-static class
    }

}
