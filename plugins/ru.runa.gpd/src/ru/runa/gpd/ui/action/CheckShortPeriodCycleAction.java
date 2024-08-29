package ru.runa.gpd.ui.action;

import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.algorithms.CheckUnlimitedTokenAlgorithm;
import ru.runa.gpd.algorithms.cycles.CheckShortPeriodCycleAlgorithm;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.custom.Dialogs;

public class CheckShortPeriodCycleAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        IEditorPart editorPart = getActiveEditor();
        if (editorPart != null) {
            IEditorInput editorInput = editorPart.getEditorInput();
            if (editorInput instanceof FileEditorInput) {
                ProcessDefinition definition = ProcessCache.getProcessDefinition(((FileEditorInput) editorInput).getFile());
                List<Transition> transitions = definition.getChildrenRecursive(Transition.class);
                List<Node> nodes = definition.getChildren(Node.class);
                CheckUnlimitedTokenAlgorithm algorithm = new CheckUnlimitedTokenAlgorithm(transitions, nodes);
                Transition redTransition = algorithm.startAlgorithm();
                if (redTransition != null) {
                    Dialogs.warning(Localization.getString("CheckingCyclesAction.UnlimitedTokens.Message"));
                    return;
                }
                CheckShortPeriodCycleAlgorithm cycAlgo = new CheckShortPeriodCycleAlgorithm(nodes, transitions);
                if (cycAlgo.hasShortPeriodCycle()) {
                    Dialogs.warning(Localization.getString("CheckingCyclesAction.SituationExist.Message", cycAlgo.getCycleIds()));
                } else {
                    Dialogs.information(Localization.getString("CheckingCyclesAction.SituationNotExist.Message"));
                }
            }
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        ProcessEditorBase editor = getActiveDesignerEditor();
        action.setEnabled(getDirtyEditors().length == 0 && editor != null && editor.getDefinition().getLanguage() == Language.BPMN
                && !editor.getDefinition().isInvalid());
    }

    private IEditorPart[] getDirtyEditors() {
        return window.getActivePage().getDirtyEditors();
    }
}