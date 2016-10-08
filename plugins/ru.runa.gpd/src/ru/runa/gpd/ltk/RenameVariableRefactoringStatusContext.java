package ru.runa.gpd.ltk;

import org.eclipse.core.resources.IFolder;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

public class RenameVariableRefactoringStatusContext extends RefactoringStatusContext {
    private final VariableRenameProvider<?> provider;
    private final IFolder definitionFolder;

    public RenameVariableRefactoringStatusContext(VariableRenameProvider<?> provider, IFolder definitionFolder) {
        super();
        this.provider = provider;
        this.definitionFolder = definitionFolder;
    }

    @Override
    public Object getCorrespondingElement() {
        return provider;
    }

    public IFolder getDefinitionFolder() {
        return definitionFolder;
    }
}
