package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;

public class RenameUserTypeRefactoring extends Refactoring {

    private final ProcessDefinition mainProcessDefinition;
    private final VariableUserType oldUserType;
    private final VariableUserType newUserType;
    private final RefactoringStatus finalStatus;

    private final List<SingleVariableRenameProvider<?>> cache = new ArrayList<>();

    private CompositeChange cachedChange;

    public RenameUserTypeRefactoring(
            ProcessDefinition processDefinition,
            VariableUserType oldUserType,
            String newTypeName) {

        this.mainProcessDefinition = processDefinition.getMainProcessDefinition();
        this.oldUserType = oldUserType;
        this.newUserType = oldUserType.getCopy();
        this.newUserType.setName(newTypeName);
        this.finalStatus = new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
        RefactoringStatus result = new RefactoringStatus();

        try {
            if (cache.isEmpty()) {
                init(mainProcessDefinition);
                for (SubprocessDefinition subprocessDefinition : mainProcessDefinition.getEmbeddedSubprocesses().values()) {
                    init(subprocessDefinition);
                }
            }
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog(e.getMessage(), e);
            result.addFatalError(Localization.getString("UnhandledException") + ": " + e.getLocalizedMessage());
        }

        return result;
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return finalStatus;
    }

    @Override
    public CompositeChange createChange(IProgressMonitor pm) throws CoreException {
        if (cachedChange == null) {
            cachedChange = new CompositeChange(getName());
            for (SingleVariableRenameProvider<?> provider : cache) {
                try {
                    List<Change> changes = provider.getChanges(oldUserType, newUserType);
                    cachedChange.addAll(changes.toArray(new Change[changes.size()]));
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("Unable to get UserType changes in " + provider.getElement(), e);

                }
            }
        }

        return cachedChange;
    }

    @Override
    public String getName() {
        return mainProcessDefinition.getName();
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
            throw new RuntimeException(e);
        }
    }

    private void init(ProcessDefinition processDefinition) {
        List<ScriptTask> scriptTasks = processDefinition.getChildrenRecursive(ScriptTask.class);
        for (ScriptTask scriptTask : scriptTasks) {
            cache.add(new DelegablePresentation(scriptTask));
        }
    }
}
