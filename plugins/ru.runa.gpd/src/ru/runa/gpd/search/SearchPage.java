package ru.runa.gpd.search;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.SubToolBarManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.search.internal.ui.text.IFileSearchContentProvider;
import org.eclipse.search.ui.text.AbstractTextSearchViewPage;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.BotTaskUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public class SearchPage extends AbstractTextSearchViewPage {
    private IFileSearchContentProvider contentProvider;

    public SearchPage() {
        super(FLAG_LAYOUT_TREE);
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        super.setActionBars(actionBars);
        getViewer().getControl().getMenu().dispose();
        SubToolBarManager toolBarManager = (SubToolBarManager) actionBars.getToolBarManager();
        IContributionItem[] items = toolBarManager.getParent().getItems();
        for (IContributionItem contributionItem : items) {
            if (contributionItem instanceof ActionContributionItem) {
                ActionContributionItem aci = (ActionContributionItem) contributionItem;
                if ("PinSearchViewAction".equals(aci.getAction().getClass().getSimpleName())) {
                    toolBarManager.getParent().remove(contributionItem);
                }
            }
        }
    }

    @Override
    protected void configureTableViewer(TableViewer viewer) {
        throw new UnsupportedOperationException("Only tree view supported");
    }

    @Override
    protected void configureTreeViewer(TreeViewer viewer) {
        viewer.setUseHashlookup(true);
        SearchLabelProvider innerLabelProvider = new SearchLabelProvider(this);
        viewer.setLabelProvider(new DecoratingLabelProvider(innerLabelProvider, PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
        viewer.setContentProvider(new SearchTreeContentProvider(viewer));
        contentProvider = (IFileSearchContentProvider) viewer.getContentProvider();
    }

    @Override
    protected void showMatch(Match match, int offset, int length, boolean activate) throws PartInitException {
        ElementMatch elementMatch = (ElementMatch) match.getElement();
        IEditorPart editor = null;
        if (ElementMatch.CONTEXT_FORM.equals(elementMatch.getContext())) {
            selectFormNode(elementMatch);
            try {
                FormNode formNode = (FormNode) elementMatch.getGraphElement();
                editor = FormTypeProvider.getFormType(formNode.getFormType()).openForm(elementMatch.getFile(), formNode);
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        } else if (ElementMatch.CONTEXT_FORM_VALIDATION.equals(elementMatch.getContext())) {
            selectFormNode(elementMatch);
            try {
                FormNode formNode = (FormNode) elementMatch.getGraphElement();
                editor = FormTypeProvider.getFormType(formNode.getFormType())
                        .openForm(IOUtils.getAdjacentFile(elementMatch.getFile(), formNode.getFormFileName()), formNode);
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        } else if (ElementMatch.CONTEXT_FORM_SCRIPT.equals(elementMatch.getContext())) {
            try {
                if (elementMatch.getGraphElement() instanceof FormNode) {
                    FormNode formNode = (FormNode) elementMatch.getGraphElement();
                    editor = FormTypeProvider.getFormType(formNode.getFormType())
                            .openForm(IOUtils.getAdjacentFile(elementMatch.getFile(), formNode.getFormFileName()), formNode);
                } else if (elementMatch.getGraphElement() instanceof ProcessDefinition) {
                    IFile formJsFile = IOUtils.getAdjacentFile(((ProcessDefinition) elementMatch.getGraphElement()).getFile(),
                            ParContentProvider.FORM_JS_FILE_NAME);
                    IDE.openEditor(getSite().getPage(), formJsFile, true);
                }
            } catch (CoreException e) {
                PluginLogger.logError(e);
            }
        } else if (ElementMatch.CONTEXT_BOT_TASK_LINK.equals(elementMatch.getContext())) {
            BotTaskUtils.editBotTaskLinkConfiguration((TaskState) elementMatch.getGraphElement());
        } else if (ElementMatch.CONTEXT_BOT_TASK.equals(elementMatch.getContext())) {
            IDE.openEditor(getSite().getPage(), elementMatch.getFile(), BotTaskEditor.ID);
        } else if (ElementMatch.CONTEXT_PROCESS_DEFINITION.equals(elementMatch.getContext())) {
            ProcessEditorBase processEditor = WorkspaceOperations.openProcessDefinition(elementMatch.getFile());
            processEditor.select(elementMatch.getGraphElement());
        } else if (elementMatch.getGraphElement() != null) {
            ProcessEditorBase processEditor = WorkspaceOperations.openProcessDefinition(elementMatch.getFile());
            processEditor.select(elementMatch.getGraphElement());
        }
        if (editor == null) {
            return;
        }
        if (offset != 0 && length != 0) {
            ITextEditor textEditor = null;
            if (editor instanceof ITextEditor) {
                textEditor = (ITextEditor) editor;
            }
            if (textEditor == null && editor.getAdapter(ITextEditor.class) != null) {
                textEditor = editor.getAdapter(ITextEditor.class);
            }
            if (textEditor != null) {
                textEditor.selectAndReveal(offset, length);
            }
        }
    }

    @Override
    protected void elementsChanged(Object[] objects) {
        if (contentProvider != null) {
            contentProvider.elementsChanged(objects);
        }
    }

    @Override
    protected void clear() {
        if (contentProvider != null) {
            contentProvider.clear();
        }
    }
	
    private void selectFormNode(ElementMatch elementMatch) {
        ProcessEditorBase processEditor = WorkspaceOperations.openProcessDefinition(elementMatch.getParent().getFile());
        processEditor.select(elementMatch.getParent().getGraphElement());
    }
}
