package ru.runa.gpd.ui.wizard;

import com.cloudbees.diff.Diff;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.util.IOUtils;

public class CompareProcessDefinitionWizardPage extends WizardPage {
    private final Map<String, IFile> definitionNameFileMap;
    private final String initialSelection;
    private ListViewer firstListViewer;
    private ListViewer secondListViewer;
    private Spinner numContextLinesSpinner;
    private Button ignoreSpacesCheckbox;
    private Text filterText;
    private ISelection firstSelection = null;
    private ISelection secondSelection = null;

    private static final Set<String> EXTENSIONS = Sets.newHashSet("xml", "ftl", "quick", "html", "css", "js");

    public CompareProcessDefinitionWizardPage(IStructuredSelection selection) {
        super(CompareProcessDefinitionWizardPage.class.getSimpleName());
        setTitle(Localization.getString("CompareProcessDefinitionWizardPage.page.title"));
        this.definitionNameFileMap = new TreeMap<String, IFile>();
        for (IFile file : ProcessCache.getAllProcessDefinitionsMap().keySet()) {
            ProcessDefinition definition = ProcessCache.getProcessDefinition(file);
            if (definition != null && !(definition instanceof SubprocessDefinition)) {
                definitionNameFileMap.put(getKey(file, definition), file);
            }
        }
        this.initialSelection = getInitialSelection(selection);
    }

    private String getInitialSelection(IStructuredSelection selection) {
        if (selection != null && !selection.isEmpty()) {
            Object selectedElement = selection.getFirstElement();
            if (selectedElement instanceof IResource) {
                IResource folder = (IResource) selectedElement;
                String path = folder.getFullPath().toString();
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                return path;
            }
        }
        return null;
    }

    private String getKey(IFile definitionFile, ProcessDefinition definition) {
        IProject project = definitionFile.getProject();
        if (IOUtils.isProjectHasProcessNature(project)) {
            String path = definitionFile.getParent().getFullPath().toString();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            return path;
        } else {
            return project.getName() + "/" + definition.getName();
        }
    }

