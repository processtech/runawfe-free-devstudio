package ru.runa.gpd.lang.par;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.view.ValidationErrorsView;

public class ProcessDefinitionValidator {

    public static int NO_ERRORS = 0;
    public static int WARNINGS = 1;
    public static int ERRORS = 2;

    /**
     * 0 = no errors 1 = only warnings 2 = errors
     */
    public static int validateDefinition(ProcessDefinition processDefinition) {
        try {
            boolean hasErrors = false;
            boolean hasWarnings = false;
            IFile definitionFile = processDefinition.getFile();
            definitionFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
            List<ValidationError> errors = Lists.newArrayList();
            processDefinition.validate(errors, definitionFile);
            for (ValidationError validationError : errors) {
                addError(definitionFile, processDefinition, validationError);
                if (validationError.getSeverity() == IMarker.SEVERITY_WARNING) {
                    hasWarnings = true;
                }
                if (validationError.getSeverity() == IMarker.SEVERITY_ERROR) {
                    hasErrors = true;
                }
            }
            if (hasErrors) {
                return ERRORS;
            }
            if (hasWarnings) {
                return WARNINGS;
            }
            return NO_ERRORS;
        } catch (Throwable e) {
            PluginLogger.logError(e);
            return ERRORS;
        }
    }

    public static void logErrors(ProcessDefinition processDefinition, List<String> errors, List<Delegable> errorSources, boolean clearBefore) {
        try {
            IFile definitionFile = processDefinition.getFile();
            if (clearBefore) {
                definitionFile.deleteMarkers(ValidationErrorsView.ID, true, IResource.DEPTH_INFINITE);
            }
            ListIterator<String> iterator = errors.listIterator();
            ListIterator<Delegable> iterator2 = errorSources.listIterator();
            while (iterator.hasNext()) {
                Delegable delegable = iterator2.next();
                addError(definitionFile, processDefinition, ValidationError
                        .createError(delegable instanceof GraphElement ? (GraphElement) delegable : processDefinition, iterator.next()));
            }
        } catch (Throwable e) {
            PluginLogger.logError(e);
        }
    }

    private static void addError(IFile definitionFile, ProcessDefinition definition, ValidationError validationError) {
        try {
            IMarker marker = definitionFile.createMarker(ValidationErrorsView.ID);
            if (marker.exists()) {
                Map<String, Object> attributes = Maps.newHashMap();
                attributes.put(IMarker.MESSAGE, validationError.getMessage());
                String elementId = validationError.getSource().toString();
                if (validationError.getSource() instanceof Node) {
                    elementId = ((Node) validationError.getSource()).getId();
                }
                if (validationError.getSource() instanceof Swimlane) {
                    attributes.put(PluginConstants.SWIMLANE_LINK_KEY, elementId);
                } else if (validationError.getSource() instanceof Action) {
                    Action action = (Action) validationError.getSource();
                    NamedGraphElement actionParent = (NamedGraphElement) action.getParent();
                    if (actionParent != null) {
                        attributes.put(PluginConstants.ACTION_INDEX_KEY, actionParent.getActions().indexOf(action));
                        String parentNodeTreePath;
                        if (actionParent instanceof Transition) {
                            parentNodeTreePath = ((NamedGraphElement) actionParent.getParent()).getName() + "|" + actionParent.getName();
                        } else {
                            parentNodeTreePath = actionParent.getName();
                        }
                        attributes.put(PluginConstants.PARENT_NODE_KEY, parentNodeTreePath);
                        elementId = action + " (" + parentNodeTreePath + ")";
                    } else {
                        elementId = action.toString();
                    }
                } else {
                    attributes.put(PluginConstants.SELECTION_LINK_KEY, elementId);
                }
                attributes.put(IMarker.LOCATION, validationError.getSource().toString());
                attributes.put(IMarker.SEVERITY, validationError.getSeverity());
                attributes.put(PluginConstants.VALIDATION_ERROR_DETAILS_KEY, validationError.getDetails());
                attributes.put(PluginConstants.PROCESS_NAME_KEY, definition.getName());
                marker.setAttributes(attributes);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

}
