package ru.runa.gpd.ui.custom;

import java.util.List;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;

import com.google.common.collect.Lists;

public class VariableNameChecker extends KeyAdapter {
    private static final List<Character> forbiddenCharacters = Lists.newArrayList('"', '\'', '@', '>', '<', '\\', '.', '|');

    @Override
    public void keyPressed(KeyEvent e) {
        if (forbiddenCharacters.contains(e.character)) {
            e.doit = false;
        }
    }

    public static boolean isValid(String string) {
        for (char c : string.toCharArray()) {
            if (forbiddenCharacters.contains(c)) {
                return false;
            }
        }
        if (string.startsWith(" ") || string.endsWith(" ")) {
            return false;
        }
        return true;
    }
}