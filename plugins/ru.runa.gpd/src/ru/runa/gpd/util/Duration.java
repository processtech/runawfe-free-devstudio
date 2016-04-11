package ru.runa.gpd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.Localization;

import com.google.common.base.Objects;

public class Duration {
    public static final String CURRENT_DATE_MESSAGE = Localization.getString("duration.baseDateNow");
    public static final String NO_DELAY_MESSAGE = Localization.getString("duration.nodelay");
    private static Pattern PATTERN_VAR = Pattern.compile("#\\{(.*)}");
    private static final List<Unit> units = new ArrayList<Unit>();
    static {
        units.add(new Unit("minutes"));
        units.add(new Unit("business minutes"));
        units.add(new Unit("hours"));
        units.add(new Unit("business hours"));
        units.add(new Unit("days"));
        units.add(new Unit("business days"));
        units.add(new Unit("weeks"));
        units.add(new Unit("business weeks"));
        units.add(new Unit("months"));
        units.add(new Unit("business months"));
        units.add(new Unit("years"));
        units.add(new Unit("business years"));
        units.add(new Unit("seconds"));
    }
    private String delay;
    private Unit unit;
    private String variableName;

    public Duration() {
        this("0 minutes");
    }

    public Duration(String duration) {
        if (duration == null) {
            throw new NullPointerException("duration is null");
        }
        Matcher matcher = PATTERN_VAR.matcher(duration);
        String sign = "";
        if (matcher.find()) {
            variableName = matcher.group(1);
            duration = duration.substring(matcher.end()).trim();
            sign = duration.substring(0, 1);
            duration = duration.substring(1).trim();
        }
        int backspaceIndex = duration.indexOf(" ");
        delay = sign + duration.substring(0, backspaceIndex);
        String unitValue = duration.substring(backspaceIndex + 1);
        for (Unit unit : units) {
            if (unit.value.equals(unitValue)) {
                setUnit(unit);
                break;
            }
        }
        delay = delay.trim();
    }

    public Duration(Duration duration) {
        this.delay = duration.getDelay();
        this.unit = duration.getUnit();
        this.variableName = duration.getVariableName();
    }

    public boolean hasDuration() {
        return !"0".equals(delay) || variableName != null;
    }

    public String getDuration() {
        String duration = "";
        if (variableName != null) {
            duration = "#{" + variableName + "} ";
            if (delay.charAt(0) != '-' && delay.charAt(0) != '+') {
                delay = "+ " + delay;
            } else if (delay.charAt(1) != ' ') {
                delay = delay.substring(0, 1) + " " + delay.substring(1);
            }
        } else {
            delay = delay.replaceAll(" ", "");
        }
        duration += delay + " " + unit.value;
        return duration;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        if (CURRENT_DATE_MESSAGE.equals(variableName)) {
            variableName = null;
        }
        this.variableName = variableName;
    }

    public static List<Unit> getUnits() {
        return units;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(variableName, delay, unit);
    }

    @Override
    public boolean equals(Object obj) {
        Duration d = (Duration) obj;
        return Objects.equal(variableName, d.variableName) && Objects.equal(delay, d.delay) && Objects.equal(unit, d.unit);
    }

    @Override
    public String toString() {
        if (!hasDuration()) {
            return NO_DELAY_MESSAGE;
        }
        String duration = "";
        String delayValue;
        if (variableName != null) {
            duration = variableName;
            if (delay.charAt(0) != '-' && delay.charAt(0) != '+') {
                delayValue = "+ " + delay;
            } else if (delay.charAt(1) != ' ') {
                delayValue = delay.substring(0, 1) + " " + delay.substring(1);
            } else {
                delayValue = delay;
            }
        } else {
            delayValue = delay.replaceAll(" ", "");
        }
        if (!"+ 0".equals(delayValue)) {
            PhraseDecliner decliner = PhraseDeclinerFactory.getDecliner();
            duration += " " + decliner.declineDuration(delayValue, unit.label);
        }
        return duration;
    }

    public static class Unit implements Comparable<Unit> {
        private final String value;
        private final String label;

        public Unit(String value) {
            this.value = value;
            this.label = Localization.getString("unit." + value.replaceAll(" ", ""));
        }

        @Override
        public String toString() {
            return label;
        }

        @Override
        public int compareTo(Unit unit) {
            return label.compareTo(unit.label);
        }

    }
}
