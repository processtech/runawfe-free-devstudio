package ru.runa.gpd.ui.custom;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class SWTUtils {
    private static HyperlinkGroup hyperlinkGroup = new HyperlinkGroup(Display.getCurrent());
    /**
     * Create  something like that: -- label ---------------
     * @param layoutColumnsCount specify 3 to complete this composite. If you want add something else at right side specify more than 3 columns.
     */
    public static Composite createStrokeComposite(Composite parent, Object layoutData, String label, int layoutColumnsCount) {
        Composite strokeComposite = new Composite(parent, SWT.NONE);
        strokeComposite.setLayoutData(layoutData);
        strokeComposite.setLayout(new GridLayout(layoutColumnsCount, false));
        Label strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData data = new GridData();
        data.widthHint = 50;
        strokeLabel.setLayoutData(data);
        Label headerLabel = new Label(strokeComposite, SWT.NONE);
        headerLabel.setText(label);
        strokeLabel = new Label(strokeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
        strokeLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return strokeComposite;
    }

    public static Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }
    
    public static Hyperlink createLink(Composite parent, String msg, LoggingHyperlinkAdapter clickListener) {
        Hyperlink link = new Hyperlink(parent, SWT.NONE);
        link.setText(msg);
        link.addHyperlinkListener(clickListener);
        hyperlinkGroup.add(link);
        return link;
    }

    public static Button createButton(Composite parent, String label, LoggingSelectionAdapter selectionAdapter) {
        Button button = new Button(parent, SWT.PUSH);
        button.setText(label);
        //button.setFont(JFaceResources.getDialogFont());
        button.addSelectionListener(selectionAdapter);
        return button;
    }

    public static Button createButtonFillHorizontal(Composite parent, String label, LoggingSelectionAdapter selectionAdapter) {
        Button button = createButton(parent, label, selectionAdapter);
        button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return button;
    }

}
