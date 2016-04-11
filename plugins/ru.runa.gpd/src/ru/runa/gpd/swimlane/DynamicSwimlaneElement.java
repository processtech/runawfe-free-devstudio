package ru.runa.gpd.swimlane;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.orgfunction.OrgFunctionDefinition;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;

import com.google.common.base.Objects;

public class DynamicSwimlaneElement extends OrgFunctionSwimlaneElement {
    private Control[] userInputControls;

    public DynamicSwimlaneElement() {
        super(OrgFunctionDefinition.DEFAULT.getName());
    }

    @Override
    public String getLabel() {
        return getSwimlaneInitializerNotNull().getDefinition().getLabel();
    }

    @Override
    public void createGUI(Composite parent) {
        Composite clientArea = createSection(parent, 2);
        int paramsSize = getSwimlaneInitializerNotNull().getParameters().size();
        userInputControls = new Control[paramsSize];
        for (int i = 0; i < paramsSize; i++) {
            final OrgFunctionParameter parameter = getSwimlaneInitializerNotNull().getParameters().get(i);
            String message = Localization.getString(parameter.getDefinition().getName()) + " *:";
            Label label = new Label(clientArea, SWT.NONE);
            label.setText(message);
            label.setLayoutData(createLayoutData(false));
            final Combo combo = new Combo(clientArea, SWT.READ_ONLY);
            combo.setVisibleItemCount(10);
            List<String> variableNames = processDefinition.getVariableNames(true, parameter.getDefinition().getType());
            for (String variableName : variableNames) {
                if (!Objects.equal(variableName, swimlaneName)) {
                    combo.add(variableName);
                }
            }
            combo.addSelectionListener(new LoggingSelectionAdapter() {
                @Override
                protected void onSelection(SelectionEvent e) throws Exception {
                    String value = combo.getItem(combo.getSelectionIndex());
                    getSwimlaneInitializerNotNull().getParameter(parameter.getDefinition().getName()).setVariableValue(value);
                    fireCompletedEvent();
                }
            });
            combo.setLayoutData(createLayoutData(true));
            userInputControls[i] = combo;
        }
    }

    private GridData createLayoutData(boolean fillGrab) {
        return new GridData(fillGrab ? GridData.FILL_HORIZONTAL : GridData.CENTER);
    }

    @Override
    public void open(String path, String swimlaneName, OrgFunctionSwimlaneInitializer swimlaneInitializer) {
        super.open(path, swimlaneName, swimlaneInitializer);
        for (int i = 0; i < userInputControls.length; i++) {
            String variableName = "";
            if (getSwimlaneInitializerNotNull().getParameters().get(i).isVariableValue()) {
                variableName = getSwimlaneInitializerNotNull().getParameters().get(i).getVariableName();
            }
            if (userInputControls[i] instanceof Text) {
                ((Text) userInputControls[i]).setText(variableName);
            }
            if (userInputControls[i] instanceof Combo) {
                ((Combo) userInputControls[i]).setText(variableName);
            }
        }
    }
}
