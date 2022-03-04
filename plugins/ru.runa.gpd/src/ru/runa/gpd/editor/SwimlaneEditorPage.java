package ru.runa.gpd.editor;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.ui.actions.Clipboard;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.google.common.base.Joiner;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.PropertyNames;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GlobalSectionDefinition;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ltk.RenameRefactoringWizard;
import ru.runa.gpd.ltk.RenameVariableRefactoring;
import ru.runa.gpd.search.ElementMatch;
import ru.runa.gpd.search.SearchResult;
import ru.runa.gpd.search.VariableSearchQuery;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.DragAndDropAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.TableViewerLocalDragAndDropSupport;
import ru.runa.gpd.ui.dialog.UpdateSwimlaneNameDialog;
import ru.runa.gpd.ui.wizard.ChooseGlbSwimlaneWizard;
import ru.runa.gpd.ui.wizard.CompactWizardDialog;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.WorkspaceOperations;

public class SwimlaneEditorPage extends EditorPartBase<Swimlane> {

    private TableViewer tableViewer;
    private Button createButton;
    private Button searchButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button renameButton;
    private Button changeButton;
    private Button deleteButton;
    private Button copyButton;
    private Button pasteButton;
    private Button importGlobalButton;
    private Button makeLocalButton;
    private final boolean swimlanesCreateDeleteEnabled;

    public SwimlaneEditorPage(ProcessEditorBase editor) {
        super(editor);
        swimlanesCreateDeleteEnabled = editor.getDefinition().getSwimlaneDisplayMode() == SwimlaneDisplayMode.none;
    }

