package ru.runa.gpd.ui.dialog;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.Localization;
import ru.runa.gpd.search.VariableSearchTarget;

public class SearchVariableDialog extends Dialog {
    private Set<VariableSearchTarget> searchTargets;
    private Set<VariableSearchTarget> possibleSearchTargets;
    private List<Button> searchTargetsCheckboxes = new ArrayList<>();
    private Button pickAllCheckbox;

    public SearchVariableDialog(boolean searchSwimlane) {
        super(Display.getCurrent().getActiveShell());
        searchTargets = EnumSet.allOf(VariableSearchTarget.class);
        possibleSearchTargets = EnumSet.allOf(VariableSearchTarget.class);
        if (searchSwimlane) {
            searchTargets.remove(VariableSearchTarget.SWIMLANE);
            possibleSearchTargets.remove(VariableSearchTarget.SWIMLANE);
        } else {
            searchTargets.remove(VariableSearchTarget.TASK_ROLE);
            possibleSearchTargets.remove(VariableSearchTarget.TASK_ROLE);
        }
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString("SearchVariableDialog.title"));
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        area.setLayout(layout);
        Composite composite = new Composite(area, SWT.NONE);

        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        composite.setLayout(gridLayout);
        composite.setLayoutData(new GridData());
        for (VariableSearchTarget searchTarget : possibleSearchTargets) {
            final Button searchVariableCheckbox = new Button(composite, SWT.CHECK);
            searchVariableCheckbox.setText(Localization.getString(searchTarget.getLabel()));
            searchVariableCheckbox.setSelection(true);
            searchVariableCheckbox.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
                String searchTypeName = ((Button) c.getSource()).getText();
                Optional<VariableSearchTarget> enumEntity = searchTargets.stream()
                        .filter(target -> Localization.getString(target.getLabel()).equals(searchTypeName)).findAny();
                if (enumEntity.isPresent()) {
                    searchTargets.remove(enumEntity.get());
                } else {
                    searchTargets.add(possibleSearchTargets.stream()
                            .filter(target -> Localization.getString(target.getLabel()).equals(searchTypeName)).findAny().get());
                }
                if (!((Button) c.getSource()).getSelection()) {
                    pickAllCheckbox.setSelection(false);
                }
            }));
            searchTargetsCheckboxes.add(searchVariableCheckbox);
        }
        new Label(composite, SWT.NONE);
        pickAllCheckbox = new Button(composite, SWT.CHECK);
        pickAllCheckbox.setText(Localization.getString("SearchVariableDialog.pickAll"));
        pickAllCheckbox.setSelection(true);
        pickAllCheckbox.addSelectionListener(SelectionListener.widgetSelectedAdapter(c -> {
            boolean checked = ((Button) c.getSource()).getSelection();
            if (checked) {
                searchTargetsCheckboxes.forEach(checkbox -> checkbox.setSelection(true));
                searchTargets = EnumSet.copyOf(possibleSearchTargets);
            } else {
                searchTargetsCheckboxes.forEach(checkbox -> checkbox.setSelection(false));
                searchTargets.clear();
            }
        }));
        return area;
    }

    public Set<VariableSearchTarget> getSearchTypes() {
        return searchTargets;
    }
}
