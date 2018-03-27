package ru.runa.gpd.lang.action;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.internal.action.SaveImageAction;
import org.eclipse.ui.internal.Workbench;

import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;

public class ExportDiagramHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        GraphitiProcessEditor editor = (GraphitiProcessEditor) Workbench.getInstance().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        DiagramBehavior diagramBehavior = editor.getDiagramEditorPage().getDiagramBehavior();
        new SaveImageAction(diagramBehavior, diagramBehavior.getConfigurationProvider()).run();
        return null;
    }

}
