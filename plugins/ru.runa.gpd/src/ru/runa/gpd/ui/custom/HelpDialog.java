package ru.runa.gpd.ui.custom;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Preconditions;

public class HelpDialog extends Dialog {
    private Text text;
    private final String content;

    public HelpDialog(Class<?> contextClass) throws IOException {
        super(Display.getCurrent().getActiveShell());
        String helpFileName = contextClass.getSimpleName() + ".help";
        String lang = Locale.getDefault().getLanguage();
        InputStream is = contextClass.getResourceAsStream(helpFileName + "_" + lang);
        if (is == null) {
            is = contextClass.getResourceAsStream(helpFileName);
        }
        Preconditions.checkNotNull(is, "Context help is not installed: " + helpFileName);
        this.content = IOUtils.readStream(is);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(700, 500);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText("ExecuteFormulaActionHandler help");

        Composite composite = new Composite(parent, SWT.NULL);
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        text = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setText(this.content);
        text.setEditable(false);

        return composite;
    }

}
