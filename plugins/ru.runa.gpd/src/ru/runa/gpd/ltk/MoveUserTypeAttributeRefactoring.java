package ru.runa.gpd.ltk;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class MoveUserTypeAttributeRefactoring extends Refactoring {
    private final IFile definitionFile;
    private final ProcessDefinition processDefinition;
    private final VariableUserType oldType;
    private final Variable attribute;
    private final Variable substitutionVariable;
    private final List<Refactoring> refactorings = Lists.newArrayList();

    /**
     * Move to top level variables mode
     */
    public MoveUserTypeAttributeRefactoring(IFile definitionFile, ProcessDefinition processDefinition, VariableUserType oldType, Variable attribute) {
        this.definitionFile = definitionFile;
        this.processDefinition = processDefinition;
        this.oldType = oldType;
        this.attribute = attribute;
        this.substitutionVariable = null;
    }

    /**
     * Move to another user type mode
     */
    public MoveUserTypeAttributeRefactoring(IFile definitionFile, ProcessDefinition processDefinition, VariableUserType oldType, Variable attribute,
            Variable substitutionVariable) {
        this.definitionFile = definitionFile;
        this.processDefinition = processDefinition;
        this.oldType = oldType;
        this.attribute = attribute;
        this.substitutionVariable = substitutionVariable;
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) {
        RefactoringStatus status = new RefactoringStatus();
        List<Variable> result = VariableUtils.findVariablesOfTypeWithAttributeExpanded(processDefinition, oldType, attribute);
        for (Variable variable : result) {
            String newName;
            String newScriptingName;
            if (substitutionVariable != null) {
                newName = substitutionVariable.getName() + VariableUserType.DELIM + attribute.getName();
                newScriptingName = substitutionVariable.getScriptingName() + VariableUserType.DELIM + attribute.getScriptingName();
            } else {
                newName = attribute.getName();
                newScriptingName = attribute.getScriptingName();
            }
            RenameVariableRefactoring refactoring = new RenameVariableRefactoring(definitionFile, processDefinition, variable, newName,
                    newScriptingName);
            status.merge(refactoring.checkInitialConditions(progressMonitor));
            refactorings.add(refactoring);
        }
        return status;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return new RefactoringStatus();
    }

    @Override
    public CompositeChange createChange(IProgressMonitor progressMonitor) throws CoreException {
        CompositeChange compositeChange = new CompositeChange(getName());
        for (Refactoring refactoring : refactorings) {
            compositeChange.merge((CompositeChange) refactoring.createChange(progressMonitor));
        }
        return compositeChange;
    }

    public boolean isUserInteractionNeeded() {
        try {
            checkInitialConditions(null);
            return createChange(null).getChildren().length > 0;
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return processDefinition.getName();
    }
}
