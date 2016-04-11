package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.ui.custom.LoggingDoubleClickAdapter;

public class TreeViewSelectionDialog extends Dialog {
    private TreeViewer viewer;
    private TreeItem items;
    private TreeItem selectedItem;
    private final String dialogText;

    public TreeViewSelectionDialog(String dialogText) {
        super(Display.getCurrent().getActiveShell());
        this.dialogText = dialogText;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        newShell.setSize(500, 500);
        newShell.setText(dialogText);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        viewer = new TreeViewer(area, SWT.BORDER);
        viewer.getControl().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        viewer.setLabelProvider(new ColoredLabelProvider());
        viewer.setContentProvider(new ITreeContentProvider() {
            @Override
            public Object[] getChildren(Object parentElement) {
                return ((TreeItem) parentElement).children.toArray();
            }

            @Override
            public Object getParent(Object element) {
                return ((TreeItem) element).parent;
            }

            @Override
            public boolean hasChildren(Object element) {
                return ((TreeItem) element).children.size() > 0;
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return ((TreeItem) inputElement).children.toArray();
            }

            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });
        GridData listData = new GridData(GridData.FILL_BOTH);
        listData.minimumHeight = 200;
        listData.minimumWidth = 100;
        viewer.getControl().setLayoutData(listData);
        viewer.setInput(items);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                selectedItem = (TreeItem) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                getButton(IDialogConstants.OK_ID).setEnabled(selectedItem.allowSelection);
            }
        });
        viewer.addDoubleClickListener(new LoggingDoubleClickAdapter() {
            protected void onDoubleClick(DoubleClickEvent event) {
                selectedItem = (TreeItem) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
                if (selectedItem.allowSelection) {
                    okPressed();
                } else {
                    boolean includeSelection = true;
                    for (Object expanded : viewer.getExpandedElements()) {
                        if (selectedItem.equals(expanded)) {
                            includeSelection = false;
                            break;
                        }
                    }
                    viewer.setExpandedElements(getExpandedItems(includeSelection));
                }
            }
        });
        return area;
    }

    private TreeItem[] getExpandedItems(boolean includeSelection) {
        List<TreeItem> expandedItems = new ArrayList<TreeItem>();
        TreeItem item = selectedItem;
        while (item != null) {
            expandedItems.add(item);
            item = item.parent;
        }
        if (!includeSelection) {
            expandedItems.remove(selectedItem);
        }
        return expandedItems.toArray(new TreeItem[expandedItems.size()]);
    }

    public TreeItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(TreeItem selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setItems(TreeItem types) {
        this.items = types;
    }

    public static class ColoredLabelProvider extends LabelProvider implements IColorProvider {
        @Override
        public String getText(Object element) {
            return ((TreeItem) element).label;
        }

        @Override
        public Color getBackground(Object element) {
            return null;
        }

        @Override
        public Color getForeground(Object element) {
            return ((TreeItem) element).color;
        }
    }
}
