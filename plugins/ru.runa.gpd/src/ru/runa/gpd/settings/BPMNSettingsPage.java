package ru.runa.gpd.settings;

import java.awt.Font;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.wfe.WFEServerConnector;

public class BPMNSettingsPage extends FieldEditorPreferencePage implements PrefConstants, IWorkbenchPreferencePage {

    private static final String FORMAT_COLOR_HEX = "0x%02X%02X%02X";
    private static final String PREF_COMMON_BPMN = "pref.common.bpmn.";
    private Button syncButton;

    public BPMNSettingsPage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    public void createFieldEditors() {
        final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(P_BPMN_FONT, new FontData("Verdana", 9, Font.PLAIN).toString());
        addField(new FontFieldEditor(P_BPMN_FONT, Localization.getString(PREF_COMMON_BPMN + P_BPMN_FONT), getFieldEditorParent()));

        addColorField(store, P_BPMN_COLOR_FONT);
        addColorField(store, P_BPMN_COLOR_BACKGROUND);
        addColorField(store, P_BPMN_COLOR_BASE);
        addColorField(store, P_BPMN_COLOR_TRANSITION);
    }

    private void addColorField(IPreferenceStore store, String name) {
        addField(new ColorFieldEditor(name, Localization.getString(PREF_COMMON_BPMN + name), getFieldEditorParent()));
    }

    @Override
    protected void contributeButtons(final Composite buttonBar) {
        ((GridLayout) buttonBar.getLayout()).numColumns++;
        syncButton = new Button(buttonBar, SWT.PUSH);
        syncButton.setText(Localization.getString("button.Synchronize"));
        Dialog.applyDialogFont(syncButton);
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        syncButton.setLayoutData(data);
        syncButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    performApply();
                    WFEServerConnector connector = WFEServerConnector.getInstance();
                    String propertiesName = "graph.properties";
                    IPreferenceStore store = Activator.getDefault().getPreferenceStore();
                    FontData fontData = PreferenceConverter.getFontData(Activator.getDefault().getPreferenceStore(), PrefConstants.P_BPMN_FONT);
                    connector.setSetting(propertiesName, "fontFamily", fontData.getName());
                    connector.setSetting(propertiesName, "fontSize", "" + fontData.getHeight());
                    setSetting(connector, propertiesName, "textColor", store, P_BPMN_COLOR_FONT);
                    setSetting(connector, propertiesName, "figureBackgroundColor", store, P_BPMN_COLOR_BACKGROUND);
                    setSetting(connector, propertiesName, "baseColor", store, P_BPMN_COLOR_BASE);
                    setSetting(connector, propertiesName, "transitionColor", store, P_BPMN_COLOR_TRANSITION);

                    Dialogs.information(Localization.getString("test.Connection.Ok"));
                } catch (Throwable th) {
                    Dialogs.error(Localization.getString("error.Synchronize"), th);
                }
            }

            private void setSetting(WFEServerConnector connector, String propertiesName, String key, IPreferenceStore store, String value) {
                RGB colorPref = PreferenceConverter.getColor(store, value);
                String color = String.format(FORMAT_COLOR_HEX, colorPref.red, colorPref.green, colorPref.blue);
                connector.setSetting(propertiesName, key, color);
            }
        });
        syncButton.setEnabled(isValid());
        applyDialogFont(buttonBar);
    }

    @Override
    protected void updateApplyButton() {
        super.updateApplyButton();
        syncButton.setEnabled(isValid());
    }
}