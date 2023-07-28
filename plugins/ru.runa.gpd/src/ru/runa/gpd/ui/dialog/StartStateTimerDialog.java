package ru.runa.gpd.ui.dialog;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;

public class StartStateTimerDialog extends Dialog {

    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static DateFormat timeFormat = new SimpleDateFormat("HH:mm");

    private String timerEventDefinition;
    private Button chkStartDate;
    private Button chkStartTime;
    private Button chkRepeat;
    private Button chkDuration;
    private DateTime dtDate;
    private DateTime dtTime;
    private Text txtRepeat;
    private Spinner spYears;
    private Spinner spMonths;
    private Spinner spWeeks;
    private Spinner spDays;
    private Spinner spHours;
    private Spinner spMinutes;

    private ModifyListener modifyListener = new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
            updateUi();
        }
    };

    public StartStateTimerDialog(String timerEventDefinition) {
        super(Display.getCurrent().getActiveShell());
        this.timerEventDefinition = timerEventDefinition;
    }

    @Override
    public Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout(4, false);
        gridLayout.horizontalSpacing = 10;
        area.setLayout(gridLayout);
        { // Date
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.startDate"));
            chkStartDate = new Button(area, SWT.CHECK);
            dtDate = new DateTime(area, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN | SWT.MEDIUM);
            decorate(dtDate, Localization.getString("StartStateTimerDialog.startDate.info"));
            GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            dtDate.setLayoutData(gridData);
            chkStartDate.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateUi();
                    if (chkStartDate.getSelection()) {
                        dtDate.setFocus();
                    }
                }
            });
        }
        { // Time
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.startTime"));
            chkStartTime = new Button(area, SWT.CHECK);
            dtTime = new DateTime(area, SWT.BORDER | SWT.TIME | SWT.SHORT);
            GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            dtTime.setLayoutData(gridData);
            chkStartTime.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateUi();
                    if (chkStartTime.getSelection()) {
                        dtTime.setFocus();
                    }
                }
            });
        }
        { // Repeat
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.repeat"));
            chkRepeat = new Button(area, SWT.CHECK);
            txtRepeat = new Text(area, SWT.BORDER | SWT.SINGLE);
            txtRepeat.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
            decorate(txtRepeat, Localization.getString("StartStateTimerDialog.repeat.info"));
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.repeat.times"));
            chkRepeat.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateUi();
                    if (chkRepeat.getSelection()) {
                        txtRepeat.setFocus();
                    }
                }
            });
            txtRepeat.addVerifyListener(new VerifyListener() {

                @Override
                public void verifyText(VerifyEvent e) {
                    if (e.text.length() > 0) {
                        try {
                            Integer.parseInt(e.text);
                        } catch (NumberFormatException x) {
                            e.doit = false;
                        }
                    }
                }
            });
            txtRepeat.addModifyListener(modifyListener);
        }
        { // Duration
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.duration"));
            chkDuration = new Button(area, SWT.CHECK);
            spYears = new Spinner(area, SWT.BORDER | SWT.SINGLE);
            spYears.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.duration.years"));
            chkDuration.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    updateUi();
                    if (chkDuration.getSelection()) {
                        spDays.setFocus();
                    }
                }
            });
            spYears.addModifyListener(modifyListener);
        }
        { // Duration.Months
            spMonths = createPart(area, Localization.getString("StartStateTimerDialog.duration.months"));
            spMonths.addModifyListener(modifyListener);
        }
        { // Duration.Weeks
            spWeeks = createPart(area, Localization.getString("StartStateTimerDialog.duration.weeks"));
            spWeeks.addModifyListener(modifyListener);
        }
        { // Duration.Days
            spDays = createPart(area, Localization.getString("StartStateTimerDialog.duration.days"));
            spDays.addModifyListener(modifyListener);
        }
        { // Duration.Hours
            spHours = createPart(area, Localization.getString("StartStateTimerDialog.duration.hours"));
            spHours.addModifyListener(modifyListener);
        }
        { // Duration.Minutes
            Button button = new Button(area, SWT.PUSH);
            button.setImage(SharedImages.getImage("icons/clear_co.png"));
            button.setToolTipText(Localization.getString("button.restore"));
            GridData gridData = new GridData();
            gridData.horizontalAlignment = SWT.CENTER;
            button.setLayoutData(gridData);
            new Label(area, SWT.NONE);
            spMinutes = new Spinner(area, SWT.BORDER);
            spMinutes.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
            new Label(area, SWT.NONE).setText(Localization.getString("StartStateTimerDialog.duration.minutes"));
            button.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    resetUi();
                }
            });
            spMinutes.addModifyListener(modifyListener);
        }

        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                resetUi();
            }
        });
        return area;
    }

    private void resetUi() {
        chkStartDate.setSelection(false);
        chkStartTime.setSelection(false);
        chkRepeat.setSelection(false);
        chkDuration.setSelection(false);
        if (timerEventDefinition != null) {
            String[] timerParts = timerEventDefinition.split("/");
            for (String part : timerParts) {
                if (part.startsWith("R")) {
                    chkRepeat.setSelection(true);
                    txtRepeat.setText(part.substring(1));
                } else if (part.startsWith("P")) {
                    chkDuration.setSelection(true);
                    Map<Character, Integer> durationParts = parseDuration(part);
                    spYears.setSelection(asInt(durationParts.get('Y')));
                    spMonths.setSelection(asInt(durationParts.get('M')));
                    spWeeks.setSelection(asInt(durationParts.get('W')));
                    spDays.setSelection(asInt(durationParts.get('D')));
                    spHours.setSelection(asInt(durationParts.get('h')));
                    spMinutes.setSelection(asInt(durationParts.get('m')));
                } else {
                    int timePartIndex = part.indexOf("T");
                    chkStartDate.setSelection(timePartIndex != 0);
                    chkStartTime.setSelection(timePartIndex >= 0);
                    String datePart = "";
                    String timePart = "";
                    if (timePartIndex < 0) {
                        datePart = part;
                    } else {
                        datePart = part.substring(0, timePartIndex);
                        timePart = part.substring(timePartIndex + 1);
                    }
                    try {
                        if (!Strings.isNullOrEmpty(datePart)) {
                            dateFormat.parse(datePart);
                            dtDate.setYear(dateFormat.getCalendar().get(Calendar.YEAR));
                            dtDate.setMonth(dateFormat.getCalendar().get(Calendar.MONTH));
                            dtDate.setDay(dateFormat.getCalendar().get(Calendar.DAY_OF_MONTH));
                        }
                        if (!Strings.isNullOrEmpty(timePart)) {
                            timeFormat.parse(timePart);
                            dtTime.setHours(timeFormat.getCalendar().get(Calendar.HOUR_OF_DAY));
                            dtTime.setMinutes(timeFormat.getCalendar().get(Calendar.MINUTE));
                        }
                    } catch (ParseException e) {
                        PluginLogger.logError(e);
                    }
                }
            }
        }
        updateUi();
    }

    private int asInt(Integer value) {
        return value == null ? 0 : value.intValue();
    }

    private Map<Character, Integer> parseDuration(String duration) {
        boolean timePart = false;
        String value = "";
        Map<Character, Integer> durationParts = Maps.newHashMap();
        for (int i = 1; i < duration.length(); i++) {
            Character c = duration.charAt(i);
            if (c == 'T') {
                timePart = true;
            } else {
                if (Character.isDigit(c)) {
                    value += c;
                } else {
                    durationParts.put(timePart ? c.toLowerCase(c) : c, Integer.parseInt(value));
                    value = "";
                }
            }
        }
        return durationParts;
    }

    private void updateUi() {
        dtDate.setEnabled(chkStartDate.getSelection());
        dtTime.setEnabled(chkStartTime.getSelection());
        txtRepeat.setEnabled(chkRepeat.getSelection());
        if (repeat().length() > 0) {
            chkDuration.setSelection(true);
        }
        boolean durationSelected = chkDuration.getSelection();
        spYears.setEnabled(durationSelected);
        spMonths.setEnabled(durationSelected);
        spWeeks.setEnabled(durationSelected);
        spDays.setEnabled(durationSelected);
        spHours.setEnabled(durationSelected);
        spMinutes.setEnabled(durationSelected);
        Button btnOk = getButton(IDialogConstants.OK_ID);
        if (btnOk != null) {
            btnOk.setEnabled(timerDefinition().length() > 0);
        }
    }

    private void decorate(Control control, String description) {
        ControlDecoration deco = new ControlDecoration(control, SWT.TOP | SWT.RIGHT);
        deco.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
        deco.setDescriptionText(description);
        deco.setShowOnlyOnFocus(false);
    }

    private Spinner createPart(Composite parent, String label) {
        new Label(parent, SWT.NONE);
        new Label(parent, SWT.NONE);
        Spinner spinner = new Spinner(parent, SWT.BORDER);
        spinner.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        new Label(parent, SWT.NONE).setText(label);
        return spinner;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Localization.getString(Localization.getString("StartStateTimerDialog.title")));
    }

    private String repeat() {
        return chkRepeat.getSelection() && !txtRepeat.getText().equals("0") ? "R" + txtRepeat.getText() : "";
    }

    private Calendar calendar(DateTime dateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(dateTime.getYear(), dateTime.getMonth(), dateTime.getDay(), dateTime.getHours(), dateTime.getMinutes(), 0);
        return calendar;
    }

    private String date() {
        return chkStartDate.getSelection() ? dateFormat.format(calendar(dtDate).getTime()) : "";
    }

    private String time() {
        return chkStartTime.getSelection() ? "T" + timeFormat.format(calendar(dtTime).getTime()) : "";
    }

    private String duration() {
        if (((!chkStartDate.getSelection() && !chkStartTime.getSelection() || chkRepeat.getSelection())) && chkDuration.getSelection()) {
            String datePart = ifNotZero(spYears.getSelection(), "Y") + ifNotZero(spMonths.getSelection(), "M")
                    + ifNotZero(spWeeks.getSelection(), "W") + ifNotZero(spDays.getSelection(), "D");
            String timePart = ifNotZero(spHours.getSelection(), "H") + ifNotZero(spMinutes.getSelection(), "M");
            String duration = datePart + (timePart.length() > 0 ? "T" + timePart : "");
            return duration.length() > 0 ? "P" + duration : "";
        }
        return "";
    }

    private String ifNotZero(int value, String unit) {
        return value > 0 ? value + unit : "";
    }

    private String timerDefinition() {
        String repeat = repeat();
        String duration = duration();
        StringBuilder timerDefinition = new StringBuilder();
        if (repeat.length() == 0 || duration.length() > 0) {
            timerDefinition.append(repeat);
            String dateTime = date() + time();
            timerDefinition.append(timerDefinition.length() > 0 && dateTime.length() > 0 ? "/" : "").append(dateTime);
            timerDefinition.append(timerDefinition.length() > 0 && duration.length() > 0 ? "/" : "").append(duration);
        }
        return timerDefinition.toString();
    }

    public String openDialog() {
        if (open() == IDialogConstants.OK_ID) {
            return timerEventDefinition;
        }
        return null;
    }

    @Override
    public boolean close() {
        timerEventDefinition = timerDefinition();
        return super.close();
    }
}
