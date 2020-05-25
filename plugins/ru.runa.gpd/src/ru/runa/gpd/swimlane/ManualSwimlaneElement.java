package ru.runa.gpd.swimlane;

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;

import com.google.common.base.Objects;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.extension.orgfunction.OrgFunctionsRegistry;
import ru.runa.gpd.ui.custom.InsertVariableTextMenuDetectListener;
import ru.runa.gpd.ui.custom.LoggingHyperlinkAdapter;
import ru.runa.gpd.ui.custom.LoggingModifyTextAdapter;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.custom.SwtUtils;

import static ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition.DEFAULT;

public class ManualSwimlaneElement extends OrgFunctionSwimlaneElement {
    private Composite paramsComposite;
    private Text classNameText;
    private Combo combo;

    public ManualSwimlaneElement() {
        super(DEFAULT.getName());
    }

    @Override
    public void createGUI(Composite clientArea) {
        createComposite(clientArea, 2);
        Label label = new Label(getClientArea(), SWT.NONE);
        label.setText(Localization.getString("OrgFunction.Type"));
        label.setLayoutData(createLayoutData(1, false));
        combo = new Combo(getClientArea(), SWT.READ_ONLY);
        combo.setVisibleItemCount(10);
        List<OrgFunctionDefinition> definitions = OrgFunctionsRegistry.getInstance().getAll();
        for (OrgFunctionDefinition definition : definitions) {
            combo.add(definition.getLabel());
        }
        combo.setText(getSwimlaneInitializerNotNull().getDefinition().getLabel());
        combo.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) throws Exception {
                String label = combo.getItem(combo.getSelectionIndex());
                if (label.equals(getSwimlaneInitializerNotNull().getDefinition().getLabel())
                        && !Objects.equal(DEFAULT, getSwimlaneInitializerNotNull().getDefinition())) {
                    return;
                }
                setOrgFunctionDefinitionName(OrgFunctionsRegistry.getInstance().getArtifactNotNullByLabel(label).getName());
                setSwimlaneInitializer(null);
                classNameText.setText(getSwimlaneInitializerNotNull().getDefinition().getName());
                reloadParametersUI();
                fireCompletedEvent();
            }
        });
        combo.setLayoutData(createLayoutData(1, true));
        classNameText = new Text(getClientArea(), SWT.BORDER);
        classNameText.setEditable(false);
        classNameText.setLayoutData(createLayoutData(2, true));
        paramsComposite = new Composite(getClientArea(), SWT.NONE);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.horizontalSpan = 2;
        gridData.heightHint = 150;
        paramsComposite.setLayoutData(gridData);
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 0;
        layout.marginRight = 1;
        layout.marginWidth = 0;
        paramsComposite.setLayout(layout);
    }

    private GridData createLayoutData(int numColumns, boolean fillGrab) {
        GridData td = new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
        td.horizontalSpan = numColumns;
        return td;
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        if (null != swimlaneInitializer){
            setOrgFunctionDefinitionName(swimlaneInitializer.getDefinition().getName());
        }
        super.open(path, swimlaneName, swimlaneInitializer);
        combo.setText(getSwimlaneInitializerNotNull().getDefinition().getLabel());
        classNameText.setText(getSwimlaneInitializerNotNull().getDefinition().getName());
        reloadParametersUI();
    }

    private void reloadParametersUI() {
        for (Control control : paramsComposite.getChildren()) {
            control.dispose();
        }
        for (final OrgFunctionParameter parameter : getSwimlaneInitializerNotNull().getParameters()) {
            String message = Localization.getString(parameter.getDefinition().getName()) + " *:";
            Control control;
            if (parameter.getDefinition().isMultiple()) {
                control = SwtUtils.createLink(paramsComposite, message, new LoggingHyperlinkAdapter() {
                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        getSwimlaneInitializerNotNull().propagateParameter(parameter, 1);
                        fireCompletedEvent();
                        reloadParametersUI();
                    }
                });
                control.setToolTipText("[+]");
            } else {
                control = new Label(paramsComposite, SWT.NONE);
                ((Label) control).setText(message);
            }
            control.setLayoutData(createLayoutData(1, false));
            if (parameter.isCanBeDeleted()) {
                GridData gridData = createLayoutData(1, false);
                gridData.widthHint = 20;
                SwtUtils.createLink(paramsComposite, "[-]", new LoggingHyperlinkAdapter() {
                    @Override
                    protected void onLinkActivated(HyperlinkEvent e) throws Exception {
                        getSwimlaneInitializerNotNull().removeParameter(parameter);
                        fireCompletedEvent();
                        reloadParametersUI();
                    }
                }).setLayoutData(gridData);
            }
            final Text text = new Text(paramsComposite, SWT.BORDER);
            text.setText(parameter.getValue());
            text.addModifyListener(new LoggingModifyTextAdapter() {
                @Override
                protected void onTextChanged(ModifyEvent e) throws Exception {
                    parameter.setValue(text.getText());
                    fireCompletedEvent();
                    text.setBackground(ColorConstants.white);
                }
            });
            List<String> variableNames = processDefinition.getVariableNames(true);
            variableNames.remove(swimlaneName);
            new InsertVariableTextMenuDetectListener(text, variableNames);
            text.setLayoutData(createLayoutData(parameter.isCanBeDeleted() ? 1 : 2, true));
        }
        paramsComposite.redraw();
        paramsComposite.layout(true, true);
    }
}
