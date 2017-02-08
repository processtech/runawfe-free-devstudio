package ru.runa.gpd.ui.action;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.view.RegulationsNotesView;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TextEditorInput;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class CreateProcessRegulations extends BaseModelActionDelegate {

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
                TextEditorInput input = new TextEditorInput(proccDefinition.getName() + ".rgl", html);
                IDE.openEditor(getWorkbenchPage(), input, "ru.runa.gpd.wysiwyg.RegulationsHTMLEditor");
            } else {
                for (ValidationError regulationsNote : regulationsValidationErrors) {
                    addRegulationsNote(getActiveDesignerEditor().getDefinitionFile(), regulationsNote.getSource().getProcessDefinition(),
                            regulationsNote);
                }
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RegulationsNotesView.ID);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }

    }

    public static void addRegulationsNote(IFile definitionFile, ProcessDefinition definition, ValidationError validationError) {
        try {
            IMarker marker = definitionFile.createMarker(RegulationsNotesView.ID);
            if (marker.exists()) {
                marker.setAttribute(IMarker.MESSAGE, validationError.getMessage());
                String elementId = validationError.getSource().toString();
                if (validationError.getSource() instanceof Node) {
                    elementId = ((Node) validationError.getSource()).getId();
                }
                if (validationError.getSource() instanceof Swimlane) {
                    marker.setAttribute(PluginConstants.SWIMLANE_LINK_KEY, elementId);
                } else if (validationError.getSource() instanceof Action) {
                    Action action = (Action) validationError.getSource();
                    NamedGraphElement actionParent = (NamedGraphElement) action.getParent();
                    if (actionParent != null) {
                        marker.setAttribute(PluginConstants.ACTION_INDEX_KEY, actionParent.getActions().indexOf(action));
                        String parentNodeTreePath;
                        if (actionParent instanceof Transition) {
                            parentNodeTreePath = ((NamedGraphElement) actionParent.getParent()).getName() + "|" + actionParent.getName();
                        } else {
                            parentNodeTreePath = actionParent.getName();
                        }
                        marker.setAttribute(PluginConstants.PARENT_NODE_KEY, parentNodeTreePath);
                        elementId = action + " (" + parentNodeTreePath + ")";
                    } else {
                        elementId = action.toString();
                    }
                } else {
                    marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, elementId);
                }
                marker.setAttribute(IMarker.LOCATION, validationError.getSource().toString());
                marker.setAttribute(IMarker.SEVERITY, validationError.getSeverity());
                marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, definition.getName());
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }

    }

    private String generateRegulations(ProcessDefinition definition) throws Exception {

        Configuration config = new Configuration();

        config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        config.setDefaultEncoding(Charsets.UTF_8.name());
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        // TODO need localization
        Path path = new Path("template/regulations.ftl");

        Bundle bundl = Activator.getDefault().getBundle();
        URL url = FileLocator.find(bundl, path, Collections.EMPTY_MAP);
        URL fileUrl = FileLocator.toFileURL(url);
        InputStream input = fileUrl.openConnection().getInputStream();
        Template template = new Template("regulations", new StringReader(IOUtils.readStream(input)), config);
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
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("proc", definition);
        map.put("listOfNodes", listOfNodes);
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

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelection() != null && getSelection().getClass().equals(ProcessDefinition.class)) {
            action.setEnabled(!getActiveDesignerEditor().getDefinition().isInvalid());
        } else {
            action.setEnabled(false);
        }
    }

    private List<Node> makeSequenceList(ProcessDefinition definition) {
        List<Node> result = Lists.newArrayList();
        if (definition != null && definition.getChildren(StartState.class).size() > 0
                && definition.getChildren(StartState.class).get(0).getLeavingTransitions().size() > 0) {
            StartState startState = definition.getChildren(StartState.class).get(0);
            result.add(startState);
            Node curNode = startState.getLeavingTransitions().get(0).getTarget();
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
                    List<Node> sequenceNodeList = makeSequenceList(subprocessDefinition);
                    for (Node nodeInSequenceList : sequenceNodeList) {
                        result.add(nodeInSequenceList);
                    }
                }
            } while (curNode != null);
        }
        return result;
    }
}
