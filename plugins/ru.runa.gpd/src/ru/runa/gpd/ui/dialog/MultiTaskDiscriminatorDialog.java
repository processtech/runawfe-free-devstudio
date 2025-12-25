package ru.runa.gpd.ui.dialog;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.custom.DragAndDropAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.custom.TableViewerLocalDragAndDropSupport;
import ru.runa.gpd.util.MultiinstanceParameters;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.collect.Lists;

public class MultiTaskDiscriminatorDialog extends Dialog {
    private final MultiTaskState state;
    private final MultiinstanceParameters parameters;
    private final List<VariableMapping> variableMappings;
    private VariablesComposite variablesComposite;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button changeButton;
    private Button deleteButton;

    public MultiTaskDiscriminatorDialog(MultiTaskState state) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.state = state;
        this.parameters = state.getMultiinstanceParameters();
        this.variableMappings = Lists.newArrayListWithExpectedSize(state.getVariableMappings().size());
        for (VariableMapping variableMapping : state.getVariableMappings()) {
            this.variableMappings.add(variableMapping.getCopy());
        }
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setSize(600, 600);
        super.configureShell(newShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));
        
        SashForm sf = new SashForm(composite, SWT.VERTICAL | SWT.SMOOTH);
        sf.setLayout(new GridLayout());
        sf.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        Group group = new Group(sf, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setText(Localization.getString("Feature.Multiinstance"));
        MultiinstanceComposite multiinstanceComposite = new MultiinstanceComposite(group, state, parameters);
        multiinstanceComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Group mappingsGroup = new Group(sf, SWT.NONE);
        mappingsGroup.setLayout(new GridLayout());
        mappingsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        mappingsGroup.setText(Localization.getString("Subprocess.VariableMappings"));

        sf.setWeights(new int[] {60, 40});

        variablesComposite = new VariablesComposite(mappingsGroup);
        variablesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    public MultiinstanceParameters getParameters() {
        return parameters;
    }

    public String getExecutorsDiscriminatorUsage() {
        return parameters.getDiscriminatorMapping().getUsage();
    }

    public String getExecutorsDiscriminatorValue() {
        return parameters.getDiscriminatorMapping().getName();
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    private List<String> getProcessVariablesNames(ProcessDefinition processDefinition) {
        List<String> variables = Lists.newArrayList();
        List<Variable> vars = processDefinition.getVariables(false, false);
        for (Variable variable : vars) {
            if (variable.getJavaClassName().equals(List.class.getName())) {
                variables.add(variable.getName());
            }
        }
        return variables;
    }

    private class VariablesComposite extends Composite {
        private final TableViewer tableViewer;

        public VariablesComposite(Composite parent) {
            super(parent, SWT.BORDER);
            setLayout(new GridLayout(2, false));

            tableViewer = new TableViewer(this, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION);
            tableViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
            Table table = tableViewer.getTable();
            table.setHeaderVisible(true);
            table.setLinesVisible(true);
            String[] columnNames = new String[] { "Subprocess.ProcessVariableName", "MultiTask.FormVariableName" };
            int[] columnWidths = new int[] { 300, 300 };
            int[] columnAlignments = new int[] { SWT.LEFT, SWT.LEFT };
            for (int i = 0; i < columnNames.length; i++) {
                TableColumn tableColumn = new TableColumn(table, columnAlignments[i]);
                tableColumn.setText(Localization.getString(columnNames[i]));
                tableColumn.setToolTipText(Localization.getString(columnNames[i] + ".description"));
                tableColumn.setWidth(columnWidths[i]);
            }
            tableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
            tableViewer.setContentProvider(new ArrayContentProvider());
            setTableInput();

            Composite buttonsComposite = new Composite(this, SWT.NONE);
            buttonsComposite.setLayout(new GridLayout());
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.LEFT;
            gridData.verticalAlignment = SWT.TOP;
            buttonsComposite.setLayoutData(gridData);
            SwtUtils.createButtonFillHorizontal(buttonsComposite, Localization.getString("button.add"), new LoggingSelectionAdapter() {

                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    editVariableMapping(null);
                }
            });
            changeButton = SwtUtils.createButtonFillHorizontal(buttonsComposite, Localization.getString("button.change"),
                    new LoggingSelectionAdapter() {

                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                            if (!selection.isEmpty()) {
                                VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                                editVariableMapping(oldMapping);
                            }
                        }
                    });
            moveUpButton = SwtUtils.createButtonFillHorizontal(buttonsComposite, Localization.getString("button.up"),
                    new MoveVariableSelectionListener(true));
            moveDownButton = SwtUtils.createButtonFillHorizontal(buttonsComposite, Localization.getString("button.down"),
                    new MoveVariableSelectionListener(false));
            deleteButton = SwtUtils.createButtonFillHorizontal(buttonsComposite, Localization.getString("button.delete"),
                    new LoggingSelectionAdapter() {

                        @Override
                        protected void onSelection(SelectionEvent e) throws Exception {
                            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                            if (!selection.isEmpty() && Dialogs.confirm(Localization.getString("confirm.delete"))) {
                                for (VariableMapping mapping : (List<VariableMapping>) selection.toList()) {
                                    variableMappings.remove(mapping);
                                }
                                tableViewer.refresh();
                                setTableInput();
                            }
                        }
                    });
            tableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
                @Override
                protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                    updateButtons();
                }
            });
            updateButtons();
            TableViewerLocalDragAndDropSupport.enable(tableViewer, new DragAndDropAdapter<VariableMapping>() {

                @Override
                public void onDropElement(VariableMapping beforeElement, VariableMapping element) {
                    if (variableMappings.remove(element)) {
                        variableMappings.add(variableMappings.indexOf(beforeElement), element);
                    }
                }

                @Override
                public void onDrop(VariableMapping beforeElement, List<VariableMapping> elements) {
                    super.onDrop(beforeElement, elements);
                    setTableInput();
                }

            });
        }

        private void updateButtons() {
            List<?> selected = ((IStructuredSelection) tableViewer.getSelection()).toList();
            changeButton.setEnabled(selected.size() == 1);
            moveUpButton.setEnabled(selected.size() == 1 && variableMappings.indexOf(selected.get(0)) > 0);
            moveDownButton.setEnabled(selected.size() == 1 && variableMappings.indexOf(selected.get(0)) < variableMappings.size() - 1);
            deleteButton.setEnabled(selected.size() > 0);
        }

        private void editVariableMapping(VariableMapping mapping) {
        	 ProcessDefinition processDefinition = state.getProcessDefinition();
        	 List<String> processVariables = getProcessVariablesNames(processDefinition);
        	    List<Variable> allVariables = processDefinition.getVariables(true, true); 
        	    
            MultiTaskVariableDialog dialog = new MultiTaskVariableDialog(processVariables, allVariables, mapping);
            if (dialog.open() != IDialogConstants.CANCEL_ID) {
                if (mapping == null) {
                    mapping = new VariableMapping();
                    variableMappings.add(mapping);
                    setTableInput();
                }
                mapping.setName(dialog.getProcessVariable());
                mapping.setMappedName(dialog.getFormVariable());
                String usage = dialog.getAccess();
                mapping.setUsage(usage);
                tableViewer.refresh();
            }
        }

        private void setTableInput() {
            tableViewer.setInput(getVariableMappings());
        }

        private class MoveVariableSelectionListener extends LoggingSelectionAdapter {
            private final boolean up;

            public MoveVariableSelectionListener(boolean up) {
                this.up = up;
            }

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
                VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                int index = variableMappings.indexOf(mapping);
                Collections.swap(variableMappings, index, up ? (index - 1) : (index + 1));
                setTableInput();
                tableViewer.setSelection(selection);
            }
        }
    }

    private static class VariableMappingTableLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public String getColumnText(Object element, int index) {
            VariableMapping mapping = (VariableMapping) element;
            switch (index) {
            case 0:
                return mapping.getName();
            case 1:
                return mapping.getMappedName();
            default:
                return "unknown " + index;
            }
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }
}
