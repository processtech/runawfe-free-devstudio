package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class FilterBox extends Composite {
    
    private Text text;
    private Button button;
    private int selectedIndex = -1;
    private List<String> items;
    private SelectionListener selectionListener;

    public FilterBox(Composite parent, List<String> items) {
        this(parent, items, null);
    }

    public FilterBox(Composite parent, List<String> items, String selectedItem) {
        super(parent, SWT.NONE);
        setItems(items);
        setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        setLayout(new GridLayout(2, false));
        text = new Text(this, SWT.READ_ONLY | SWT.BORDER);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (selectedItem != null) {
            text.setText(selectedItem);
        }
        button = new Button(this, SWT.PUSH);
        button.setText("...");
        button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ChooseItemDialog<String> dialog = new ChooseVariableNameDialog(FilterBox.this.items);
                dialog.setSelectedItem(text.getText());
                String result = dialog.openDialog();
                if (result != null) {
                    text.setText(result);
                    selectedIndex = FilterBox.this.items.indexOf(result);
                    if (selectionListener != null) {
                        e.widget = FilterBox.this;
                        e.text = result;
                        selectionListener.widgetSelected(e);
                    }
                }
            }
            
        });
    }
    
    public void setSelectionListener (SelectionListener listener) {
        selectionListener = listener;
    }

    public void setItems(List<String> items) {
        if (items == null) {
            this.items = new ArrayList<>();
        } else {
            this.items = items;
        }
    }

    public void setItems(String[] items) {
        setItems(new ArrayList<>(Arrays.asList(items)));
    }

    public void setSelectedItem(String selectedItem) {
        text.setText(selectedItem);
        selectedIndex = items.indexOf(selectedItem);
    }
    
    public String getSelectedItem() {
        return text.getText();
    }

    public void select(int index) {
        if (items != null && index >=0 && items.size() > index) {
            selectedIndex = index;
            text.setText(items.get(index));
        }
    }

    public void remove(int index) {
        if (items != null && items.size() > index) {
            if (text.getText().equals(items.get(index))) {
                text.setText("");
            }
            items.remove(index);
        }
    }

    public void add(String value, int index) {
        if (items != null) {
            items.add(index, value);
        }
    }

    public void deselectAll() {
        text.setText("");
    }

    public String getText() {
        return getSelectedItem();
    }

    public void add(String item) {
        if (items != null && item != null) {
            items.add(item);
        }
    }

    public int getSelectionIndex() {
        return selectedIndex;
    }

    public String getItem(int index) {
        if (items != null && items.size() > index) {
            return items.get(index);
        }
        return null;
    }

    @Override
    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        super.setEnabled(enabled);
    }
}
