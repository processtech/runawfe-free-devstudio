package ru.runa.gpd.ui.control;

import java.util.function.Consumer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import ru.runa.gpd.Localization;
import ru.runa.gpd.ui.custom.LoggingSelectionAdapter;
import ru.runa.gpd.ui.dialog.IntervalEditDialog;
import ru.runa.gpd.util.Duration;

public class IntervalControl {

    private static final String DEFAULT_VALUE = "5 minutes";

    private final Consumer<Duration> onChange;

    private Duration interval;
    private Button intervalButton;

    public IntervalControl(
            Composite parent,
            Duration initialValue,
            Consumer<Duration> onChange,
            GridData layoutData
    ) {
        if (initialValue == null || !initialValue.hasDuration()) {
            this.interval = new Duration(DEFAULT_VALUE);
            onChange.accept(this.interval);
        } else {
            this.interval = initialValue;
        }
        this.onChange = onChange;
        createControls(parent, layoutData);
    }

    private void createControls(Composite parent, GridData gridData) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(Localization.getString("property.conditional.checkInterval"));

        intervalButton = new Button(parent, SWT.NONE);
        intervalButton.setLayoutData(gridData);
        intervalButton.setText(format(interval));

        intervalButton.addSelectionListener(new LoggingSelectionAdapter() {
            @Override
            protected void onSelection(SelectionEvent e) {
                IntervalEditDialog dialog = new IntervalEditDialog(interval);
                Duration newDuration = dialog.openDialog();

                if (newDuration != null) {
                    interval = newDuration;
                    onChange.accept(newDuration);
                    intervalButton.setText(format(interval));
                    intervalButton.getParent().layout(true, true);
                }
            }
        });
    }

    protected String format(Duration duration) {
        if (duration == null || !duration.hasDuration()) {
            return Localization.getString("duration.singleCheck");
        }
        return duration.toString();
    }
}
