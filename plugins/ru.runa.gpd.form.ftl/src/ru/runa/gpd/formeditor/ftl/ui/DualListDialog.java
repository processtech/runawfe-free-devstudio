package ru.runa.gpd.formeditor.ftl.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

import ru.runa.gpd.Localization;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;

import com.google.common.collect.Lists;

public class DualListDialog extends Dialog {
    private final java.util.List<String> availableNames;
    private final java.util.List<String> selectedValues;
    private final String titleKey;
    private List availableItems;
    private List selectedItems;
    private Button addButton;
    private Button removeButton;
    private Button moveUpButton;
    private Button moveDownButton;

    public DualListDialog(java.util.List<String> variableNames, java.util.List<String> value, String titleKey) {
        super(Display.getCurrent().getActiveShell());
        this.availableNames = variableNames;
        this.selectedValues = value;
        this.selectedValues.remove("");
        this.titleKey = titleKey;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString(titleKey));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 2;
        layout.verticalSpacing = 2;
        layout.marginBottom = 5;
        layout.marginTop = 5;
        area.setLayout(layout);
        java.util.List<String> availableList = Lists.newArrayList(availableNames);
        availableList.removeAll(selectedValues);
        availableItems = createList(area, availableList, true);
        addButton = SwtUtils.createButton(area, Localization.getString("button.add"), new AddRemoveVariableSelectionListener(true));
        removeButton = SwtUtils.createButton(area, Localization.getString("button.delete"), new AddRemoveVariableSelectionListener(false));
        moveUpButton = SwtUtils.createButton(area, Localization.getString("button.up"), new MoveVariableSelectionListener(true));
        moveDownButton = SwtUtils.createButton(area, Localization.getString("button.down"), new MoveVariableSelectionListener(false));
        selectedItems = createList(area, selectedValues, false);
        return area;
    }

    private class AddRemoveVariableSelectionListener extends LoggingSelectionAdapter {
        private final boolean add;

        public AddRemoveVariableSelectionListener(boolean add) {
            this.add = add;
        }

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            List fromItems = add ? availableItems : selectedItems;
            List toItems = add ? selectedItems : availableItems;
            String[] selection = fromItems.getSelection();
            if (selection != null && selection.length > 0) {
                for (String selectedValue : selection) {
                    toItems.add(selectedValue);
                    fromItems.remove(selectedValue);
                    if (add) {
                        selectedValues.add(selectedValue);
                    } else {
                        selectedValues.remove(selectedValue);
                    }
                }
                if (add) {
                    addButton.setEnabled(false);
                } else {
                    removeButton.setEnabled(false);
                }
            }
        }
    }

    private class MoveVariableSelectionListener extends LoggingSelectionAdapter {
        private final boolean up;

        public MoveVariableSelectionListener(boolean up) {
            this.up = up;
        }

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            moveItem(selectedItems);
        }

        private void moveItem(List list) {
            if (list.getSelection() != null && list.getSelection().length == 1) {
                int index = list.getSelectionIndex();
                String selectedItem = list.getItem(index);
                list.remove(index);
                int newIndex = up ? index - 1 : index + 1;
                list.add(selectedItem, newIndex);
                list.setSelection(newIndex);
                updateMoveButtonsState(list, true);
                selectedValues.clear();
                for (String item : selectedItems.getItems()) {
                    selectedValues.add(item);
                }
            }
        }
    }

    private List createList(Composite area, java.util.List<String> data, final boolean available) {
        List list = new List(area, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 4;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = false;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        gridData.minimumHeight = 100;
        list.setLayoutData(gridData);
        for (String string : data) {
            list.add(string);
        }
        list.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                List target = available ? availableItems : selectedItems;
                String[] selection = target.getSelection();
                boolean isSelected = selection != null && selection.length > 0;
                if (!available) {
                    removeButton.setEnabled(isSelected);
                    addButton.setEnabled(false);
                    if (isSelected) {
                        availableItems.setSelection(-1);
                    }
                    updateMoveButtonsState(selectedItems, isSelected);
                } else {
                    addButton.setEnabled(isSelected);
                    removeButton.setEnabled(false);
                    if (isSelected) {
                        selectedItems.setSelection(-1);
                    }
                }
            }

        });
        return list;
    }

    private void updateMoveButtonsState(List target, boolean isSelected) {
        if (isSelected) {
            if (target.getSelectionIndex() > 0) {
                moveUpButton.setEnabled(true);
            } else {
                moveUpButton.setEnabled(false);
            }
            if (target.getSelectionIndex() < (target.getItemCount() - 1)) {
                moveDownButton.setEnabled(true);
            } else {
                moveDownButton.setEnabled(false);
            }
        } else {
            moveUpButton.setEnabled(false);
            moveDownButton.setEnabled(false);
        }
    }

    public java.util.List<String> openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return selectedValues;
        }
        return null;
    }

}
