package ru.runa.gpd.extension.regulations;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.ui.RegulationsNotesView;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;

public class RegulationsUtil {
    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);

    public static String getNodeLabel(Node node) {
        if (Strings.isNullOrEmpty(node.getName())) {
            return node.getTypeDefinition().getLabel() + " [" + node.getId() + "]";
        }
        return node.getName() + " [" + node.getId() + "]";
    }

    public static String generate(ProcessDefinition processDefinition) throws Exception {
        Template template = new Template("regulations", RegulationsRegistry.getTemplate(), configuration);
        List<Node> listOfNodes = getSequencedNodes(processDefinition);
        List<NodeModel> nodeModels = Lists.newArrayList();
        for (Node node : listOfNodes) {
            nodeModels.add(new NodeModel(node));
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("processName", processDefinition.getName());
        map.put("processDescription", processDefinition.getDescription());
        map.put("nodeModels", nodeModels);
        Map<String, ValidatorDefinition> validatorDefinitions = ValidatorDefinitionRegistry.getValidatorDefinitions();
        map.put("validatorDefinitions", validatorDefinitions);
        map.put("swimlanes", processDefinition.getSwimlanes());
        map.put("variables", processDefinition.getVariables(false, false, null));
        map.put("endToken", processDefinition.getChildrenRecursive(EndTokenState.class));
        map.put("end", processDefinition.getChildrenRecursive(EndState.class));
        IFile htmlDescriptionFile = IOUtils.getAdjacentFile(processDefinition.getFile(), ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
        if (htmlDescriptionFile.exists()) {
            map.put("processHtmlDescription", IOUtils.readStream(htmlDescriptionFile.getContents()));
        }
        Writer writer = new StringWriter();
        template.process(map, writer);
        return writer.toString();
    }
    
    public static List<Node> getSequencedNodes(ProcessDefinition processDefinition) {
        List<Node> result = Lists.newArrayList();
        Node currentNode = processDefinition.getFirstChild(StartState.class);
        if (currentNode != null) {
            boolean append = !(processDefinition instanceof SubprocessDefinition);
            do {
                if (append) {
                    result.add(currentNode);
                }
                currentNode = currentNode.getRegulationsProperties().getNextNode();
                append = true;
                if (currentNode != null && currentNode.getClass().equals(Subprocess.class)) {
                    result.add(currentNode);
                    append = false;
                    SubprocessDefinition subprocessDefinition = ((Subprocess) currentNode).getEmbeddedSubprocess();
                    if (subprocessDefinition != null) {
                        result.addAll(getSequencedNodes(subprocessDefinition));
                    }
                }
            } while (currentNode != null);
        }
        return result;
    }
    
    public static void fillRegulationPropertiesWithSequence(ProcessDefinition processDefinition) {
        List<Node> listOfNodes = processDefinition.getChildren(Node.class);
        List<Transition> usedTransitions = Lists.newArrayList();
        for (Node node : listOfNodes) {
            NodeRegulationsProperties regulationProperties = node.getRegulationsProperties();
            if (node instanceof StartState) {
                Node targetNode = node.getLeavingTransitions().get(0).getTarget();
                regulationProperties.setNextNode(targetNode);
                targetNode.getRegulationsProperties().setPreviousNode(node);
                continue;
            }
            if (node instanceof EndState || node instanceof EndTokenState) {
                continue;
            }
            regulationProperties.setEnabled(true);
            addSequenceToNode(node, regulationProperties, usedTransitions);
        }
    }
    
    /**
     * Sets nextNode parameter for current node and previousNode for next node. Method gets target node of current node first leaving transition. If
     * target node has one arriving transition method sets nextNode parameter. If target node has multiple arriving transitions method adds current
     * transition to list of used transitions and then gets all unused transitions. If it doesn't have any unused ones, or target node neither
     * parallel nor exclusive gateway as well as not end - sets target node as next, else - finds first node of parallel branch and sets it as next.
     * 
     * @param node
     *            current node
     * @param regulationProperties
     *            current node regulation properties
     * @param usedTransitions
     *            list of transitions that already been used. Helps define which branch of current node haven't been covered yet.
     */

    private static void addSequenceToNode(Node node, NodeRegulationsProperties regulationProperties, List<Transition> usedTransitions) {
        Transition currentLeavingTransition = node.getLeavingTransitions().get(0);
        if (node.getArrivingTransitions().stream().map(Transition::getSource)
                .anyMatch(transition -> Objects.equal(transition, node.getLeavingTransitions().get(0).getTarget()))) {
            currentLeavingTransition = node.getLeavingTransitions().get(1);
        }
        Node targetNode = currentLeavingTransition.getTarget();
        List<Transition> targetArrivingTransitions = targetNode.getArrivingTransitions();
        if (targetArrivingTransitions.size() == 1) {
            fillRegulationProperties(node, regulationProperties, targetNode, targetNode.getRegulationsProperties());
        } else {
            usedTransitions.add(currentLeavingTransition);
            if (targetArrivingTransitions.stream().noneMatch(transition -> !usedTransitions.contains(transition))
                    || !(targetNode instanceof ParallelGateway) && !(targetNode instanceof ExclusiveGateway) && !(targetNode instanceof EndTokenState)
                            && !(targetNode instanceof EndState)) {
                fillRegulationProperties(node, regulationProperties, targetNode, targetNode.getRegulationsProperties());
            } else {
                Node parallelBranchFirstNode = findPreviousParallelGateway(node, currentLeavingTransition, usedTransitions, targetNode);
                fillRegulationProperties(node, regulationProperties, parallelBranchFirstNode, parallelBranchFirstNode.getRegulationsProperties());
            }
        }
    }

    private static void fillRegulationProperties(Node node, NodeRegulationsProperties regulationProperties, Node targetNode,
            NodeRegulationsProperties targetNodeRegulationsProperties) {
        if (!targetNodeRegulationsProperties.isEnabled()) {
            targetNodeRegulationsProperties.setEnabled(true);
        }
        if (targetNodeRegulationsProperties.getPreviousNode() == null) {
            targetNodeRegulationsProperties.setPreviousNode(node);
        }
        if (regulationProperties.getNextNode() == null) {
            regulationProperties.setNextNode(targetNode);
        }
    }
    
    /**
     * Finds next node. Gets previous transition of a node and gets its source until finds node with multiple leaving transitions,
     * then adds current branch transition to usedTransitions and checks, whether found node has any other transitions. In case of having one,
     * returns target node of first remaining transition. In case of not having one returns firstBranchNextNode.
     * 
     * @param node
     *            current node
     * @param nextNodeTransition
     *            transition that leads to next Node
     * @param usedTransitions
     *            list of transitions that already been used. Helps define which branch of current node haven't been covered yet.
     * @param firstBranchNextNode
     *            stores node that has multiple arriving transitions. Used for returning this node in case of every branch covered.
     * @return first Node from other parallel branch.
     */
    private static Node findPreviousParallelGateway(Node node, Transition nextNodeTransition, List<Transition> usedTransitions,
            Node firstBranchNextNode) {
        if (node instanceof StartState) {
            return firstBranchNextNode;
        }
        List<Transition> transitions = node.getLeavingTransitions();
        // in case of returning to the very beginning of process
        if (transitions.size() == 1) {
            Transition firstArrivingTransition = node.getArrivingTransitions().get(0);
            return findPreviousParallelGateway(firstArrivingTransition.getSource(), firstArrivingTransition, usedTransitions, firstBranchNextNode);
        } else if (transitions.size() > 1) {
            usedTransitions.add(nextNodeTransition);
            return transitions.stream()
                    .filter(transition -> !usedTransitions.contains(transition))
                    .findFirst().get().getTarget();
        }
        return null;
    }

    public static boolean validate(ProcessDefinition processDefinition) {
        List<ValidationError> errors = Lists.newArrayList();
        IFile definitionFile = processDefinition.getFile();
        for (Node node : processDefinition.getNodes()) {
            if (node.getRegulationsProperties().isEnabled()) {
                Node nextNode = node.getRegulationsProperties().getNextNode();
                if (nextNode != null && !nextNode.getRegulationsProperties().isEnabled()) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nextNodeIsDisabled", node, nextNode));
                }
                if (nextNode != null && !Objects.equal(nextNode.getRegulationsProperties().getPreviousNode(), node)) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nextPreviousNodeMismatch", nextNode, node));
                }
            }
        }
        Node curNode = processDefinition.getFirstChild(StartState.class);
        Set<String> loopCheckIds = Sets.newHashSet();
        while (curNode != null) {
            if (loopCheckIds.contains(curNode.getId())) {
                errors.add(ValidationError.createLocalizedWarning(processDefinition, "regulations.loopDetected", curNode));
                break;
            }
            loopCheckIds.add(curNode.getId());
            curNode = curNode.getRegulationsProperties().getNextNode();
        }
        boolean result = true;
        for (Subprocess subprocess : processDefinition.getChildren(Subprocess.class)) {
            if (subprocess.isEmbedded()) {
                SubprocessDefinition subprocessDefinition = subprocess.getEmbeddedSubprocess();
                if (subprocessDefinition.isInvalid()) {
                    errors.add(ValidationError.createLocalizedWarning(subprocessDefinition, "regulations.subprocessContainsErrors",
                            subprocessDefinition.getName()));
                } else {
                    result &= validate(subprocessDefinition);
                }
            }
        }
        result &= errors.isEmpty();
        updateView(definitionFile, errors);
        return result;
    }

    private static void updateView(IFile definitionFile, List<ValidationError> errors) {
        try {
            definitionFile.deleteMarkers(RegulationsNotesView.ID, true, IResource.DEPTH_INFINITE);
            for (ValidationError validationError : errors) {
                IMarker marker = definitionFile.createMarker(RegulationsNotesView.ID);
                if (marker.exists()) {
                    marker.setAttribute(IMarker.MESSAGE, validationError.getMessage());
                    marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, validationError.getSource().getId());
                    marker.setAttribute(IMarker.LOCATION, validationError.getSource().toString());
                    marker.setAttribute(IMarker.SEVERITY, validationError.getSeverity());
                    marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, validationError.getSource().getProcessDefinition().getName());
                }
            }
            if (!errors.isEmpty()) {
                EditorUtils.showView(RegulationsNotesView.ID);
            }
        } catch (CoreException e) {
            PluginLogger.logErrorWithoutDialog(e.toString());
        }
    }

}
