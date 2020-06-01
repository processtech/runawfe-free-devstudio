package ru.runa.gpd.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.editor.graphiti.DiagramEditorPage;
import ru.runa.gpd.editor.graphiti.GraphitiProcessEditor;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.ui.dialog.FindElementDialog;
import ru.runa.gpd.util.WorkspaceOperations;

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
            String elementIds = findElementDlg.getUserInput();
            String[] ss = elementIds.split("\\.");
            List<String> elmPath = Arrays.stream(ss).map((s) -> s.trim().toLowerCase()).collect(Collectors.toList());
            if (elmPath.size() == 0) {
                elmPath.add(elementIds.trim().toLowerCase());
            }

            List<GraphElement> lstRoots = new ArrayList<GraphElement>(1);
            lstRoots.add(procDef);

            for (GraphElement rootElement : lstRoots) {
                List<Subprocess> subProcesses = new ArrayList<Subprocess>();
                GraphElement elementFounded = findElement(rootElement, elmPath, subProcesses);
                if (elementFounded != null) {

                    if (subProcesses.size() == 0) {
                        diaPage.select(elementFounded);
                    } else {
                        for (Subprocess subProcess : subProcesses) {
                            WorkspaceOperations.openSubprocessDefinition(subProcess);
                        }

                        IEditorPart subPart = this.getActiveEditor();
                        DiagramEditorPage subPage = ((GraphitiProcessEditor) subPart).getDiagramEditorPage();
                        subPage.select(elementFounded);
                    }
                }
            }
        }

    }

    private String extractId(String fullId) {
        int n = fullId.indexOf('.');
        if (n >= 0) {
            return fullId.substring(n + 1);
        } else {
            return fullId;
        }
    }

    private String extractSub(String fullId) {
        int n = fullId.indexOf('.');
        if (n >= 0) {
            return fullId.substring(0, n);
        } else {
            return fullId;
        }
    }

    private GraphElement findElement(GraphElement rootElement, List<String> elmPath, List<Subprocess> subProcesses) {
        if (elmPath.size() == 0) {
            return null;
        } else if (elmPath.size() == 1) {
            return findElementFlat(rootElement, elmPath.get(0), subProcesses);
        } else {
            return findElementHier(rootElement, elmPath, subProcesses);
        }
    }

    private GraphElement findElementFlat(GraphElement rootElement, String elmId, List<Subprocess> subProcesses) {
        if (elmId.equalsIgnoreCase(rootElement.getId())) {
            return rootElement;
        }

        List<GraphElement> lstAllElements = rootElement.getChildrenRecursive(GraphElement.class);
        GraphElement elementFounded = lstAllElements.stream().filter((e) -> safeEqualsName(elmId, e)).findAny().orElse(null);
        return elementFounded;
    }

    private GraphElement findElementHier(GraphElement rootElement, List<String> elmPath, List<Subprocess> subProcesses) {
        GraphElement actualRootElement = null;
        if (elmPath.get(0).equalsIgnoreCase(rootElement.getId())) {
            actualRootElement = rootElement;
        } else {
            actualRootElement = rootElement.getChildren(GraphElement.class).stream().filter((e) -> safeEqualsName(elmPath.get(0), e)).findAny()
                    .orElse(null);
        }
        if (actualRootElement == null) {
            return null;
        }

        if (actualRootElement instanceof Subprocess) {
            Subprocess subProcess = (Subprocess) actualRootElement;
            subProcesses.add(subProcess);
            return findElement(subProcess.getEmbeddedSubprocess(), elmPath.subList(1, elmPath.size()), subProcesses);
        } else {
            return null;
        }
    }

    private boolean safeEqualsName(String name, GraphElement element) {
        String elmName = element.getId();
        if (elmName == null) {
            return false;
        }

        elmName = extractId(elmName);
        if (name.equalsIgnoreCase(elmName)) {
            return true;
        }

        if (element instanceof Subprocess) {
            SubprocessDefinition subDef = ((Subprocess) element).getEmbeddedSubprocess();
            if (subDef == null) {
                return false;
            }

            elmName = extractId(subDef.getId());
            if (name.equalsIgnoreCase(elmName)) {
                return true;
            }

            elmName = extractSub(subDef.getId());
            if (name.equalsIgnoreCase(elmName)) {
                return true;
            }

            elmName = subDef.getName();
            if (name.equalsIgnoreCase(elmName)) {
                return true;
            }
        }
        return false;
    }
}
