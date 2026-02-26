package ru.runa.gpd.office.excel;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.office.Messages;
import ru.runa.gpd.office.excel.ConstraintsModel.ColumnMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.var.format.ListFormat;

public class ExcelColumnMappingDialog extends Dialog {

    private final Delegable delegable;
    private final ConstraintsModel cmodel;
    private TableViewer viewer;
    private List<String> availableAttributes;

    public ExcelColumnMappingDialog(Shell parentShell, Delegable delegable, ConstraintsModel cmodel) {
        super(parentShell);
        this.delegable = delegable;
        this.cmodel = cmodel;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("ExcelColumnMappingDialog.Title"));
        newShell.setSize(550, 450);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(1, false));

        createHeaderLabel(area);
        createTableViewer(area);

        initializeAttributes();

        return area;
    }

    private void createHeaderLabel(Composite parent) {
        Label infoLabel = new Label(parent, SWT.WRAP);
        infoLabel.setText(Messages.getString("label.UdtFieldInfo"));
        infoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    }

    private void createTableViewer(Composite parent) {
        createAndConfigureTable(parent);
        createFieldColumn();
        createExcelColumn();
    }

    private void createAndConfigureTable(Composite parent) {
        viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        viewer.setContentProvider(ArrayContentProvider.getInstance());
    }

    private void createFieldColumn() {
        TableViewerColumn fieldColumn = new TableViewerColumn(viewer, SWT.NONE);
        fieldColumn.getColumn().setText(Messages.getString("label.UdtField"));
        fieldColumn.getColumn().setWidth(350);
        fieldColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return (String) element;
            }
        });
    }

    private void createExcelColumn() {
        TableViewerColumn columnColumn = new TableViewerColumn(viewer, SWT.NONE);
        String key;
        if (cmodel.type == ConstraintsModel.ROW) {
            key = "label.ExcelRow";
        } else {
            key = "label.ExcelColumn";
        }
        columnColumn.getColumn().setText(Messages.getString(key));
        columnColumn.getColumn().setWidth(100);
        columnColumn.setLabelProvider(createExcelLabelProvider());
        columnColumn.setEditingSupport(createExcelEditingSupport());
    }

    private ColumnLabelProvider createExcelLabelProvider() {
        return new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                String attributeName = (String) element;
                for (ColumnMapping mapping : cmodel.columns) {
                    if (mapping.attributeName.equals(attributeName)) {
                        return String.valueOf(mapping.column);
                    }
                }
                return "";
            }
        };
    }

    private EditingSupport createExcelEditingSupport() {
        return new EditingSupport(viewer) {
            @Override
            protected boolean canEdit(Object element) {
                return true;
            }

            @Override
            protected org.eclipse.jface.viewers.CellEditor getCellEditor(Object element) {
                return new TextCellEditor(viewer.getTable());
            }

            @Override
            protected Object getValue(Object element) {
                String attributeName = (String) element;
                for (ColumnMapping mapping : cmodel.columns) {
                    if (mapping.attributeName.equals(attributeName)) {
                        return String.valueOf(mapping.column);
                    }
                }
                return "";
            }

            @Override
            protected void setValue(Object element, Object value) throws NumberFormatException {
                String attributeName = (String) element;
                String strValue = (String) value;

                cmodel.columns.removeIf(m -> m.attributeName.equals(attributeName));

                if (strValue != null && !strValue.trim().isEmpty()) {
                    int col = Integer.parseInt(strValue.trim());
                    if (col > 0) {
                        cmodel.columns.add(new ColumnMapping(attributeName, col));
                    }
                }
                viewer.update(element, null);
            }
        };
    }

    private void initializeAttributes() {
        tryInitializeAttributesFromVariable();
        if (availableAttributes == null) {
            initializeDefaultAttributes();
        }
        viewer.setInput(availableAttributes);
    }

    private void tryInitializeAttributesFromVariable() {
        if (!isDelegableGraphElement()) {
            return;
        }

        Variable variable = findVariable();
        if (shouldUseVariableAttributes(variable)) {
            availableAttributes = extractAttributesFromVariable(variable);
            cleanUpUnusedColumns();
        }
    }

    private boolean isDelegableGraphElement() {
        return delegable instanceof GraphElement;
    }

    private Variable findVariable() {
        return VariableUtils.getVariableByName(((GraphElement) delegable).getProcessDefinition(), cmodel.variableName);
    }

    private boolean shouldUseVariableAttributes(Variable variable) {
        return variable != null && variable.getFormatClassName().equals(ListFormat.class.getName())
                && variable.getFormatComponentClassNames().length > 0 && variable.getFormatComponentClassNames()[0] != null;
    }

    private List<String> extractAttributesFromVariable(Variable variable) {
        String userTypeName = variable.getFormatComponentClassNames()[0];
        return new ArrayList<>(
                VariableUtils.getUserTypeExpandedAttributeNames(((GraphElement) delegable).getProcessDefinition().getVariableUserType(userTypeName)));
    }

    private void cleanUpUnusedColumns() {
        cmodel.columns.removeIf(m -> !availableAttributes.contains(m.attributeName));
    }

    private void initializeDefaultAttributes() {
        availableAttributes = new ArrayList<>();
    }
}