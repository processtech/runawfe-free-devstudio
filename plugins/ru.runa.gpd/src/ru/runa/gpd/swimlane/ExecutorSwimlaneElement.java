package ru.runa.gpd.swimlane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.wfe.WFEServerExecutorsImporter;
import ru.runa.wfe.extension.orgfunction.ExecutorByNameFunction;

public class ExecutorSwimlaneElement extends OrgFunctionSwimlaneElement {
    private int mask;
    private Text selectionText;

    public ExecutorSwimlaneElement() {
        super(ExecutorByNameFunction.class.getName());
    }

    public void setMask(String maskString) {
        this.mask = Integer.parseInt(maskString);
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 2);
        selectionText = new Text(clientArea, SWT.READ_ONLY | SWT.BORDER);
        selectionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        SWTUtils.createLink(clientArea, Localization.getString("button.choose"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                List<String> items = new ArrayList<String>();
                Map<String, Boolean> executors = WFEServerExecutorsImporter.getInstance().loadCachedData();
                for (String name : executors.keySet()) {
                    boolean isGroup = executors.get(name);
                    if (isGroup && (mask & 2) != 0) {
                        items.add(name);
                    }
                    if (!isGroup && (mask & 1) != 0) {
                        items.add(name);
                    }
                }
                ChooseItemDialog<String> dialog = new ChooseItemDialog<String>(Localization.getString("WFDialog.Text"), items);
                String result = dialog.openDialog();
                if (result != null) {
                    selectionText.setText(result);
                    setOrgFunctionParameterValue(0, selectionText.getText());
                    fireCompletedEvent();
                }
            }
        });
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        selectionText.setText(getOrgFunctionParameterValue(0));
    }
}
