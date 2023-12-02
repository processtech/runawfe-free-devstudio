package ru.runa.gpd.extension.decision;

import java.util.Collection;
import java.util.HashSet;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.lang.model.Decision;

public class DefaultDecisionProvider extends DelegableProvider implements IDecisionProvider {

    @Override
    public Collection<String> getTransitionNames(Decision decision) {
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
