package ru.runa.gpd.editor.graphiti;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.graphiti.features.impl.IIndependenceSolver;

public class IndependenceSolver implements IIndependenceSolver {
    // rm3262#note-11 static for memory optimization
    // despite of one instance per DiagramFeatureProvider, all instances are called with all opened diagram entities
    private static Map<String, Object> objectMap = new HashMap<>();

    @Override
    public String getKeyForBusinessObject(Object bo) {
        String result = null;
        if (bo != null) {
            result = String.valueOf(System.identityHashCode(bo));
            objectMap.put(result, bo);
        }
        return result;
    }

    @Override
    public Object getBusinessObjectForKey(String key) {
        return objectMap.get(key);
    }
}
