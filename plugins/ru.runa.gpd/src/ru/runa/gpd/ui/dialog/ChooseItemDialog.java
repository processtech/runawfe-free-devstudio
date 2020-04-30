package ru.runa.gpd.ui.dialog;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;

public class ChooseItemDialog<T extends Comparable<? super T>> extends Dialog {
    private final String dialogText;
    private final List<T> items;
    private T selectedItem;
    private final boolean sort;
    private Comparator<T> comparator;
    private final String labelText;
    private LabelProvider labelProvider;
    private final boolean useFilter;
    private Text filterText;
    private ListViewer itemsList;

    public ChooseItemDialog(String dialogText, List<T> items, boolean sort, String labelText, boolean useFilter) {
        super(Display.getCurrent().getActiveShell());
        Preconditions.checkNotNull(items, "Items are not set");
        this.dialogText = dialogText;
        this.items = Lists.newArrayList(items);
        this.sort = sort;
        this.labelText = labelText;
        this.useFilter = useFilter;
    }

    public ChooseItemDialog(String dialogText, List<T> items) {
        this(dialogText, items, true, null, true);
    }

    public void setLabelProvider(LabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }

    public void setComparator(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, true);
        area.setLayout(layout);
        if (labelText != null) {
            Label label = new Label(area, SWT.NO_BACKGROUND);
            label.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
            label.setText(labelText);
        }
        if (useFilter) {
            filterText = new Text(area, SWT.BORDER);
            filterText.setMessage(Localization.getString("filter"));
            filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            filterText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    itemsList.refresh();
                }
            });
        }
        itemsList = new ListViewer(area, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
        GridData listData = new GridData(GridData.FILL_BOTH);
        listData.minimumHeight = 200;
        listData.heightHint = 200;
        listData.minimumWidth = 300;
        itemsList.getControl().setLayoutData(listData);
        itemsList.setContentProvider(new ArrayContentProvider());
        if (sort) {
            if (comparator != null) {
                Collections.sort(this.items, comparator);
            } else {
                Collections.sort(this.items);
            }
        }
        itemsList.setInput(items);
        if (useFilter) {
            itemsList.addFilter(new ItemsFilter());
        }
        if (labelProvider != null) {
            itemsList.setLabelProvider(labelProvider);
        }
        if (selectedItem != null) {
            itemsList.setSelection(new StructuredSelection(selectedItem));
        }
        itemsList.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectedItem = (T) ((IStructuredSelection) event.getSelection()).getFirstElement();
                getButton(IDialogConstants.OK_ID).setEnabled(selectedItem != null);
            }
        });
        itemsList.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            @Override
            protected void onDoubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        return area;
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
        return control;
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(dialogText);
    }

    public void setSelectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
        if (itemsList != null) {
            if (selectedItem != null) {
                itemsList.setSelection(new StructuredSelection(selectedItem));
            } else {
                itemsList.setSelection(new StructuredSelection());
            }
        }
    }

    public T openDialog() {
        if (open() != IDialogConstants.CANCEL_ID) {
            return selectedItem;
        }
        return null;
    }

    private class ItemsFilter extends ViewerFilter {
        @Override
        public boolean select(Viewer viewer, Object parentElement, Object element) {
            String elementText = labelProvider != null ? labelProvider.getText(element) : element.toString();
            return elementText.toLowerCase().contains(filterText.getText().toLowerCase());
        }
    }
}
