package ru.runa.gpd.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IFolder;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.graphiti.ui.internal.parts.ContainerShapeEditPart;
import org.eclipse.ui.actions.ActionFactory;

import ru.runa.gpd.Localization;
import ru.runa.gpd.editor.gef.part.graph.NodeGraphicalEditPart;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;

public class CopyAction extends SelectionAction {
    private final ProcessEditorBase editor;

    public CopyAction(ProcessEditorBase editor) {
        super(editor);
        this.editor = editor;
        setText(Localization.getString("button.copy"));
    }

    @Override
    protected boolean calculateEnabled() {
        return extractNodes().size() > 0;
    }

    private Set<NamedGraphElement> extractNodes() {
        List<EditPart> editParts = editor.getGraphicalViewer().getSelectedEditParts();
        Set<NamedGraphElement> result = new HashSet<>();
        for (EditPart editPart : editParts) {
            if (!(editPart instanceof AbstractGraphicalEditPart)) {
                continue;
            }
            NamedGraphElement node = null;
            if (editPart instanceof NodeGraphicalEditPart) {
                // gef way
                node = ((NodeGraphicalEditPart) editPart).getModel();
            } else if (editPart instanceof ContainerShapeEditPart) {
                // graphiti way
                ContainerShapeEditPart container = (ContainerShapeEditPart) editPart;
                Object object = container.getFeatureProvider().getBusinessObjectForPictogramElement(container.getPictogramElement());
                if (object instanceof Action || object instanceof TextDecorationNode) {
                    continue;
                }
                node = (NamedGraphElement) object;
            }
            // 1. If transition selected, it is not able to detect from NodeGraphicalEditPart/ContainerShapeEditPart and return null
            // 2. Text decoration for Start end End created automatically and don't need copy too.
            if (node != null) {
                result.add(node);
                // copy internal storage along with ScriptTask even if it is not selected
                if (node instanceof ScriptTask) {
                    ScriptTask scriptTask = (ScriptTask) node;
                    if (scriptTask.isUseExternalStorageOut()) {
                        result.addAll(scriptTask.getDottedTransitionTarget());
                    }
                    if (scriptTask.isUseExternalStorageIn()) {
                        result.addAll(scriptTask.getDottedTransitionSource());
                    }

				}
            }
        }
        return result;
    }

    @Override
    public void run() {
        CopyBuffer copyBuffer = new CopyBuffer((IFolder) editor.getDefinitionFile().getParent(), editor.getDefinition().getLanguage(),
                extractNodes(), editor.toString(), ((FigureCanvas) editor.getGraphicalViewer().getControl()).getViewport().getViewLocation().getCopy());
        copyBuffer.setToClipboard();
        ((SelectionAction) ((ActionRegistry) editor.getAdapter(ActionRegistry.class)).getAction(ActionFactory.PASTE.getId())).update();
    }
}