package ru.runa.gpd.ui.dialog;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.swimlane.SwimlaneElementListener;
import ru.runa.gpd.swimlane.SwimlaneElement;
import ru.runa.gpd.swimlane.SwimlaneElementRegistry;
import ru.runa.gpd.swimlane.SwimlaneInitializer;
import ru.runa.gpd.swimlane.SwimlaneInitializerParser;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SwimlaneConfigDialog extends Dialog implements SwimlaneElementListener {
    private final List<SwimlaneElement> swimlaneElements = SwimlaneElementRegistry.getSwimlaneElements();
    private CTabFolder typeTabFolder;
    private CTabFolder orgFunctionsTabFolder;
    private final Swimlane swimlane;
    private String configuration;
    private String path;
    private boolean publicVisibility;

    public SwimlaneConfigDialog(ProcessDefinition definition, Swimlane swimlane) {
        super(Display.getCurrent().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        this.swimlane = swimlane;
        this.configuration = swimlane.getDelegationConfiguration();
        this.publicVisibility = swimlane.isPublicVisibility();
        this.path = swimlane.getEditorPath() != null ? swimlane.getEditorPath() : "SwimlaneElement.ManualLabel";
        for (SwimlaneElement swimlaneElement : swimlaneElements) {
            swimlaneElement.setProcessDefinition(definition);
        }
    }

    @Override
    protected Point getInitialSize() {
        return new Point(750, 500);
    }

    @Override
    public void completed(String path, SwimlaneInitializer swimlaneInitializer) {
        this.configuration = swimlaneInitializer.toString();
        this.path = path;
    }

    @Override
    public void opened(String path, boolean createNewInitializer) {
        try {
            SwimlaneInitializer swimlaneInitializer = null;
            if (!createNewInitializer) {
                swimlaneInitializer = SwimlaneInitializerParser.parse(configuration);
            }
            for (SwimlaneElement swimlaneElement : swimlaneElements) {
                if (path.startsWith(swimlaneElement.getName())) {
                    int index = swimlaneElements.indexOf(swimlaneElement);
                    if (index >= 1) {
                        orgFunctionsTabFolder.setSelection(index - 1);
                    }
                    swimlaneElement.open(path, swimlane.getName(), swimlaneInitializer);
                } else {
                    swimlaneElement.close();
                }
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        getShell().setText(Localization.getString("SwimlaneConfigDialog.title"));
        typeTabFolder = new CTabFolder(parent, SWT.BOTTOM | SWT.BORDER);
        typeTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite composite1 = new Composite(typeTabFolder, SWT.NONE);
        composite1.setLayout(new GridLayout());
        CTabItem tabItem1 = new CTabItem(typeTabFolder, SWT.NONE);
        tabItem1.setText(Localization.getString("tab.constructor.relation"));
        tabItem1.setControl(composite1);
        {
            SwimlaneElement swimlaneElement = swimlaneElements.get(0);
            Composite composite = new Composite(composite1, SWT.NONE);
            composite.setLayout(new GridLayout());
            swimlaneElement.createGUI(composite);
            swimlaneElement.addElementListener(this);
            for (SwimlaneElement childElement : (List<SwimlaneElement>) swimlaneElement.getChildren()) {
                childElement.createGUI(swimlaneElement.getClientArea());
            }
        }
        Composite composite2 = new Composite(typeTabFolder, SWT.NONE);
        composite2.setLayout(new GridLayout());
        CTabItem tabItem2 = new CTabItem(typeTabFolder, SWT.NONE);
        tabItem2.setText(Localization.getString("tab.constructor.orgfunction"));
        tabItem2.setControl(composite2);
        orgFunctionsTabFolder = new CTabFolder(composite2, SWT.TOP | SWT.BORDER);
        orgFunctionsTabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
        for (int i = 1; i < swimlaneElements.size(); i++) {
            SwimlaneElement swimlaneElement = swimlaneElements.get(i);
            Composite composite = new Composite(orgFunctionsTabFolder, SWT.NONE);
            composite.setLayout(new GridLayout());
            swimlaneElement.createGUI(composite);
            swimlaneElement.addElementListener(this);
            for (SwimlaneElement childElement : (List<SwimlaneElement>) swimlaneElement.getChildren()) {
                childElement.createGUI(swimlaneElement.getClientArea());
            }
            CTabItem tabItem = new CTabItem(orgFunctionsTabFolder, SWT.NONE);
            tabItem.setText(swimlaneElement.getLabel());
            tabItem.setControl(composite);
        }
        boolean rel = configuration != null && configuration.indexOf("@") == 0;
        typeTabFolder.setSelection(rel ? 0 : 1);
        typeTabFolder.addSelectionListener(new TypeTabSelectionHandler());
        orgFunctionsTabFolder.addSelectionListener(new TabSelectionHandler());
        return orgFunctionsTabFolder;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        ((GridLayout) parent.getLayout()).makeColumnsEqualWidth = false;
        final Button publicVisibilityCheckbox = new Button(parent, SWT.CHECK);
        publicVisibilityCheckbox.setSelection(publicVisibility);
        publicVisibilityCheckbox.setText(Localization.getString("Variable.property.publicVisibility"));
        publicVisibilityCheckbox.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                publicVisibility = publicVisibilityCheckbox.getSelection();
            }
        });
        setButtonLayoutData(publicVisibilityCheckbox);
        super.createButtonsForButtonBar(parent);
    }

    @Override
    protected void initializeBounds() {
        super.initializeBounds();
        opened(path, false);
    }

    public String getConfiguration() {
        return configuration;
    }

    public boolean isPublicVisibility() {
        return publicVisibility;
    }

    public String getPath() {
        return path;
    }

    private class TypeTabSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            if (typeTabFolder.getSelectionIndex() == 1 && orgFunctionsTabFolder.getSelection() == null) {
                orgFunctionsTabFolder.setSelection(0);
            }
        }
    }

    private class TabSelectionHandler extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            String path = swimlaneElements.get(orgFunctionsTabFolder.getSelectionIndex() + 1).getName();
            opened(path, true);
        }
    }
}
