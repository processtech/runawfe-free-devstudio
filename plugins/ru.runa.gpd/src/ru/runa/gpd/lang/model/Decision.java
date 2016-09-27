package ru.runa.gpd.lang.model;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.decision.IDecisionProvider;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.wfe.extension.decision.GroovyDecisionHandler;

public class Decision extends Node implements Delegable {
    public Decision() {
        setDelegationClassName(GroovyDecisionHandler.class.getName());
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.DECISION;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (isDelegable()) {
            IDecisionProvider provider = HandlerRegistry.getProvider(this);
            Collection<String> modelTransitionNames = provider.getTransitionNames(this);
            if (modelTransitionNames != null) {
                for (Transition transition : getLeavingTransitions()) {
                    if (!modelTransitionNames.remove(transition.getName())) {
                        errors.add(ValidationError.createLocalizedWarning(this, "decision.unreachableTransition", transition.getName()));
                    }
                }
            }
            for (String modelTransitionName : modelTransitionNames) {
                errors.add(ValidationError.createLocalizedError(this, "decision.transitionDoesNotExist", modelTransitionName));
            }
        }
    }
}
