package ru.runa.gpd.swimlane;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.settings.WFEConnectionPreferencePage;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SWTUtils;
import ru.runa.gpd.ui.custom.SyncUIHelper;
import ru.runa.gpd.ui.dialog.ChooseItemDialog;
import ru.runa.gpd.wfe.WFEServerRelationsImporter;
import ru.runa.wfe.user.Executor;

public class RelationComposite extends Composite {
    protected RelationSwimlaneInitializer swimlaneInitializer;
    protected Text relationNameText;
    protected Combo variableCombo;
    protected Button inversedButton;

    public RelationComposite(Composite parent, boolean displayParameter, ProcessDefinition processDefinition) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout());
        setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING & GridData.FILL_HORIZONTAL));
        SyncUIHelper.createHeader(this, WFEServerRelationsImporter.getInstance(), WFEConnectionPreferencePage.class, null);
        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        Label relationNameLabel = new Label(composite, SWT.NONE);
        relationNameLabel.setText(Localization.getString("Relation.Name"));
        relationNameText = new Text(composite, SWT.BORDER);
        relationNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        relationNameText.addModifyListener(new LoggingModifyTextAdapter() {
            @Override
            protected void onTextChanged(ModifyEvent e) throws Exception {
                swimlaneInitializer.setRelationName(relationNameText.getText());
            }
        });
        SWTUtils.createLink(composite, Localization.getString("button.choose"), new LoggingHyperlinkAdapter() {
            @Override
            protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                List<String> relations = WFEServerRelationsImporter.getInstance().loadCachedData();
                ChooseItemDialog<String> dialog = new ChooseItemDialog<String>(Localization.getString("Relations"), relations);
                String result = dialog.openDialog();
                if (result != null) {
                    relationNameText.setText(result);
                    swimlaneInitializer.setRelationName(relationNameText.getText());
                }
            }
        });
        if (displayParameter) {
            Label parameterLabel = new Label(composite, SWT.NONE);
            parameterLabel.setText(Localization.getString("Relation.Parameter"));
            variableCombo = new Combo(composite, SWT.READ_ONLY);
            for (String variableName : processDefinition.getVariableNames(true, Executor.class.getName(), String.class.getName())) {
                variableCombo.add(variableName);
            }
            variableCombo.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    swimlaneInitializer.setRelationParameterVariableName(variableCombo.getText());
                }
            });
            new Label(composite, SWT.NONE);
        }
        new Label(composite, SWT.NONE);
        inversedButton = new Button(composite, SWT.CHECK);
        inversedButton.setText(Localization.getString("Relation.Inversed"));
        inversedButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                swimlaneInitializer.setInversed(inversedButton.getSelection());
            }
        });
        new Label(composite, SWT.NONE);
    }

    public void init(RelationSwimlaneInitializer swimlaneInitializer) {
        this.swimlaneInitializer = swimlaneInitializer;
        inversedButton.setSelection(swimlaneInitializer.isInversed());
        relationNameText.setText(swimlaneInitializer.getRelationName());
        if (variableCombo != null) {
            variableCombo.setText(swimlaneInitializer.getRelationParameterVariableName());
        }
    }

}
