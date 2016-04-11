package ru.runa.gpd.ui.dialog;

import java.net.URL;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Application;
import ru.runa.gpd.Localization;
import ru.runa.gpd.SharedImages;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;

public class AboutDialog extends Dialog {

    public AboutDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        parent = (Composite) super.createDialogArea(parent);
        Label imageLabel = new Label(parent, SWT.NULL);
        Image image = SharedImages.getImage("icons/splash.png");
        imageLabel.setImage(image);
        imageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
        Text versionText = new Text(parent, SWT.READ_ONLY);
        versionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        versionText.setText(Localization.getString("version") + ": " + Application.getVersion().toString());
        SWTUtils.createLink(parent, "runawfe.org", new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
                IWebBrowser browser = support.getExternalBrowser();
                browser.openURL(new URL("http://runawfe.org"));
            }
        });
        return parent;
    }

}
