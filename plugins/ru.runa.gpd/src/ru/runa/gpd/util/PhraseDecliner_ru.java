package ru.runa.gpd.util;

import org.eclipse.osgi.util.NLS;

import ru.runa.gpd.PluginLogger;

public class PhraseDecliner_ru extends PhraseDecliner {
    @Override
    public String declineDuration(String delay, String unit) {
        Rule rule = Rule.find(unit);
        if (rule == null) {
            PluginLogger.logErrorWithoutDialog("No rule found for '" + unit + "'");
            return super.declineDuration(delay, unit);
        }
        int pos;
        if (delay.endsWith("11") || delay.endsWith("12") || delay.endsWith("13") || delay.endsWith("14")) {
            pos = 2;
        } else if (delay.endsWith("2") || delay.endsWith("3") || delay.endsWith("4")) {
            pos = 1;
        } else if (delay.endsWith("1")) {
            pos = 0;
        } else {
            pos = 2;
        }
        return delay + " " + applyRule(unit, rule, pos);
    }

    private String applyRule(String base, Rule rule, int pos) {
        String[] words = base.split(" ", -1);
        String result = "";
        int i = 0;
        int ri = pos * 2;
        if (words.length == 2) {
            result += words[i].substring(0, words[i].length() - rule.charsToRemove[ri]);
            result += rule.charsToAdd[ri];
            result += " ";
            i++;
        }
        result += words[i].substring(0, words[i].length() - rule.charsToRemove[ri + 1]);
        result += rule.charsToAdd[ri + 1];
        return result;
    }

    public static class Messages extends NLS {
        private static final String BUNDLE_NAME = "ru.runa.gpd.util.PhraseDecliner";
        public static String years;
        public static String months;
        public static String weeks;
        public static String days;
        public static String hours;
        public static String minutes;
        public static String seconds;
        public static String a;
        public static String x;
        public static String ya;
        public static String aya;
        public static String y;
        public static String ey;
        public static String ov;
        public static String let;
        public static String en;
        public static String ev;
        public static String soft;
        static {
            NLS.initializeMessages(BUNDLE_NAME, PhraseDecliner_ru.Messages.class);
        }
    }

    private static enum Rule {
        MIN(new int[] { 2, 1, 0, 0, 1, 1 }, new String[] { Messages.aya, Messages.a, "", "", Messages.x, "" }),
        //
        W(new int[] { 2, 1, 0, 0, 1, 1 }, new String[] { Messages.aya, Messages.ya, "", "", Messages.x, Messages.soft }),
        //
        D(new int[] { 1, 2, 1, 1, 1, 1 }, new String[] { Messages.y, Messages.en, Messages.x, Messages.ya, Messages.x, Messages.ey }),
        //
        H(new int[] { 1, 1, 1, 1, 1, 1 }, new String[] { Messages.y, "", Messages.x, Messages.a, Messages.x, Messages.ov }),
        //
        Y(new int[] { 1, 1, 1, 1, 1, 4 }, new String[] { Messages.y, "", Messages.x, Messages.a, Messages.x, Messages.let }),
        //
        MONTH(new int[] { 1, 1, 1, 1, 1, 1 }, new String[] { Messages.y, "", Messages.x, Messages.a, Messages.x, Messages.ev });
        private final int[] charsToRemove;
        private final String[] charsToAdd;

        private Rule(int[] charsToRemove, String[] charsToAdd) {
            this.charsToRemove = charsToRemove;
            this.charsToAdd = charsToAdd;
        }

        public static Rule find(String base) {
            if (base.contains(Messages.hours)) {
                return H;
            } else if (base.contains(Messages.minutes)) {
                return MIN;
            } else if (base.contains(Messages.days)) {
                return D;
            } else if (base.contains(Messages.weeks)) {
                return W;
            } else if (base.contains(Messages.months)) {
                return MONTH;
            } else if (base.contains(Messages.years)) {
                return Y;
            } else if (base.contains(Messages.seconds)) {
                return MIN;
            } else {
                return null;
            }
        }
    }
}
