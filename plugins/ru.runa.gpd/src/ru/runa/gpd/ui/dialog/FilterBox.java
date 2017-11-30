package ru.runa.gpd.ui.dialog;

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
    private List<String> items;
    private SelectionListener selectionListener;

    public FilterBox(Composite parent, List<String> items) {
        this(parent, items, null);
    }

    public FilterBox(Composite parent, List<String> items, String selectedItem) {
        super(parent, SWT.NONE);
        this.items = items;
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
                    if (selectionListener != null) {
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
        this.items = items;
    }
    
    public void setSelectedItem(String selectedItem) {
        text.setText(selectedItem);
    }
    
    public String getSelectedItem() {
        return text.getText();
    }

}
