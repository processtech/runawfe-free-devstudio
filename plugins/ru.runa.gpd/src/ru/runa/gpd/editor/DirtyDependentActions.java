package ru.runa.gpd.editor;

import com.google.common.collect.Maps;
import java.util.Map;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IActionDelegate;

public class DirtyDependentActions {

    private static Map<IActionDelegate, IAction> actionMap = Maps.newHashMap();

    public static void add(IActionDelegate actionDelegate, IAction action) {
        if (!actionMap.containsKey(actionDelegate)) {
            actionMap.put(actionDelegate, action);
        }
    }

    public static IAction remove(IActionDelegate actionDelegate) {
        return actionMap.remove(actionDelegate);
    }

    public static void update() {
        actionMap.entrySet().stream().forEach(entry -> entry.getKey().selectionChanged(entry.getValue(), null));
    }
}
