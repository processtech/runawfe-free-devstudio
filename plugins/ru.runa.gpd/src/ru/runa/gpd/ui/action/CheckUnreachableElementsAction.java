package ru.runa.gpd.ui.action;

import com.google.common.base.Joiner;
import java.util.List;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.algorithms.CheckUnlimitedTokenAlgorithm;
import ru.runa.gpd.algorithms.ProcessGraphConverter;
import ru.runa.gpd.algorithms.reachability.CheckUnreachableElementsAlgorithm;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.ui.custom.Dialogs;

public class CheckUnreachableElementsAction extends BaseActionDelegate {
    @Override
    public void run(IAction action) {
        IEditorPart editorPart = getActiveEditor();
        if (editorPart != null) {
            IEditorInput editorInput = editorPart.getEditorInput();
            if (editorInput instanceof FileEditorInput) {
                ProcessDefinition definition = ProcessCache.getProcessDefinition(((FileEditorInput) editorInput).getFile());
                List<Transition> transitions = definition.getChildrenRecursive(Transition.class);
                List<Node> nodes = definition.getChildren(Node.class);
                if (new CheckUnlimitedTokenAlgorithm(transitions, nodes).startAlgorithm() != null) {
                    Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnlimitedTokens.Message"));
                    return;
                }
                ProcessGraphConverter converter = new ProcessGraphConverter(definition);
                if (!converter.getUnsupportedNodes().isEmpty()) {
                    Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnsupportedElements.Message",
                            Joiner.on(", ").join(converter.getUnsupportedNodes())));
                    return;
                }
                CheckUnreachableElementsAlgorithm algorithm = new CheckUnreachableElementsAlgorithm(converter.getGraph());
                algorithm.startAlgorithm(() -> false);
                if (!algorithm.isTokenCountBounded()) {
                    Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnboundedStates.Message",
                            algorithm.getUnboundedElement().toString()));
                } else if (!algorithm.getUnreachableElements().isEmpty()) {
                    Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.SituationExist.Message",
                            Joiner.on(", ").join(algorithm.getUnreachableElements())));
                } else {
                    Dialogs.information(Localization.getString("CheckingUnreachableElementsAction.SituationNotExist.Message"));
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
