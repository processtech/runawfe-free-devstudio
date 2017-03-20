package ru.runa.gpd.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.resources.IFile;

import ru.runa.gpd.Application;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.Describable;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.EndTokenSubprocessDefinitionBehavior;
import ru.runa.gpd.lang.model.EventNodeType;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.NodeAsyncExecution;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Synchronizable;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.TimerAction;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.Conjunction;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.lang.model.jpdl.ActionEventType;
import ru.runa.gpd.lang.model.jpdl.ActionImpl;
import ru.runa.gpd.lang.model.jpdl.ActionNode;
import ru.runa.gpd.lang.model.jpdl.Fork;
import ru.runa.gpd.lang.model.jpdl.Join;
import ru.runa.gpd.lang.model.jpdl.CatchEventNode;
import ru.runa.gpd.lang.model.jpdl.ThrowEventNode;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;
import ru.runa.wfe.lang.AsyncCompletionMode;
import ru.runa.wfe.lang.MultiTaskCreationMode;
import ru.runa.wfe.lang.MultiTaskSynchronizationMode;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class JpdlSerializer extends ProcessSerializer {
    private static final String PROCESS_DEFINITION = "process-definition";

    private static final String INVALID = "invalid";
    private static final String ACCESS = "access";
    private static final String END_STATE = "end-state";
    private static final String SUB_PROCESS = "sub-process";
    private static final String MAPPED_NAME = "mapped-name";
    private static final String PROCESS_STATE = "process-state";
    private static final String MULTIINSTANCE_STATE = "multiinstance-state";
    private static final String DECISION = "decision";
    private static final String CONJUNCTION = "conjunction";
    private static final String JOIN = "join";
    private static final String FORK = "fork";
    private static final String DUEDATE = "duedate";
    private static final String DEFAULT_TASK_DUEDATE = "default-task-duedate";
    private static final String REPEAT = "repeat";
    private static final String TIMER = "timer";
    private static final String ASSIGNMENT = "assignment";
    private static final String TASK_NODE = "task-node";
    private static final String TASK = "task";
    private static final String WAIT_STATE = "wait-state";
    private static final String START_STATE = "start-state";
    private static final String TO = "to";
    private static final String ACTION = "action";
    private static final String EVENT = "event";
    private static final String HANDLER = "handler";
    private static final String DESCRIPTION = "description";
    private static final String SWIMLANE = "swimlane";
    private static final String TRANSITION = "transition";
    private static final String SEND_MESSAGE = "send-message";
    private static final String RECEIVE_MESSAGE = "receive-message";
    private static final String ACTION_NODE = "node";
    private static final String TIMER_ESCALATION = "__ESCALATION";
    private static final String END_TOKEN = "end-token-state";
    private static final String MULTI_TASK_NODE = "multi-task-node";
    private static final String TASK_EXECUTORS_USAGE = "taskExecutorsUsage";
    private static final String TASK_EXECUTORS_VALUE = "taskExecutors";
    private static final String TASK_EXECUTION_MODE = "taskExecutionMode";

    @Override
    public boolean isSupported(Document document) {
        return PROCESS_DEFINITION.equals(document.getRootElement().getName());
    }

    @Override
    public Document getInitialProcessDefinitionDocument(String processName, Map<String, String> properties) {
        Document document = XmlUtil.createDocument(PROCESS_DEFINITION);
        document.getRootElement().addAttribute(NAME, processName);
        if (properties != null && properties.containsKey(ID)) {
            document.getRootElement().addAttribute(ID, properties.get(ID));
        }
        if (properties != null && properties.containsKey(ACCESS_TYPE)) {
            document.getRootElement().addAttribute(ACCESS_TYPE, properties.get(ACCESS_TYPE));
        }
        return document;
    }

    @Override
    public void saveToXML(ProcessDefinition definition, Document document) {
        Element root = document.getRootElement();
        if (definition.getId() != null) {
            root.addAttribute(ID, definition.getId());
        }
        root.addAttribute(NAME, definition.getName());
        root.addAttribute(VERSION, Application.getVersion().toString());
        root.addAttribute(ACCESS_TYPE, definition.getAccessType().name());
        if (definition.getDefaultTaskTimeoutDelay().hasDuration()) {
            root.addAttribute(DEFAULT_TASK_DUEDATE, definition.getDefaultTaskTimeoutDelay().getDuration());
        }
        if (definition.isInvalid()) {
            root.addAttribute(INVALID, String.valueOf(definition.isInvalid()));
        }
        if (definition.getDefaultNodeAsyncExecution() != NodeAsyncExecution.DEFAULT) {
            root.addAttribute(NODE_ASYNC_EXECUTION, definition.getDefaultNodeAsyncExecution().getValue());
        }
        if (!Strings.isNullOrEmpty(definition.getDescription())) {
            Element desc = root.addElement(DESCRIPTION);
            setNodeValue(desc, definition.getDescription());
        }
        if (definition.getClass() != SubprocessDefinition.class) {
            List<Swimlane> swimlanes = definition.getSwimlanes();
            for (Swimlane swimlane : swimlanes) {
                Element swimlaneElement = writeElement(root, swimlane);
                writeDelegation(swimlaneElement, ASSIGNMENT, swimlane);
            }
        }
        StartState startState = definition.getFirstChild(StartState.class);
        if (startState != null) {
            Element startStateElement = writeTaskState(root, startState);
            writeTransitions(startStateElement, startState);
        }
        // back compatibility
        List<ActionNode> actionNodeNodes = definition.getChildren(ActionNode.class);
        for (ActionNode actionNode : actionNodeNodes) {
            Element actionNodeElement = writeNode(root, actionNode, null);
            for (Action action : actionNode.getActions()) {
                ActionImpl actionImpl = (ActionImpl) action;
                if (!ActionEventType.NODE_ACTION.equals(actionImpl.getEventType())) {
                    writeEvent(actionNodeElement, new ActionEventType(actionImpl.getEventType()), actionImpl);
                }
            }
        }
        List<Decision> decisions = definition.getChildren(Decision.class);
        for (Decision decision : decisions) {
            writeNode(root, decision, HANDLER);
        }
        List<Conjunction> conjunctions = definition.getChildren(Conjunction.class);
        for (Conjunction conjunction : conjunctions) {
            writeNode(root, conjunction, null);
        }
        List<TaskState> states = definition.getChildren(TaskState.class);
        for (TaskState state : states) {
            Element stateElement = writeTaskState(root, state);
            if (state instanceof MultiTaskState) {
                MultiTaskState multiTaskNode = (MultiTaskState) state;
                stateElement.addAttribute(PropertyNames.PROPERTY_MULTI_TASK_CREATION_MODE, multiTaskNode.getCreationMode().name());
                stateElement.addAttribute(TASK_EXECUTION_MODE, multiTaskNode.getSynchronizationMode().name());
                stateElement.addAttribute(TASK_EXECUTORS_USAGE, multiTaskNode.getDiscriminatorUsage());
                stateElement.addAttribute(TASK_EXECUTORS_VALUE, multiTaskNode.getDiscriminatorValue());
                for (VariableMapping variable : multiTaskNode.getVariableMappings()) {
                    writeVariableAttrs(stateElement, variable);
                }
            }
            if (state.isAsync()) {
                stateElement.addAttribute(ASYNC, Boolean.TRUE.toString());
                stateElement.addAttribute(ASYNC_COMPLETION_MODE, state.getAsyncCompletionMode().name());
            }
            writeTimer(stateElement, state.getTimer());
            writeBoundaryEvents(stateElement, state.getCatchEventNodes());
            if (state.isUseEscalation()) {
                String timerName = TIMER_ESCALATION;
                Duration escalationDuration = state.getEscalationDelay();
                Element timerElement = stateElement.addElement(TIMER);
                setAttribute(timerElement, NAME, timerName);
                if (escalationDuration != null && escalationDuration.hasDuration()) {
                    setAttribute(timerElement, DUEDATE, escalationDuration.getDuration());
                }
                TimerAction escalationAction = state.getEscalationAction();
                if (escalationAction != null) {
                    if (escalationAction.getRepeatDelay().hasDuration()) {
                        setAttribute(timerElement, REPEAT, escalationAction.getRepeatDelay().getDuration());
                    }
                    writeDelegation(timerElement, ACTION, escalationAction);
                }
            }
            writeTransitions(stateElement, state);
        }
        List<Timer> timers = definition.getChildren(Timer.class);
        for (Timer timer : timers) {
            Element stateElement = writeWaitState(root, timer);
            writeTransitions(stateElement, timer);
        }
        List<Fork> forks = definition.getChildren(Fork.class);
        for (ru.runa.gpd.lang.model.Node node : forks) {
            writeNode(root, node, null);
        }
        List<Join> joins = definition.getChildren(Join.class);
        for (ru.runa.gpd.lang.model.Node node : joins) {
            writeNode(root, node, null);
        }
        List<Subprocess> subprocesses = definition.getChildren(Subprocess.class);
        for (Subprocess subprocess : subprocesses) {
            Element processStateElement = writeNode(root, subprocess, null);
            Element subProcessElement = processStateElement.addElement(SUB_PROCESS);
            setAttribute(subProcessElement, NAME, subprocess.getSubProcessName());
            if (subprocess.isEmbedded()) {
                setAttribute(subProcessElement, EMBEDDED, Boolean.TRUE.toString());
            } else {
                for (VariableMapping variable : subprocess.getVariableMappings()) {
                    writeVariableAttrs(processStateElement, variable);
                }
            }
        }
        List<ThrowEventNode> sendMessageNodes = definition.getChildren(ThrowEventNode.class);
        for (ThrowEventNode messageNode : sendMessageNodes) {
            Element messageElement = writeNode(root, messageNode, null);
            messageElement.addAttribute(DUEDATE, messageNode.getTtlDuration().getDuration());
            messageElement.addAttribute(TYPE, messageNode.getEventNodeType().name());
            for (VariableMapping variable : messageNode.getVariableMappings()) {
                writeVariableAttrs(messageElement, variable);
            }
        }
        List<CatchEventNode> receiveMessageNodes = definition.getChildren(CatchEventNode.class);
        for (CatchEventNode messageNode : receiveMessageNodes) {
            Element messageElement = writeNode(root, messageNode, null);
            messageElement.addAttribute(TYPE, messageNode.getEventNodeType().name());
            for (VariableMapping variable : messageNode.getVariableMappings()) {                
                writeVariableAttrs(messageElement, variable);
            }
            writeTimer(messageElement, messageNode.getTimer());
        }
        List<EndTokenState> endTokenStates = definition.getChildren(EndTokenState.class);
        for (EndTokenState state : endTokenStates) {
            Element element = writeElement(root, state);
            if (definition instanceof SubprocessDefinition) {
                element.addAttribute(BEHAVIOR, state.getSubprocessDefinitionBehavior().name());
            }
        }
        List<EndState> endStates = definition.getChildren(EndState.class);
        for (EndState state : endStates) {
            writeElement(root, state);
        }
    }

	private void writeVariableAttrs(Element processStateElement, VariableMapping variable) {
		Element variableElement = processStateElement.addElement(VARIABLE);
		setAttribute(variableElement, NAME, variable.getName());
		setAttribute(variableElement, MAPPED_NAME, variable.getMappedName());
		setAttribute(variableElement, ACCESS, variable.getUsage());
	}

    private Element writeNode(Element parent, Node node, String delegationNodeName) {
        Element nodeElement = writeElement(parent, node);
        if (delegationNodeName != null) {
            writeDelegation(nodeElement, delegationNodeName, (Delegable) node);
        }
        writeTransitions(nodeElement, node);
        return nodeElement;
    }

    private Element writeTaskState(Element parent, SwimlanedNode swimlanedNode) {
        Element nodeElement = writeElement(parent, swimlanedNode);
        Element taskElement = nodeElement.addElement(TASK);
        setAttribute(taskElement, NAME, swimlanedNode.getName());
        setAttribute(taskElement, SWIMLANE, swimlanedNode.getSwimlaneName());
        if (swimlanedNode instanceof TaskState) {
            TaskState taskState = (TaskState) swimlanedNode;
            if (taskState.isReassignSwimlaneToInitializerValue()) {
                setAttribute(taskElement, REASSIGN, "true");
            }
            if (!taskState.isReassignSwimlaneToTaskPerformer()) {
                setAttribute(taskElement, REASSIGN_SWIMLANE_TO_TASK_PERFORMER, "false");
            }
            if (taskState.isIgnoreSubstitutionRules()) {
                setAttribute(taskElement, IGNORE_SUBSTITUTION_RULES, "true");
            }
            setAttribute(taskElement, DUEDATE, taskState.getTimeOutDueDate());
        }
        for (Action action : swimlanedNode.getActions()) {
            ActionImpl actionImpl = (ActionImpl) action;
            writeEvent(taskElement, new ActionEventType(actionImpl.getEventType()), actionImpl);
        }
        return nodeElement;
    }

    private Element writeWaitState(Element parent, Timer timer) {
        Element nodeElement = writeElement(parent, timer, WAIT_STATE);
        writeTimer(nodeElement, timer);
        return nodeElement;
    }

    private void writeTimer(Element parent, Timer timer) {
        if (timer == null) {
            return;
        }
        Element timerElement = parent.addElement(TIMER);
        setAttribute(timerElement, ID, timer.getId());
        setAttribute(timerElement, DUEDATE, timer.getDelay().getDuration());
        if (timer.getAction() != null) {
            if (timer.getAction().getRepeatDelay().hasDuration()) {
                setAttribute(timerElement, REPEAT, timer.getAction().getRepeatDelay().getDuration());
            }
            writeDelegation(timerElement, ACTION, timer.getAction());
        }
        setAttribute(timerElement, TRANSITION, PluginConstants.TIMER_TRANSITION_NAME);
    }
    
	private void writeBoundaryEvents(Element stateElement, CatchEventNode catchEventNodes) {
		if (catchEventNodes == null) {
			return;
		}
		Element catchEventElement = stateElement.addElement(RECEIVE_MESSAGE);
        setAttribute(catchEventElement, ID, catchEventNodes.getId());
	}

    private Element writeElement(Element parent, GraphElement element) {
        return writeElement(parent, element, element.getTypeDefinition().getJpdlElementName());
    }

    private Element writeElement(Element parent, GraphElement element, String typeName) {
        Element result = parent.addElement(typeName);
        setAttribute(result, ID, element.getId());
        if (element instanceof NamedGraphElement) {
            setAttribute(result, NAME, ((NamedGraphElement) element).getName());
        }
        if (element instanceof Node) {
            Node node = (Node) element;
            if (node.getAsyncExecution() != NodeAsyncExecution.DEFAULT) {
                setAttribute(result, NODE_ASYNC_EXECUTION, node.getAsyncExecution().getValue());
            }
        }
        if (element instanceof Describable) {
            String description = ((Describable) element).getDescription();
            if (description != null && description.length() > 0) {
                Element desc = result.addElement(DESCRIPTION);
                setNodeValue(desc, description);
            }
        }
        return result;
    }

    private void writeTransitions(Element parent, Node node) {
        List<Transition> transitions = node.getLeavingTransitions();
        for (Transition transition : transitions) {
            Element transitionElement = writeElement(parent, transition);
            transitionElement.addAttribute(TO, transition.getTarget().getId());
            for (Action action : transition.getActions()) {
                writeDelegation(transitionElement, ACTION, action);
            }
        }
    }

    private void writeEvent(Element parent, ActionEventType actionEventType, ActionImpl action) {
        Element eventElement = writeElement(parent, actionEventType, EVENT);
        setAttribute(eventElement, TYPE, actionEventType.getType());
        writeDelegation(eventElement, ACTION, action);
    }

    private void writeDelegation(Element parent, String elementName, Delegable delegable) {
        Element delegationElement = parent.addElement(elementName);
        if (delegable instanceof Action) {
            setAttribute(delegationElement, ID, ((Action) delegable).getId());
        }
        setAttribute(delegationElement, CLASS, delegable.getDelegationClassName());
        if (delegable instanceof Describable) {
            Describable describable = (Describable) delegable;
            if (!Strings.isNullOrEmpty(describable.getDescription())) {
                setAttribute(delegationElement, DESCRIPTION, describable.getDescription());
            }
        }
        setNodeValue(delegationElement, delegable.getDelegationConfiguration());
    }

    private void setDelegableClassName(Delegable delegable, String className) {
        className = BackCompatibilityClassNames.getClassName(className);
        delegable.setDelegationClassName(className);
    }

    @Override
    public void validateProcessDefinitionXML(IFile file) {
        // TODO cannot find declaration of process-definition element
        // try {
        // XmlUtil.parseWithXSDValidation(file.getContents(), "jpdl-4.0.xsd");
        // } catch (Exception e) {
        // throw new RuntimeException(e);
        // }
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent) {
        return create(node, parent, node.getName());
    }

    private <T extends GraphElement> T create(Element node, GraphElement parent, String typeName) {
        GraphElement element = NodeRegistry.getNodeTypeDefinition(Language.JPDL, typeName).createElement(parent, false);
        init(element, node);
        if (parent != null) {
            parent.addChild(element);
        }
        return (T) element;
    }

    private void init(GraphElement element, Element node) {
        String nodeId = node.attributeValue(ID);
        String name = node.attributeValue(NAME);
        if (element instanceof Node && nodeId == null) {
            nodeId = name;
        }
        element.setId(nodeId);
        if (element instanceof NamedGraphElement) {
            ((NamedGraphElement) element).setName(name);
        }
        String nodeAsyncExecutionValue = node.attributeValue(NODE_ASYNC_EXECUTION);
        if (element instanceof Node && !Strings.isNullOrEmpty(nodeAsyncExecutionValue)) {
            ((Node) element).setAsyncExecution(NodeAsyncExecution.getByValueNotNull(nodeAsyncExecutionValue));
        }
        List<Element> nodeList = node.elements();
        for (Element childNode : nodeList) {
            if (DESCRIPTION.equals(childNode.getName())) {
                ((Describable) element).setDescription(childNode.getTextTrim());
            }
            if (HANDLER.equals(childNode.getName()) || ASSIGNMENT.equals(childNode.getName())) {
                setDelegableClassName((Delegable) element, childNode.attributeValue(CLASS));
                element.setDelegationConfiguration(childNode.getText());
            }
            if (ACTION.equals(childNode.getName())) {
                // only transition actions loaded here
                String eventType;
                if (element instanceof Transition) {
                    eventType = ActionEventType.TRANSITION;
                } else if (element instanceof ActionNode) {
                    eventType = ActionEventType.NODE_ACTION;
                } else {
                    throw new RuntimeException("Unexpected action in XML, context of " + element);
                }
                parseAction(childNode, element, eventType);
            }
            if (TRANSITION.equals(childNode.getName())) {
                parseTransition(childNode, element);
            }
        }
    }

    private void parseTransition(Element node, GraphElement parent) {
        Transition transition = create(node, parent);
        String targetName = node.attributeValue(TO);
        TRANSITION_TARGETS.put(transition, targetName);
    }

    private void parseAction(Element node, GraphElement parent, String eventType) {
        ActionImpl action = NodeRegistry.getNodeTypeDefinition(ActionImpl.class).createElement(parent, false);
        action.setId(node.attributeValue(ID));
        setDelegableClassName(action, node.attributeValue(CLASS));
        action.setDelegationConfiguration(node.getText());
        action.setId(parent.getId() + "." + parent.getActions().size());
        parent.addAction(action, -1);
        action.setEventType(eventType);
        action.setDescription(node.attributeValue(DESCRIPTION));
    }

    private static Map<Transition, String> TRANSITION_TARGETS = new HashMap<Transition, String>();

    @Override
    public void parseXML(Document document, ProcessDefinition definition) {
        TRANSITION_TARGETS.clear();
        Element root = document.getRootElement();
        init(definition, root);
        String defaultTaskTimeoutDuration = root.attributeValue(DEFAULT_TASK_DUEDATE);
        if (!Strings.isNullOrEmpty(defaultTaskTimeoutDuration)) {
            definition.setDefaultTaskTimeoutDelay(new Duration(defaultTaskTimeoutDuration));
        }
        String accessTypeString = root.attributeValue(ACCESS_TYPE);
        if (!Strings.isNullOrEmpty(accessTypeString)) {
            definition.setAccessType(ProcessDefinitionAccessType.valueOf(accessTypeString));
        }
        String nodeAsyncExecutionValue = root.attributeValue(NODE_ASYNC_EXECUTION);
        if (!Strings.isNullOrEmpty(nodeAsyncExecutionValue)) {
            definition.setDefaultNodeAsyncExecution(NodeAsyncExecution.getByValueNotNull(nodeAsyncExecutionValue));
        }
        List<Element> swimlanes = root.elements(SWIMLANE);
        for (Element node : swimlanes) {
            Swimlane swimlane = create(node, definition);
            if (!Strings.isNullOrEmpty(swimlane.getDelegationConfiguration())) {
                String[] orgFunctionParts = swimlane.getDelegationConfiguration().split("\\(");
                if (orgFunctionParts.length == 2) {
                    String className = BackCompatibilityClassNames.getClassName(orgFunctionParts[0].trim());
                    swimlane.setDelegationConfiguration(className + "(" + orgFunctionParts[1]);
                }
            }
        }
        List<Element> startStates = root.elements(START_STATE);
        if (startStates.size() > 0) {
            if (startStates.size() > 1) {
                Dialogs.error(Localization.getString("model.validation.multipleStartStatesNotAllowed"));
            }
            Element node = startStates.get(0);
            StartState startState = create(node, definition);
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TASK.equals(stateNodeChild.getName())) {
                    String swimlaneName = stateNodeChild.attributeValue(SWIMLANE);
                    Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                    startState.setSwimlane(swimlane);
                }
            }
        }
        // this is for back compatibility
        List<Element> actionNodeNodes = root.elements(ACTION_NODE);
        for (Element node : actionNodeNodes) {
            ActionNode actionNode = create(node, definition);
            List<Element> aaa = node.elements();
            for (Element a : aaa) {
                if (EVENT.equals(a.getName())) {
                    String eventType = a.attributeValue("type");
                    List<Element> actionNodes = a.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION.equals(aa.getName())) {
                            parseAction(aa, actionNode, eventType);
                        }
                    }
                }
            }
        }
        List<Element> states = new ArrayList<Element>(root.elements(TASK_NODE));
        states.addAll(root.elements(MULTI_TASK_NODE));
        for (Element node : states) {
            List<Element> nodeList = node.elements();
            int transitionsCount = 0;
            boolean hasTimeOutTransition = false;
            for (Element childNode : nodeList) {
                if (TRANSITION.equals(childNode.getName())) {
                    String transitionName = childNode.attributeValue(NAME);
                    if (PluginConstants.TIMER_TRANSITION_NAME.equals(transitionName)) {
                        hasTimeOutTransition = true;
                    }
                    transitionsCount++;
                }
            }
            // backCompatibility: waitState was persisted as taskState earlier
            Node state;
            if (transitionsCount == 1 && hasTimeOutTransition) {
                state = create(node, definition, WAIT_STATE);
            } else {
                state = create(node, definition);
            }
            if (state instanceof Synchronizable) {
                ((Synchronizable) state).setAsync(Boolean.parseBoolean(node.attributeValue(ASYNC, "false")));
                String asyncCompletionMode = node.attributeValue(ASYNC_COMPLETION_MODE);
                if (asyncCompletionMode != null) {
                    ((Synchronizable) state).setAsyncCompletionMode(AsyncCompletionMode.valueOf(asyncCompletionMode));
                }
            }
            if (state instanceof MultiTaskState) {
                MultiTaskState multiTaskState = (MultiTaskState) state;
                multiTaskState.setCreationMode(MultiTaskCreationMode.valueOf(node.attributeValue(PropertyNames.PROPERTY_MULTI_TASK_CREATION_MODE,
                        MultiTaskCreationMode.BY_EXECUTORS.name())));
                multiTaskState.setSynchronizationMode(MultiTaskSynchronizationMode.valueOf(node.attributeValue(TASK_EXECUTION_MODE)));
                multiTaskState.setDiscriminatorUsage(node.attributeValue(TASK_EXECUTORS_USAGE, MultiTaskState.USAGE_DEFAULT));
                multiTaskState.setDiscriminatorValue(node.attributeValue(TASK_EXECUTORS_VALUE));
                List<VariableMapping> mappings = Lists.newArrayList();
                List<Element> vars = node.elements();
                for (Element childNode : vars) {
                    if (VARIABLE.equals(childNode.getName())) {
                        parseVariableAttrs(childNode, mappings);
                    }
                }
                multiTaskState.setVariableMappings(mappings);
            }
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TASK.equals(stateNodeChild.getName())) {
                    String swimlaneName = stateNodeChild.attributeValue(SWIMLANE);
                    if (swimlaneName != null && state instanceof SwimlanedNode) {
                        Swimlane swimlane = definition.getSwimlaneByName(swimlaneName);
                        ((SwimlanedNode) state).setSwimlane(swimlane);
                        String reassign = stateNodeChild.attributeValue(REASSIGN);
                        if (reassign != null) {
                            boolean forceReassign = Boolean.parseBoolean(reassign);
                            ((TaskState) state).setReassignSwimlaneToInitializerValue(forceReassign);
                        }
                        String reassignSwimlaneToTaskPerformer = stateNodeChild.attributeValue(REASSIGN_SWIMLANE_TO_TASK_PERFORMER);
                        if (reassignSwimlaneToTaskPerformer != null) {
                            ((TaskState) state).setReassignSwimlaneToTaskPerformer(Boolean.parseBoolean(reassignSwimlaneToTaskPerformer));
                        }
                    }
                    String ignore = stateNodeChild.attributeValue(IGNORE_SUBSTITUTION_RULES);
                    if (ignore != null) {
                        ((TaskState) state).setIgnoreSubstitutionRules(Boolean.parseBoolean(ignore));
                    }
                    String duedateAttr = stateNodeChild.attributeValue(DUEDATE);
                    if (!Strings.isNullOrEmpty(duedateAttr)) {
                        ((TaskState) state).setTimeOutDelay(new Duration(duedateAttr));
                    }
                    List<Element> aaa = stateNodeChild.elements();
                    for (Element a : aaa) {
                        if (EVENT.equals(a.getName())) {
                            String eventType = a.attributeValue(TYPE);
                            List<Element> actionNodes = a.elements();
                            for (Element aa : actionNodes) {
                                if (ACTION.equals(aa.getName())) {
                                    parseAction(aa, state, eventType);
                                }
                            }
                        }
                    }
                }
                if (TIMER.equals(stateNodeChild.getName())) {
                    String nameTimer = stateNodeChild.attributeValue(NAME);
                    String dueDate = stateNodeChild.attributeValue(DUEDATE);
                    if (TIMER_ESCALATION.equals(nameTimer)) {
                        ((TaskState) state).setUseEscalation(true);
                        if (!Strings.isNullOrEmpty(dueDate)) {
                            ((TaskState) state).setEscalationDelay(new Duration(dueDate));
                        }
                    } else {
                        if (state instanceof TaskState) {
                            Timer timer = new Timer();
                            timer.setId(stateNodeChild.attributeValue(ID));
                            if (dueDate != null) {
                                timer.setDelay(new Duration(dueDate));
                            }
                            state.addChild(timer);
                        }
                        if (state instanceof Timer) {
                            if (dueDate != null) {
                                ((Timer) state).setDelay(new Duration(dueDate));
                            }
                        }
                    }
                    List<Element> actionNodes = stateNodeChild.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(definition);
                            setDelegableClassName(timerAction, aa.attributeValue(CLASS));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeatDuration(stateNodeChild.attributeValue(REPEAT));
                            if (TIMER_ESCALATION.equals(nameTimer)) {
                                ((TaskState) state).setEscalationAction(timerAction);
                            } else {
                                ((ITimed) state).getTimer().setAction(timerAction);
                            }
                        }
                    }
                }
            }
        }
        List<Element> waitStates = root.elements(WAIT_STATE);
        for (Element node : waitStates) {
            Timer timer = create(node, definition);
            List<Element> stateChilds = node.elements();
            for (Element stateNodeChild : stateChilds) {
                if (TIMER.equals(stateNodeChild.getName())) {
                    String dueDate = stateNodeChild.attributeValue(DUEDATE);
                    if (dueDate != null) {
                        timer.setDelay(new Duration(dueDate));
                    }
                    List<Element> actionNodes = stateNodeChild.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(definition);
                            setDelegableClassName(timerAction, aa.attributeValue(CLASS));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeatDuration(stateNodeChild.attributeValue(REPEAT));
                            timer.setAction(timerAction);
                        }
                    }
                }
            }
        }
        List<Element> forks = root.elements(FORK);
        for (Element node : forks) {
            create(node, definition);
        }
        List<Element> joins = root.elements(JOIN);
        for (Element node : joins) {
            create(node, definition);
        }
        List<Element> decisions = root.elements(DECISION);
        for (Element node : decisions) {
            create(node, definition);
        }
        List<Element> conjunctions = root.elements(CONJUNCTION);
        for (Element node : conjunctions) {
            create(node, definition);
        }
        List<Element> processStates = root.elements(PROCESS_STATE);
        for (Element node : processStates) {
            Subprocess subprocess = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (SUB_PROCESS.equals(childNode.getName())) {
                    subprocess.setSubProcessName(childNode.attributeValue(NAME));
                    subprocess.setEmbedded(Boolean.parseBoolean(childNode.attributeValue(EMBEDDED, "false")));
                }
                if (VARIABLE.equals(childNode.getName())) {
                    parseVariableAttrs(childNode, variablesList);
                }
            }
            subprocess.setVariableMappings(variablesList);
        }
        List<Element> multiSubprocessStates = root.elements(MULTIINSTANCE_STATE);
        for (Element node : multiSubprocessStates) {
            MultiSubprocess multiSubprocess = create(node, definition);
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (SUB_PROCESS.equals(childNode.getName())) {
                    multiSubprocess.setSubProcessName(childNode.attributeValue(NAME));
                }
                if (VARIABLE.equals(childNode.getName())) {
                    parseVariableAttrs(childNode, variablesList);
                }
            }
            multiSubprocess.setVariableMappings(variablesList);
            MultiinstanceParameters.convertBackCompatible(multiSubprocess);
        }
        List<Element> sendMessageNodes = root.elements(SEND_MESSAGE);
        for (Element node : sendMessageNodes) {
            ThrowEventNode messageNode = create(node, definition);
            messageNode.setTtlDuration(new Duration(node.attributeValue(DUEDATE, "1 days")));
            messageNode.setEventNodeType(EventNodeType.valueOf(node.attributeValue(TYPE, "message")));
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (VARIABLE.equals(childNode.getName())) {
                    parseVariableAttrs(childNode, variablesList);
                }
            }
            messageNode.setVariableMappings(variablesList);
        }
        List<Element> receiveMessageNodes = root.elements(RECEIVE_MESSAGE);
        for (Element node : receiveMessageNodes) {
            CatchEventNode messageNode = create(node, definition);
            messageNode.setEventNodeType(EventNodeType.valueOf(node.attributeValue(TYPE, "message")));
            List<VariableMapping> variablesList = new ArrayList<VariableMapping>();
            List<Element> nodeList = node.elements();
            for (Element childNode : nodeList) {
                if (VARIABLE.equals(childNode.getName())) {
                    parseVariableAttrs(childNode, variablesList);
                }
                if (TIMER.equals(childNode.getName())) {
                    Timer timer = create(childNode, messageNode, WAIT_STATE);
                    timer.setDelay(new Duration(childNode.attributeValue(DUEDATE)));
                    List<Element> actionNodes = childNode.elements();
                    for (Element aa : actionNodes) {
                        if (ACTION.equals(aa.getName())) {
                            TimerAction timerAction = new TimerAction(definition);
                            setDelegableClassName(timerAction, aa.attributeValue(CLASS));
                            timerAction.setDelegationConfiguration(aa.getTextTrim());
                            timerAction.setRepeatDuration(childNode.attributeValue(REPEAT));
                            timer.setAction(timerAction);
                        }
                    }
                }
            }
            messageNode.setVariableMappings(variablesList);
        }
        List<Element> endTokenStates = root.elements(END_TOKEN);
        for (Element node : endTokenStates) {
            EndTokenState endTokenState = create(node, definition);
            if (!Strings.isNullOrEmpty(node.attributeValue(BEHAVIOR))) {
                endTokenState.setSubprocessDefinitionBehavior(EndTokenSubprocessDefinitionBehavior.valueOf(node.attributeValue(BEHAVIOR)));
            }
        }
        List<Element> endStates = root.elements(END_STATE);
        for (Element node : endStates) {
            create(node, definition);
        }
        List<Transition> tmpTransitions = new ArrayList<Transition>(TRANSITION_TARGETS.keySet());
        for (Transition transition : tmpTransitions) {
            String targetNodeId = TRANSITION_TARGETS.remove(transition);
            try {
                Node target = definition.getGraphElementByIdNotNull(targetNodeId);
                transition.setTarget(target);
            } catch (Exception e) {
                PluginLogger.logError(e);
                throw new RuntimeException("Problem with " + transition.getId() + ": " + transition.getParent() + " -> " + targetNodeId);
            }
        }
    }

	private void parseVariableAttrs(Element element, List<VariableMapping> variablesList) {
		VariableMapping variable = new VariableMapping();
		variable.setName(element.attributeValue(NAME));
		variable.setMappedName(element.attributeValue(MAPPED_NAME));
		variable.setUsage(element.attributeValue(ACCESS));
		variablesList.add(variable);
	}
}
