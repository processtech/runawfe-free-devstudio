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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.SubprocessMap;
import ru.runa.gpd.extension.regulations.ui.RegulationsNotesView;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.MultiSubprocess;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.Conjunction;
import ru.runa.gpd.lang.model.bpmn.DataStore;
import ru.runa.gpd.lang.model.bpmn.ParallelGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.jpdl.Fork;
import ru.runa.gpd.lang.model.jpdl.Join;
import ru.runa.gpd.lang.model.jpdl.ReceiveMessageNode;
import ru.runa.gpd.lang.model.jpdl.SendMessageNode;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.Duration;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
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
        map.put("nodeModels", nodeModels);
        Map<String, ValidatorDefinition> validatorDefinitions = ValidatorDefinitionRegistry.getValidatorDefinitions();
        map.put("validatorDefinitions", validatorDefinitions);
        if (!RegulationsRegistry.hasExtendedRegulations()) {
            map.put("processName", processDefinition.getName());
            map.put("processDescription", processDefinition.getDescription());
            map.put("swimlanes", processDefinition.getSwimlanes());
            map.put("variables", processDefinition.getVariables(false, false));
            map.put("endToken", processDefinition.getChildrenRecursive(EndTokenState.class));
            map.put("end", processDefinition.getChildrenRecursive(EndState.class));
        }
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
                if (currentNode != null && currentNode.getClass() == Subprocess.class) {
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

    public static void autoFillRegulationProperties(ProcessDefinition processDefinition) throws Exception {
        LinkedList<Node> sequencedNodes = new LinkedList<>();
        List<Node> endTokenNodes = new LinkedList<>();
        List<Node> endNodes = new LinkedList<>();
        for (Node node : processDefinition.getChildrenRecursive(Node.class)) {
            if (node instanceof StartState) {
                sequencedNodes.addFirst(node);
                continue;
            }
            if (node instanceof EndTokenState) {
                endTokenNodes.add(node);
                continue;
            }
            if (node instanceof EndState) {
                endNodes.add(node);
                continue;
            }
            if (node instanceof Subprocess) {
                ProcessDefinition subProcessDefinition = null;
                Subprocess subprocess = (Subprocess) node;
                if (subprocess.isEmbedded()) {
                    subProcessDefinition = subprocess.getEmbeddedSubprocess();
                } else {
                    String subProcessFolderName = SubprocessMap.get(subprocess.getQualifiedId());
                    if (subProcessFolderName != null) {
                        IFile definitionFile = (IFile) ResourcesPlugin.getWorkspace().getRoot()
                                .findMember(subProcessFolderName + "/" + ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                        subProcessDefinition = NodeRegistry.parseProcessDefinition(definitionFile);
                    } else {
                        subProcessDefinition = ProcessCache.getFirstProcessDefinition(subprocess.getSubProcessName(), null);
                    }
                }
                autoFillRegulationProperties(subProcessDefinition);
            }
            sequencedNodes.addLast(node);
        }
        sequencedNodes.addAll(endTokenNodes);
        sequencedNodes.addAll(endNodes);
        // now sequencedNodes has order similar to legacy impl; just fill node regulation properties
        Node previousNode = null;
        for (Node nextNode : sequencedNodes) {
            nextNode.getRegulationsProperties().setEnabled(true);
            nextNode.getRegulationsProperties().setPreviousNode(previousNode);
            nextNode.getRegulationsProperties().setDescription(getRegulationPropertiesDescription(nextNode));
            if (previousNode != null) {
                previousNode.getRegulationsProperties().setNextNode(nextNode);
            }
            previousNode = nextNode;
        }
    }

    private static String getRegulationPropertiesDescription(Node node) {
        ProcessDefinition processDefinition = node.getProcessDefinition();
        StringBuilder sb = new StringBuilder();

        // node header
        sb.append(getNodeName(node));

        // embedded subprocess
        if (processDefinition instanceof SubprocessDefinition) {
            sb.append("<div class=\"embedded-subprocess\">Действие в рамках композиции <span class=\"name\">" + processDefinition.getName()
                    + "</span></div>");
        }

        // node type info
        sb.append(getNodeTypeInfo(node));

        // swimlane info
        if (node instanceof SwimlanedNode) {
            if (((SwimlanedNode) node).getSwimlane() != null) {
                sb.append("<div class=\"swimlane\">Роль: <span class=\"name\">" + ((SwimlanedNode) node).getSwimlane().getName() + "</span></div>");
            } 
        }

        // leaving transitions info
        sb.append(getTransitionsInfo(node));

        // timer option info
        if (node instanceof ITimed) {
            sb.append(getTimerOptionInfo(node));
        }

        // message live time
        if (node.getClass() == SendMessageNode.class && ((MessageNode) node).getTtlDuration().hasDuration()) {
            sb.append("<div class=\"ttl\">Время жизни сообщения: " + ((MessageNode) node).getTtlDuration().toString() + "</div>");
        }

        // subprocess name
        if ((node.getClass() == Subprocess.class || node.getClass() == MultiSubprocess.class)
                && !((Subprocess) node).isEmbedded()) {
            sb.append("<div class=\"subprocess\">Имя подпроцесса: <span class=\"name\">" + ((Subprocess) node).getSubProcessName());
        }

        // validation info
        if (node instanceof FormNode && ((FormNode) node).hasFormValidation()) {
            FormNodeValidation formNodeValidation = ((FormNode) node).getValidation(node.getProcessDefinition().getFile());
            Map<String, Map<String, ValidatorConfig>> formNodeValidationFieldConfigs = formNodeValidation.getFieldConfigs();
            sb.append("<div class=\"variables\">\n" + "   <table class=\"data\">\n" + "       <tr>\n" + "           <th>Переменная</th>\n"
                    + "           <th>Проверка ввода</th>\n" + "       </tr>");
            for (Map.Entry<String, Map<String, ValidatorConfig>> entry : formNodeValidationFieldConfigs.entrySet()) {
                sb.append("<tr>\n" + "   <td class=\"variableName\">" + entry.getKey() + "</td>\n" + "   <td>\n" + "       <ul>");
                for (Map.Entry<String, ValidatorConfig> validatiorConfigEntry : entry.getValue().entrySet()) {
                    sb.append("<li>");
                    ValidatorConfig fieldValidatorConfig = validatiorConfigEntry.getValue();
                    ValidatorDefinition validatorDefinition = ValidatorDefinitionRegistry.getValidatorDefinitions()
                            .get(fieldValidatorConfig.getType());

                    if (!fieldValidatorConfig.getMessage().isEmpty()) {
                        sb.append(fieldValidatorConfig.getMessage());
                    } else {
                        sb.append(validatorDefinition.getDescription());
                    }

                    if (!fieldValidatorConfig.getTransitionNames().isEmpty()) {
                        sb.append("(только в случае ");
                        sb.append(String.join(",", fieldValidatorConfig.getTransitionNames()) + ")");
                    }

                    if (!fieldValidatorConfig.getParams().isEmpty()) {
                        sb.append("<ul>");
                        for (Map.Entry<String, String> paramsEntry : fieldValidatorConfig.getParams().entrySet()) {
                            sb.append("<li>");
                            sb.append(validatorDefinition.getParams().get(paramsEntry.getKey()).getLabel());
                            sb.append(Localization.getString(fieldValidatorConfig.getParams().get(paramsEntry.getKey())));
                            sb.append("</li>");
                        }
                        sb.append("</ul>");
                    }
                    sb.append("</li>");
                }
                sb.append("     </ul>\n" + "   </td>\n" + "</tr>");
            }

            if (!formNodeValidation.getGlobalConfigs().isEmpty()) {
                sb.append("<tr>\n" + "   <td><span class=\"name\">Комплексные проверки данных</span></td>\n" + "   <td>\n" + "       <ul>");
                for (ValidatorConfig validatorConfig : formNodeValidation.getGlobalConfigs()) {
                    sb.append("<li>");
                    sb.append(validatorConfig.getMessage().isEmpty() ? "Без сообщения" : validatorConfig.getMessage());
                    sb.append("</li>");
                }
                sb.append("     </ul>  \n" + "   </td>\n" + "</tr>");
            }

            sb.append(" </table>\n" + "</div>");

        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    private static String getNodeName(Node node) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div id=" + node.getId() + " class=\"header\">\n");
        Class nodeClass = node.getClass();
        if (nodeClass == StartState.class) {
            sb.append("<span class=\"step\">Начало выполнения бизнес-процесса:</span>\n");
        } else if (nodeClass == ParallelGateway.class || nodeClass == Fork.class) {
            sb.append("<span class=\"step\">Шаг: Параллельный шлюз </span>\n");
        } else if (nodeClass == Join.class) {
            sb.append("<span class=\"step\">Шаг: Соединение </span>\n");
        } else if (nodeClass == EndTokenState.class) {
            sb.append("<span class=\"step\">Завершение потока  выполнения бизнес-процесса:</span>\n");
        } else if (nodeClass == EndState.class) {
            sb.append("<span class=\"step\">Завершение процесса  выполнения бизнес-процесса:</span>\n");
        } else {
            sb.append("<span class=\"step\">Шаг:</span>\n");
        }
        sb.append("<span class=\"name\">" + node.getName() + "</span>\n" + "</div>");

        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    private static String getNodeTypeInfo(Node node) {
        StringBuilder sb = new StringBuilder();
        Class nodeClass = node.getClass();
        if (nodeClass != StartState.class && nodeClass != EndTokenState.class && nodeClass != EndState.class) {
            sb.append("<div class=\"type\">Тип шага: <span class=\"name\">");
            if (nodeClass == ParallelGateway.class || nodeClass == Fork.class) {
                sb.append(node.getLeavingTransitions().size() > node.getArrivingTransitions().size() ? "Разделение" : "Слияние");
            } else if (nodeClass == Join.class) {
                sb.append("Соединение");
            } else if (nodeClass == ReceiveMessageNode.class) {
                sb.append("Прием сообщения");
            } else if (nodeClass == SendMessageNode.class) {
                sb.append("Отправка сообщения");
            } else if (nodeClass == Subprocess.class) {
                sb.append("Запуск подпроцесса");
            } else if (nodeClass == MultiSubprocess.class) {
                sb.append("Запуск мультиподпроцесса");
            } else if (nodeClass == MultiTaskState.class) {
                sb.append("Запуск мультидействия");
            } else if (nodeClass == ScriptTask.class) {
                sb.append("Выполнение сценария");
            } else if (nodeClass == DataStore.class) {
                sb.append("Взаимодействие с источником данных");
            } else {
                sb.append(node.getTypeDefinition().getLabel());
            }
            sb.append("</span></div>");
        }
        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
    private static String getTransitionsInfo(Node node) {
        StringBuilder sb = new StringBuilder();
        Class nodeClass = node.getClass();
        if (nodeClass != EndTokenState.class && nodeClass != EndState.class && nodeClass != DataStore.class) {
            List<Transition> leavingTransitions = node.getLeavingTransitions();
            Node targetNode = leavingTransitions.get(0).getTarget();
            sb.append("<div class=\"transition\">");
            if (nodeClass == Join.class) {
                sb.append("Далее cоединяются " + node.getArrivingTransitions().size()
                        + " точек управления, и управление переходит к шагу<span class=\"name\"><a href=\"#" + targetNode.getId() + "\">"
                        + leavingTransitions.get(0).getTarget().getName() + "</a></span>");
            } else if (nodeClass == Timer.class) {
                Duration timerDelay = (Duration) node.getPropertyValue("timerDelay");
                sb.append(timerDelay.hasDuration() ? "После истечения " + timerDelay.toString() + " управление переходит к шагу "
                        : timerDelay.toString() + " времени управление переходит к шагу ");
                sb.append(getNextNodeInfo(targetNode));
                
            } else if (nodeClass == ReceiveMessageNode.class) {
                sb.append("После приема сообщения управление переходит к шагу " + getNextNodeInfo(targetNode));
            } else if (nodeClass == SendMessageNode.class) {
                sb.append("После отправки сообщения управление переходит к шагу " + getNextNodeInfo(targetNode));
            } else if (nodeClass == Subprocess.class || nodeClass == MultiSubprocess.class) {
            } else {
                if (leavingTransitions.size() == 1) {
                    sb.append("Далее управление переходит к шагу " + getNextNodeInfo(targetNode));
                } else if (leavingTransitions.size() > 1) {
                    sb.append("Далее управление переходит к шагу ");
                    for (Transition leavingTransition : leavingTransitions) {
                        sb.append("<div class=\"transition\">в случае <span class=\"name\">" + leavingTransition.getName() + " </span>"
                                + getNextNodeInfo(leavingTransition.getTarget()) + "</div>");
                    }
                }
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    private static String getNextNodeInfo(Node node) {
        return "<span class=\"name\"><a href=\"#" + node.getId() + "\">" + node.getName() + "</a></span>";
    }

    @SuppressWarnings("rawtypes")
    private static String getTimerOptionInfo(Node node) {
        StringBuilder sb = new StringBuilder();
        Class nodeClass = node.getClass();
        Timer timer = ((ITimed) node).getTimer();
        if (timer != null) {
            String timerDelay = ((Duration) node.getPropertyValue("timerDelay")).toString();
            if (nodeClass == TaskState.class || nodeClass == Decision.class || nodeClass == Conjunction.class) {
                sb.append(timer.getDelay().hasDuration() ? "После истечения " + timerDelay + " управление переходит к шагу "
                        : timerDelay + " времени управление переходит к шагу ");
                sb.append(getNextNodeInfo(timer.getLeavingTransitions().get(0).getTarget()));
            } else if (nodeClass == ReceiveMessageNode.class) {
                sb.append("В случае задержки задания на " + timerDelay + " управление переходит к шагу "
                        + getNextNodeInfo(timer.getLeavingTransitions().get(0).getTarget()));
            }
        }
        return sb.toString();
    }

    public static boolean validate(ProcessDefinition processDefinition, boolean validationFromAutoFill) {
        List<ValidationError> errors = Lists.newArrayList();
        IFile definitionFile = processDefinition.getFile();
        for (Node node : processDefinition.getNodes()) {
            if (!node.getRegulationsProperties().isValid() && !validationFromAutoFill) {
                errors.add(ValidationError.createLocalizedWarning(node, "regulations.invalidProperties", node));
            }
            if (node.getRegulationsProperties().isEnabled()) {
                Node nextNode = node.getRegulationsProperties().getNextNode();
                if (nextNode != null && !nextNode.getRegulationsProperties().isEnabled()) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nextNodeIsDisabled", node, nextNode));
                }
                if (nextNode != null && !Objects.equal(nextNode.getRegulationsProperties().getPreviousNode(), node)) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nextPreviousNodeMismatch", nextNode, node));
                }
            }
            if (node instanceof DataStore) {
                if (((DataStore) node).getLeavingDottedTransitions().size() == 0 && ((DataStore) node).getArrivingDottedTransitions().size() == 0) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nodeHasNotLeavingOrArrivingTransitions", node));
                }
            } else if (!(node instanceof StartState) && !(node instanceof EndState) && !(node instanceof EndTokenState)) {
                if (node.getLeavingTransitions().size() == 0 || node.getArrivingTransitions().size() == 0) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nodeHasNotLeavingOrArrivingTransitions", node));
                }
            } else {
                if (node.getLeavingTransitions().size() == 0 && node.getArrivingTransitions().size() == 0) {
                    errors.add(ValidationError.createLocalizedWarning(node, "regulations.nodeHasNotLeavingOrArrivingTransitions", node));
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
                    result &= validate(subprocessDefinition, validationFromAutoFill);
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
