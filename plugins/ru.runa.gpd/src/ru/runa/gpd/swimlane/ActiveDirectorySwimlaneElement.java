package ru.runa.gpd.swimlane;

import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ldap.LDAPConnectionPreferencePage;
import ru.runa.gpd.ldap.LDAPExecutorsImporter;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction;

import com.google.common.collect.Lists;

public class ActiveDirectorySwimlaneElement extends OrgFunctionSwimlaneElement {
    private Text selectionText;

    public ActiveDirectorySwimlaneElement() {
        super(ExecutorByNameFunction.class.getName());
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        selectionText.setText(getOrgFunctionParameterValue(0));
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 1);
        SyncUIHelper.createHeader(clientArea, LDAPExecutorsImporter.getInstance(), LDAPConnectionPreferencePage.class, null);
        Composite content = new Composite(clientArea, SWT.NONE);
        content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        content.setLayout(new GridLayout(2, false));
        selectionText = new Text(content, SWT.READ_ONLY | SWT.BORDER);
        selectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        SWTUtils.createLink(content, Localization.getString("button.choose"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                Map<String, Boolean> executors = LDAPExecutorsImporter.getInstance().loadCachedData();
                List<String> list = Lists.newArrayList(executors.keySet());
                ChooseItemDialog<String> dialog = new ChooseItemDialog<String>(Localization.getString("WFDialog.Text"), list);
                String result = dialog.openDialog();
                if (result != null) {
                    selectionText.setText(result);
                    setOrgFunctionParameterValue(0, selectionText.getText());
                    fireCompletedEvent();
                }
            }
        });
    }
}
