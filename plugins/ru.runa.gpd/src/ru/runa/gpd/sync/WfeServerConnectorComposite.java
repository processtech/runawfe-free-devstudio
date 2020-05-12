package ru.runa.gpd.sync;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.settings.WfeServerConnectorPreferencePage;
import ru.runa.gpd.settings.WfeServerConnectorsPreferenceNode;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;

public class WfeServerConnectorComposite extends Composite {
    private final WfeServerConnectorDataImporter<?> importer;
    private final ConnectorCallback callback;
    private Combo combo;
    private Hyperlink synchronizeLink;

    public WfeServerConnectorComposite(Composite parent, WfeServerConnectorDataImporter<?> importer, ConnectorCallback callback) {
        super(parent, SWT.NONE);
        this.importer = importer;
        this.callback = callback;
        setLayout(new GridLayout(3, true));
        createCombo();
        createConnectionSettingsLink();
        createSynchronizeLink();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        combo.setEnabled(enabled);
    }

    private void createCombo() {
        final String[][] items = WfeServerConnectorsPreferenceNode.getComboItems();
        String selectedIndexString = String.valueOf(WfeServerConnectorsPreferenceNode.getSelectedIndex());
        combo = new Combo(this, SWT.READ_ONLY);
        combo.setFont(getFont());
        for (int i = 0; i < items.length; i++) {
            combo.add(items[i][0], i);
            if (selectedIndexString.equals(items[i][1])) {
                combo.setText(items[i][0]);
            }
        }
        combo.addSelectionListener(new LoggingSelectionAdapter() {

            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String name = combo.getText();
                for (String[] entry : items) {
                    if (name.equals(entry[0])) {
                        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                        store.setValue(PrefConstants.P_WFE_SERVER_CONNECTOR_SELECTED_INDEX, entry[1]);
                        WfeServerConnector.getInstance().setSettings(WfeServerConnectorSettings.loadSelected());
                        break;
                    }
                }
                synchronizeLink.setEnabled(WfeServerConnector.getInstance().isConfigured());
                importer.synchronize();
                if (callback != null) {
                    callback.onSynchronizationCompleted();
                }
            }
        });
    }

    private void createConnectionSettingsLink() {
        SwtUtils.createLink(this, Localization.getString("button.ConnectionSettings"), new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                openConnectionSettingsDialog();
            }

        });
    }

    private void openConnectionSettingsDialog() {
        IPreferencePage page = new WfeServerConnectorPreferencePage(WfeServerConnectorsPreferenceNode.getSelectedPrefix());
        PreferenceManager preferenceManager = new PreferenceManager();
        IPreferenceNode node = new PreferenceNode("1", page);
        preferenceManager.addToRoot(node);
        PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), preferenceManager);
        dialog.create();
        dialog.setMessage(page.getTitle());
        if (dialog.open() == IDialogConstants.OK_ID) {
            importer.synchronize();
            if (callback != null) {
                callback.onSynchronizationCompleted();
            }
        }
    }

    private void createSynchronizeLink() {
        synchronizeLink = SwtUtils.createLink(this, Localization.getString("button.Synchronize"), new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                importer.synchronize();
                if (callback != null) {
                    callback.onSynchronizationCompleted();
                }
            }

        });
        synchronizeLink.setEnabled(WfeServerConnector.getInstance().isConfigured());
    }

}
