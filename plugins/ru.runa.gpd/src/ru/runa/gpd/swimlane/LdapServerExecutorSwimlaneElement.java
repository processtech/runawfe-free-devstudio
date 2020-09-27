package ru.runa.gpd.swimlane;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import ru.runa.gpd.Localization;
import ru.runa.gpd.sync.LdapServerConnectorComposite;
import ru.runa.gpd.sync.LdapServerExecutorImporter;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction;

public class LdapServerExecutorSwimlaneElement extends OrgFunctionSwimlaneElement {
    private Text selectionText;

    public LdapServerExecutorSwimlaneElement() {
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
        new LdapServerConnectorComposite(clientArea);
        Composite content = new Composite(clientArea, SWT.NONE);
        content.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        content.setLayout(new GridLayout(2, false));
        selectionText = new Text(content, SWT.READ_ONLY | SWT.BORDER);
        selectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        SwtUtils.createLink(content, Localization.getString("button.choose"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                Map<String, Boolean> executors = LdapServerExecutorImporter.getInstance().getData();
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
