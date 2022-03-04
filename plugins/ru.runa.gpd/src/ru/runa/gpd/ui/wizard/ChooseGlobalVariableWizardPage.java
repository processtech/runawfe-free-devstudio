package ru.runa.gpd.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
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
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.ui.custom.ContentWizardPage;
import ru.runa.gpd.util.IOUtils;
import ru.runa.wfe.var.format.ListFormat;

public class ChooseGlobalVariableWizardPage extends ContentWizardPage {

    private TableViewer globalVariableViewer;
    private static ProcessDefinition definition;
    private IContainer initialSelection;
    private ArrayList<Variable> globalVariablesFromProject = new ArrayList<Variable>();

    public ArrayList<Variable> getGlobalVariablesFromProject() {
        return globalVariablesFromProject;
    }

    public ChooseGlobalVariableWizardPage(ProcessDefinition process, IContainer initialSelection) {
        setTitle(Localization.getString("ChooseGlobalVariableWizardPage.title"));
        setDescription(Localization.getString("ChooseGlobalVariableWizardPage.description"));
        this.initialSelection = initialSelection;
        IOUtils.getAllProcessContainers();
        ChooseGlobalVariableWizardPage.definition = process;
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
        ArrayList<Variable> globalVariablesToAdd = getGlobalVariablesToAdd(
                getGlobalVariablesFromProject(initialSelection, new ArrayList<Variable>()));
        parent.setSize(400, 400);

        globalVariableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        globalVariableViewer.setContentProvider(new ArrayContentProvider());
        globalVariableViewer.setLabelProvider(new GlobalLabelProvider());
        Table table = globalVariableViewer.getTable();
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        table.setLayout(new GridLayout());
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.heightHint = 400;
        table.setLayoutData(gridData);

        TableViewerColumn variableName = new TableViewerColumn(globalVariableViewer, SWT.NONE);
        variableName.getColumn().setWidth(400);
        variableName.getColumn().setText(Localization.getString("property.name"));
        variableName.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Variable v = (Variable) element;
                return v.getName();
            }
        });

        TableViewerColumn variableFormat = new TableViewerColumn(globalVariableViewer, SWT.NONE);
        variableFormat.getColumn().setWidth(400);
        variableFormat.getColumn().setText(Localization.getString("Variable.property.format"));
        variableFormat.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                Variable v = (Variable) element;
                return v.getFormatLabel();
            }
        });

        globalVariableViewer.setInput(globalVariablesToAdd);
        globalVariableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent arg0) {
                IStructuredSelection selections = globalVariableViewer.getStructuredSelection();
                Variable checkSwim = (Variable) selections.getFirstElement();
            }
        });
    }

    public static ArrayList<Variable> getGlobalVariablesFromProject(IContainer initialSelection, ArrayList<Variable> variables) {
        IContainer parent = initialSelection;
        if (parent == null) {
            return variables;
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
                ArrayList<Variable> globalVariables = (ArrayList<Variable>) ProcessCache.getProcessDefinition(definitionFile)
                        .getChildren(Variable.class, e -> !(e instanceof Swimlane));

                for (Variable variable : globalVariables) {

                    if (variables.stream().noneMatch(s -> s.getName().equals(IOUtils.GLOBAL_OBJECT_PREFIX + variable.getName()))) {
                        Variable copy = new Variable();
                        copy.setName(IOUtils.GLOBAL_OBJECT_PREFIX + variable.getName());
                        copy.setScriptingName(IOUtils.GLOBAL_OBJECT_PREFIX + variable.getScriptingName());
                        copy.setDescription(variable.getDescription());
                        copy.setDefaultValue(variable.getDefaultValue());
                        copy.setFormat(variable.getFormat());
                        copy.setDelegationClassName(variable.getDelegationClassName());
                        copy.setDelegationConfiguration(variable.getDelegationConfiguration());
                        copy.setPublicVisibility(variable.isPublicVisibility());
                        copy.setStoreType(variable.getStoreType());
                        String variableFormat = variable.getFormatClassName();
                        if (ListFormat.class.getName().equals(variableFormat)) {
                            String typeName = variable.getFormat();
                            int leng = typeName.length();
                            final String name = typeName.substring(1, leng - 1).substring((variableFormat.length()));
                            if (!ProcessCache.getGlobalProcessDefinition(ProcessCache.getProcessDefinition(definitionFile)).getVariableUserTypes()
                                    .stream().noneMatch(p -> p.getName().equals(name))) {
                                typeName = variable.getFormat().replace(name, IOUtils.GLOBAL_OBJECT_PREFIX + name);
                                copy.setFormat(typeName);
                            }
                        }
                        if (variable.getUserType() != null) {
                            definition.addGlobalType(variable.getUserType());
                            copy.setUserType(definition.getGlobalUserTypeByName(IOUtils.GLOBAL_OBJECT_PREFIX + variable.getUserType().getName()));
                        }
                        copy.setGlobal(true);
                        if (variables != null) {
                            variables.add(copy);
                        }
                    }
                }

            }
        } catch (CoreException e) {
            return variables;
        }

        return getGlobalVariablesFromProject(parent.getParent(), variables);
    }

    @Override
    protected void verifyContentIsValid() {
        // do nothing
    }

    private ArrayList<Variable> getGlobalVariablesToAdd(ArrayList<Variable> globalVariables) {
        List<Variable> processGlobalVariables = definition.getGlobalVariables();
        ArrayList<Variable> visibleVariables = new ArrayList<Variable>();
        if (processGlobalVariables.isEmpty()) {
            return globalVariables;
        }
        for (Variable globalVariable : globalVariables) {
            if (processGlobalVariables.stream().noneMatch(p -> p.getName().equals(globalVariable.getName()))) {
                visibleVariables.add(globalVariable);
            }
        }
        return visibleVariables;
    }

    public boolean finish() {
        final IStructuredSelection selection = (IStructuredSelection) globalVariableViewer.getSelection();
        final List<Variable> Variables = selection.toList();
        for (Variable variable : Variables) {
            definition.addGlobalVariable(variable);
        }
        return true;
    }

    private static class GlobalLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int columnIndex) {
            Variable swimRow = (Variable) element;
            switch (columnIndex) {
            case 0:
                return swimRow.getName();
            case 1:
                return swimRow.getScriptingName();
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
