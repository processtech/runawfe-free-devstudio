package ru.runa.gpd.extension.handler;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.DelegableConfigurationDialog;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.swimlane.RelationComposite;
import ru.runa.gpd.swimlane.RelationSwimlaneInitializer;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

public class EscalationActionHandlerProvider extends DelegableProvider {

    @Override
    protected DelegableConfigurationDialog createConfigurationDialog(IDelegable iDelegable) {
        return new EscalationConfigurationDialog(iDelegable.getDelegationConfiguration());
    }

    @Override
    public boolean validateValue(IDelegable iDelegable, List<ValidationError> errors) {
        String configuration = iDelegable.getDelegationConfiguration();
        if (!configuration.startsWith(RelationSwimlaneInitializer.RELATION_BEGIN)) {
            OrgFunctionsRegistry.getInstance().getArtifact(configuration);
        }
        return true;
    }

    public class EscalationConfigurationDialog extends DelegableConfigurationDialog {
        private RelationSwimlaneInitializer swimlaneInitializer = new RelationSwimlaneInitializer();

        public EscalationConfigurationDialog(String initialValue) {
            super(initialValue);
        }

        @Override
        protected Point getInitialSize() {
            return new Point(500, 300);
        }

        @Override
        protected void createDialogHeader(Composite parent) {
            boolean tabRelationEnabled = initialValue.startsWith(RelationSwimlaneInitializer.RELATION_BEGIN);
            CTabFolder typeTabFolder = new CTabFolder(parent, SWT.BOTTOM | SWT.BORDER);
            typeTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
            Composite composite1 = new Composite(typeTabFolder, SWT.NONE);
            composite1.setLayout(new GridLayout());
            CTabItem tabItem1 = new CTabItem(typeTabFolder, SWT.NONE);
            tabItem1.setText(Localization.getString("tab.constructor.relation"));
            tabItem1.setControl(composite1);
            {
                final RelationComposite relationComposite = new RelationComposite(composite1, false, null);
                try {
                    if (tabRelationEnabled) {
                        swimlaneInitializer = new RelationSwimlaneInitializer(initialValue);
                    }
                } catch (Exception e) {
                }
                relationComposite.init(swimlaneInitializer);
                swimlaneInitializer.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        styledText.setText(swimlaneInitializer.toString());
                    }

                });
            }
            Composite composite2 = new Composite(typeTabFolder, SWT.NONE);
            composite2.setLayout(new GridLayout());
            CTabItem tabItem2 = new CTabItem(typeTabFolder, SWT.NONE);
            tabItem2.setText(Localization.getString("tab.constructor.orgfunction"));
            tabItem2.setControl(composite2);
            {
                Composite composite = new Composite(composite2, SWT.NONE);
                composite.setLayout(new GridLayout());
                composite.setLayoutData(new GridData(GridData.FILL_BOTH));

                Composite gui = new Composite(composite, SWT.NONE);
                gui.setLayout(new GridLayout(2, false));
                gui.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                String orgFunctionLabel = "";
                try {
                    if (!tabRelationEnabled) {
                        orgFunctionLabel = OrgFunctionsRegistry.getInstance().getArtifact(initialValue).getLabel();
                    }
                } catch (Exception e) {
                }
                {
                    Label label = new Label(gui, SWT.NONE);
                    label.setText(Localization.getString("swimlane.initializer"));
                }
                final Combo combo = new Combo(gui, SWT.READ_ONLY);
                combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getInstance().getAll();
                for (OrgFunctionDefinition definition : definitions) {
                    if (definition.isUsedForEscalation()) {
                        combo.add(definition.getLabel());
                    }
                }
                combo.setText(orgFunctionLabel);
                combo.addSelectionListener(new LoggingSelectionAdapter() {

                    @Override
                    protected void onSelection(SelectionEvent e) throws Exception {
                        styledText.setText(OrgFunctionsRegistry.getInstance().getArtifactNotNullByLabel(combo.getText()).getName());
                    }
                });
            }
            typeTabFolder.setSelection(tabRelationEnabled ? 0 : 1);
        }

    }
}
