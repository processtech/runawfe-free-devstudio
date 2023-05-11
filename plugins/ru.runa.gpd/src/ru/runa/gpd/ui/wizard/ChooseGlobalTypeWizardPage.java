package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.google.common.base.Throwables;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.globalsection.GlobalSectionUtils;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.util.IOUtils;

public class ChooseGlobalTypeWizardPage extends ContentWizardPage {

    private TableViewer globalVariableTypeViewer;
    private ProcessDefinition definition;
    private IContainer initialSelection;
    private ArrayList<VariableUserType> globalTypes = new ArrayList<VariableUserType>();

    public ArrayList<VariableUserType> getGlobalTypes() {
        return globalTypes;
    }

    public ChooseGlobalTypeWizardPage(ProcessDefinition process, IContainer initialSelection) {
        setTitle(Localization.getString("ChooseTypesPage.page.title"));
        setDescription(Localization.getString("ChooseTypesPage.page.description"));
        this.initialSelection = initialSelection;
        IOUtils.getAllProcessContainers();
        this.definition = process;
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
        globalVariableTypeViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        parent.setSize(800, 800);

        globalVariableTypeViewer.setContentProvider(new ArrayContentProvider());
        globalVariableTypeViewer.setLabelProvider(new GlobalLabelProvider());
        Table table = globalVariableTypeViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayout(new GridLayout());

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.heightHint = 400;
        table.setLayoutData(gridData);

        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(Localization.getString("VariableUserType.collection"));
        tableColumn.setWidth(800);

        globalTypes = getGlobalTypes(initialSelection, globalTypes);
        globalVariableTypeViewer.setInput(checkOpenGlobalType(globalTypes));

    }

    public static ArrayList<VariableUserType> getGlobalTypes(IContainer initialSelection, ArrayList<VariableUserType> types) {
        IContainer parent = initialSelection;
        if (parent == null) {
            return types;
        }

        try {
            for (IResource r : parent.members()) {
                if (!GlobalSectionUtils.isGlobalSectionResource(r) || r.getType() != IResource.FOLDER) {
                    continue;
                }

                IFile definitionFile = (IFile) ((IFolder) r).findMember(ParContentProvider.PROCESS_DEFINITION_FILE_NAME);
                if (definitionFile == null) {
                    continue;
                }

                List<VariableUserType> globalTypes = ProcessCache.getProcessDefinition(definitionFile).getVariableUserTypes();
                for (VariableUserType type : globalTypes) {
                    if (types.stream().noneMatch(t -> t.getName().equals(IOUtils.GLOBAL_OBJECT_PREFIX + type.getName()))) {
                        VariableUserType copy = type.getCopy();
                        copy.setName(IOUtils.GLOBAL_OBJECT_PREFIX + copy.getName());
                        copy.setGlobal(true);
                        types.add(copy);
                    }
                }

            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
            return types;
        }
        return getGlobalTypes(parent.getParent(), types);
    }

    @Override
    protected void verifyContentIsValid() {
        // do nothing
    }

    private ArrayList<VariableUserType> checkOpenGlobalType(ArrayList<VariableUserType> AllGlobalType) {
        List<VariableUserType> processGlobalTypes = definition.getGlobalTypes();
        ArrayList<VariableUserType> visibleTypes = new ArrayList<VariableUserType>();
        if (AllGlobalType.size() == processGlobalTypes.size()) {
            return null;
        }
        if (processGlobalTypes.isEmpty()) {
            return AllGlobalType;
        }
        for (VariableUserType GlobalVariable : AllGlobalType) {
            if (processGlobalTypes.stream().noneMatch(p -> p.getName().equals(GlobalVariable.getName()))) {
                visibleTypes.add(GlobalVariable);
            }
        }
        return visibleTypes;
    }

    public boolean finish() {
        final IStructuredSelection selection = (IStructuredSelection) globalVariableTypeViewer.getSelection();
        if (selection.isEmpty()) {
            return false;
        }
        final List<VariableUserType> types = selection.toList();
        try {
            for (VariableUserType type : types) {
                if (definition.getVariableUserType(type.getName()) != null) {
                    throw new Exception(Localization.getString("ChooseGlobalType.error.typeIsAlreadyPresent" + type.getName()));
                }
            }
        } catch (Exception exception) {
            setErrorMessage(Throwables.getRootCause(exception).getMessage());
            return false;
        }
        for (VariableUserType type : types) {
            definition.addGlobalType(type);
        }
        return true;
    }

    private static class GlobalLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            VariableUserType VarUserTypeRow = (VariableUserType) element;
            switch (columnIndex) {
            case 0:
                return VarUserTypeRow.getName();
            case 1:
                return "";
            default:
                return null;
            }
        }

        @Override
        public Image getColumnImage(Object arg0, int arg1) {
            // do nothing
            return null;
        }
    }

}
