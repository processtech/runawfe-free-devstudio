package ru.runa.xpdl.convertor;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.runa.xpdl.generated.bpmnxpdl.ActivityType;
import ru.runa.xpdl.generated.bpmnxpdl.Artifact;
import ru.runa.xpdl.generated.bpmnxpdl.DataMappingType;
import ru.runa.xpdl.generated.bpmnxpdl.ExpressionType;
import ru.runa.xpdl.generated.bpmnxpdl.LaneType;
import ru.runa.xpdl.generated.bpmnxpdl.PackageType;
import ru.runa.xpdl.generated.bpmnxpdl.PoolType;
import ru.runa.xpdl.generated.bpmnxpdl.ProcessType;
import ru.runa.xpdl.generated.jpdl32.Assignment;
import ru.runa.xpdl.generated.jpdl32.Decision;
import ru.runa.xpdl.generated.jpdl32.DecisionType;
import ru.runa.xpdl.generated.jpdl32.EndState;
import ru.runa.xpdl.generated.jpdl32.EventType;
import ru.runa.xpdl.generated.jpdl32.Fork;
import ru.runa.xpdl.generated.jpdl32.ForkType;
import ru.runa.xpdl.generated.jpdl32.GeneralNodeType;
import ru.runa.xpdl.generated.jpdl32.Join;
import ru.runa.xpdl.generated.jpdl32.ProcessDefinition;
import ru.runa.xpdl.generated.jpdl32.ProcessState;
import ru.runa.xpdl.generated.jpdl32.StartState;
import ru.runa.xpdl.generated.jpdl32.Swimlane;
import ru.runa.xpdl.generated.jpdl32.Task;
import ru.runa.xpdl.generated.jpdl32.TaskNode;
import ru.runa.xpdl.generated.jpdl32.Transition;
import ru.runa.xpdl.generated.jpdl32.Variable;
import ru.runa.xpdl.generated.jpdl32.impl.AssignmentImpl;
import ru.runa.xpdl.generated.jpdl32.impl.DecisionImpl;
import ru.runa.xpdl.generated.jpdl32.impl.DecisionTypeImpl;
import ru.runa.xpdl.generated.jpdl32.impl.EndStateImpl;
import ru.runa.xpdl.generated.jpdl32.impl.EventImpl;
import ru.runa.xpdl.generated.jpdl32.impl.ForkImpl;
import ru.runa.xpdl.generated.jpdl32.impl.ForkTypeImpl;
import ru.runa.xpdl.generated.jpdl32.impl.JoinImpl;
import ru.runa.xpdl.generated.jpdl32.impl.ProcessDefinitionImpl;
import ru.runa.xpdl.generated.jpdl32.impl.ProcessStateImpl;
import ru.runa.xpdl.generated.jpdl32.impl.ProcessStateTypeImpl;
import ru.runa.xpdl.generated.jpdl32.impl.StartStateImpl;
import ru.runa.xpdl.generated.jpdl32.impl.SwimlaneImpl;
import ru.runa.xpdl.generated.jpdl32.impl.TaskImpl;
import ru.runa.xpdl.generated.jpdl32.impl.TaskNodeImpl;
import ru.runa.xpdl.generated.jpdl32.impl.TransitionImpl;
import ru.runa.xpdl.generated.jpdl32.impl.VariableImpl;
import ru.runa.xpdl.resource.Messages;

/**
 * Created by IntelliJ IDEA. User: Mika95 Date: 03.05.2012 Time: 14:32:09
 * 
 * 
 * Комментарии: В XSD-схемах используется зарезервированное слово class и они не компилировались. Решение - слово class заменялось на class2 -
 * компилировалась схема и потом в Impl-ах руками делалась обратная замена.
 * 
 * Не сохранялись в XML конструкция <![CDATA[]]> с пустой строкой. А с непустой строкой не хочет работать GPD. Если вставлять <![CDATA[]]> как
 * текствый элемент, то он сериализуется как &lt;![CDATA[]]&gt; и редактор с этим тоже не работает. Решение - был написан EmptyCDataWriter , который
 * выполнят обратное преобразование &lt(&gt); в <(>)
 */
@SuppressWarnings("unchecked")
public class XPDLImporter {
    class VariableDescription {
        String format;
        Boolean publicVisibility;
        String defaultValue;
        Boolean swimlane;

        VariableDescription(String format, Boolean publicVisibility, String defaultValue, Boolean swimlane) {
            this.format = format;
            this.publicVisibility = publicVisibility;
            this.defaultValue = defaultValue;
            this.swimlane = swimlane;
        }
    }

    class FormDescription {
        String id;
        String content;
        String name;
        String stateName;
        List<String> variables;

        FormDescription(String id, String name, String stateName) {
            this.id = "F" + id;
            this.name = ((name == null) ? ("") : (name));
            this.stateName = stateName;
            content = "<P>" + name + "</P><br>";
            variables = new LinkedList<String>();
        }

        void addDataObject(String labelName, String varname) {
            content += "<P>" + labelName + "</P><br>";
            content += "<P><TEXTAREA cols=\"80\" name=\"" + varname + "\" rows=\"100\"/></P><br>";
            addVarName(varname);
        }

        void addChoiceObject(String varname, int value, String display) {
            content += "<P><INPUT  name=\"" + varname + "\" type=\"radio\" value=\"" + value + "\"/>" + display + "</P><br>";
            addVarName(varname);
        }

        String getValidatorString() {
            String result = "<validators>";
            for (String s : variables) {
                result += "<field name=\"" + s + "\">\n" + "<field-validator type=\"required\">\n" + "\t<message>"
                        + Messages.getString("XPDLConnector.formValidator.fieldRequiredForEntry") + "</message>\n" + "</field-validator>\n" + "</field>";
            }
            result += "</validators>";
            return result;
        }

        private void addVarName(String varname) {
            for (String s : variables) {
                if (s.equals(varname)) {
                    return;
                }
            }
            variables.add(varname);
        }
    }

    class TransitionDescription {
        Transition transition;
        GeneralNodeType from;
        GeneralNodeType to;

        public TransitionDescription(Transition transition, GeneralNodeType from, GeneralNodeType to) {
            this.transition = transition;
            this.from = from;
            this.to = to;
        }
    }

    private void createDir(File dirFile) throws Exception {
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        if (dirFile.exists()) {
            if (!dirFile.isDirectory()) {
                throw new Exception("Project Dir [" + dirFile.getCanonicalPath() + "] is not a directory");
            }
        } else {
            throw new Exception("Project Dir [" + dirFile.getCanonicalPath() + "] not exists or not created");
        }
    }

    public String getProjectName(String fileName) throws Exception {
        JAXBContext context = JAXBContext.newInstance("ru.runa.xpdl.generated.bpmnxpdl");
        Unmarshaller u = context.createUnmarshaller();
        Object o = u.unmarshal(new File(fileName));
        PackageType pack = (PackageType) o;
        return pack.getName() + "." + pack.getId();
    }

