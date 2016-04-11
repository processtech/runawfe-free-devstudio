package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.util.SelectionItem;

public class MultipleSelectionDialog extends Dialog {
    private final String title;
    private final List<? extends SelectionItem> items;

    public MultipleSelectionDialog(String title, List<? extends SelectionItem> items) {
        super(Display.getCurrent().getActiveShell());
        this.title = title;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.items = items;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 400);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(title);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        composite.setLayout(new GridLayout());

        ScrolledComposite scrolledComposite = new ScrolledComposite(composite, SWT.V_SCROLL | SWT.BORDER);
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);
        scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite clientArea = new Composite(scrolledComposite, SWT.BORDER);
        clientArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        clientArea.setLayout(new GridLayout());

        scrolledComposite.setContent(clientArea);

        for (SelectionItem selectionItem : items) {
            final Button button = new Button(clientArea, SWT.CHECK);
            button.setText(selectionItem.getLabel());
            button.setSelection(selectionItem.isEnabled());
            button.setData(selectionItem);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    ((SelectionItem) button.getData()).setEnabled(button.getSelection());
                }
            });
            button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        }
        clientArea.layout(true, true);
        scrolledComposite.setMinHeight(clientArea.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);

        return composite;
    }

}
