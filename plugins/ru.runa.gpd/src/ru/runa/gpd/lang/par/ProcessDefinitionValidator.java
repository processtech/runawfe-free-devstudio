package ru.runa.gpd.lang.par;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.view.ValidationErrorsView;

import com.google.common.collect.Lists;

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

    private static void addError(IFile definitionFile, ProcessDefinition definition, ValidationError validationError) {
        try {
            IMarker marker = definitionFile.createMarker(ValidationErrorsView.ID);
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

}
