package ru.runa.gpd.extension.regulations.ui;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.action.BaseActionDelegate;
import ru.runa.gpd.util.EditorUtils;

import com.google.common.collect.Lists;

public class ShowRegulationsSequenceAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            IFile definitionFile = getActiveDesignerEditor().getDefinitionFile();
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            definitionFile.deleteMarkers(RegulationsSequenceView.ID, true, IResource.DEPTH_ONE);
            List<ValidationError> validationErrors = Lists.newArrayList();
            RegulationsUtil.validate(definitionFile, processDefinition, validationErrors);
            List<Node> listOfNodes = RegulationsUtil.getSequencedNodes(processDefinition);
            for (int i = 0; i < listOfNodes.size(); i++) {
                addRegulationsSequenceNote(definitionFile, listOfNodes.get(i).getProcessDefinition(), i + 1, listOfNodes.get(i));
            }
            EditorUtils.showView(RegulationsSequenceView.ID);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(CommonPreferencePage.isRegulationsMenuItemsEnabled() && editor != null
                && !(editor.getDefinition() instanceof SubprocessDefinition));
    }

    private static void addRegulationsSequenceNote(IFile definitionFile, ProcessDefinition definition, long n, Node node) throws CoreException {
        IMarker marker = definitionFile.createMarker(RegulationsSequenceView.ID);
        if (marker.exists()) {
            marker.setAttribute(IMarker.MESSAGE, node.getName());
            String elementId = node.getId();
            marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, elementId);
            marker.setAttribute(IMarker.LOCATION, String.valueOf(n));
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, definition.getName());
        }
    }

}