    private void createViewer(ListViewer listViewer) {
        listViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        listViewer.setContentProvider(new ArrayContentProvider());
        listViewer.setInput(definitionNameFileMap.keySet());
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                setPageComplete(!event.getSelection().isEmpty());
            }
        });
        listViewer.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                String elementText = element.toString();
                return elementText.toLowerCase().contains(filterText.getText().toLowerCase());
            }
        });
    }

    public boolean isValid() {
        Object firstElement = ((StructuredSelection) firstListViewer.getSelection()).getFirstElement();
        Object secondElement = ((StructuredSelection) secondListViewer.getSelection()).getFirstElement();
        return firstElement != null && secondElement != null && !Objects.equal(firstElement, secondElement);
    }

    @Override
    public void createControl(Composite parent) {
        Composite pageControl = new Composite(parent, SWT.NONE);
        pageControl.setLayout(new GridLayout(1, false));
        pageControl.setLayoutData(new GridData(GridData.FILL_BOTH));

        filterText = new Text(pageControl, SWT.BORDER);
        filterText.setMessage(Localization.getString("filter"));
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filterText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!firstListViewer.getSelection().equals(StructuredSelection.EMPTY)) {
                    firstSelection = firstListViewer.getSelection();
                }
                firstListViewer.refresh();
                firstListViewer.setSelection(firstSelection);
                
                if (!secondListViewer.getSelection().equals(StructuredSelection.EMPTY)) {
                    secondSelection = secondListViewer.getSelection();
                }
                secondListViewer.refresh();
                secondListViewer.setSelection(secondSelection);
            }
        });

        SashForm sashForm = new SashForm(pageControl, SWT.HORIZONTAL);
        sashForm.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group firstProcessListGroup = new Group(sashForm, SWT.NONE);
        firstProcessListGroup.setLayout(new GridLayout(1, false));
        firstProcessListGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        firstProcessListGroup.setText(Localization.getString("CompareProcessDefinitionWizardPage.page.firstProcess"));
        firstListViewer = new ListViewer(firstProcessListGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        createViewer(firstListViewer);
        if (initialSelection != null) {
            firstListViewer.setSelection(new StructuredSelection(initialSelection));
        }

        Group secondProcessViewerGroup = new Group(sashForm, SWT.NONE);
        secondProcessViewerGroup.setLayout(new GridLayout(1, false));
        secondProcessViewerGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        secondProcessViewerGroup.setText(Localization.getString("CompareProcessDefinitionWizardPage.page.secondProcess"));
        secondListViewer = new ListViewer(secondProcessViewerGroup, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        createViewer(secondListViewer);

        Group settingsGroup = new Group(pageControl, SWT.NONE);
        settingsGroup.setLayout(new GridLayout(2, false));

        Font font = parent.getFont();
        Label destinationLabel = new Label(settingsGroup, SWT.NONE);
        destinationLabel.setText(Localization.getString("CompareProcessDefinitionWizardPage.page.numContextLines"));
        destinationLabel.setFont(font);

        numContextLinesSpinner = new Spinner(settingsGroup, SWT.BORDER);
        numContextLinesSpinner.setFont(font);
        numContextLinesSpinner.setMinimum(1);
        numContextLinesSpinner.setSelection(3);
        numContextLinesSpinner.setIncrement(1);
        setControl(pageControl);

        Label ignoreSpacesLabel = new Label(settingsGroup, SWT.NONE);
        ignoreSpacesLabel.setText(Localization.getString("CompareProcessDefinitionWizardPage.page.ignoreSpaces"));
        ignoreSpacesLabel.setFont(font);

        ignoreSpacesCheckbox = new Button(settingsGroup, SWT.CHECK);
        ignoreSpacesCheckbox.setSelection(true);
    }

    public boolean performFinish() {
        String firstProcessPath = (String) ((StructuredSelection) firstListViewer.getSelection()).getFirstElement();
        String secondProcessPath = (String) ((StructuredSelection) secondListViewer.getSelection()).getFirstElement();
        IContainer firstProcessFolder = ProcessCache.getProcessDefinition(definitionNameFileMap.get(firstProcessPath)).getFile().getParent();
        IContainer secondProcessFolder = ProcessCache.getProcessDefinition(definitionNameFileMap.get(secondProcessPath)).getFile().getParent();
        String result = "";
        try {
            Set<String> unsortedFileNames = new HashSet<>();

            Map<String, IFile> firstProcessFileMap = Stream.of(firstProcessFolder.members())
                    .filter(resource -> resource instanceof IFile && EXTENSIONS.contains(((IFile) resource).getFileExtension()))
                    .collect(Collectors.toMap(resource -> ((IFile) resource).getName(), resource -> (IFile) resource));
            Map<String, IFile> secondProcessFileMap = Stream.of(secondProcessFolder.members())
                    .filter(resource -> resource instanceof IFile && EXTENSIONS.contains(((IFile) resource).getFileExtension()))
                    .collect(Collectors.toMap(resource -> ((IFile) resource).getName(), resource -> (IFile) resource));
            unsortedFileNames.addAll(firstProcessFileMap.keySet());
            unsortedFileNames.addAll(secondProcessFileMap.keySet());
            List<String> sortedFileNames = new ArrayList<>(unsortedFileNames);
            Collections.sort(sortedFileNames);

            for (String fileName : sortedFileNames) {

                List<String> content1StringList = new ArrayList<>(), content2StringList = new ArrayList<>();

                if (firstProcessFileMap.get(fileName) != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(firstProcessFileMap.get(fileName).getContents()))) {
                        content1StringList = br.lines().collect(Collectors.toList());
                    } catch (IOException | CoreException e) {
                        throw Throwables.propagate(e);
                    }
                }

                if (secondProcessFileMap.get(fileName) != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(secondProcessFileMap.get(fileName).getContents()))) {
                        content2StringList = br.lines().collect(Collectors.toList());
                    } catch (IOException | CoreException e) {
                        throw Throwables.propagate(e);
                    }
                }

                Diff diff = Diff.diff(content1StringList, content2StringList, ignoreSpacesCheckbox.getSelection());
                if (diff.isEmpty()) {
                    continue;
                }
                String unifiedDiff = diff.toUnifiedDiff(firstProcessPath + "/" + fileName, secondProcessPath + "/" + fileName, //
                        new StringReader(String.join("\n", content1StringList)), new StringReader(String.join("\n", content2StringList)),
                        numContextLinesSpinner.getSelection());
                result += unifiedDiff + "\n";
            }

            IStorage storage = new DiffStorage(result,
                    Localization.getString("CompareProcessDefinitionWizardPage.page.editor.title", firstProcessPath, secondProcessPath));
            IStorageEditorInput input = new DiffInput(storage);
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            IWorkbenchPage page = window.getActivePage();
            if (page != null)
                page.openEditor(input, ru.runa.gpd.editors.DiffEditor.ID);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }

        return true;
    }

    class DiffStorage implements IStorage {
        private String string, name;

        DiffStorage(String input, String name) {
            this.string = input;
            this.name = name;
        }

        public InputStream getContents() throws CoreException {
            return new ByteArrayInputStream(string.getBytes());
        }

        public IPath getFullPath() {
            return null;
        }

        public Object getAdapter(Class adapter) {
            return null;
        }

        public String getName() {
            return name;
        }

        public boolean isReadOnly() {
            return true;
        }
    }

    public class DiffInput implements IStorageEditorInput {
        private IStorage storage;

        private DiffInput(IStorage storage) {
            this.storage = storage;
        }

        public boolean exists() {
            return true;
        }

        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        public String getName() {
            return storage.getName();
        }

        public IPersistableElement getPersistable() {
            return null;
        }

        public IStorage getStorage() {
            return storage;
        }

        public String getToolTipText() {
            return Localization.getString("CompareProcessDefinitionWizardPage.page.editor.title.toolTip", storage.getName());
        }

        public Object getAdapter(Class adapter) {
            return null;
        }
    }

}
