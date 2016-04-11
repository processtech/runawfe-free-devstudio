package ru.runa.gpd.formeditor.ftl.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.extension.LocalizationRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.formeditor.ftl.parameter.RichComboParameter;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.ui.dialog.ChooseVariableNameDialog;
import ru.runa.gpd.wfe.DataImporter;
import ru.runa.gpd.wfe.WFEServerExecutorsImporter;
import ru.runa.gpd.wfe.WFEServerRelationsImporter;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class RichComboDialog extends Dialog {
    private final String variableTypeFilter;
    private final List<String> variableNames;
    private String value;
    private Text valueText;

    public RichComboDialog(String variableTypeFilter, List<String> variableNames, String value) {
        super(Display.getCurrent().getActiveShell());
        this.variableTypeFilter = variableTypeFilter;
        this.variableNames = variableNames;
        this.value = value;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.getString("RichComboDialog.title"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        area.setLayout(new GridLayout(2, false));
        final SyncType syncType = getType();
        if (syncType != null) {
            final DataImporter dataImporter = getDataImporter(syncType);
            SyncUIHelper.createHeader(area, dataImporter, WFEConnectionPreferencePage.class, null);
            SWTUtils.createLink(area, Messages.getString("button.choose.value"), new LoggingHyperlinkAdapter() {

                @Override
                protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                    List<String> items;
                    Object data = dataImporter.loadCachedData();
                    if (data instanceof List) {
                        // relations
                        items = (List<String>) data;
                    } else {
                        items = Lists.newArrayList();
                        Map<String, Boolean> executors = (Map<String, Boolean>) data;
                        Boolean isGroup = syncType == SyncType.GROUP ? Boolean.TRUE : (syncType == SyncType.ACTOR ? Boolean.FALSE : null);
                        for (Map.Entry<String, Boolean> entry : executors.entrySet()) {
                            if (isGroup == null || (isGroup != null && Objects.equal(isGroup, entry.getValue()))) {
                                items.add(entry.getKey());
                            }
                        }
                    }
                    ChooseItemDialog<String> dialog = new ChooseItemDialog<String>(LocalizationRegistry.getLabel(variableTypeFilter), items);
                    String result = dialog.openDialog();
                    if (result != null) {
                        value = RichComboParameter.VALUE_PREFIX + result;
                        valueText.setText(result);
                    }
                }
            });
        }
        valueText = new Text(area, SWT.BORDER);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 200;
        valueText.setLayoutData(gridData);
        if (value != null) {
            if (value.startsWith(RichComboParameter.VALUE_PREFIX)) {
                value = value.substring(RichComboParameter.VALUE_PREFIX.length());
            }
            valueText.setText(value);
        }
        valueText.addModifyListener(new LoggingModifyTextAdapter() {

            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                value = RichComboParameter.VALUE_PREFIX + valueText.getText();
            }
        });
        SWTUtils.createLink(area, Messages.getString("button.choose.variable"), new LoggingHyperlinkAdapter() {

            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                ChooseVariableNameDialog dialog = new ChooseVariableNameDialog(variableNames);
                String result = dialog.openDialog();
                if (result != null) {
                    valueText.setText(result);
                    value = result;
                }
            }
        });
        return area;
    }

    public String openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return value;
        }
        return null;
    }

    private DataImporter getDataImporter(SyncType syncType) {
        switch (syncType) {
        case EXECUTOR:
        case GROUP:
        case ACTOR:
            return WFEServerExecutorsImporter.getInstance();
        case RELATION:
            return WFEServerRelationsImporter.getInstance();
        default:
            throw new RuntimeException("" + syncType);
        }
    }

    private SyncType getType() {
        if (VariableFormatRegistry.isAssignableFrom(Group.class.getName(), variableTypeFilter)) {
            return SyncType.GROUP;
        } else if (VariableFormatRegistry.isAssignableFrom(Actor.class.getName(), variableTypeFilter)) {
            return SyncType.ACTOR;
        } else if (VariableFormatRegistry.isAssignableFrom(Executor.class.getName(), variableTypeFilter)) {
            return SyncType.EXECUTOR;
        } else if (VariableFormatRegistry.isAssignableFrom(String.class.getName(), variableTypeFilter)) {
            return SyncType.RELATION;
        }
        return null;
    }

    enum SyncType {
        EXECUTOR, GROUP, ACTOR, RELATION
    }
}