    public void parseXPDLFile(File processFolder, String xpdlFileName, boolean useDefaultSwimlane, String runaGroupName) throws Exception {
        JAXBContext context = JAXBContext.newInstance("ru.runa.xpdl.generated.bpmnxpdl");
        Unmarshaller u = context.createUnmarshaller();
        Object o = u.unmarshal(new File(xpdlFileName));
        PackageType pack = (PackageType) o;
        // Делаем swimlane
        List lanes = null;
        double width = 0;
        double height = 0;
        Map<String, Swimlane> swimlaneMap = new HashMap<String, Swimlane>();
        Map<String, ProcessDefinition> OutWorkFlowProcessMap = new HashMap<String, ProcessDefinition>();
        Map<String, Map<String, VariableDescription>> varMaps = new HashMap<String, Map<String, VariableDescription>>();
        Map<String, Artifact> artifactMap = new HashMap<String, Artifact>();
        Map<String, Element> gpdMap = new HashMap<String, Element>();
        Map<String, List<FormDescription>> processFormsMap = new HashMap<String, List<FormDescription>>();
        int swimlaneIndex = 0;
        for (Object p : pack.getPools().getPool()) {
            PoolType pool = (PoolType) (p);
            lanes = pool.getLanes().getLane();
            for (Object l : lanes) {
                LaneType lane = (LaneType) (l);
                // System.out.println("Lane:"+lane.getId()+",index="+swimlaneIndex);
                Swimlane swimlane = new SwimlaneImpl();
                String swimlaneName = lane.getName() + "Роль_" + swimlaneIndex++;
                swimlane.setName(swimlaneName);
                swimlaneMap.put(lane.getId(), swimlane);
                Assignment assignment = new AssignmentImpl();
                assignment.setClass2("ru.runa.wfe.handler.assign.DefaultAssignmentHandler");
                assignment.setConfigType("configuration-property");
                assignment.getContent().add(new String("<![CDATA[]]>"));
                swimlane.setAssignment(assignment);
            }
            java.util.List NGIList = pool.getNodeGraphicsInfos().getNodeGraphicsInfo();
            for (Object _ngi : NGIList) {
                ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfoType nodeGraphicsInfo = (ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfoType) (_ngi);
                width += nodeGraphicsInfo.getWidth();
                height += nodeGraphicsInfo.getHeight();
            }
        }
        if (pack.getArtifacts() != null) {
            for (Object _p : pack.getArtifacts().getArtifactAndAny()) {
                ru.runa.xpdl.generated.bpmnxpdl.Artifact artifact = (ru.runa.xpdl.generated.bpmnxpdl.Artifact) (_p);
                if (artifact.getArtifactType().equalsIgnoreCase("DataObject")) {
                    artifactMap.put(artifact.getId(), artifact);
                }
            }
        }
        java.util.List workflowProcesses = pack.getWorkflowProcesses().getWorkflowProcess();
        for (Object _wp : workflowProcesses) {
            ru.runa.xpdl.generated.bpmnxpdl.ProcessType workflowProcess = (ru.runa.xpdl.generated.bpmnxpdl.ProcessType) (_wp);
            ProcessDefinition processDefinition = new ProcessDefinitionImpl();
            processDefinition.setName(workflowProcess.getName());
            OutWorkFlowProcessMap.put(workflowProcess.getId(), processDefinition);
            varMaps.put(workflowProcess.getId(), new HashMap<String, VariableDescription>());
            processFormsMap.put(workflowProcess.getId(), new LinkedList<FormDescription>());
            if (workflowProcess.getParticipants() != null) {
                for (Object _participant : workflowProcess.getParticipants().getParticipant()) {
                    if (_participant instanceof ru.runa.xpdl.generated.bpmnxpdl.Participant) {
                        ru.runa.xpdl.generated.bpmnxpdl.Participant participant = (ru.runa.xpdl.generated.bpmnxpdl.Participant) (_participant);
                        Swimlane swimlane = new SwimlaneImpl();
                        String swimlaneName = Helper.generateVariableName(participant.getName() + "_" + participant.getId());
                        swimlane.setName(swimlaneName);
                        swimlaneMap.put(participant.getId(), swimlane);
                        Assignment assignment = new AssignmentImpl();
                        assignment.setClass2("ru.runa.wfe.handler.assign.DefaultAssignmentHandler");
                        assignment.setConfigType("configuration-property");
                        assignment.getContent().add(new String("<![CDATA[ru.runa.af.organizationfunction.ExecutorByNameFunction(" + runaGroupName + ")]]>"));
                        swimlane.setAssignment(assignment);
                    }
                }
            }
        }
        for (Object _wp : workflowProcesses) {
            ru.runa.xpdl.generated.bpmnxpdl.ProcessType workflowProcess = (ru.runa.xpdl.generated.bpmnxpdl.ProcessType) (_wp);
            makeWorkFlowProcess(workflowProcess, width, height, swimlaneMap, gpdMap, OutWorkFlowProcessMap.get(workflowProcess.getId()), varMaps.get(workflowProcess.getId()),
                    processFormsMap.get(workflowProcess.getId()), ((pack.getAssociations() != null) ? (pack.getAssociations().getAssociationAndAny()) : (null)), artifactMap,
                    useDefaultSwimlane);
        }
        for (Object _wp : workflowProcesses) {
            ru.runa.xpdl.generated.bpmnxpdl.ProcessType workflowProcess = (ru.runa.xpdl.generated.bpmnxpdl.ProcessType) (_wp);
            makeWorkFlowProcessReferences(workflowProcess.getId(), swimlaneMap, OutWorkFlowProcessMap, varMaps, OutWorkFlowProcessMap.get(workflowProcess.getId()));
        }
        for (Object _wp : workflowProcesses) {
            ru.runa.xpdl.generated.bpmnxpdl.ProcessType workflowProcess = (ru.runa.xpdl.generated.bpmnxpdl.ProcessType) (_wp);
            saveData(processFolder, OutWorkFlowProcessMap.get(workflowProcess.getId()), varMaps.get(workflowProcess.getId()), gpdMap.get(workflowProcess.getId()),
                    processFormsMap.get(workflowProcess.getId()));
        }
    }

