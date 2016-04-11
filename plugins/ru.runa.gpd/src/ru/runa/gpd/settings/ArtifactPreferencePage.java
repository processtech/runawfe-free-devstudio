package ru.runa.gpd.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.Artifact;
import ru.runa.gpd.extension.ArtifactRegistry;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;

import com.google.common.collect.Lists;

@SuppressWarnings({ "unchecked" })
public class ArtifactPreferencePage<T extends Artifact> extends PreferencePage implements IWorkbenchPreferencePage, PrefConstants {
    private final ArtifactRegistry<T> registry;
    private CheckboxTableViewer tableViewer;
    private Button editButton;
    private Button removeButton;

    protected ArtifactPreferencePage(ArtifactRegistry<T> registry) {
        this.registry = registry;
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite ancestor) {
        Composite parent = new Composite(ancestor, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        Composite innerParent = new Composite(parent, SWT.NONE);
        GridLayout innerLayout = new GridLayout();
        innerLayout.numColumns = 2;
        innerLayout.marginHeight = 0;
        innerLayout.marginWidth = 0;
        innerParent.setLayout(innerLayout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        innerParent.setLayoutData(gd);
        Composite tableComposite = new Composite(innerParent, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 360;
        data.heightHint = convertHeightInCharsToPixels(10);
        tableComposite.setLayoutData(data);
        TableColumnLayout columnLayout = new TableColumnLayout();
        tableComposite.setLayout(columnLayout);
        Table table = new Table(tableComposite, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        //        GC gc = new GC(getShell());
        //        gc.setFont(JFaceResources.getDialogFont());
        TableColumn column1 = new TableColumn(table, SWT.LEFT);
        column1.setText("Class name");
        columnLayout.setColumnData(column1, new ColumnWeightData(2, 300, true));
        TableColumn column2 = new TableColumn(table, SWT.LEFT);
        column2.setText("Display name");
        columnLayout.setColumnData(column2, new ColumnWeightData(1, 200, true));
        //        TableColumn column3 = new TableColumn(table, SWT.NONE);
        //        column3.setText("Variable type");
        //        columnLayout.setColumnData(column3, new ColumnWeightData(3, 200, true));
        //        gc.dispose();
        tableViewer = new CheckboxTableViewer(table);
        tableViewer.setLabelProvider(new ArtifactLabelProvider());
        tableViewer.setContentProvider(new IStructuredContentProvider() {
            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }

            @Override
            public Object[] getElements(Object inputElement) {
                List<T> list = registry.getAll();
                return list.toArray(new Artifact[list.size()]);
            }
        });
        //        tableViewer.setComparator(new ViewerComparator() {
        //            @Override
        //            public int compare(Viewer viewer, Object object1, Object object2) {
        //                if ((object1 instanceof TemplatePersistenceData) && (object2 instanceof TemplatePersistenceData)) {
        //                    Template left = ((TemplatePersistenceData) object1).getTemplate();
        //                    Template right = ((TemplatePersistenceData) object2).getTemplate();
        //                    int result = Collator.getInstance().compare(left.getName(), right.getName());
        //                    if (result != 0) {
        //                        return result;
        //                    }
        //                    return Collator.getInstance().compare(left.getDescription(), right.getDescription());
        //                }
        //                return super.compare(viewer, object1, object2);
        //            }
        //
        //            @Override
        //            public boolean isSorterProperty(Object element, String property) {
        //                return true;
        //            }
        //        });
        tableViewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent e) {
                edit();
            }
        });
        tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent e) {
                updateButtons();
            }
        });
        tableViewer.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Artifact d = (Artifact) event.getElement();
                d.setEnabled(event.getChecked());
            }
        });
        Composite buttons = new Composite(innerParent, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        buttons.setLayout(layout);
        addButton(buttons, "button.create", new CreateSelectionListener());
        editButton = addButton(buttons, "button.change", new EditSelectionListener());
        removeButton = addButton(buttons, "button.delete", new DeleteSelectionListener());
        createSeparator(buttons);
        addButton(buttons, "button.import", new ImportSelectionListener());
        addButton(buttons, "button.export", new ExportSelectionListener());
        tableViewer.setInput(registry);
        refreshTableViewer();
        updateButtons();
        Dialog.applyDialogFont(parent);
        innerParent.layout();
        return parent;
    }

    private void refreshTableViewer() {
        tableViewer.refresh();
        tableViewer.setAllChecked(false);
        List<Artifact> checked = Lists.newArrayList();
        for (Artifact artifact : registry.getAll()) {
            if (artifact.isEnabled()) {
                checked.add(artifact);
            }
        }
        tableViewer.setCheckedElements(checked.toArray());
    }

    /**
     * Creates a separator between buttons.
     *
     * @param parent the parent composite
     * @return a separator
     */
    private Label createSeparator(Composite parent) {
        Label separator = new Label(parent, SWT.NONE);
        separator.setVisible(false);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = 4;
        separator.setLayoutData(gd);
        return separator;
    }

    protected Button addButton(Composite parent, String buttonKey, SelectionAdapter selectionListener) {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        Button button = new Button(parent, SWT.PUSH);
        button.setText(Localization.getString(buttonKey));
        button.setLayoutData(gridData);
        button.addSelectionListener(selectionListener);
        return button;
    }

    protected void updateButtons() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        int selectionCount = selection.size();
        int itemCount = tableViewer.getTable().getItemCount();
        editButton.setEnabled(selectionCount == 1);
        removeButton.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
    }

    /**
     * Creates the edit dialog. Subclasses may override this method to provide a
     * custom dialog.
     *
     * @param artifact the artifact being edited
     * @param edit whether the dialog should be editable
     */
    protected T editArtifact(T artifact, boolean edit) {
        EditArtifactDialog dialog = new EditArtifactDialog(getShell(), artifact, edit);
        if (dialog.open() == Window.OK) {
            return (T) dialog.getArtifact();
        }
        return null;
    }

    private void edit() {
        IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
        Object[] objects = selection.toArray();
        if ((objects == null) || (objects.length != 1)) {
            return;
        }
        T artifact = (T) selection.getFirstElement();
        T newArtifact = editArtifact(artifact, true);
        if (newArtifact != null) {
            artifact.setLabel(newArtifact.getLabel());
            tableViewer.refresh(artifact);
            updateButtons();
            tableViewer.setChecked(artifact, artifact.isEnabled());
            tableViewer.setSelection(new StructuredSelection(artifact));
        }
    }

    @Override
    protected void performDefaults() {
        try {
            registry.load();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        refreshTableViewer();
    }

    @Override
    public boolean performOk() {
        try {
            registry.save();
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        return super.performOk();
    }

    @Override
    public boolean performCancel() {
        try {
            registry.load();
        } catch (Exception e) {
            PluginLogger.logError(e);
            return false;
        }
        return super.performCancel();
    }

    private class CreateSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            T newArtifact = editArtifact((T) new Artifact(true, "", ""), false);
            if (newArtifact != null) {
                registry.add(newArtifact);
                tableViewer.refresh();
                tableViewer.setChecked(newArtifact, true);
                tableViewer.setSelection(new StructuredSelection(newArtifact));
            }
        }
    }

    private class EditSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            edit();
        }
    }

    private class DeleteSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
            Iterator<T> elements = selection.iterator();
            while (elements.hasNext()) {
                T data = elements.next();
                registry.delete(data);
            }
            tableViewer.refresh();
        }
    }

    private class ImportSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            FileDialog dialog = new FileDialog(getShell());
            dialog.setText(Localization.getString("button.import"));
            dialog.setFilterExtensions(new String[] { "xml" });
            String path = dialog.open();
            if (path != null) {
                try {
                    File file = new File(path);
                    if (file.exists()) {
                        registry.load(new FileInputStream(file));
                    }
                    refreshTableViewer();
                } catch (Exception ex) {
                    PluginLogger.logError(ex);
                }
            }
        }
    }

    private class ExportSelectionListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
            dialog.setText(Localization.getString("button.export"));
            dialog.setFilterExtensions(new String[] { "xml" });
            dialog.setFileName("localizations");
            String path = dialog.open();
            if (path == null) {
                return;
            }
            File file = new File(path);
            try {
                registry.export(new FileOutputStream(file));
            } catch (Exception ex) {
                PluginLogger.logError(ex);
            }
        }
    }

    class ArtifactLabelProvider extends LabelProvider implements ITableLabelProvider {
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            Artifact artifact = (Artifact) element;
            switch (columnIndex) {
            case 0:
                return artifact.getName();
            case 1:
                return artifact.getLabel();
            default:
                return null;
            }
        }
    }
}
