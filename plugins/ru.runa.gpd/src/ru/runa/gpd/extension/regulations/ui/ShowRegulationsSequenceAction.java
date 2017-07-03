package ru.runa.gpd.extension.regulations.ui;

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import ru.runa.gpd.PluginConstants;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.action.BaseActionDelegate;
import ru.runa.gpd.util.EditorUtils;

public class ShowRegulationsSequenceAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            processDefinition.getFile().deleteMarkers(RegulationsSequenceView.ID, true, IResource.DEPTH_ONE);
            for (SubprocessDefinition subprocessDefinition : processDefinition.getEmbeddedSubprocesses().values()) {
                subprocessDefinition.getFile().deleteMarkers(RegulationsSequenceView.ID, true, IResource.DEPTH_ONE);
            }
            RegulationsUtil.validate(processDefinition);
            List<Node> listOfNodes = RegulationsUtil.getSequencedNodes(processDefinition);
            for (int i = 0; i < listOfNodes.size(); i++) {
                addRegulationsSequenceNote(listOfNodes.get(i).getProcessDefinition(), i + 1, listOfNodes.get(i));
            }
            RegulationsSequenceView view = (RegulationsSequenceView) EditorUtils.showView(RegulationsSequenceView.ID);
            view.refresh(processDefinition);
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

    private static void addRegulationsSequenceNote(ProcessDefinition processDefinition, int order, Node node) throws CoreException {
        IMarker marker = processDefinition.getFile().createMarker(RegulationsSequenceView.ID);
        if (marker.exists()) {
            marker.setAttribute(PluginConstants.SELECTION_LINK_KEY, node.getId());
            marker.setAttribute(IMarker.MESSAGE, RegulationsUtil.getNodeLabel(node));
            marker.setAttribute(RegulationsSequenceView.ORDER, order);
            marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
            marker.setAttribute(PluginConstants.PROCESS_NAME_KEY, processDefinition.getName());
        }
    }

}
