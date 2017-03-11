package ru.runa.gpd.ui.custom;

import java.util.List;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import com.google.common.collect.Lists;

public class JavaIdentifierChecker extends KeyAdapter {
    private static final List<Character> forbiddenCharacters = Lists.newArrayList('"', '\'', '@', '&', '>', '<', '\\', '.', '|');

    @Override
    public void keyPressed(KeyEvent e) {
        if (!Character.isJavaIdentifierPart(e.character)) {
            e.doit = false;
        }
    }

    public static boolean isValid(String string) {
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == 0) {
                if (!Character.isJavaIdentifierStart(chars[i])) {
                    return false;
                }
            } else {
                if (!Character.isJavaIdentifierPart(chars[i])) {
                    return false;
                }
            }
        }
        return true;
    }
}