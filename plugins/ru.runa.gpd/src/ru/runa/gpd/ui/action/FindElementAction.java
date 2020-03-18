package ru.runa.gpd.ui.action;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

import ru.runa.gpd.editor.graphiti.DiagramEditorPage;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.dialog.FindElementDialog;

public class FindElementAction extends BaseActionDelegate {
	
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(internalEnabled() );
    }
	
	private boolean internalEnabled() {
		if (this.window == null) {
			return false;
		}
		
		IEditorPart editorPart = getActiveEditor();
		if (editorPart == null) {
			return false;
		}
		if (!(editorPart instanceof GraphitiProcessEditor)) {
			return false;
		}
		
		GraphitiProcessEditor gpePart = (GraphitiProcessEditor) editorPart;
		DiagramEditorPage diaPage = gpePart.getDiagramEditorPage();
		if (diaPage == null) {
			return false;
		}
		
		ProcessDefinition procDef = diaPage.getDefinition();
		if (procDef == null) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void run(IAction action) {
		if (this.window == null) {
			return;
		}
		
		IEditorPart editorPart = getActiveEditor();
		if (editorPart == null) {
			return;
		}
		if (!(editorPart instanceof GraphitiProcessEditor)) {
			return;
		}
		
		GraphitiProcessEditor gpePart = (GraphitiProcessEditor) editorPart;
		DiagramEditorPage diaPage = gpePart.getDiagramEditorPage();
		if (diaPage == null) {
			return;
		}
		
		ProcessDefinition procDef = diaPage.getDefinition();
		if (procDef == null) {
			return;
		}
		
		FindElementDialog findElementDlg = new FindElementDialog();
		if (Window.OK == findElementDlg.open()) {
			String elementID = findElementDlg.getUserInput();
			
			List<GraphElement> lstAllElements = procDef.getChildrenRecursive(GraphElement.class);
			GraphElement elementFounded = lstAllElements.stream().filter( (e) -> (e.getId() != null && e.getId().equalsIgnoreCase(elementID)) ).findAny().orElse(null);
			if (elementFounded != null) {
				diaPage.select(elementFounded);
			}
						
		}
		
	}

}
