package ru.runa.gpd.ltk;

import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class RenameVariableRefactoringStatusContext extends RefactoringStatusContext {
    private final Exception exception;

    public RenameVariableRefactoringStatusContext(Exception exception) {
        super();
        this.exception = exception;
    }

    @Override
    public Object getCorrespondingElement() {
        return exception;
    }
}
