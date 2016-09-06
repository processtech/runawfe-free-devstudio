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

public class RenameUserTypeAttributeRefactoring extends Refactoring {
    private final IFile definitionFile;
    private final ProcessDefinition processDefinition;
    private final VariableUserType type;
    private final Variable oldAttribute;
    private final String newAttributeName;
    private final String newAttributeScriptingName;
    private final List<RenameVariableRefactoring> refactorings = Lists.newArrayList();
    private final RefactoringStatus finalStatus;

    public RenameUserTypeAttributeRefactoring(IFile definitionFile, ProcessDefinition processDefinition, VariableUserType type,
            Variable oldAttribute, String newAttributeName, String newAttributeScriptingName) {
        this.definitionFile = definitionFile;
        this.processDefinition = processDefinition;
        this.type = type;
        this.oldAttribute = oldAttribute;
        this.newAttributeName = newAttributeName;
        this.newAttributeScriptingName = newAttributeScriptingName;
        this.finalStatus = new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor progressMonitor) {
        RefactoringStatus status = new RefactoringStatus();
        try {
            if (refactorings.size() == 0) {
                List<Variable> result = VariableUtils.findVariablesOfTypeWithAttributeExpanded(processDefinition, type, oldAttribute);
                for (Variable variable : result) {
                    String newName = variable.getName().replace(VariableUserType.DELIM + oldAttribute.getName(),
                            VariableUserType.DELIM + newAttributeName);
                    String newScriptingName = variable.getScriptingName().replace(VariableUserType.DELIM + oldAttribute.getScriptingName(),
                            VariableUserType.DELIM + newAttributeScriptingName);
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

    @Override
    public CompositeChange createChange(IProgressMonitor progressMonitor) throws CoreException {
        return RefactoringUtils.createChangeUserTypeAttribute(getName(), progressMonitor, refactorings, finalStatus);
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
