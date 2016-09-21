package ru.runa.gpd.extension.handler;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.ui.custom.XmlHighlightTextStyling;
import ru.runa.gpd.ui.dialog.SwimlaneConfigDialog;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.util.XmlUtil;

public class SwimlaneAssignerCellEditorProvider extends DelegableProvider {
    private ProcessDefinition definition;

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(IDelegable iDelegable) {
        if (!HandlerArtifact.ACTION.equals(iDelegable.getDelegationType())) {
            throw new IllegalArgumentException("For action handler only");
        }
        definition = ((GraphElement) iDelegable).getProcessDefinition();
        return new SwimlaneAssignerConfigurationDialog(iDelegable.getDelegationConfiguration(), definition.getSwimlanes());
    }

    @Override
    public boolean validateValue(IDelegable iDelegable, List<ValidationError> errors) {
        return XmlUtil.isXml(iDelegable.getDelegationConfiguration());
    }

    public class SwimlaneAssignerConfigurationDialog extends DelegableConfigurationDialog {
        private final List<String> swimlaneNames;
        private Combo swimlaneNameCombo;
        private Text swimlaneInitializerText;
        private Button swimlaneInitializerButton;

        public SwimlaneAssignerConfigurationDialog(String initialValue, List<Swimlane> swimlanes) {
            super(initialValue);
            this.swimlaneNames = VariableUtils.getVariableNames(swimlanes);
        }

        @Override
        protected Point getInitialSize() {
            return new Point(500, 300);
        }

        @Override
        protected void createDialogHeader(Composite composite) {
            Composite gui = new Composite(composite, SWT.NONE);
            gui.setLayout(new GridLayout(3, false));
            gui.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            String initialSwimlaneName = "";
            String initialSwimlaneInitializer = "";
            try {
                Document document = XmlUtil.parseWithoutValidation(initialValue);
                Element root = document.getRootElement();
                initialSwimlaneName = root.attributeValue("swimlaneName");
                initialSwimlaneInitializer = root.attributeValue("swimlaneInititalizer");
            } catch (Exception e) {
            }
            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Localization.getString("swimlane.name"));
            }
            swimlaneNameCombo = new Combo(gui, SWT.READ_ONLY);
            GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
            gridData.horizontalSpan = 2;
            swimlaneNameCombo.setLayoutData(gridData);
            swimlaneNameCombo.setItems(swimlaneNames.toArray(new String[swimlaneNames.size()]));
            swimlaneNameCombo.setText(initialSwimlaneName);
            swimlaneNameCombo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateText();
                }
            });
            {
                Label label = new Label(gui, SWT.NONE);
                label.setText(Localization.getString("swimlane.initializer"));
            }
            swimlaneInitializerText = new Text(gui, SWT.BORDER);
            swimlaneInitializerText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            swimlaneInitializerText.setText(initialSwimlaneInitializer);
            swimlaneInitializerText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(ModifyEvent e) {
                    updateText();
                }
            });
            swimlaneInitializerButton = new Button(gui, SWT.NONE);
            swimlaneInitializerButton.setText("...");
            swimlaneInitializerButton.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    Swimlane swimlane = new Swimlane();
                    swimlane.setParent(definition);
                    swimlane.setName("TEST");
                    swimlane.setDelegationConfiguration(swimlaneInitializerText.getText());
                    SwimlaneConfigDialog dialog = new SwimlaneConfigDialog(definition, swimlane);
                    if (dialog.open() == IDialogConstants.OK_ID) {
                        swimlaneInitializerText.setText(dialog.getConfiguration());
                    }
                }
            });
            super.createDialogHeader(composite);
        }

        public void updateText() {
            String xml = "<Assign swimlaneName=\"" + swimlaneNameCombo.getText() + "\" ";
            xml += "swimlaneInititalizer=\"" + swimlaneInitializerText.getText() + "\"/>";
            styledText.setText(xml);
        }

        @Override
        protected void createDialogFooter(Composite composite) {
            styledText.addLineStyleListener(new XmlHighlightTextStyling());
        }
    }
}
