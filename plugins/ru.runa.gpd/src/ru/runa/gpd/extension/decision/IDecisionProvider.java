package ru.runa.gpd.extension.decision;

import java.util.Collection;
import ru.runa.gpd.lang.model.Decision;

public interface IDecisionProvider {

    /**
     * @return null if configuration is correct but it's impossible to parse it
     */
    public Collection<String> getTransitionNames(Decision decision);
    
    public void transitionRenamed(Decision decision, String oldName, String newName);
    
    public String getDefaultTransitionName(Decision decision);
    
}
