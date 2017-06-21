package ru.runa.gpd.ui.action;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.view.RegulationsNotesView;
import ru.runa.gpd.ui.view.RegulationsSequenceView;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.RegulationsRegistry;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;
import ru.runa.gpd.validation.ValidatorDefinitionRegistry;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class CreateProcessRegulations extends BaseModelActionDelegate {
    private static List<Node> sequenceNodeList = Lists.newArrayList();

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition proccDefinition = getActiveDesignerEditor().getDefinition();
            getActiveDesignerEditor().getDefinitionFile().deleteMarkers(RegulationsNotesView.ID, true, IResource.DEPTH_INFINITE);
            List<ValidationError> regulationsValidationErrors = Lists.newArrayList();
            boolean resultOfValidation = ProcessDefinition.validateRegulations(proccDefinition, regulationsValidationErrors);
            if (resultOfValidation) {
                IViewReference[] viewParts = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                for (IViewReference iviewReference : viewParts) {
                    if (iviewReference.getId().equals(RegulationsNotesView.ID)) {
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(iviewReference);
                        break;
                    }
                }
                String html = generateRegulations(proccDefinition);
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), "regulations.html");
                IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
                IDE.openEditor(getWorkbenchPage(), file, "ru.runa.gpd.wysiwyg.RegulationsHTMLEditor");
            } else {
                for (ValidationError error : regulationsValidationErrors) {
                    addRegulationsNote(getActiveDesignerEditor().getDefinitionFile(), error.getSource().getProcessDefinition(), error);
                }
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RegulationsNotesView.ID);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        disableNotConnectedBotNodes();
        if (getSelection() != null && CommonPreferencePage.isRegulationsMenuItemsEnabled()
                && getSelection().getClass().equals(ProcessDefinition.class)) {
            action.setEnabled(!getActiveDesignerEditor().getDefinition().isInvalid());
        } else {
            action.setEnabled(false);
        }
    }

    @Override
    protected ProcessEditorBase getActiveDesignerEditor() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return (ProcessEditorBase) editor;
        }
        return null;
    }

    private static void addRegulationsNote(IFile definitionFile, ProcessDefinition definition, ValidationError validationError) {
        try {
            IMarker marker = definitionFile.createMarker(RegulationsNotesView.ID);
            if (marker.exists()) {
                marker.setAttribute(IMarker.MESSAGE, validationError.getMessage());
                String elementId = validationError.getSource().toString();
                if (validationError.getSource() instanceof Node) {
                    elementId = ((Node) validationError.getSource()).getId();
                }
                marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, elementId);
                marker.setAttribute(IMarker.LOCATION, validationError.getSource().toString());
                marker.setAttribute(IMarker.SEVERITY, validationError.getSeverity());
                marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, definition.getName());
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    public static void addRegulationsSequenceNote(IFile definitionFile, ProcessDefinition definition, long n, Node node) {
        try {
            IMarker marker = definitionFile.createMarker(RegulationsSequenceView.ID);
            if (marker.exists()) {
                marker.setAttribute(IMarker.MESSAGE, node.getName());
                String elementId = node.getId();

                marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, elementId);
                marker.setAttribute(IMarker.LOCATION, String.valueOf(n));
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
                marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, definition.getName());
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    private void disableNotConnectedBotNodes() {
        ProcessDefinition definition = getActiveDesignerDefinition();
        List<Node> listOfNodes = definition.getNodes();
        for (Node node : listOfNodes) {
            if (node.getNodeRegulationsProperties().isEnabled() && node.getNodeRegulationsProperties().getPreviousNode() == null
                    && node.getNodeRegulationsProperties().getNextNode() == null && node instanceof TaskState
                    && ((TaskState) node).getBotTaskLink() != null) {
                node.getNodeRegulationsProperties().setEnabled(false);
                definition.setDirty(true);
            }
        }
    }

    private String generateRegulations(ProcessDefinition definition) throws Exception {
        Configuration config = new Configuration();
        config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        config.setDefaultEncoding(Charsets.UTF_8.name());
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        Template template = new Template("regulations", new StringReader(RegulationsRegistry.getTemplate()), config);
        List<Node> listOfNodes = Lists.newArrayList();
        listOfNodes = makeSequenceList(definition);
        HashMap<String, FormNodeValidation> mapOfFormNodeValidation = Maps.newHashMap();
        for (Node node : listOfNodes) {
            if (node instanceof FormNode) {
                FormNode formNode = (FormNode) node;
                if (formNode.hasFormValidation()) {
                    mapOfFormNodeValidation.put(formNode.getId(),
                            formNode.getValidation(ProcessCache.getProcessDefinitionFile(definition).getParent()));
                }
            }
        }
        HashMap<String, Object> map = new HashMap<>();
        map.put("proc", definition);
        map.put("listOfNodes", listOfNodes);

        HashMap<String, String> subprocessDescriptions = new HashMap<>();
        for (Node node : listOfNodes) {
            if (node instanceof Subprocess) {
                Subprocess subprocessNode = (Subprocess) node;
                ProcessDefinition subprocessDefinition = ProcessCache.getFirstProcessDefinition(subprocessNode.getSubProcessName());
                if (subprocessDefinition != null) {
                    IFile descriptionFile = IOUtils.getAdjacentFile(getDefinitionFile(), subprocessDefinition.getId() + "."
                            + ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
                    if (descriptionFile.exists()) {
                        String descriptionFileText = IOUtils.readStream(descriptionFile.getContents());
                        subprocessDescriptions.put(subprocessNode.getSubProcessName(), descriptionFileText);
                    }
                }
            }
        }
        map.put("subprocessDescriptions", subprocessDescriptions);
        map.put("mapOfFormNodeValidation", mapOfFormNodeValidation);
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

        Map<String, List<ValidatorConfig>> globalValidatorDefinitionsMap = Maps.newHashMap();
        HashMap<String, ValidatorDefinition> globalValidatorDefinitions = Maps.newHashMap();
        for (GraphElement element : definition.getNodesRecursive()) {
            if (element instanceof FormNode && ((FormNode) element).hasFormValidation()) {
                FormNode formNodeElement = ((FormNode) element);
                FormNodeValidation validation = formNodeElement.getValidation(IOUtils.getAdjacentFile(getDefinitionFile(),
                        formNodeElement.getValidationFileName()));
                List<ValidatorConfig> globalValidators = Lists.newArrayList();
                for (ValidatorConfig globalConfig : validation.getGlobalConfigs()) {
                    globalValidators.add(globalConfig);
                    ValidatorDefinition definitionByValidatorType = ValidatorDefinitionRegistry.getDefinition(globalConfig.getType());
                    globalValidatorDefinitions.put(globalConfig.getType(), definitionByValidatorType);
                }
                globalValidatorDefinitionsMap.put(formNodeElement.getId(), globalValidators);
            }
        }

        map.put("globalValidatorDefinitions", globalValidatorDefinitions);
        map.put("globalValidatorDefinitionsMap", globalValidatorDefinitionsMap);

        IFile htmlDefinition = IOUtils.getAdjacentFile(getActiveDesignerEditor().getDefinitionFile(),
                ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
        if (htmlDefinition.exists()) {
            map.put("brief", IOUtils.readStream(htmlDefinition.getContents()));
        }

        // freeMarker can't work with params like StartState.class directly
        HashMap<String, Object> model = new HashMap<String, Object>();
        map.put("model", model);
        model.put("start", StartState.class);
        model.put("task", TaskState.class);
        model.put("node", Node.class);
        model.put("end", EndState.class);
        model.put("variable", Variable.class);
        model.put("timer", Timer.class);
        model.put("endToken", EndTokenState.class);

        Writer writer = new StringWriter();
        template.process(map, writer);
        return writer.toString();

    }

    private static List<Node> makeSequenceList(ProcessDefinition definition) {
        List<Node> result = Lists.newArrayList();
        if (definition != null && definition.getChildren(StartState.class).size() > 0) {
            Node curNode = definition.getChildren(StartState.class).get(0);
            boolean isAppend = true;
            do {
                if (isAppend) {
                    result.add(curNode);
                }
                curNode = (Node) curNode.getNodeRegulationsProperties().getNextNode();
                isAppend = true;
                if (curNode != null && curNode.getClass().equals(Subprocess.class)) {
                    result.add(curNode);
                    isAppend = false;
                    SubprocessDefinition subprocessDefinition = ((Subprocess) curNode).getEmbeddedSubprocess();
                    sequenceNodeList = makeSequenceList(subprocessDefinition);
                    for (Node nodeInSequenceList : sequenceNodeList) {
                        result.add(nodeInSequenceList);
                    }
                }
            } while (curNode != null);
        }
        return result;
    }

    public static List<Node> getRegulationsSequence() {
        try {
            List<ValidationError> regulationsValidationErrors = Lists.newArrayList();
            ProcessDefinition processDefinition = getActiveDesignerDefinition();
            boolean resultOfValidation = ProcessDefinition.validateRegulations(processDefinition, regulationsValidationErrors);
            IViewReference[] viewParts = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
            for (IViewReference iviewReference : viewParts) {
                if (iviewReference.getId().equals(RegulationsNotesView.ID)) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(iviewReference);
                    break;
                }
            }
            sequenceNodeList = makeSequenceList(processDefinition);
            if (resultOfValidation == false) {
                for (ValidationError regulationsNote : regulationsValidationErrors) {
                    addRegulationsNote(getActiveDesignerDefinitionFile(), regulationsNote.getSource().getProcessDefinition(), regulationsNote);
                }
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RegulationsNotesView.ID);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        return sequenceNodeList;
    }

    public static ProcessDefinition getActiveDesignerDefinition() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return ((ProcessEditorBase) editor).getDefinition();
        }
        return null;
    }

    public static IFile getActiveDesignerDefinitionFile() {
        IEditorPart editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        if (editor instanceof ProcessEditorBase) {
            return ((ProcessEditorBase) editor).getDefinitionFile();
        }
        return null;
    }
}