package ru.runa.gpd.extension.regulations;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.ui.RegulationsNotesView;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class RegulationsUtil {
    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
    static {
        // configuration.setDefaultEncoding(Charsets.UTF_8.name());
    }

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
        Map<String, FormNodeValidation> mapOfFormNodeValidation = Maps.newHashMap();
        Map<String, List<ValidatorConfig>> globalValidatorDefinitionsMap = Maps.newHashMap();
        for (Node node : listOfNodes) {
            if (node instanceof FormNode) {
                FormNode formNode = (FormNode) node;
                if (formNode.hasFormValidation()) {
                    FormNodeValidation validation = formNode.getValidation(processDefinition.getFile());
                    mapOfFormNodeValidation.put(formNode.getId(), validation);
                    globalValidatorDefinitionsMap.put(formNode.getId(), validation.getGlobalConfigs());
                }
            }
            nodeModels.add(new NodeModel(node));
        }
        Map<String, Object> map = Maps.newHashMap();
        map.put("nodeModels", nodeModels);
        map.put("mapOfFormNodeValidation", mapOfFormNodeValidation);
        map.put("globalValidatorDefinitionsMap", globalValidatorDefinitionsMap);
        for (String nodeId : mapOfFormNodeValidation.keySet()) {
            Map<String, Map<String, ValidatorConfig>> nodeFieldConfigs = mapOfFormNodeValidation.get(nodeId).getFieldConfigs();
            for (Map<String, ValidatorConfig> nodeFieldConfigsValue : nodeFieldConfigs.values()) {
                for (ValidatorConfig validatorConfig : nodeFieldConfigsValue.values()) {
                    String typeNameBeforeLocalization = validatorConfig.getType();
                    if (Localization.isLocalizationExists("ValidatorConfig.type." + validatorConfig.getType())) {
                        validatorConfig.setType(Localization.getString("ValidatorConfig.type." + validatorConfig.getType()));
                    }
                    Map<String, String> localizedParameters = Maps.newHashMap();
                    for (Iterator<String> iterator = validatorConfig.getParams().keySet().iterator(); iterator.hasNext();) {
                        String currentParameterName = iterator.next();
                        String currentParameterValue = validatorConfig.getParams().get(currentParameterName);
                        if (Localization.isLocalizationExists("ValidatorConfig.type." + typeNameBeforeLocalization + "." + currentParameterName)) {
                            if (Localization.isLocalizationExists("ValidatorConfig.parameters.values." + currentParameterValue)) {
                                localizedParameters.put(
                                        Localization.getString("ValidatorConfig.type." + typeNameBeforeLocalization + "." + currentParameterName),
                                        Localization.getString("ValidatorConfig.parameters.values." + currentParameterValue));
                            } else {
                                if (currentParameterValue.isEmpty() != true) {
                                    localizedParameters
                                            .put(Localization.getString("ValidatorConfig.type." + typeNameBeforeLocalization + "."
                                                    + currentParameterName), currentParameterValue);
                                } else {
                                    localizedParameters
                                            .put(Localization.getString("ValidatorConfig.type." + typeNameBeforeLocalization + "."
                                                    + currentParameterName), Localization.getString("ValidatorConfig.parameters.values.empty"));
                                }
                            }
                        } else {
                            if (Localization.isLocalizationExists("ValidatorConfig.parameters.values." + currentParameterValue)) {
                                localizedParameters.put(currentParameterName,
                                        Localization.getString("ValidatorConfig.parameters.values." + currentParameterValue));
                            } else {
                                if (currentParameterValue.isEmpty() != true) {
                                    localizedParameters.put(currentParameterName, currentParameterValue);
                                } else {
                                    localizedParameters.put(currentParameterName, Localization.getString("ValidatorConfig.parameters.values.empty"));
                                }
                            }
                        }
                    }
                    validatorConfig.setParams(localizedParameters);
                }
            }
        }

        Map<String, ValidatorDefinition> validatorDefinitions = ValidatorDefinitionRegistry.getValidatorDefinitions();
        map.put("validatorDefinitions", validatorDefinitions);

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

    public static boolean validate(ProcessDefinition processDefinition) {
        List<ValidationError> errors = Lists.newArrayList();
        IFile definitionFile = processDefinition.getFile();
        for (Node node : processDefinition.getNodes()) {
            if (!node.getRegulationsProperties().isValid()) {
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
