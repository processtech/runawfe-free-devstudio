package ru.runa.gpd.ui.dialog;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.ui.custom.DragAndDropAdapter;
import ru.runa.gpd.ui.custom.DropDownButton;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionChangedAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.custom.TableViewerLocalDragAndDropSupport;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.format.VariableFormat;

public class MessageNodeDialog extends Dialog {
    /**
     * paste operation is supported in application instance scope only
     */
    private static boolean isPasteButtonEnabled;

    private final ProcessDefinition definition;
    private final List<VariableMapping> variableMappings;
    private final boolean sendMode;
    private final String title;
    private TableViewer selectorTableViewer;
    private TableViewer dataTableViewer;
    private Button selectorChangeButton;
    private Button selectorDeleteButton;
    private Button dataMoveUpButton;
    private Button dataMoveDownButton;
    private Button dataChangeButton;
    private Button dataDeleteButton;
    private Button copyButton;
    private Button pasteButton;

    public MessageNodeDialog(ProcessDefinition definition, List<VariableMapping> variableMappings, boolean sendMode, String title) {
        super(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        this.variableMappings = new ArrayList<>(variableMappings);
        this.definition = definition;
        this.sendMode = sendMode;
        this.title = title;
        setShellStyle(SWT.TITLE | SWT.RESIZE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout());
        Group routeGroup = new Group(composite, SWT.NONE);
        routeGroup.setLayout(new GridLayout(2, false));
        routeGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        routeGroup.setText(Localization.getString(sendMode ? "MessageNodeDialog.SelectorSend" : "MessageNodeDialog.SelectorReceive"));
        createSelectorTableViewer(routeGroup);
        addSelectorButtons(routeGroup);
        Group variablesGroup = new Group(composite, SWT.NONE);
        variablesGroup.setLayout(new GridLayout(2, false));
        variablesGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        variablesGroup.setText(Localization.getString("MessageNodeDialog.VariablesList"));
        createDataTableViewer(variablesGroup);
        addDataButtons(variablesGroup);
        return composite;
    }

    private void createSelectorTableViewer(Composite parent) {
        selectorTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 100;
        selectorTableViewer.getControl().setLayoutData(data);
        final Table table = selectorTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("property.name"), Localization.getString("property.value") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        selectorTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                }
            }
        });
        selectorTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        selectorTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_SELECTOR));
        setSelectorTableInput();
        TableViewerLocalDragAndDropSupport.enable(selectorTableViewer, new DragAndDropAdapter<VariableMapping>() {

            @Override
            public void onDropElement(VariableMapping beforeElement, VariableMapping element) {
                if (variableMappings.remove(element)) {
                    variableMappings.add(variableMappings.indexOf(beforeElement), element);
                }
            }

            @Override
            public void onDrop(VariableMapping beforeElement, List<VariableMapping> elements) {
                super.onDrop(beforeElement, elements);
                setSelectorTableInput();
            }

        });
    }

    private void addSelectorButtons(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        composite.setLayoutData(gridData);
        DropDownButton addButton = new DropDownButton(composite);
        addButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addButton.setAlignment(SWT.CENTER);
        addButton.setText(Localization.getString("button.add"));
        addButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                editVariableMapping(null, VariableMapping.USAGE_SELECTOR);
            }
        });
        addButton.addButton(Localization.getString("MessageNodeDialog.addByProcessId"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                VariableMapping mapping = new VariableMapping("processId", VariableUtils.CURRENT_PROCESS_ID, VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping, true);
                }
            }
        });
        addButton.addButton(Localization.getString("MessageNodeDialog.addByProcessName"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                VariableMapping mapping = new VariableMapping("processDefinitionName", VariableUtils.CURRENT_PROCESS_DEFINITION_NAME,
                        VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping, true);
                }
            }
        });
        addButton.addButton(Localization.getString("MessageNodeDialog.addByNodeName"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                VariableMapping mapping = new VariableMapping("processNodeName", VariableUtils.CURRENT_NODE_NAME, VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping, true);
                }
            }
        });
        addButton.addButton(Localization.getString("MessageNodeDialog.addByNodeId"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                VariableMapping mapping = new VariableMapping("processNodeId", VariableUtils.CURRENT_NODE_ID, VariableMapping.USAGE_SELECTOR);
                if (sendMode) {
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                } else {
                    addVariableMapping(mapping, true);
                }
            }
        });
        selectorChangeButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.change"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping mapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(mapping, VariableMapping.USAGE_SELECTOR);
                }
            }
        });
        selectorDeleteButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.delete"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) selectorTableViewer.getSelection();
                List<VariableMapping> mappings = selection.toList();
                for (VariableMapping mapping : mappings) {
                    variableMappings.remove(mapping);
                }
                selectorTableViewer.refresh();
            }
        });
        selectorTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                updateSelectorButtons();
            }
        });
        updateSelectorButtons();
    }

    private void createDataTableViewer(Composite parent) {
        dataTableViewer = new TableViewer(parent, SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.minimumHeight = 200;
        dataTableViewer.getControl().setLayoutData(data);
        final Table table = dataTableViewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        String[] columnNames = new String[] { Localization.getString("MessageNodeDialog.VariableName"),
                Localization.getString("MessageNodeDialog.Alias") };
        for (int i = 0; i < columnNames.length; i++) {
            TableColumn tableColumn = new TableColumn(table, SWT.LEFT);
            tableColumn.setText(columnNames[i]);
            tableColumn.setWidth(300);
        }
        dataTableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(oldMapping, VariableMapping.USAGE_READ);
                }
            }
        });
        dataTableViewer.setLabelProvider(new VariableMappingTableLabelProvider());
        dataTableViewer.setContentProvider(new UsageContentProvider(VariableMapping.USAGE_READ));
        setDataTableInput();
        TableViewerLocalDragAndDropSupport.enable(dataTableViewer, new DragAndDropAdapter<VariableMapping>() {

            @Override
            public void onDropElement(VariableMapping beforeElement, VariableMapping element) {
                if (variableMappings.remove(element)) {
                    variableMappings.add(variableMappings.indexOf(beforeElement), element);
                }
            }

            @Override
            public void onDrop(VariableMapping beforeElement, List<VariableMapping> elements) {
                super.onDrop(beforeElement, elements);
                setDataTableInput();
            }

        });
    }

    private void setSelectorTableInput() {
        selectorTableViewer.setInput(new Object());
    }

    private void setDataTableInput() {
        dataTableViewer.setInput(new Object());
    }

    private void addDataButtons(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.TOP;
        composite.setLayoutData(gridData);
        SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.add"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                editVariableMapping(null, VariableMapping.USAGE_READ);
            }
        });
        dataChangeButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.change"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                if (!selection.isEmpty()) {
                    VariableMapping oldMapping = (VariableMapping) selection.getFirstElement();
                    editVariableMapping(oldMapping, VariableMapping.USAGE_READ);
                }
            }
        });
        dataMoveUpButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.up"),
                new MoveVariableSelectionListener(true));
        dataMoveDownButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.down"), new MoveVariableSelectionListener(
                false));
        dataDeleteButton = SwtUtils.createButtonFillHorizontal(composite, Localization.getString("button.delete"), new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
                List<VariableMapping> mappings = selection.toList();
                for (VariableMapping mapping : mappings) {
                    variableMappings.remove(mapping);
                }
                dataTableViewer.refresh();
            }
        });
        dataTableViewer.addSelectionChangedListener(new LoggingSelectionChangedAdapter() {
            @Override
            protected void onSelectionChanged(SelectionChangedEvent event) throws Exception {
                updateDataButtons();
            }
        });
        updateDataButtons();
    }

    private void updateSelectorButtons() {
        List<?> selected = ((IStructuredSelection) selectorTableViewer.getSelection()).toList();
        selectorChangeButton.setEnabled(selected.size() == 1);
        selectorDeleteButton.setEnabled(selected.size() > 0);
        updateCopyPasteButtons();
    }

    private void updateDataButtons() {
        List<?> selected = ((IStructuredSelection) dataTableViewer.getSelection()).toList();
        dataChangeButton.setEnabled(selected.size() == 1);
        dataMoveUpButton.setEnabled(selected.size() == 1 && variableMappings.indexOf(selected.get(0)) > 0);
        dataMoveDownButton.setEnabled(selected.size() == 1 && variableMappings.indexOf(selected.get(0)) < variableMappings.size() - 1);
        dataDeleteButton.setEnabled(selected.size() > 0);
        updateCopyPasteButtons();
    }

    private void updateCopyPasteButtons() {
        if (pasteButton != null) {
            copyButton.setEnabled(!(selectorTableViewer.getSelection().isEmpty() && dataTableViewer.getSelection().isEmpty()));
            pasteButton.setEnabled(isPasteButtonEnabled);
        }
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private void editVariableMapping(VariableMapping mapping, String usage) {
        MessageVariableDialog dialog = new MessageVariableDialog(definition.getVariableNames(true), VariableMapping.USAGE_SELECTOR.equals(usage),
                mapping);
        if (dialog.open() != IDialogConstants.CANCEL_ID) {
            if (mapping == null) {
                mapping = new VariableMapping();
            }
            mapping.setName(dialog.getVariable());
            mapping.setMappedName(dialog.getAlias());
            mapping.setUsage(usage);
            if (!variableMappings.contains(mapping)) {
                addVariableMapping(mapping, false);
            }
            selectorTableViewer.refresh();
            dataTableViewer.refresh();
        }
    }

    public List<VariableMapping> getVariableMappings() {
        Collections.sort(variableMappings, (a, b) -> {
            if (a.isPropertySelector() == b.isPropertySelector()) {
                return 0;
            }
            if (a.isPropertySelector()) {
                return -1;
            }
            return 1;
        });
        return variableMappings;
    }

    private void addVariableMapping(VariableMapping mapping, boolean updateViewers) {
        for (VariableMapping existingMapping : variableMappings) {
            if (Objects.equal(existingMapping.getName(), mapping.getName())) {
                variableMappings.remove(existingMapping);
                break;
            }
        }
        variableMappings.add(mapping);
        if (updateViewers) {
            selectorTableViewer.refresh();
            dataTableViewer.refresh();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        copyButton = createButton(parent, IDialogConstants.CLIENT_ID, Localization.getString("button.copy"), false);
        copyButton.setEnabled(false);
        copyButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                CopyPasteModel copyPasteModel = new CopyPasteModel();
                selectorTableViewer.getStructuredSelection().toList().stream().forEach(e -> {
                    VariableMapping m = (VariableMapping) e;
                    copyPasteModel.getRouting().add(new CopyPasteRow(m.getName(), m.getMappedName(), null));
                });
                dataTableViewer.getStructuredSelection().toList().stream().forEach(e -> {
                    VariableMapping m = (VariableMapping) e;
                    String type = null;
                    Variable variable = VariableUtils.getVariableByName(definition, m.getName());
                    if (variable != null && !variable.isComplex()) {
                        try {
                            VariableFormat variableFormat = ClassLoaderUtil.instantiate(variable.getFormatClassName());
                            type = variableFormat.getName();
                        } catch (Exception ex) {
                            // leave type undefined
                        }
                    }
                    copyPasteModel.getPayload().add(new CopyPasteRow(m.getMappedName(), m.getName(), type));
                });
                if (!copyPasteModel.isEmpty()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.setSerializationInclusion(Include.NON_NULL);
                    String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(copyPasteModel);
                    Clipboard clipboard = new Clipboard(null);
                    clipboard.setContents(new String[] { json }, new Transfer[] { TextTransfer.getInstance() });
                    clipboard.dispose();
                    isPasteButtonEnabled = true;
                    updateCopyPasteButtons();
                }
            };

        });
        pasteButton = createButton(parent, IDialogConstants.CLIENT_ID, Localization.getString("button.paste"), false);
        pasteButton.setEnabled(false);
        pasteButton.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent event) throws Exception {
                Clipboard clipboard = new Clipboard(null);
                String json = (String) clipboard.getContents(TextTransfer.getInstance());
                clipboard.dispose();
                ObjectMapper objectMapper = new ObjectMapper();
                CopyPasteModel copyPasteModel = objectMapper.reader().readValue(json, CopyPasteModel.class);
                copyPasteModel.getRouting().forEach(e -> {
                    for (Iterator<VariableMapping> i = variableMappings.iterator(); i.hasNext();) {
                        VariableMapping m = i.next();
                        if (m.getName().equals(e.getName()) && VariableMapping.USAGE_SELECTOR.equals(m.getUsage())) {
                            i.remove();
                            break;
                        }
                    }
                    variableMappings.add(new VariableMapping(e.getName(), e.getValue(), VariableMapping.USAGE_SELECTOR));
                });
                copyPasteModel.getPayload().forEach(e -> {
                    for (Iterator<VariableMapping> i = variableMappings.iterator(); i.hasNext();) {
                        VariableMapping m = i.next();
                        if (m.getName().equals(e.getValue()) && VariableMapping.USAGE_READ.equals(m.getUsage())) {
                            i.remove();
                            break;
                        }
                    }
                    variableMappings.add(new VariableMapping(e.getValue(), e.getName(), VariableMapping.USAGE_READ));
                });
                selectorTableViewer.refresh();
                dataTableViewer.refresh();
            };

        });
        Button spacer = createButton(parent, IDialogConstants.CLIENT_ID, "", false); // spacer
        spacer.setVisible(false);
        ((GridData) spacer.getLayoutData()).grabExcessHorizontalSpace = true;
        super.createButtonsForButtonBar(parent);
        updateCopyPasteButtons();
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = (Composite) super.createButtonBar(parent);
        ((GridLayout) composite.getLayout()).makeColumnsEqualWidth = false;
        composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_HORIZONTAL));
        return composite;
    }

    private class MoveVariableSelectionListener extends LoggingSelectionAdapter {
        private final boolean up;

        public MoveVariableSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            IStructuredSelection selection = (IStructuredSelection) dataTableViewer.getSelection();
            VariableMapping mapping = (VariableMapping) selection.getFirstElement();
            int index = variableMappings.indexOf(mapping);
            Collections.swap(variableMappings, index, up ? (index - 1) : (index + 1));
            setDataTableInput();
            dataTableViewer.setSelection(selection);
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
            }
            return "";
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    private class UsageContentProvider implements IStructuredContentProvider {
        private final String usage;

        private UsageContentProvider(String usage) {
            this.usage = usage;
        }

        @Override
        public Object[] getElements(Object inputElement) {
            List<VariableMapping> list = new ArrayList<VariableMapping>();
            for (VariableMapping variableMapping : variableMappings) {
                if (usage.equals(variableMapping.getUsage())) {
                    list.add(variableMapping);
                }
            }
            return list.toArray(new VariableMapping[list.size()]);
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // do nothing.
        }

        @Override
        public void dispose() {
            // do nothing.
        }
    }

    public static class CopyPasteModel {
        private final List<CopyPasteRow> routing = new ArrayList<>();
        private final List<CopyPasteRow> payload = new ArrayList<>();

        public List<CopyPasteRow> getRouting() {
            return routing;
        }

        public List<CopyPasteRow> getPayload() {
            return payload;
        }

        @JsonIgnore
        public boolean isEmpty() {
            return routing.isEmpty() && payload.isEmpty();
        }
    }

    public static class CopyPasteRow {
        private String name;
        private String value;
        private String type;

        public CopyPasteRow() {
        }

        public CopyPasteRow(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }
}
