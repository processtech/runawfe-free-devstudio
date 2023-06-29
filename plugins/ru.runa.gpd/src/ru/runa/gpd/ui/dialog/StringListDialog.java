package ru.runa.gpd.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.Window;
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
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;

public class StringListDialog extends Dialog {
    private final java.util.List<String> selectedValues;
    private final String title;
    private List selectedItems;
    private Button removeButton;
    private Button moveUpButton;
    private Button moveDownButton;

    public StringListDialog(String title, java.util.List<String> value) {
        super(Display.getCurrent().getActiveShell());
        this.title = title;
        this.selectedValues = value;
        this.selectedValues.remove("");
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
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
        SwtUtils.createButton(area, Localization.getString("button.add"), new AddSelectionListener());
        removeButton = SwtUtils.createButton(area, Localization.getString("button.delete"), new RemoveSelectionListener());
        moveUpButton = SwtUtils.createButton(area, Localization.getString("button.up"), new MoveSelectionListener(true));
        moveDownButton = SwtUtils.createButton(area, Localization.getString("button.down"), new MoveSelectionListener(false));
        selectedItems = createList(area, selectedValues);
        return area;
    }

    private class AddSelectionListener extends LoggingSelectionAdapter {

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            UserInputDialog userInputDialog = new UserInputDialog();
            if (Window.OK == userInputDialog.open()) {
                selectedValues.add(userInputDialog.getUserInput());
                selectedItems.add(userInputDialog.getUserInput());
            }
        }
    }

    private class RemoveSelectionListener extends LoggingSelectionAdapter {

        @Override
        protected void onSelection(SelectionEvent e) throws Exception {
            String[] selection = selectedItems.getSelection();
            if (selection != null && selection.length > 0) {
                for (String selectedValue : selection) {
                    selectedItems.remove(selectedValue);
                    selectedValues.remove(selectedValue);
                }
                removeButton.setEnabled(false);
            }
        }
    }

    private class MoveSelectionListener extends LoggingSelectionAdapter {
        private final boolean up;

        public MoveSelectionListener(boolean up) {
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

    private List createList(Composite area, java.util.List<String> data) {
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
                String[] selection = selectedItems.getSelection();
                boolean isSelected = selection != null && selection.length > 0;
                removeButton.setEnabled(isSelected);
                updateMoveButtonsState(selectedItems, isSelected);
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
