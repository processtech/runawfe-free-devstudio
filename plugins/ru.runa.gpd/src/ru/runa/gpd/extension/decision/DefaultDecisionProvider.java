package ru.runa.gpd.extension.decision;

import java.util.HashSet;
import java.util.Set;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.model.Decision;

public class DefaultDecisionProvider extends DelegableProvider implements IDecisionProvider {

    @Override
    public Set<String> getTransitionNames(Decision decision) {
        return new HashSet<String>();
    }
    
    @Override
    public void transitionRenamed(Decision decision, String oldName, String newName) {
        
    }

    @Override
    public String getDefaultTransitionName(Decision decision) {
        return null;
    }
    
}
