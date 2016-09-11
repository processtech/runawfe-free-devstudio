package ru.runa.gpd.ltk;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
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
    private final List<RenameVariableRefactoring> refactorings = Lists.newArrayList();
    private final RefactoringStatus finalStatus;

    /**
     * Move to top level variables mode
     */
    public MoveUserTypeAttributeRefactoring(IFile definitionFile, ProcessDefinition processDefinition, VariableUserType oldType, Variable attribute) {
        this.definitionFile = definitionFile;
        this.processDefinition = processDefinition;
        this.oldType = oldType;
        this.attribute = attribute;
        this.substitutionVariable = null;
        this.finalStatus = new RefactoringStatus();
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
        this.finalStatus = new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) {
        RefactoringStatus status = new RefactoringStatus();
        try {
            if (refactorings.size() == 0) {
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
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
            status.addFatalError(Localization.getString("UnhandledException") + ": " + e.getLocalizedMessage());
        }
        return status;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return finalStatus;
    }

    private CompositeChange cashedChange = null;

    @Override
    public CompositeChange createChange(IProgressMonitor progressMonitor) throws CoreException {
        if (cashedChange == null) {
            cashedChange = RefactoringUtils.createChangeUserTypeAttribute(getName(), progressMonitor, refactorings, finalStatus);
        }
        return cashedChange;
    }

    public boolean isUserInteractionNeeded() {
        try {
            RefactoringStatus initialStatus = checkInitialConditions(null);
            if (initialStatus.hasFatalError()) {
                finalStatus.merge(initialStatus);
                return true;
            }
            CompositeChange change = createChange(null);
            if (finalStatus.hasWarning() || finalStatus.hasError() || finalStatus.hasFatalError()) {
                return true;
            }
            return change.getChildren().length > 0;
        } catch (CoreException e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return processDefinition.getName();
    }
}
