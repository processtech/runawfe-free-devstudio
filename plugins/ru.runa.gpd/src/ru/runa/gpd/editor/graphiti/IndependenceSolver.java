package ru.runa.gpd.editor.graphiti;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.graphiti.features.impl.IIndependenceSolver;

public class IndependenceSolver implements IIndependenceSolver {
    private Map<String, Object> objectMap = new HashMap<String, Object>();

    @Override
    public String getKeyForBusinessObject(Object bo) {
        String result = null;
        if (bo != null) {
            result = String.valueOf(bo.hashCode());
            if (!objectMap.containsKey(result)) {
                objectMap.put(result, bo);
            }
        }
        return result;
    }

    @Override
    public Object getBusinessObjectForKey(String key) {
        return objectMap.get(key);
    }
}
