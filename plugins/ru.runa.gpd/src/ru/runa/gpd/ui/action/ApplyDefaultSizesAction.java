package ru.runa.gpd.ui.action;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Node;

public class ApplyDefaultSizesAction extends BaseActionDelegate {

    @Override
    public void run(IAction action) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        for (Node node : editor.getDefinition().getChildren(Node.class)) {
            Rectangle oldRectangle = node.getConstraint();
            Dimension defaultSize = node.getTypeDefinition().getGraphitiEntry().getDefaultSize(node);
            node.setConstraint(new Rectangle(oldRectangle.x, oldRectangle.y, defaultSize.width, defaultSize.height));
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(editor != null && editor.getDefinition().getLanguage() == Language.BPMN);
    }
}
