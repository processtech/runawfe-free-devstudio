package ru.runa.gpd.sync;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class LdapServerConnectorComposite extends Composite {

    public LdapServerConnectorComposite(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(2, true));
        createConnectionSettingsLink(LdapServerConnectorPreferencePage.class);
        createSynchronizeLink();
    }

    private void createConnectionSettingsLink(final Class<? extends IPreferencePage> pageClass) {
        SwtUtils.createLink(this, Localization.getString("button.ConnectionSettings"), new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                openConnectionSettingsDialog(pageClass);
            }

        });
    }

    private void openConnectionSettingsDialog(final Class<? extends IPreferencePage> pageClass) {
        IPreferencePage page = ClassLoaderUtil.instantiate(pageClass);
        PreferenceManager preferenceManager = new PreferenceManager();
        IPreferenceNode node = new PreferenceNode("1", page);
        preferenceManager.addToRoot(node);
        PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), preferenceManager);
        dialog.create();
        dialog.setMessage(page.getTitle());
        dialog.open();
    }

    private void createSynchronizeLink() {
        Hyperlink hyperlink = SwtUtils.createLink(this, Localization.getString("button.Synchronize"), new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                LdapServerExecutorImporter.getInstance().synchronize();
            }

        });
        hyperlink.setEnabled(LdapServerExecutorImporter.getInstance().getConnector().isConfigured());
    }

}