    private void makeWorkFlowProcessReferences(String processId, Map<String, Swimlane> swimlaneMap, Map<String, ProcessDefinition> outWorkFlowProcessMap,
            Map<String, Map<String, VariableDescription>> varMaps, ProcessDefinition processDefinition) {
        // Поиск ссылок на подпроцессы
        List genObjectList = processDefinition.getDescriptionOrSwimlaneOrStartState();
        Map<String, VariableDescription> myVarMap = varMaps.get(processId);
        for (Object genObject : genObjectList) {
            if ((genObject instanceof ru.runa.xpdl.generated.jpdl32.ProcessState) || (genObject instanceof ru.runa.xpdl.generated.jpdl32.MultiinstanceState)) {
                String subProcessId = null;
                ru.runa.xpdl.generated.jpdl32.ProcessState processState = null;
                ru.runa.xpdl.generated.jpdl32.MultiinstanceState multiinstanceState = null;
                if (genObject instanceof ru.runa.xpdl.generated.jpdl32.ProcessState) {
                    processState = (ru.runa.xpdl.generated.jpdl32.ProcessState) (genObject);
                } else {
                    multiinstanceState = (ru.runa.xpdl.generated.jpdl32.MultiinstanceState) (genObject);
                }
                ru.runa.xpdl.generated.jpdl32.ProcessStateType.SubProcess subProcess = null;
                List processStateObjectList = ((processState != null) ? (processState.getSubProcessOrVariableOrDescription()) : (multiinstanceState
                        .getSubProcessOrVariableOrDescription()));
                for (Object procObject : processStateObjectList) {
                    if (procObject instanceof ru.runa.xpdl.generated.jpdl32.ProcessStateType.SubProcess) {
                        subProcess = (ru.runa.xpdl.generated.jpdl32.ProcessStateType.SubProcess) (procObject);
                        break;
                    }
                }
                subProcessId = subProcess.getName();
                ProcessDefinition subProcessDefinition = outWorkFlowProcessMap.get(subProcessId);
                if (subProcessDefinition != null) {
                    subProcess.setName(subProcessDefinition.getName());
                    // маппинг свимлайнов
                    String subProcessDefaultSwimlane = null;
                    List subProcObjectList = subProcessDefinition.getDescriptionOrSwimlaneOrStartState();
                    for (Object subProcObject : subProcObjectList) {
                        if (subProcObject instanceof ru.runa.xpdl.generated.jpdl32.StartState) {
                            List startStateObjectList = ((ru.runa.xpdl.generated.jpdl32.StartState) (subProcObject)).getDescriptionOrTaskOrTransition();
                            for (Object startStateObject : startStateObjectList) {
                                if (startStateObject instanceof ru.runa.xpdl.generated.jpdl32.Task) {
                                    subProcessDefaultSwimlane = ((ru.runa.xpdl.generated.jpdl32.Task) (startStateObject)).getSwimlane();
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    // Маппим все мои свимлэйны на subProcessDefaultSwimlane
                    for (Object genObject4Swimlane : genObjectList) {
                        if (genObject4Swimlane instanceof ru.runa.xpdl.generated.jpdl32.Swimlane) {
                            ru.runa.xpdl.generated.jpdl32.Swimlane mySwimlane = (ru.runa.xpdl.generated.jpdl32.Swimlane) (genObject4Swimlane);
                            ru.runa.xpdl.generated.jpdl32.Variable swimlaneMappingVariable = new ru.runa.xpdl.generated.jpdl32.impl.VariableImpl();
                            if (processState != null) {
                                processState.getSubProcessOrVariableOrDescription().add(swimlaneMappingVariable);
                            } else {
                                multiinstanceState.getSubProcessOrVariableOrDescription().add(swimlaneMappingVariable);
                            }
                            swimlaneMappingVariable.setName(mySwimlane.getName());
                            swimlaneMappingVariable.setMappedName(subProcessDefaultSwimlane);
                            swimlaneMappingVariable.setAccess("read");
                        }
                    }
                    // Построение и мэппинг переменных
                    Iterator<Map.Entry<String, VariableDescription>> subProcessVariableIter = varMaps.get(subProcessId).entrySet().iterator();
                    while (subProcessVariableIter.hasNext()) {
                        Map.Entry<String, VariableDescription> subProcessVar = subProcessVariableIter.next();
                        if ((subProcessVar.getValue().swimlane != null) && (subProcessVar.getValue().swimlane.booleanValue())) {
                            continue;
                        }
                        ru.runa.xpdl.generated.jpdl32.Variable mappingVariable = new ru.runa.xpdl.generated.jpdl32.impl.VariableImpl();
                        if (processState != null) {
                            processState.getSubProcessOrVariableOrDescription().add(mappingVariable);
                        } else {
                            multiinstanceState.getSubProcessOrVariableOrDescription().add(mappingVariable);
                        }
                        String myVariableName = "V" + subProcessVar.getKey();
                        mappingVariable.setName(myVariableName);
                        mappingVariable.setMappedName(subProcessVar.getKey());
                        mappingVariable.setAccess("read,write");
                        if (!myVarMap.containsKey(myVariableName)) {
                            myVarMap.put(myVariableName,
                                    new VariableDescription(subProcessVar.getValue().format, subProcessVar.getValue().publicVisibility, subProcessVar.getValue().defaultValue,
                                            subProcessVar.getValue().swimlane));
                        }
                    }
                }
            }
        }
    }

    private void saveData(File dir, ProcessDefinition processDefinition, Map<String, VariableDescription> stringVariableDescriptionMap, Element processDiagram,
            List<FormDescription> processFormList) throws Exception {
        File processDir = new File(dir, processDefinition.getName());
        createDir(processDir);
        writeEmptyFiles(processDir);
        PrintWriter varFile = new PrintWriter(new File(processDir, "variables.xml"), "UTF-8");
        varFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?><variables>");
        Iterator<Map.Entry<String, VariableDescription>> varIter = stringVariableDescriptionMap.entrySet().iterator();
        while (varIter.hasNext()) {
            Map.Entry<String, VariableDescription> var = varIter.next();
            varFile.println("<variable format=" + var.getValue().format + "\n" + " name=\"" + var.getKey() + "\""
                    + ((var.getValue().publicVisibility != null) ? (" public=\"" + var.getValue().publicVisibility.toString() + "\"") : (""))
                    + ((var.getValue().defaultValue != null) ? (" defaultValue=" + var.getValue().defaultValue) : (""))
                    + ((var.getValue().swimlane != null) ? (" swimlane=\"" + var.getValue().swimlane.toString() + "\"") : ("")) + "/>");
        }
        varFile.println("</variables>");
        varFile.close();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result gpdResult = new StreamResult(new File(processDir, "gpd.xml"));
        JAXBContext context = JAXBContext.newInstance("ru.runa.xpdl.generated.jpdl32");
        Marshaller m = context.createMarshaller();
        transformer.transform(new DOMSource(processDiagram), gpdResult);
        m.marshal(processDefinition, new java.io.FileOutputStream(new File(processDir, "processdefinition.xml")));
        PrintWriter formsFile = new PrintWriter(new File(processDir, "forms.xml"), "UTF-8");
        formsFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<forms xmlns=\"http://runa.ru/xml\"\n"
                + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://runa.ru/xml forms.xsd\" >");
        for (FormDescription formDescription : processFormList) {
            String validationFileName = formDescription.id + ".validation.xml";
            formsFile.println("<form file=\"" + formDescription.id + ".ftl\"\n" + "jsValidation=\"false\"\n" + "state=\"" + formDescription.stateName + "\"\n"
                    + "type=\"ftl\" validationFile=\"" + validationFileName + "\"/>");
            PrintWriter formsContentFile = new PrintWriter(new File(processDir, formDescription.id + ".ftl"), "UTF-8");
            formsContentFile.println(formDescription.content);
            formsContentFile.close();
            PrintWriter formsValidateFile = new PrintWriter(new File(processDir, validationFileName), "UTF-8");
            formsValidateFile.println(formDescription.getValidatorString());
            formsValidateFile.close();
        }
        formsFile.println("</forms>");
        formsFile.close();
    }

    private void makeWorkFlowProcess(ProcessType workflowProcess, double width, double height, Map<String, Swimlane> swimlaneMap, Map<String, Element> gpdMap,
            ProcessDefinition processDefinition, Map<String, VariableDescription> processVarMap, List<FormDescription> processFormsList, List associationList,
            Map<String, Artifact> artifactMap, boolean useDefaultSwimlane) throws Exception {
        Map<String, Swimlane> processSwimlaneMap = new HashMap<String, Swimlane>();
        Swimlane defaultSwimlane = null;
        Document gpd = ((DocumentBuilderFactory.newInstance()).newDocumentBuilder()).newDocument();
        gpd.createProcessingInstruction("xml", "version = \"1.0\"" + " encoding=\"UTF-8\"");
        Element processDiagram = gpd.createElement("process-diagram");
        gpdMap.put(workflowProcess.getId(), processDiagram);
        processDiagram.setAttribute("name", workflowProcess.getName());
        processDiagram.setAttribute("notation", "bpmn");
        processDiagram.setAttribute("showActions", "true");
        processDiagram.setAttribute("showGrid", "false");
        String startStateName = "$$begin_" + workflowProcess.getId();
        String endStateName = "$$end_" + workflowProcess.getId();
        String startTransitionDecisionName = "$$start_transition_" + workflowProcess.getId();
        String startFictiveDecisionTaskName = "$$start_task_" + workflowProcess.getId();
        List transitions = workflowProcess.getTransitions().getTransition();
        Map<String, GeneralNodeType> nodeMapById = new HashMap<String, GeneralNodeType>();
        Map<String, Element> gpdNodeMap = new HashMap<String, Element>();
        Map<String, List<TransitionDescription>> reverseTransitionMap = new HashMap<String, List<TransitionDescription>>(); // карта обратных
        // переходов
        Map<String, TransitionDescription> transitionDescriptionMap = new HashMap<String, TransitionDescription>();
        processDiagram.setAttribute("width", String.valueOf(new Double(width).intValue()));
        processDiagram.setAttribute("height", String.valueOf(new Double(height).intValue()));
        StartState startState = new StartStateImpl();
        startState.setName(startStateName);
        {
            Task task = new TaskImpl();
            task.setName(startStateName);
            startState.getDescriptionOrTaskOrTransition().add(task);
        }
        processDefinition.getDescriptionOrSwimlaneOrStartState().add(startState);
        Element gpdNodebegin = createGPDNode("48", "48", startStateName, String.valueOf(new Double(width / 2).intValue()), "10", processDiagram, gpd);
        EndState endState = new EndStateImpl();
        endState.setName(endStateName);
        processDefinition.getDescriptionOrSwimlaneOrStartState().add(endState);
        createGPDNode("48", "48", endStateName, String.valueOf(new Double(width / 2).intValue()), String.valueOf(new Double(height - 58).intValue()), processDiagram, gpd);
        EventType eventTypeNode = new EventImpl();
        eventTypeNode.setType("subprocess-created");
        processDefinition.getDescriptionOrSwimlaneOrStartState().add(eventTypeNode);
        List activityList = workflowProcess.getActivities().getActivity();
        for (Object _ac : activityList) {
            ru.runa.xpdl.generated.bpmnxpdl.ActivityType activityType = (ru.runa.xpdl.generated.bpmnxpdl.ActivityType) (_ac);
            // Ветвления пока не делаем
            String taskName = activityType.getName() + "_" + activityType.getId();
            ru.runa.xpdl.generated.bpmnxpdl.RouteType routeType = activityType.getRoute();
            ru.runa.xpdl.generated.bpmnxpdl.ImplementationType implementationType = activityType.getImplementation();
            ru.runa.xpdl.generated.bpmnxpdl.SubFlowType subFlowType = null;
            if (implementationType != null) {
                subFlowType = implementationType.getSubFlow();
            }
            TaskNode taskNode = null;
            Task task = null;
            Decision decisionNode = null;
            Fork forkNode = null;
            Join joinNode = null;
            ProcessState processStateNode = null;
            // never null
            if ((taskName == null) || (taskName.length() == 0)) {
                taskName = activityType.getId();
            }
            if (subFlowType != null) {
                processStateNode = new ProcessStateImpl();
                String subProcessId = subFlowType.getId();
                taskName = "Запуск процесса  [" + taskName + "]";
                nodeMapById.put(activityType.getId(), processStateNode);
                processStateNode.setName(taskName);
                processDefinition.getDescriptionOrSwimlaneOrStartState().add(processStateNode);
                ru.runa.xpdl.generated.jpdl32.ProcessStateType.SubProcess subProcess = new ProcessStateTypeImpl.SubProcessImpl();
                subProcess.setName(subProcessId); // Временно, при установке перекресных ссылок тут надо будет проставлять реальное имя процесса
                subProcess.setBinding("late"); // проверить - может зависеть от значения параметра Execution
                processStateNode.getSubProcessOrVariableOrDescription().add(subProcess);
                {
                    List dataMappings = null;
                    if (subFlowType.getDataMappings() != null) {
                        dataMappings = subFlowType.getDataMappings().getDataMapping();
                    }
                    if (dataMappings != null) {
                        for (Object _datamapping : dataMappings) {
                            if (!(_datamapping instanceof DataMappingType)) {
                                continue;
                            }
                            DataMappingType dataMappingType = (DataMappingType) (_datamapping);
                            ExpressionType actualExpression = dataMappingType.getActual();
                            String actualVarName = null;
                            if (actualExpression != null) {
                                if (actualExpression.getContent() != null) {
                                    for (Object aeCo : actualExpression.getContent()) {
                                        actualVarName += (actualVarName == null) ? (aeCo.toString()) : ("$" + aeCo.toString());
                                    }
                                }
                            }
                            if (actualVarName == null) {
                                continue;
                            }
                            actualVarName = Helper.generateVariableName(actualVarName);
                            String formalName = dataMappingType.getFormal();
                            if (formalName == null) {
                                continue;
                            }
                            String direction = dataMappingType.getDirection();
                            if (direction == null) {
                                direction = "IN";
                            }
                            Variable variable = new VariableImpl();
                            variable.setName(actualVarName);
                            variable.setMappedName(formalName);
                            variable.setAccess(direction.equalsIgnoreCase("IN") ? ("read") : (direction.equalsIgnoreCase("OUT") ? ("write") : ("read,write")));
                            processStateNode.getSubProcessOrVariableOrDescription().add(variable);
                            if (!processVarMap.containsKey(actualVarName)) {
                                processVarMap.put(actualVarName, new VariableDescription("\"ru.runa.wfe.var.format.LongFormat\"", true, "\"0\"", null));
                            }
                        }
                    }
                }
            } else if (routeType != null) {
                // Работаем с Route - Делаем пока то, что знаем
                boolean exclusive = ((routeType.getGatewayType().equalsIgnoreCase("Exclusive")) || (routeType.getGatewayType().equalsIgnoreCase("XOR"))
                        || (routeType.getGatewayType().equalsIgnoreCase("Inclusive")) || (routeType.getGatewayType().equalsIgnoreCase("OR")));
                if (!exclusive && (isMergeActivity(activityType, transitions))) {
                    joinNode = new JoinImpl();
                    nodeMapById.put(activityType.getId(), joinNode);
                    processDefinition.getDescriptionOrSwimlaneOrStartState().add(joinNode);
                    joinNode.setName(taskName);
                } else if (exclusive) {
                    decisionNode = createDescisionNode(nodeMapById, activityType.getId(), processDefinition, taskName, processVarMap);
                } else {
                    forkNode = new ForkImpl();
                    nodeMapById.put(activityType.getId(), forkNode);
                    processDefinition.getDescriptionOrSwimlaneOrStartState().add(forkNode);
                    forkNode.setName(taskName);
                }
            } else {
                taskNode = new TaskNodeImpl();
                nodeMapById.put(activityType.getId(), taskNode);
                taskNode.setName(taskName);
                task = new TaskImpl();
                taskNode.getTaskOrDescriptionOrEvent().add(task);
                task.setName(taskName);
                processDefinition.getDescriptionOrSwimlaneOrStartState().add(taskNode);
            }
            // boolean swimLaneSet=false;
            if ((!useDefaultSwimlane) && (activityType.getPerformers() != null) && (activityType.getPerformers().getPerformer() != null) && (task != null)) {
                for (Object _performer : activityType.getPerformers().getPerformer()) {
                    if (_performer instanceof ru.runa.xpdl.generated.bpmnxpdl.Performer) {
                        ru.runa.xpdl.generated.bpmnxpdl.Performer performer = (ru.runa.xpdl.generated.bpmnxpdl.Performer) (_performer);
                        Swimlane swimlane = swimlaneMap.get(performer.getValue());
                        if (swimlane != null) {
                            task.setSwimlane(swimlane.getName());
                            if (processSwimlaneMap.get(performer.getValue()) == null) {
                                processSwimlaneMap.put(performer.getValue(), swimlane);
                                processDefinition.getDescriptionOrSwimlaneOrStartState().add(swimlane);
                                processVarMap.put(swimlane.getName(), new VariableDescription("\"ru.runa.wfe.var.format.StringFormat\"", null, null, true));
                            }
                        }
                    }
                }
            }
            List NGIList = activityType.getNodeGraphicsInfos().getNodeGraphicsInfo();
            for (Object _ngi : NGIList) {
                ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfoType nodeGraphicsInfo = (ru.runa.xpdl.generated.bpmnxpdl.NodeGraphicsInfoType) (_ngi);
                // if ((!swimLaneSet) && (task != null))
                if ((task != null) && ((defaultSwimlane == null) || (task.getSwimlane() == null))) {
                    Swimlane swimlane = swimlaneMap.get(nodeGraphicsInfo.getLaneId());
                    if (swimlane != null) {
                        if (defaultSwimlane == null) {
                            defaultSwimlane = swimlane;
                            ((Task) (startState.getDescriptionOrTaskOrTransition().get(0))).setSwimlane(defaultSwimlane.getName());
                        }
                        if (task.getSwimlane() == null) {
                            task.setSwimlane(swimlane.getName());
                        }
                        if (processSwimlaneMap.get(nodeGraphicsInfo.getLaneId()) == null) {
                            processSwimlaneMap.put(nodeGraphicsInfo.getLaneId(), swimlane);
                            processDefinition.getDescriptionOrSwimlaneOrStartState().add(swimlane);
                            processVarMap.put(swimlane.getName(), new VariableDescription("\"ru.runa.wfe.var.format.StringFormat\"", null, null, true));
                        }
                    }
                }
                Element gpdNode = createGPDNode(String.valueOf(new Double(nodeGraphicsInfo.getHeight()).intValue()),
                        String.valueOf(new Double(nodeGraphicsInfo.getWidth()).intValue()), taskName,
                        String.valueOf(new Double(nodeGraphicsInfo.getCoordinates().getXCoordinate()).intValue()),
                        String.valueOf(new Double(nodeGraphicsInfo.getCoordinates().getYCoordinate()).intValue()), processDiagram, gpd);
                gpdNodeMap.put(taskName, gpdNode);
                GeneralNodeType genNode = ((taskNode != null) ? (taskNode) : (decisionNode != null) ? (decisionNode) : (forkNode != null) ? (forkNode)
                        : (joinNode != null) ? (joinNode) : (processStateNode != null) ? (processStateNode) : (null));
                if (isStartActivity(activityType, transitions)) {
                    generateStartTransition(startState, genNode, gpdNodebegin, gpd, reverseTransitionMap, transitionDescriptionMap);
                }
                if (isFinishActivity(activityType, transitions)) {
                    if (genNode != null) {
                        generateEndTransition(genNode, endState, gpdNode, gpd, reverseTransitionMap, transitionDescriptionMap);
                    }
                }
                break;
            }
            // Посторение формы заполнения данных
            if (associationList != null) {
                for (Object _acc : associationList) {
                    Artifact artifact = null;
                    ru.runa.xpdl.generated.bpmnxpdl.Association association = (ru.runa.xpdl.generated.bpmnxpdl.Association) (_acc);
                    if (activityType.getId().equals(association.getSource())) {
                        artifact = artifactMap.get(association.getTarget());
                    }
                    if (activityType.getId().equals(association.getTarget())) {
                        artifact = artifactMap.get(association.getSource());
                    }
                    if (artifact == null) {
                        continue;
                    }
                    this.generateFormTextAreaDescription(processFormsList, taskName, activityType.getId(), artifact.getName(), processVarMap, artifact.getId());
                }
            }
        }
        int transitionIndex = 1;
        // Проверяем сколько переходов из стартового состояния и если>1 -то добавляем фиктивный Decision
        List startStateTransitionList = startState.getDescriptionOrTaskOrTransition();
        for (Object _o : startStateTransitionList) {
            if (!(_o instanceof ru.runa.xpdl.generated.jpdl32.Transition)) {
                continue;
            }
            transitionIndex++;
            if (transitionIndex == 3) {
                break;
            }
        }
        if (transitionIndex == 3) // более одного перехода делаем фиктивное разветвление
        {
            Decision decision = createDescisionNode(nodeMapById, startTransitionDecisionName, processDefinition, startTransitionDecisionName, processVarMap);
            Element decisionGpdNode = createGPDNode("48", "48", decision.getName(), "50", "50", processDiagram, gpd);
            TaskNode taskNode = new TaskNodeImpl();
            nodeMapById.put(startFictiveDecisionTaskName, taskNode);
            taskNode.setName(startFictiveDecisionTaskName);
            Task task = new TaskImpl();
            task.setSwimlane(defaultSwimlane.getName());
            taskNode.getTaskOrDescriptionOrEvent().add(task);
            task.setName(startFictiveDecisionTaskName);
            processDefinition.getDescriptionOrSwimlaneOrStartState().add(taskNode);
            Element taskGpdNode = createGPDNode("48", "48", taskNode.getName(), "100", "50", processDiagram, gpd);
            // Удаляем старые переходы из стартовой ноды и перенос их в decision
            Iterator startStateTransitionIter = startStateTransitionList.iterator();
            while (startStateTransitionIter.hasNext()) {
                Object _o = startStateTransitionIter.next();
                if (!(_o instanceof ru.runa.xpdl.generated.jpdl32.Transition)) {
                    continue;
                }
                ru.runa.xpdl.generated.jpdl32.Transition transition = (ru.runa.xpdl.generated.jpdl32.Transition) (_o);
                org.w3c.dom.NodeList childDodesList = gpdNodebegin.getChildNodes();
                for (int i = 0; i < childDodesList.getLength(); i++) {
                    org.w3c.dom.Node node = childDodesList.item(i);
                    if ((node.getNodeName().equals("transition")) && (node.getAttributes().getNamedItem("name").getNodeValue().equals(transition.getName()))) {
                        gpdNodebegin.removeChild(node);
                        break;
                    }
                }
                Transition _transition = new TransitionImpl();
                String transitionName = transition.getName() + "$$START_DECISION";
                _transition.setName(transitionName);
                _transition.setTo(transition.getTo());
                addTransitionToReverseMap(_transition, reverseTransitionMap, transitionDescriptionMap, decision, transitionDescriptionMap.get(transition.getName()).to);
                decision.addTransition(_transition);
                Element transElement = gpd.createElement("transition");
                transElement.setAttribute("name", _transition.getName());
                decisionGpdNode.appendChild(transElement);
                removeTransitionFromReverseMap(transition, reverseTransitionMap, transitionDescriptionMap);
                startStateTransitionIter.remove();
            }
            // генерим переход из стартового состояния в фиктивный узел -действие
            generateStartTransition(startState, taskNode, gpdNodebegin, gpd, reverseTransitionMap, transitionDescriptionMap);
            // генерим переход из фиктивного узла -действия в decision
            Transition __transition = new TransitionImpl();
            __transition.setName("F$$_" + startTransitionDecisionName);
            __transition.setTo(decision.getName());
            addTransitionToReverseMap(__transition, reverseTransitionMap, transitionDescriptionMap, taskNode, decision);
            taskNode.addTransition(__transition);
            Element transElement = gpd.createElement("transition");
            transElement.setAttribute("name", __transition.getName());
            taskGpdNode.appendChild(transElement);
            // Генерим форму для стартового состояния
            generateFormDescription(processFormsList, taskNode, decision, startState.getName());
        }
        transitionIndex = 1;
        for (Object _tr : transitions) {
            ru.runa.xpdl.generated.bpmnxpdl.TransitionType transitionType = (ru.runa.xpdl.generated.bpmnxpdl.TransitionType) (_tr);
            GeneralNodeType nodeFrom = nodeMapById.get(transitionType.getFrom());
            if (nodeFrom == null) {
                continue;
            }
            String nodeNameFrom = nodeFrom.getName();
            GeneralNodeType nodeTo = nodeMapById.get(transitionType.getTo());
            if (nodeTo == null) {
                continue;
            }
            String nodeNameTo = nodeTo.getName();
            Element gpdNode = gpdNodeMap.get(nodeNameFrom);
            if (gpdNode == null) {
                continue;
            }
            Transition transition = new TransitionImpl();
            String transitionName = transitionType.getName();
            if (transitionName == null) {
                transition.setName("Переход_" + transitionIndex++);
            } else {
                transition.setName(transitionName + "_" + transitionIndex++);
            }
            transition.setTo(nodeNameTo);
            addTransitionToReverseMap(transition, reverseTransitionMap, transitionDescriptionMap, nodeFrom, nodeTo);
            nodeFrom.addTransition(transition);
            Element transElement = gpd.createElement("transition");
            transElement.setAttribute("name", transition.getName());
            gpdNode.appendChild(transElement);
        }
        // генерим join-ы по параллельным веткам
        {
            LinkedList<Join> addJoinNodeList = new LinkedList<Join>();
            for (GeneralNodeType node : nodeMapById.values()) {
                {
                    if (node instanceof Join) {
                        continue;
                    }
                    // System.out.println("Reverse Processing node:"+node.getName());
                    List<TransitionDescription> transitionToList = reverseTransitionMap.get(node.getName());
                    if ((transitionToList == null) || (transitionToList.size() <= 1)) {
                        continue;
                    }
                    Map<String, List<GeneralNodeType>> transitionNodeMap = new HashMap<String, List<GeneralNodeType>>(); // карта доступных вершин для
                    // каждого перехода
                    List<TransitionDescription> transitionToJoinList = new LinkedList<TransitionDescription>();
                    for (TransitionDescription transitionDescription : transitionToList) {
                        List<GeneralNodeType> transitionNodeList = new LinkedList<GeneralNodeType>();
                        transitionNodeMap.put(transitionDescription.transition.getName(), fillTransitionNodeList(transitionDescription, reverseTransitionMap, transitionNodeList));
                    }
                    Map<String, TransitionDescription> forkEntryMap = new HashMap<String, TransitionDescription>();// карта форк-вершин и первого
                    // перехода.
                    for (Map.Entry<String, List<GeneralNodeType>> transitionNodeMapEntry : transitionNodeMap.entrySet()) {
                        // System.out.println("Entry for Transition:"+transitionNodeMapEntry.getKey());
                        for (GeneralNodeType nodeInBranch : transitionNodeMapEntry.getValue()) {
                            // System.out.println("nodeInBranch: name="+nodeInBranch.getName()+",is ForkTypeImpl="+(nodeInBranch instanceof
                            // ForkTypeImpl));
                            if (nodeInBranch instanceof ForkTypeImpl) {
                                if (forkEntryMap.containsKey(nodeInBranch.getName())) // параллельная ветка уже встретилась
                                {
                                    if (!isTransitionInList(transitionToJoinList, forkEntryMap.get(nodeInBranch.getName()))) {
                                        transitionToJoinList.add(forkEntryMap.get(nodeInBranch.getName()));
                                    }
                                    transitionToJoinList.add(transitionDescriptionMap.get(transitionNodeMapEntry.getKey()));
                                } else {
                                    forkEntryMap.put(nodeInBranch.getName(), transitionDescriptionMap.get(transitionNodeMapEntry.getKey()));
                                }
                                break;
                            }
                        }
                    }
                    // System.out.println("transitiontojoinlist size:" + transitionToJoinList.size());
                    if (transitionToJoinList.size() > 0) // надо объединять переходы в join
                    {
                        Join joinNode = new JoinImpl();
                        String joinNodeName = "$$FJ" + node.getName();
                        // nodeMapById.put(joinNodeName,joinNode);
                        addJoinNodeList.add(joinNode);
                        processDefinition.getDescriptionOrSwimlaneOrStartState().add(joinNode);
                        joinNode.setName(joinNodeName);
                        Element joinGpdNode = createGPDNode("48", "48", joinNode.getName(), "50", "50", processDiagram, gpd);
                        // генерим переходы в эту самую join-ноду
                        for (TransitionDescription joinTransitionDescr : transitionToJoinList) {
                            // Удаляем старые переходы
                            Iterator fromNodeTransitionIter = joinTransitionDescr.from.getSubObjectList().iterator();
                            Element fromNodeGpdNode = gpdNodeMap.get(joinTransitionDescr.from.getName());
                            List<Transition> addToNodeTransitionList = new LinkedList<Transition>();
                            while (fromNodeTransitionIter.hasNext()) {
                                Object _o = fromNodeTransitionIter.next();
                                if (!(_o instanceof ru.runa.xpdl.generated.jpdl32.Transition)) {
                                    continue;
                                }
                                ru.runa.xpdl.generated.jpdl32.Transition transition = (ru.runa.xpdl.generated.jpdl32.Transition) (_o);
                                if (!transition.getName().equals(joinTransitionDescr.transition.getName())) {
                                    continue;
                                }
                                org.w3c.dom.NodeList childDodesList = fromNodeGpdNode.getChildNodes();
                                for (int i = 0; i < childDodesList.getLength(); i++) {
                                    org.w3c.dom.Node _gpdNode = childDodesList.item(i);
                                    if ((_gpdNode.getNodeName().equals("transition"))
                                            && (_gpdNode.getAttributes().getNamedItem("name").getNodeValue().equals(transition.getName()))) {
                                        fromNodeGpdNode.removeChild(_gpdNode);
                                        break;
                                    }
                                }
                                Transition _transition = new TransitionImpl();
                                _transition.setName(transition.getName() + "$$JOIN_TRANS");
                                _transition.setTo(joinNode.getName());
                                addTransitionToReverseMap(_transition, reverseTransitionMap, transitionDescriptionMap, joinTransitionDescr.from, joinNode);
                                // joinTransitionDescr.from.addTransition(_transition);
                                addToNodeTransitionList.add(_transition);
                                Element transElement = gpd.createElement("transition");
                                transElement.setAttribute("name", _transition.getName());
                                fromNodeGpdNode.appendChild(transElement);
                                removeTransitionFromReverseMap(transition, reverseTransitionMap, transitionDescriptionMap);
                                fromNodeTransitionIter.remove();
                                break;
                            }
                            for (Transition transition : addToNodeTransitionList) {
                                joinTransitionDescr.from.addTransition(transition);
                            }
                        }
                        // генерим переход из join-ноды
                        Transition _transition = new TransitionImpl();
                        _transition.setName("$$JOIN_TRANSITION_" + joinNode.getName());
                        _transition.setTo(node.getName());
                        addTransitionToReverseMap(_transition, reverseTransitionMap, transitionDescriptionMap, joinNode, node);
                        joinNode.addTransition(_transition);
                        Element transElement = gpd.createElement("transition");
                        transElement.setAttribute("name", _transition.getName());
                        joinGpdNode.appendChild(transElement);
                    }
                }
            }
            for (Join joinNode : addJoinNodeList) {
                nodeMapById.put(joinNode.getName(), joinNode);
            }
        }
        // Формы по переходам
        for (Object tr : transitions) {
            ru.runa.xpdl.generated.bpmnxpdl.TransitionType transitionType = (ru.runa.xpdl.generated.bpmnxpdl.TransitionType) (tr);
            GeneralNodeType nodeFrom = nodeMapById.get(transitionType.getFrom());
            if (nodeFrom == null) {
                continue;
            }
            GeneralNodeType nodeTo = nodeMapById.get(transitionType.getTo());
            if (nodeTo == null) {
                continue;
            }
            // Работаем с формой
            if (nodeTo instanceof DecisionType) {
                // генерим запросную форму
                generateFormDescription(processFormsList, nodeFrom, (DecisionType) (nodeTo), transitionType.getFrom());
            }
        }
    }

    private List<GeneralNodeType> fillTransitionNodeList(TransitionDescription transitionDescription, Map<String, List<TransitionDescription>> reverseTransitionMap,
            List<GeneralNodeType> transitionNodeList) {
        // System.out.println("_fillTransitionNodeList:From:"+transitionDescription.from.getName()+",TO:"+transitionDescription.to.getName());
        if (!(transitionDescription.from instanceof DecisionType) && !(transitionDescription.from instanceof ProcessState) && !(transitionDescription.from instanceof TaskNode)
                && !(transitionDescription.from instanceof ForkType)) {
            return transitionNodeList;
        }
        if (isNodeInList(transitionDescription.from, transitionNodeList)) {
            return transitionNodeList;
        }
        transitionNodeList.add(transitionDescription.from);
        if (transitionDescription instanceof ForkType) {
            return transitionNodeList;
        }
        List<TransitionDescription> nodeFromReverseTransitionDescriptionList = reverseTransitionMap.get(transitionDescription.from.getName());
        if (nodeFromReverseTransitionDescriptionList == null) {
            return transitionNodeList;
        }
        for (TransitionDescription _transitionDescription : nodeFromReverseTransitionDescriptionList) {
            fillTransitionNodeList(_transitionDescription, reverseTransitionMap, transitionNodeList);
        }
        return transitionNodeList;
    }

    private boolean isNodeInList(GeneralNodeType node, List<GeneralNodeType> transitionNodeList) {
        for (GeneralNodeType generalNodeType : transitionNodeList) {
            if (generalNodeType.getName().equals(node.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isTransitionInList(List<TransitionDescription> transitionToJoinList, TransitionDescription transitionDescription) {
        for (TransitionDescription _transitionDescription : transitionToJoinList) {
            if (_transitionDescription.transition.getName().equals(transitionDescription.transition.getName())) {
                return true;
            }
        }
        return false;
    }

    private void removeTransitionFromReverseMap(Transition transition, Map<String, List<TransitionDescription>> reverseTransitionMap,
            Map<String, TransitionDescription> transitionDescriptionMap) {
        transitionDescriptionMap.remove(transition.getName());
        List<TransitionDescription> reverseTransitionList = reverseTransitionMap.get(transition.getTo());
        if (reverseTransitionList != null) {
            Iterator<TransitionDescription> transitionDescriptionIterator = reverseTransitionList.iterator();
            while (transitionDescriptionIterator.hasNext()) {
                TransitionDescription transitionDescription = transitionDescriptionIterator.next();
                if (transitionDescription.transition.getName().equals(transition.getName())) {
                    transitionDescriptionIterator.remove();
                    break;
                }
            }
        }
    }

    private void addTransitionToReverseMap(Transition transition, Map<String, List<TransitionDescription>> reverseTransitionMap,
            Map<String, TransitionDescription> transitionDescriptionMap, GeneralNodeType from, GeneralNodeType to) {
        TransitionDescription transitionDescription = transitionDescriptionMap.get(transition.getName());
        if (transitionDescription != null) {
            return;
        }
        transitionDescription = new TransitionDescription(transition, from, to);
        transitionDescriptionMap.put(transition.getName(), transitionDescription);
        List<TransitionDescription> reverseTransitionList = reverseTransitionMap.get(transition.getTo());
        if (reverseTransitionList == null) {
            reverseTransitionList = new LinkedList<TransitionDescription>();
            reverseTransitionMap.put(transition.getTo(), reverseTransitionList);
        }
        reverseTransitionList.add(transitionDescription);
    }

    private Element createGPDNode(String height, String width, String name, String x, String y, Element processDiagram, Document gpd) {
        Element gpdNode = gpd.createElement("node");
        gpdNode.setAttribute("height", height);
        gpdNode.setAttribute("width", width);
        gpdNode.setAttribute("minimizedView", "false");
        gpdNode.setAttribute("name", name);
        gpdNode.setAttribute("x", x);
        gpdNode.setAttribute("y", y);
        processDiagram.appendChild(gpdNode);
        return gpdNode;
    }

    private void generateFormTextAreaDescription(List<FormDescription> processFormsList, String nodeName, String id, String dataObjectName,
            Map<String, VariableDescription> processVarMap, String artefactId) {
        FormDescription formDescription = null;
        for (FormDescription _formDescription : processFormsList) {
            if (_formDescription.stateName.equals(nodeName)) {
                formDescription = _formDescription;
                break;
            }
        }
        if (formDescription == null) {
            formDescription = new FormDescription(id, nodeName, nodeName);
            processFormsList.add(formDescription);
        }
        String varName = Helper.generateVariableName(artefactId);
        if (!processVarMap.containsKey(varName)) {
            processVarMap.put(varName,
                    new VariableDescription("\"ru.runa.wfe.var.format.StringFormat\"", true, "\"" + Messages.getString("XPDLConnector.formValidator.EnterDataToTextField") + "\"",
                            null));
        }
        formDescription.addDataObject(dataObjectName, varName);
    }

    private void generateFormDescription(List<FormDescription> processFormsList, GeneralNodeType nodeFrom, DecisionType nodeTo, String from) {
        FormDescription formDescription = null;
        String nodeNameFrom = nodeFrom.getName();
        for (FormDescription _formDescription : processFormsList) {
            if (_formDescription.stateName.equals(nodeNameFrom)) {
                formDescription = _formDescription;
                break;
            }
        }
        if (formDescription == null) {
            formDescription = new FormDescription(from, nodeNameFrom, nodeNameFrom);
            processFormsList.add(formDescription);
        }
        String varName = Helper.generateVariableName(nodeTo.getName());
        int transIndex = 0;
        for (Object __tr : nodeTo.getDescriptionOrHandlerOrEvent()) {
            if (!(__tr instanceof Transition)) {
                continue;
            }
            Transition _trans = (Transition) (__tr);
            formDescription.addChoiceObject(varName, transIndex, "Переход к[" + _trans.getTo() + "]");
            transIndex++;
        }
    }

    private Decision createDescisionNode(Map<String, GeneralNodeType> nodeMap, String id, ProcessDefinition processDefinition, String taskName,
            Map<String, VariableDescription> processVarMap) {
        DecisionImpl decisionNode = new DecisionImpl();
        nodeMap.put(id, decisionNode);
        processDefinition.getDescriptionOrSwimlaneOrStartState().add(decisionNode);
        decisionNode.setName(taskName);
        ru.runa.xpdl.generated.jpdl32.DecisionType.Handler handler = new DecisionTypeImpl.HandlerImpl();
        handler.setClass2("ru.runa.wfe.handler.decision.GroovyDecisionHandler");
        handler.setConfigType("configuration-property");
        String varName = Helper.generateVariableName(taskName);
        if (!processVarMap.containsKey(varName)) {
            processVarMap.put(varName, new VariableDescription("\"ru.runa.wfe.var.format.LongFormat\"", true, "\"0\"", null));
        }
        handler.getContent().add(new String("<![CDATA[]]>"));
        decisionNode.getDescriptionOrHandlerOrEvent().add(handler);
        return decisionNode;
    }

    private boolean isMergeActivity(ActivityType activityType, List transitions) {
        int outGoingTransitions = 0;
        for (Object _tr : transitions) {
            ru.runa.xpdl.generated.bpmnxpdl.TransitionType transition = (ru.runa.xpdl.generated.bpmnxpdl.TransitionType) (_tr);
            if (transition.getFrom().equals(activityType.getId())) {
                outGoingTransitions++;
            }
        }
        return ((outGoingTransitions <= 1) ? (true) : (false));
    }

    private boolean isFinishActivity(ActivityType activityType, List transitions) {
        for (Object _tr : transitions) {
            ru.runa.xpdl.generated.bpmnxpdl.TransitionType transition = (ru.runa.xpdl.generated.bpmnxpdl.TransitionType) (_tr);
            if (transition.getFrom().equals(activityType.getId())) {
                return false;
            }
        }
        return true;
    }

    // Определяет является ли activityType стартовоым
    private boolean isStartActivity(ActivityType activityType, List transitions) {
        for (Object _tr : transitions) {
            ru.runa.xpdl.generated.bpmnxpdl.TransitionType transition = (ru.runa.xpdl.generated.bpmnxpdl.TransitionType) (_tr);
            if (transition.getTo().equals(activityType.getId())) {
                return false;
            }
        }
        return true; // To change body of created methods use File | Settings | File Templates.
    }

    private void generateStartTransition(StartState startState, GeneralNodeType nodeTo, Element gpdNodebegin, Document gpd,
            Map<String, List<TransitionDescription>> reverseTransitionMap, Map<String, TransitionDescription> transitionDescriptionMap) {
        Transition transition = new TransitionImpl();
        transition.setName(nodeTo.getName() + "_start_tr");
        transition.setTo(nodeTo.getName());
        addTransitionToReverseMap(transition, reverseTransitionMap, transitionDescriptionMap, startState, nodeTo);
        startState.getDescriptionOrTaskOrTransition().add(transition);
        Element transElement = gpd.createElement("transition");
        transElement.setAttribute("name", transition.getName());
        gpdNodebegin.appendChild(transElement);
    }

    private void generateEndTransition(GeneralNodeType node, EndState endState, Element gpdNode, Document gpd, Map<String, List<TransitionDescription>> reverseTransitionMap,
            Map<String, TransitionDescription> transitionDescriptionMap) {
        Transition transition = new TransitionImpl();
        transition.setName(node.getName() + "_fin_tr");
        transition.setTo(endState.getName());
        addTransitionToReverseMap(transition, reverseTransitionMap, transitionDescriptionMap, node, endState);
        node.addTransition(transition);
        Element transElement = gpd.createElement("transition");
        transElement.setAttribute("name", transition.getName());
        gpdNode.appendChild(transElement);
    }

    private void writeEmptyFiles(File processDir) throws Exception {
        PrintWriter swimlaneGUIConfigFile = new PrintWriter(new File(processDir, "swimlaneGUIconfig.xml"), "UTF-8");
        swimlaneGUIConfigFile.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<swimlanes/>");
        swimlaneGUIConfigFile.close();
    }
}
