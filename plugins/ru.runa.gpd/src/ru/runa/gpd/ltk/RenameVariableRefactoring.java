package ru.runa.gpd.ltk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.Decision;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.model.jpdl.Action;
import ru.runa.gpd.util.VariableUtils;

@SuppressWarnings("unchecked")
public class RenameVariableRefactoring extends Refactoring {
    private final List<VariableRenameProvider<?>> cache = new ArrayList<VariableRenameProvider<?>>();
    private final IFolder definitionFolder;
    private final ProcessDefinition mainProcessDefinition;
    private final SortedMap<Variable, Variable> variablesMap;
    private final RefactoringStatus finalStatus;

    public RenameVariableRefactoring(IFile definitionFile, ProcessDefinition definition, Variable oldVariable, String newName, String newScriptingName) {
        this.definitionFolder = (IFolder) definitionFile.getParent();
        this.mainProcessDefinition = definition.getMainProcessDefinition();
        this.variablesMap = new TreeMap<Variable, Variable>(new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        Variable newVariable = new Variable(newName, newScriptingName, oldVariable);
        this.variablesMap.put(oldVariable, newVariable);
        if (oldVariable.isComplex()) {
            for (Variable oldVariableAttribute : VariableUtils.expandComplexVariable(oldVariable, oldVariable)) {
                String newVariableName = oldVariableAttribute.getName().replaceFirst(oldVariable.getName() + ".", newVariable.getName() + ".");
                String newVariableScriptingName = oldVariableAttribute.getScriptingName().replaceFirst(oldVariable.getScriptingName() + ".",
                        newVariable.getScriptingName() + ".");
                variablesMap.put(oldVariableAttribute, new Variable(newVariableName, newVariableScriptingName, oldVariableAttribute));
            }
        }
        this.finalStatus = new RefactoringStatus();
    }

    @Override
    public RefactoringStatus checkInitialConditions(IProgressMonitor pm) {
        RefactoringStatus result = new RefactoringStatus();
        try {
            if (cache.size() == 0) {
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

    private void init(ProcessDefinition processDefinition) {
        List<FormNode> formNodes = processDefinition.getChildren(FormNode.class);
        for (FormNode formNode : formNodes) {
            cache.add(new FormNodePresentation(definitionFolder, formNode));
        }
        List<TaskState> stateNodes = processDefinition.getChildren(TaskState.class);
        for (TaskState taskState : stateNodes) {
            cache.add(new TimedPresentation(taskState));
        }
        List<Timer> timers = processDefinition.getChildren(Timer.class);
        for (Timer timer : timers) {
            cache.add(new TimerPresentation(timer));
        }
        List<MultiTaskState> multiTaskNodes = processDefinition.getChildren(MultiTaskState.class);
        for (MultiTaskState multiTaskNode : multiTaskNodes) {
            cache.add(new MultiTaskPresentation(multiTaskNode));
        }
        List<TaskState> taskStates = processDefinition.getChildren(TaskState.class);
        for (TaskState taskState : taskStates) {
            if (taskState.getBotTaskLink() != null) {
                cache.add(new BotTaskLinkParametersRenameProvider(taskState.getBotTaskLink()));
            } else {
                String botName = taskState.getSwimlaneBotName();
                if (botName != null) {
                    // if bot task exists with same as task name
                    BotTask botTask = BotCache.getBotTask(botName, taskState.getName());
                    if (botTask != null && botTask.getType() == BotTaskType.SIMPLE) {
                        cache.add(new BotTaskConfigRenameProvider(botTask));
                    }
                }
            }
        }
        List<Action> actions = processDefinition.getChildrenRecursive(Action.class);
        for (Action action : actions) {
            cache.add(new DelegablePresentation(action));
        }
        List<ScriptTask> scriptTasks = processDefinition.getChildrenRecursive(ScriptTask.class);
        for (ScriptTask scriptTask : scriptTasks) {
            cache.add(new DelegablePresentation(scriptTask));
        }
        List<Decision> decisions = processDefinition.getChildren(Decision.class);
        for (Decision decision : decisions) {
            cache.add(new DelegablePresentation(decision));
        }
        List<Subprocess> subprocesses = processDefinition.getChildren(Subprocess.class);
        for (Subprocess subprocess : subprocesses) {
            cache.add(new SubprocessPresentation(subprocess));
        }
        List<Swimlane> swimlaneNodes = processDefinition.getChildren(Swimlane.class);
        for (Swimlane swimlaneNode : swimlaneNodes) {
            cache.add(new SwimlanePresentation(swimlaneNode));
        }
        List<NodeTypeDefinition> typesWithProvider = NodeRegistry.getTypesWithVariableRenameProvider();
        for (NodeTypeDefinition elementTypeDefinition : typesWithProvider) {
            List<? extends GraphElement> list = processDefinition.getChildrenRecursive(elementTypeDefinition.getModelClass());
            for (GraphElement graphElement : list) {
                VariableRenameProvider provider = elementTypeDefinition.createVariableRenameProvider();
                provider.setElement(graphElement);
                cache.add(provider);
            }
        }
    }

    @Override
    public RefactoringStatus checkFinalConditions(IProgressMonitor pm) {
        return finalStatus;
    }

    private CompositeChange cashedChange = null;

    @Override
    public CompositeChange createChange(IProgressMonitor pm) {
        if (cashedChange == null) {
            cashedChange = new CompositeChange(getName());
            for (VariableRenameProvider<?> classPresentation : cache) {
                try {
                    List<Change> changes = classPresentation.getChanges(variablesMap);
                    cashedChange.addAll(changes.toArray(new Change[changes.size()]));
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("Unable to get used variabes in " + classPresentation.element, e);
                    RenameVariableRefactoringStatusContext context = new RenameVariableRefactoringStatusContext(classPresentation, definitionFolder);
                    finalStatus.addWarning(
                            Localization.getString("RenameVariableException") + classPresentation.element + ": " + e.getLocalizedMessage(), context);
                }
            }
        }
        return cashedChange;
    }

    public boolean isUserInteractionNeeded() {
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
    }

    @Override
    public String getName() {
        return mainProcessDefinition.getName();
    }
}
