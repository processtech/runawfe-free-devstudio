package ru.runa.gpd.ui.action;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import ru.runa.gpd.editor.graphiti.DiagramEditorPage;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.ui.dialog.FindElementDialog;
import ru.runa.gpd.ui.view.SearchResultContentProvider;
import ru.runa.gpd.ui.view.SearchResultView;
import ru.runa.wfe.InternalApplicationException;

public class FindElementAction extends BaseActionDelegate {

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        action.setEnabled(internalEnabled());
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
            
            String elementId = findElementDlg.getUserInputID();
            String elementName = findElementDlg.getUserInputName();
            
            List<GraphElement> lstRoots = new ArrayList<GraphElement>(1);
            lstRoots.add(getRootProcessDefinition(procDef));

            for (GraphElement rootElement : lstRoots) {
                List<Subprocess> subProcesses = new ArrayList<Subprocess>();
                List<GraphElement> lstFounded = new ArrayList<>();
                findElement(rootElement, elementId, elementName, subProcesses, lstFounded);
                 
                if (lstFounded.size() > 0) {
                    SearchResultContentProvider.foundResults(lstFounded);
                } else {
                    SearchResultContentProvider.clearResults();
                    
                    if (subProcesses.size() == 0) {
                        diaPage.select(null);
                    } else {
                        IEditorPart subPart = this.getActiveEditor();
                        DiagramEditorPage subPage = ((GraphitiProcessEditor) subPart).getDiagramEditorPage();
                        subPage.select(null);
                    }
                }
            }

            try {
                IViewPart part = window.getActivePage().showView(SearchResultView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
                if (part instanceof SearchResultView) {
                    ((SearchResultView) part).refreshContent();
                }
            } catch (PartInitException e) {
                throw new InternalApplicationException(e);
            }
        }

    }

    private void findElement(GraphElement rootElement, String elementId, String elementName, List<Subprocess> subProcesses, List<GraphElement> dest) {
        if (elementId == null && elementName == null) {
            return;
        }
        List<GraphElement> lstAllElements = rootElement.getChildrenRecursive(GraphElement.class);
        lstAllElements.stream().filter((e) -> safeEqualsName(elementId, elementName, e)).forEach(e -> {
            dest.add(e);
        });

        rootElement.getChildren(Subprocess.class).stream().forEach(subProcess -> {
            subProcesses.add(subProcess);
            if (subProcess.isEmbedded()) {
                SubprocessDefinition def = subProcess.getEmbeddedSubprocess();
                if (def != null) {
                    findElement(def, elementId, elementName, subProcesses, dest);
                }
            } else {
                findElement(subProcess, elementId, elementName, subProcesses, dest);
            }
        });
    }

    private boolean safeEqualsName(final String ids, final String name, GraphElement element) {
        if (ids == null && name == null) {
            return false;
        }
        return ((ids == null)?true:safeEqualsIds(ids, element)) && ((name == null)?true:safeEqualsLabel(name, element));
    }

    private boolean safeEqualsLabel(final String name, GraphElement element) {
        if (element.getLabel() == null) {
            return false;
        }
        if (element.getLabel().toLowerCase().contains(name.toLowerCase())) {
            return true;
        }
        return false;
    }    

    private boolean safeEqualsIds(final String elementId, GraphElement element) {
        if (element.getId() == null) {
            return false;
        }
        if (elementId.toLowerCase().equals(element.getId().toLowerCase())) {
            return true;
        }
        return false;
    }

    private ProcessDefinition getRootProcessDefinition(ProcessDefinition processDefinition) {
        GraphElement parent = processDefinition.getParent();
        if (processDefinition instanceof SubprocessDefinition && parent != null && checkActualParent(processDefinition, (ProcessDefinition) parent)) {
            processDefinition = getRootProcessDefinition((ProcessDefinition) parent);
        }
        return processDefinition;
    }

    private boolean checkActualParent(ProcessDefinition processDefinition, ProcessDefinition parent) {
        return parent.getChildren(Subprocess.class).stream().filter(Subprocess::isEmbedded)
                .anyMatch(subProcess -> subProcess.getEmbeddedSubprocess().getId().equals(processDefinition.getId()));
    }
}