    @Override
    public void createPartControl(Composite parent) {
        SashForm sashForm = createSashForm(parent, SWT.VERTICAL, "DesignerSwimlaneEditorPage.label.swimlanes");

        Composite allSwimlanesComposite = createSection(sashForm, "DesignerSwimlaneEditorPage.label.all_swimlanes");

        tableViewer = createMainViewer(allSwimlanesComposite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
        tableViewer.setLabelProvider(new TableViewerLabelProvider());
        TableViewerLocalDragAndDropSupport.enable(tableViewer, new DragAndDropAdapter<Swimlane>() {

            @Override
            public void onDropElement(Swimlane beforeElement, Swimlane swimlane) {
                editor.getDefinition().changeChildIndex(swimlane, beforeElement);
            }
        });

        createTable(tableViewer, new DataViewerComparator<>(new ValueComparator<Swimlane>() {
            @Override
            public int compare(Swimlane o1, Swimlane o2) {
                int result = 0;
                switch (getColumn()) {
                case 0:
                    result = o1.getName().compareTo(o2.getName());
                    break;
                }
                return result;
            }
        }), new TableColumnDescription("property.name", 200, SWT.LEFT), new TableColumnDescription("swimlane.initializer", 400, SWT.LEFT));

        Composite buttonsBar = createActionBar(allSwimlanesComposite);
        createButton = addButton(buttonsBar, "button.create", new CreateSwimlaneSelectionListener(), false);
        createButton.setEnabled(swimlanesCreateDeleteEnabled);
        renameButton = addButton(buttonsBar, "button.rename", new RenameSwimlaneSelectionListener(), true);
        changeButton = addButton(buttonsBar, "button.change", new ChangeSwimlaneSelectionListener(), true);
        copyButton = addButton(buttonsBar, "button.copy", new CopySwimlaneSelectionListener(), true);
        pasteButton = addButton(buttonsBar, "button.paste", new PasteSwimlaneSelectionListener(), true);
        searchButton = addButton(buttonsBar, "button.search", new SearchSwimlaneUsageSelectionListener(), true);
        if (CommonPreferencePage.isGlobalObjectsEnabled()) {
            importGlobalButton = addButton(buttonsBar, "button.importGlobal", new ImportGlobalSwimlaneSelectionListener(), true);
            makeLocalButton = addButton(buttonsBar, "button.makeLocal", new MakeLocalListener(), true);
        }
        moveUpButton = addButton(buttonsBar, "button.up", new MoveSwimlaneSelectionListener(true), true);
        moveDownButton = addButton(buttonsBar, "button.down", new MoveSwimlaneSelectionListener(false), true);
        deleteButton = addButton(buttonsBar, "button.delete", new RemoveSwimlaneSelectionListener(), true);

        updateViewer();
    }

    @Override
    public void dispose() {
        for (Swimlane swimlane : getDefinition().getSwimlanes()) {
            swimlane.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String type = event.getPropertyName();
        if (PropertyNames.PROPERTY_CHILDREN_CHANGED.equals(type)) {
            updateViewer();
        } else if (event.getSource() instanceof Swimlane) {
            if (PropertyNames.PROPERTY_NAME.equals(type) || PropertyNames.PROPERTY_CONFIGURATION.equals(type)) {
                tableViewer.refresh(event.getSource());
            }
        }
    }

    @Override
    protected void updateUI() {
        List<Swimlane> swimlanes = (List<Swimlane>) tableViewer.getInput();
        List<Swimlane> selected = ((IStructuredSelection) tableViewer.getSelection()).toList();
        boolean withoutGlobals = withoutGlobals(selected);
        boolean isGlobalSection = isGlobalSection();
        boolean isUsingGlobals = isUsingGlobals();
        enableAction(searchButton, selected.size() == 1);
        enableAction(changeButton, withoutGlobals && selected.size() == 1);
        enableAction(moveUpButton, withoutGlobals && selected.size() == 1 && swimlanes.indexOf(selected.get(0)) > 0);
        enableAction(moveDownButton, withoutGlobals && selected.size() == 1 && swimlanes.indexOf(selected.get(0)) < swimlanes.size() - 1);
        enableAction(deleteButton, swimlanesCreateDeleteEnabled && selected.size() > 0);
        enableAction(renameButton, withoutGlobals && selected.size() == 1);
        enableAction(copyButton, withoutGlobals && selected.size() > 0);
        if (CommonPreferencePage.isGlobalObjectsEnabled()) {
            enableAction(importGlobalButton, (selected.size() >= 0 && !isGlobalSection && isUsingGlobals));
            enableAction(makeLocalButton, (!withoutGlobals && selected.size() == 1 && !isGlobalSection && isUsingGlobals));
        }
        boolean pasteEnabled = false;
        if (Clipboard.getDefault().getContents() instanceof List) {
            List<?> list = (List<?>) Clipboard.getDefault().getContents();
            if (list.size() > 0 && list.get(0) instanceof Swimlane) {
                pasteEnabled = true;
            }
        }
        enableAction(pasteButton, swimlanesCreateDeleteEnabled && pasteEnabled);
    }

    private boolean withoutGlobals(List<Swimlane> swimlanes) {
        return swimlanes.stream().noneMatch(Swimlane::isGlobal);
    }

    @SuppressWarnings("unchecked")
    private void updateViewer() {
        List<Swimlane> swimlanes = getDefinition().getSwimlanes();
        tableViewer.setInput(swimlanes);
        for (Swimlane swimlane : swimlanes) {
            swimlane.addPropertyChangeListener(this);
        }
        updateUI();
    }

    private boolean isGlobalSection() {
        return GlobalSectionUtils.isGlobalSectionName(getDefinition().getName());
    }

    private boolean isUsingGlobals() {
        return getDefinition().isUsingGlobalVars();
    }

    private void delete(Swimlane swimlane) throws Exception {
        List<String> confirmationInfo = new ArrayList<>();
        List<FormNode> formNodes = new ArrayList<>();
        VariableSearchQuery query = new VariableSearchQuery(editor.getDefinitionFile(), getDefinition(), swimlane);
        NewSearchUI.runQueryInForeground(PlatformUI.getWorkbench().getActiveWorkbenchWindow(), query);
        SearchResult searchResult = query.getSearchResult();
        if (searchResult.getMatchCount() > 0) {
            confirmationInfo.add(Localization.getString("UsagesFoundFor", swimlane.getName()) + ":\n");
            for (Object object : searchResult.getElements()) {
                ElementMatch elementMatch = (ElementMatch) object;
                confirmationInfo.add(" * " + elementMatch.toString(searchResult));
                if (elementMatch.getGraphElement() instanceof FormNode) {
                    formNodes.add((FormNode) elementMatch.getGraphElement());
                }
            }
        }
        if (!confirmationInfo.isEmpty()
                && !Dialogs.confirm(Localization.getString("deletion.allEditorsWillBeSaved") + "\n\n" + Localization.getString("confirm.delete"),
                        Joiner.on("\n").join(confirmationInfo))) {
            return;
        }
        ProcessDefinition mainProcessDefinition = getDefinition().getMainProcessDefinition();
        for (SwimlanedNode node : mainProcessDefinition.getChildren(SwimlanedNode.class)) {
            if (swimlane.getName().equals(node.getSwimlaneName())) {
                node.setSwimlane(null);
            }
        }
        for (SubprocessDefinition subprocessDefinition : mainProcessDefinition.getEmbeddedSubprocesses().values()) {
            for (SwimlanedNode node : subprocessDefinition.getChildren(SwimlanedNode.class)) {
                if (swimlane.getName().equals(node.getSwimlaneName())) {
                    node.setSwimlane(null);
                }
            }
        }
        ParContentProvider.rewriteFormValidationsRemoveVariable(formNodes, swimlane.getName());
        getDefinition().removeChild(swimlane);
        IResource projectRoot = editor.getDefinitionFile().getParent();
        IDE.saveAllEditors(new IResource[] { projectRoot }, false);
    }

    private class MoveSwimlaneSelectionListener extends SelectionAdapter {
        private final boolean up;

        public MoveSwimlaneSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            List<Swimlane> children = swimlane.getParent().getChildren(Swimlane.class);
            int index = children.indexOf(swimlane);
            swimlane.getParent().swapChildren(swimlane, up ? children.get(index - 1) : children.get(index + 1));
            tableViewer.setSelection(selection);
        }
    }

    private class SearchSwimlaneUsageSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            VariableSearchQuery query = new VariableSearchQuery(editor.getDefinitionFile(), getDefinition(), swimlane);
            NewSearchUI.runQueryInBackground(query);
        }
    }

    private class ImportGlobalSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            ChooseGlbSwimlaneWizard wizard = new ChooseGlbSwimlaneWizard(getDefinition(), selection);
            CompactWizardDialog dialog = new CompactWizardDialog(wizard);
            if (dialog.open() != Window.OK) {
                return;
            }
            updateViewer();
        }
    }

    private class MakeLocalListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            getDefinition().getSwimlaneByName(swimlane.getName()).setGlobal(false);
            getDefinition().setDirty();
            updateViewer();
        }
    }

    private class RenameSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            UpdateSwimlaneNameDialog renameDialog = new UpdateSwimlaneNameDialog(swimlane.getProcessDefinition(), swimlane);
            if (renameDialog.open() != IDialogConstants.OK_ID) {
                return;
            }
            IResource projectRoot = editor.getDefinitionFile().getParent();
            IDE.saveAllEditors(new IResource[] { projectRoot }, false); // https://rm.processtech.ru/issues/1825#note-196
            String newName = renameDialog.getName();
            String oldName = swimlane.getName();
            String newScriptingName = renameDialog.getScriptingName();
            renameSwimlane(oldName, newName, newScriptingName, editor.getDefinitionFile(), editor.getDefinition(), swimlane);
            if (isGlobalSection()) {
                oldName = IOUtils.GLOBAL_OBJECT_PREFIX + oldName;
                newName = IOUtils.GLOBAL_OBJECT_PREFIX + newName;
                newScriptingName = IOUtils.GLOBAL_OBJECT_PREFIX + newScriptingName;
                Map<IFile, ProcessDefinition> pf = ProcessCache.getAllProcessDefinitionsMap();
                for (IFile file : pf.keySet()) {
                    ProcessDefinition definition = pf.get(file);
                    if (!(definition instanceof GlobalSectionDefinition)) {
                        Swimlane sw = definition.getGlobalSwimlaneByName(oldName);
                        if (sw != null) {
                            renameSwimlane(oldName, newName, newScriptingName, file, definition, sw);
                            ProcessCache.invalidateProcessDefinition(file);
                        }
                    }
                }
            }
        }

        private void renameSwimlane(String oldName, String newName, String newScriptingName, IFile definitionFile, ProcessDefinition definition,
                Swimlane swimlane) throws Exception {
            RenameVariableRefactoring ref = new RenameVariableRefactoring(definitionFile, definition, swimlane, newName, newScriptingName);
            if (ref.isUserInteractionNeeded()) {
                RenameRefactoringWizard wizard = new RenameRefactoringWizard(ref);
                wizard.setDefaultPageTitle(Localization.getString("Refactoring.variable.name"));
                RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
                int result = op.run(Display.getCurrent().getActiveShell(), "");
                if (result != IDialogConstants.OK_ID) {
                    return;
                }
                if (definition.getEmbeddedSubprocesses().size() > 0) {
                    for (SubprocessDefinition subprocessDefinition : definition.getEmbeddedSubprocesses().values()) {
                        WorkspaceOperations.saveProcessDefinition(subprocessDefinition);
                    }
                }
            }
            swimlane.setName(newName);
            swimlane.setScriptingName(newScriptingName);
        }
    }

    private class RemoveSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @SuppressWarnings("unchecked")
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            final IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            final List<Swimlane> swimlanes = selection.toList();
            final ProcessDefinition definition = getDefinition();
            for (Swimlane swimlane : swimlanes) {
                definition.removeGlobalSwimlaneInAllProcesses(swimlane, definition.getFile().getParent().getParent());
                if (!isGlobalSection()) {
                    delete(swimlane);
                }
            }
        }
    }

    private class CreateSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            UpdateSwimlaneNameDialog dialog = new UpdateSwimlaneNameDialog(getDefinition(), null);
            if (dialog.open() == IDialogConstants.OK_ID) {
                Swimlane newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(getDefinition(), false);
                newSwimlane.setName(dialog.getName());
                newSwimlane.setScriptingName(dialog.getScriptingName());
                getDefinition().addChild(newSwimlane);
                tableViewer.setSelection(new StructuredSelection(newSwimlane));
            }
        }
    }

    private class ChangeSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Swimlane swimlane = (Swimlane) selection.getFirstElement();
            DelegableProvider provider = HandlerRegistry.getProvider(swimlane.getDelegationClassName());
            String configuration = provider.showConfigurationDialog(swimlane);
            if (configuration != null) {
                swimlane.setDelegationConfiguration(configuration);
                tableViewer.setSelection(selection);
                if (isGlobalSection()) {
                    final GlobalSectionDefinition definition = (GlobalSectionDefinition) getDefinition();
                    definition.updateGlobalSwimlaneInAllProcesses(swimlane, definition.getFile().getParent().getParent());
                }
            }
        }
    }

    private class CopySwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Clipboard.getDefault().setContents(selection.toList());
        }
    }

    private class PasteSwimlaneSelectionListener extends LoggingSelectionAdapter {
        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            @SuppressWarnings("unchecked")
            List<Swimlane> newSwimlanes = (List<Swimlane>) Clipboard.getDefault().getContents();
            for (Swimlane swimlane : newSwimlanes) {
                boolean add = false;
                Swimlane newSwimlane = getDefinition().getSwimlaneByName(swimlane.getName());
                if (newSwimlane == null) {
                    newSwimlane = NodeRegistry.getNodeTypeDefinition(Swimlane.class).createElement(getDefinition(), false);
                    newSwimlane.setName(swimlane.getName());
                    newSwimlane.setScriptingName(swimlane.getScriptingName());
                    if (newSwimlane.getName() == null) {
                        // variable of that name already exists
                        continue;
                    }
                    add = true;
                }
                newSwimlane.setDelegationClassName(swimlane.getDelegationClassName());
                newSwimlane.setDelegationConfiguration(swimlane.getDelegationConfiguration());
                if (add) {
                    getDefinition().addChild(newSwimlane);
                }
            }
        }
    }

    private static class TableViewerLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            Swimlane swimlane = (Swimlane) element;
            if (index == 0) {
                return swimlane.getName();
            } else if (index == 1) {
                return swimlane.getDelegationConfiguration();
            } else {
                return "unknown " + index;
            }
        }

        @Override
        public String getText(Object element) {
            Swimlane swimlane = (Swimlane) element;
            return swimlane.getName();
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
}
