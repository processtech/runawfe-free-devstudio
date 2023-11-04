package ru.runa.gpd.lang.model;

import java.util.List;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.editor.graphiti.TextDecoratorEmulation;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.wfe.definition.ProcessDefinitionAccessType;

public class StartState extends FormNode implements HasTextDecorator {

    protected TextDecoratorEmulation decoratorEmulation;

    public StartState() {
        decoratorEmulation = new TextDecoratorEmulation(this);
    }

    @Override
    protected boolean allowArrivingTransition(Node source, List<Transition> transitions) {
        return false;
    }

    @Override
    protected boolean allowLeavingTransition(List<Transition> transitions) {
        return true;
    }

    @Override
    protected boolean isSwimlaneDisabled() {
        return getProcessDefinition() instanceof SubprocessDefinition;
    }

    @Override
    public void validate(List<ValidationError> errors, IFile definitionFile) {
        super.validate(errors, definitionFile);
        if (hasForm()) {
            if (getProcessDefinition() instanceof SubprocessDefinition) {
                errors.add(ValidationError.createLocalizedError(this, "startState.formIsNotUsableInEmbeddedSubprocess"));
            } else if (getProcessDefinition().getAccessType() == ProcessDefinitionAccessType.OnlySubprocess) {
                errors.add(ValidationError.createLocalizedWarning(this, "startState.formIsNotUsableInSubprocess"));
            }
        }
        if (hasFormScript()) {
            if (getProcessDefinition() instanceof SubprocessDefinition) {
                errors.add(ValidationError.createLocalizedError(this, "startState.formScriptIsNotUsableInEmbeddedSubprocess"));
            } else if (getProcessDefinition().getAccessType() == ProcessDefinitionAccessType.OnlySubprocess) {
                errors.add(ValidationError.createLocalizedWarning(this, "startState.formScriptIsNotUsableInSubprocess"));
            }
        }
        if (hasFormValidation() && getProcessDefinition() instanceof SubprocessDefinition) {
            errors.add(ValidationError.createLocalizedError(this, "startState.formValidationIsNotUsableInEmbeddedSubprocess"));
        }
        if (isSwimlaneDisabled() && getSwimlane() != null) {
            errors.add(ValidationError.createLocalizedError(this, "startState.swimlaneIsNotUsableInEmbeddedSubprocess"));
        }
    }

    @Override
    public TextDecoratorEmulation getTextDecoratorEmulation() {
        return decoratorEmulation;
    }

}
