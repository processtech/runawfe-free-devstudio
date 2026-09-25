package ru.runa.gpd.ui.action;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.CancellationException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
                CheckOperation operation = new CheckOperation(definition);
                try {
                    new ProgressMonitorDialog(window.getShell()).run(true, true, operation);
                } catch (InvocationTargetException e) {
                    Throwables.throwIfUnchecked(e.getTargetException());
                    throw new RuntimeException(e.getTargetException());
                } catch (InterruptedException e) {
                    return;
                }
                showResult(operation);
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

    private void showResult(CheckOperation operation) {
        if (operation.unlimitedTokens) {
            Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnlimitedTokens.Message"));
        } else if (!operation.converter.getUnsupportedNodes().isEmpty()) {
            Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnsupportedElements.Message",
                    Joiner.on(", ").join(operation.converter.getUnsupportedNodes())));
        } else if (!operation.algorithm.isTokenCountBounded()) {
            Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.UnboundedStates.Message",
                    operation.algorithm.getUnboundedElement().toString()));
        } else if (!operation.algorithm.getUnreachableElements().isEmpty()) {
            Dialogs.warning(Localization.getString("CheckingUnreachableElementsAction.SituationExist.Message",
                    Joiner.on(", ").join(operation.algorithm.getUnreachableElements())));
        } else {
            Dialogs.information(Localization.getString("CheckingUnreachableElementsAction.SituationNotExist.Message"));
        }
    }

    private static class CheckOperation implements IRunnableWithProgress {
        private final ProcessDefinition definition;
        private boolean unlimitedTokens;
        private ProcessGraphConverter converter;
        private CheckUnreachableElementsAlgorithm algorithm;

        CheckOperation(ProcessDefinition definition) {
            this.definition = definition;
        }

        @Override
        public void run(IProgressMonitor monitor) throws InterruptedException {
            monitor.beginTask(Localization.getString("task.CheckUnreachableElements"), IProgressMonitor.UNKNOWN);
            try {
                List<Transition> transitions = definition.getChildrenRecursive(Transition.class);
                List<Node> nodes = definition.getChildren(Node.class);
                unlimitedTokens = new CheckUnlimitedTokenAlgorithm(transitions, nodes).startAlgorithm() != null;
                if (unlimitedTokens) {
                    return;
                }
                if (monitor.isCanceled()) {
                    throw new InterruptedException();
                }
                converter = new ProcessGraphConverter(definition);
                if (converter.getUnsupportedNodes().isEmpty()) {
                    algorithm = new CheckUnreachableElementsAlgorithm(converter.getGraph());
                    algorithm.startAlgorithm(monitor::isCanceled);
                }
            } catch (CancellationException e) {
                throw new InterruptedException();
            } finally {
                monitor.done();
            }
        }
    }
}
