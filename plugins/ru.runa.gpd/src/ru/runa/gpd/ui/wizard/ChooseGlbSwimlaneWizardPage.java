package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.util.IOUtils;

public class ChooseGlbSwimlaneWizardPage extends ContentWizardPage {
    private ProcessDefinition definition;
    private TableViewer globalSwimlanesViewer;
    private IContainer initialSelection;

    public ChooseGlbSwimlaneWizardPage(ProcessDefinition definition, IContainer initialSelection) {
        this.definition = definition;
        this.initialSelection = initialSelection;
        setTitle(Localization.getString("ChooseGlbSwimlaneWizardPage.title"));
        setDescription(Localization.getString("ChooseGlbSwimlaneWizardPage.description"));
        IOUtils.getAllProcessContainers();
    }

    @Override
    protected void createContent(Composite composite) {
        initializeDialogUnits(composite);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;

        composite.setLayoutData(gridData);
        createViewer(composite);

        setPageComplete(false);
        setErrorMessage(null); // should not initially have error message
        setControl(composite);

    }

    public void createViewer(Composite parent) {
        globalSwimlanesViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        parent.setSize(800, 800);

        globalSwimlanesViewer.setContentProvider(new ArrayContentProvider());
        globalSwimlanesViewer.setLabelProvider(new GlobalLabelProvider());
        Table table = globalSwimlanesViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayout(new GridLayout());

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.heightHint = 400;
        table.setLayoutData(gridData);

        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(Localization.getString("DesignerSwimlaneEditorPage.label.swimlanes"));
        tableColumn.setWidth(700);

        ArrayList<Swimlane> swimlanesToAdd = getGlobalSwimlanesToAdd(getGlobalSwimlanesFromProject(initialSelection, new ArrayList<Swimlane>()));
        globalSwimlanesViewer.setInput(swimlanesToAdd);
        globalSwimlanesViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                IStructuredSelection selections = globalSwimlanesViewer.getStructuredSelection();
            }
        });

    }

    public static ArrayList<Swimlane> getGlobalSwimlanesFromProject(IContainer initialSelection, ArrayList<Swimlane> swimlanes) {

        if (initialSelection == null) {
            return swimlanes;
        }

        try {
            for (IResource r : initialSelection.members()) {
                if (!GlobalSectionUtils.isGlobalSectionResource(r) || r.getType() != IResource.FOLDER) {
                    continue;
                }

                IFile definitionFile = (IFile) ((IFolder) r).findMember(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                if (definitionFile == null) {
                    continue;
                }
                ArrayList<Swimlane> processSwimlanes = (ArrayList<Swimlane>) ProcessCache.getProcessDefinition(definitionFile)
                        .getChildren(Swimlane.class);

                for (Swimlane swimlane : processSwimlanes) {
                    if (swimlanes.stream().noneMatch(s -> s.getName().equals(IOUtils.GLOBAL_OBJECT_PREFIX + swimlane.getName()))) {

                        Swimlane copy = new Swimlane();
                        copy.setName(IOUtils.GLOBAL_OBJECT_PREFIX + swimlane.getName());
                        copy.setScriptingName(IOUtils.GLOBAL_OBJECT_PREFIX + swimlane.getScriptingName());
                        copy.setDescription(swimlane.getDescription());
                        copy.setDefaultValue(swimlane.getDefaultValue());
                        copy.setFormat(swimlane.getFormat());
                        copy.setDelegationClassName(swimlane.getDelegationClassName());
                        copy.setDelegationConfiguration(swimlane.getDelegationConfiguration());
                        copy.setPublicVisibility(swimlane.isPublicVisibility());
                        copy.setStoreType(swimlane.getStoreType());
                        copy.setGlobal(true);
                        if (swimlanes != null) {
                            swimlanes.add(copy);
                        }
                    }
                }

            }
        } catch (CoreException e) {
            return swimlanes;
        }
        return getGlobalSwimlanesFromProject(initialSelection.getParent(), swimlanes);
    }

    @Override
    protected void verifyContentIsValid() {

    }

    private ArrayList<Swimlane> getGlobalSwimlanesToAdd(ArrayList<Swimlane> projectGlobalSwimlanes) {
        ArrayList<Swimlane> swimlanesToShow = new ArrayList<Swimlane>();
        if (projectGlobalSwimlanes == null || projectGlobalSwimlanes.isEmpty()) {
            return swimlanesToShow;
        }
        List<Swimlane> processGlobalSwimlanes = definition.getGlobalSwimlanes();

        if (processGlobalSwimlanes.isEmpty()) {
            return projectGlobalSwimlanes;
        }
        for (Swimlane swimlane : projectGlobalSwimlanes) {
            if (processGlobalSwimlanes.stream().noneMatch(p -> p.getName().equals(swimlane.getName()))) {
                swimlanesToShow.add(swimlane);
            }
        }
        return swimlanesToShow;
    }

    public boolean finish() {
        final List<Swimlane> swimlanes = ((IStructuredSelection) globalSwimlanesViewer.getSelection()).toList();
        for (Swimlane swimlane : swimlanes) {
            definition.addGlobalSwimlane(swimlane);
        }
        definition.setDirty();
        return true;
    }

    private static class GlobalLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            Swimlane swimlaneRow = (Swimlane) element;
            switch (columnIndex) {

            case 0:
                return swimlaneRow.getName();

            case 1:
                return swimlaneRow.getScriptingName();
            default:
                return null;
            }
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            return null;
        }
    }

}
