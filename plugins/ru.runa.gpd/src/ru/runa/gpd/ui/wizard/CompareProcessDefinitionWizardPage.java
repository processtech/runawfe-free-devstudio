package ru.runa.gpd.ui.wizard;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
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

        Group numContextLinesGroup = new Group(pageControl, SWT.NONE);
        numContextLinesGroup.setLayout(new GridLayout(2, false));
        numContextLinesGroup.setLayoutData(new GridData(SWT.BEGINNING));
        Font font = parent.getFont();

        Label destinationLabel = new Label(numContextLinesGroup, SWT.NONE);
        destinationLabel.setText(Localization.getString("CompareProcessDefinitionWizardPage.page.numContextLines"));
        destinationLabel.setFont(font);

        numContextLinesSpinner = new Spinner(numContextLinesGroup, SWT.BORDER);
        numContextLinesSpinner.setFont(font);
        numContextLinesSpinner.setMinimum(1);
        numContextLinesSpinner.setSelection(3);
        numContextLinesSpinner.setIncrement(1);
        setControl(pageControl);
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

                Patch<String> diff = DiffUtils.diff(content1StringList, content2StringList);
                if (diff.getDeltas().isEmpty()) {
                    continue;
                }
                List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(firstProcessPath + "/" + fileName,
                        secondProcessPath + "/" + fileName, //
                        content1StringList, diff, numContextLinesSpinner.getSelection());
                result += String.join("\n", unifiedDiff) + "\n";
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
