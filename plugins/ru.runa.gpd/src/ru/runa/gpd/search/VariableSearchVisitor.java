package ru.runa.gpd.search;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.search.internal.ui.SearchMessages;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.Match;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskLink;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.MessageNode;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.bpmn.BusinessRule;
import ru.runa.gpd.lang.model.bpmn.ExclusiveGateway;
import ru.runa.gpd.lang.model.bpmn.ScriptTask;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;

public class VariableSearchVisitor {

    public static final String REGEX_SCRIPT_VARIABLE = "[\"'{(,\\s=]%s[\"'}),.;\\s=]";

    private final VariableSearchQuery query;
    private IProgressMonitor progressMonitor;
    private int numberOfScannedElements;
    private int numberOfElementsToScan;
    private GraphElement currentElement = null;
    private final MultiStatus status;
    private final Matcher matcherByVariable;
    private final Matcher matcherByScriptingVariable;
    private final Matcher scriptMatcherByVariable;
    private final Matcher scriptMatcherByScriptingVariable;
    private final String regexScriptVariableWithCheckAfterDot;
    private Set<VariableSearchTarget> searchTargets;

    public VariableSearchVisitor(VariableSearchQuery query, Set<VariableSearchTarget> searchTargets) {
        this.query = query;
        this.status = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
        this.regexScriptVariableWithCheckAfterDot = "(^|!|[\"'{(,\\s=])%s(\"|'|}|\\)|,|;|\\s|=|(\\.(?!"
                + getSearchedVariablesScriptingNames(query.getVariable()) + "))|$)";
        this.scriptMatcherByVariable = Pattern.compile(String.format(regexScriptVariableWithCheckAfterDot, Pattern.quote(query.getSearchText())))
                .matcher("");
        this.scriptMatcherByScriptingVariable = Pattern
                .compile(String.format(regexScriptVariableWithCheckAfterDot, query.getVariable().getScriptingName()))
                .matcher("");
        this.matcherByVariable = Pattern.compile("(\"|>)" + Pattern.quote(query.getSearchText()) + "(<|\")").matcher("");
        this.matcherByScriptingVariable = Pattern.compile(Pattern.quote(query.getVariable().getScriptingName())).matcher("");
        this.searchTargets = searchTargets;
    }

    public IStatus search(SearchResult searchResult, IProgressMonitor monitor) {
        Map<ProcessDefinition, IFile> map = Maps.newHashMap();
        map.put(query.getMainProcessDefinition(), query.getMainProcessdefinitionFile());
        numberOfElementsToScan = query.getMainProcessDefinition().getChildrenRecursive(GraphElement.class).size();
        for (SubprocessDefinition subprocessDefinition : query.getMainProcessDefinition().getEmbeddedSubprocesses().values()) {
            map.put(subprocessDefinition, subprocessDefinition.getFile());
            numberOfElementsToScan = subprocessDefinition.getChildrenRecursive(GraphElement.class).size();
        }
        progressMonitor = monitor == null ? new NullProgressMonitor() : monitor;
        numberOfScannedElements = 0;
        Job monitorUpdateJob = new Job(SearchMessages.TextSearchVisitor_progress_updating_job) {
            private int lastNumberOfScannedElements = 0;

            @Override
            public IStatus run(IProgressMonitor inner) {
                while (!inner.isCanceled()) {
                    if (currentElement != null && currentElement instanceof NamedGraphElement) {
                        String name = ((NamedGraphElement) currentElement).getName();
                        Object[] args = { name, numberOfScannedElements, numberOfElementsToScan };
                        progressMonitor.subTask(MessageFormat.format(SearchMessages.TextSearchVisitor_scanning, args));
                        int steps = numberOfScannedElements - lastNumberOfScannedElements;
                        progressMonitor.worked(steps);
                        lastNumberOfScannedElements += steps;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                }
                return Status.OK_STATUS;
            }
        };
        try {
            String taskName = SearchMessages.TextSearchVisitor_filesearch_task_label;
            progressMonitor.beginTask(taskName, numberOfElementsToScan);
            monitorUpdateJob.setSystem(true);
            monitorUpdateJob.schedule();
            try {
                for (Map.Entry<ProcessDefinition, IFile> entry : map.entrySet()) {
                    processNode(entry.getValue(), entry.getKey());
                    List<GraphElement> children = entry.getKey().getChildrenRecursive(GraphElement.class);
                    for (GraphElement child : children) {
                        processNode(entry.getValue(), child);
                        if (progressMonitor.isCanceled()) {
                            throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
                        }
                    }
                }
                return status;
            } finally {
                monitorUpdateJob.cancel();
            }
        } finally {
            progressMonitor.done();
        }
    }

    public IStatus search(IProgressMonitor monitor) {
        return search(query.getSearchResult(), monitor);
    }

    private void processNode(IFile definitionFile, GraphElement graphElement) {
        try {
            currentElement = graphElement;
            if (graphElement instanceof FormNode) {
                processFormNode(definitionFile, (FormNode) graphElement);
            }
            if (graphElement instanceof Delegable) {
                processDelegableNode(definitionFile, (Delegable) graphElement);
            }
            if (graphElement instanceof ITimed && searchTargets.contains(VariableSearchTarget.TIMER)) {
                processTimedNode(definitionFile, (ITimed) graphElement);
            }
            if (graphElement instanceof Timer && searchTargets.contains(VariableSearchTarget.TIMER)) {
                processTimer(definitionFile, (Timer) graphElement, graphElement);
            }
            if (graphElement instanceof Subprocess) {
                processSubprocessNode(definitionFile, (Subprocess) graphElement);
            }
            if (graphElement instanceof MessageNode
                    && searchTargets.contains(VariableSearchTarget.MESSAGING_NODE)) {
                processMessagingNode(definitionFile, (MessageNode) graphElement);
            }
            if (graphElement instanceof TaskState) {
                processTaskNode(definitionFile, (TaskState) graphElement);
            }
            if (graphElement instanceof MultiTaskState) {
                processMultiTaskNode(definitionFile, (MultiTaskState) graphElement);
            }
            if (graphElement instanceof ProcessDefinition
                    && searchTargets.contains(VariableSearchTarget.FORM_SCRIPT)) {
                processProcessDefinitionNode(definitionFile, (ProcessDefinition) graphElement);
            }
        } catch (Exception e) {
            status.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
        } finally {
            numberOfScannedElements++;
        }
    }

    private void processProcessDefinitionNode(IFile definitionFile, ProcessDefinition processDefinition) throws Exception {
        if (processDefinition instanceof SubprocessDefinition) {
            return;
        }
        IFile file = IOUtils.getAdjacentFile(definitionFile, ParContentProvider.FORM_JS_FILE_NAME);
        if (file.exists()) {
            ElementMatch elementMatch = new ElementMatch(processDefinition, file, ElementMatch.CONTEXT_FORM_SCRIPT);
            List<Match> matches = findInFile(elementMatch, file, scriptMatcherByScriptingVariable);
            elementMatch.setMatchesCount(matches.size());
            if (!query.getVariable().getName().equals(query.getVariable().getScriptingName())) {
                matches.addAll(findInFile(elementMatch, file, scriptMatcherByVariable));
            }
            elementMatch.setPotentialMatchesCount(matches.size() - elementMatch.getMatchesCount());
            for (Match match : matches) {
                query.getSearchResult().addMatch(match);
            }
        }
    }

    private boolean isNotPredefinedSearchTarget(Delegable delegable) {
        return !(delegable instanceof ScriptTask || delegable instanceof ExclusiveGateway || delegable instanceof Swimlane
                || delegable instanceof BotTask || delegable instanceof BusinessRule);
    };

    private void processDelegableNode(IFile definitionFile, Delegable delegable) throws Exception {
        if (delegable instanceof ScriptTask && searchTargets.contains(VariableSearchTarget.SCRIPT_TASK)
                || delegable instanceof ExclusiveGateway
                        && searchTargets.contains(VariableSearchTarget.EXCLUSIVE_GATEWAY)
                || delegable instanceof Swimlane && searchTargets.contains(VariableSearchTarget.SWIMLANE)
                || (delegable instanceof BotTask || delegable instanceof BotTaskLink)
                        && searchTargets.contains(VariableSearchTarget.BOT_TASK)
                || delegable instanceof BusinessRule && searchTargets.contains(VariableSearchTarget.BUSINESS_RULE)
                || isNotPredefinedSearchTarget(delegable)) {
            Matcher delegableMatcher;
            if (HandlerRegistry.SCRIPT_HANDLER_CLASS_NAMES.contains(delegable.getDelegationClassName())) {
                delegableMatcher = scriptMatcherByScriptingVariable;
            } else {
                delegableMatcher = scriptMatcherByVariable;
            }
            String conf = delegable.getDelegationConfiguration();
            ElementMatch elementMatch = new ElementMatch((GraphElement) delegable, definitionFile);
            List<Match> matches = findInString(elementMatch, "(" + conf + ")", delegableMatcher);
            elementMatch.setPotentialMatchesCount(matches.size());
            for (Match match : matches) {
                query.getSearchResult().addMatch(match);
            }
            if (matches.isEmpty() && !HandlerRegistry.SCRIPT_HANDLER_CLASS_NAMES.contains(delegable.getDelegationClassName())
                    && !query.getVariable().getName().equals(query.getVariable().getScriptingName())) {
                matches = findInString(elementMatch, "(" + conf + ")", scriptMatcherByScriptingVariable);
                elementMatch.setPotentialMatchesCount(matches.size());
                for (Match match : matches) {
                    query.getSearchResult().addMatch(match);
                }
            }
        }
    }

    private void processTimedNode(IFile definitionFile, ITimed timedNode) throws Exception {
        Timer timer = timedNode.getTimer();
        if (timer == null) {
            return;
        }
        processTimer(definitionFile, timer, (GraphElement) timedNode);
    }

    private void processTimer(IFile definitionFile, Timer timer, GraphElement node) throws Exception {
        if (query.getSearchText().equals(timer.getDelay().getVariableName())) {
            ElementMatch elementMatch = new ElementMatch(node, definitionFile, ElementMatch.CONTEXT_TIMED_VARIABLE);
            elementMatch.setMatchesCount(1);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processSubprocessNode(IFile definitionFile, Subprocess subprocessNode) throws Exception {
        int matchesCount = findInVariableMappings(subprocessNode.getVariableMappings());
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(subprocessNode, definitionFile);
            elementMatch.setMatchesCount(matchesCount);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processMessagingNode(IFile definitionFile, MessageNode messageNode) throws Exception {
        int matchesCount = findInVariableMappings(messageNode.getVariableMappings());
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(messageNode, definitionFile);
            elementMatch.setMatchesCount(matchesCount);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processTaskNode(IFile definitionFile, TaskState state) throws Exception {
        int matchesCount = 0;
        if (state.getTimeOutDelay() != null && Objects.equal(query.getSearchText(), state.getTimeOutDelay().getVariableName())) {
            matchesCount++;
        }
        if (state.getEscalationDelay() != null && Objects.equal(query.getSearchText(), state.getEscalationDelay().getVariableName())) {
            matchesCount++;
        }
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(state, definitionFile);
            elementMatch.setMatchesCount(matchesCount);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processMultiTaskNode(IFile definitionFile, MultiTaskState state) throws Exception {
        int matchesCount = 0;
        VariableMapping discriminatorMapping = state.getDiscriminatorMapping();
        if (discriminatorMapping.isMultiinstanceLinkByVariable() && Objects.equal(query.getSearchText(), discriminatorMapping.getName())) {
            matchesCount++;
        }
        if (discriminatorMapping.isMultiinstanceLinkByGroup() && !discriminatorMapping.isText()
                && Objects.equal(query.getSearchText(), discriminatorMapping.getName())) {
            matchesCount++;
        }
        if (discriminatorMapping.isMultiinstanceLinkByRelation() && discriminatorMapping.getName().contains("(" + query.getSearchText() + ")")) {
            matchesCount++;
        }
        matchesCount += findInVariableMappings(state.getVariableMappings());
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(state, definitionFile);
            elementMatch.setMatchesCount(matchesCount);
            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
        }
    }

    private void processFormNode(IFile definitionFile, FormNode formNode) throws Exception {
        try {
            ElementMatch nodeElementMatch = new ElementMatch(formNode, definitionFile, ElementMatch.CONTEXT_SWIMLANE);
            if (formNode.hasForm() && searchTargets.contains(VariableSearchTarget.FORM_FILE)) {
                IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getFormFileName());
                Map<String, FormVariableAccess> formVariables = formNode.getFormVariables((IFolder) definitionFile.getParent());
                ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM);
                elementMatch.setParent(nodeElementMatch);
                int matchesCount = 0;
                if (formVariables.keySet().contains(query.getSearchText())) {
                    matchesCount++;
                }
                elementMatch.setMatchesCount(matchesCount);
                List<Match> matches = findInFile(elementMatch, file, matcherByVariable);
                matches.addAll(findInFile(elementMatch, file, matcherByScriptingVariable));
                elementMatch.setPotentialMatchesCount(matches.size() - matchesCount);
                for (Match match : matches) {
                    query.getSearchResult().addMatch(match);
                }
            }
            if (formNode.hasFormValidation() && searchTargets.contains(VariableSearchTarget.FORM_VALIDATION)) {
                IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getValidationFileName());
                FormNodeValidation validation = formNode.getValidation(definitionFile.getParent());
                ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM_VALIDATION);
                elementMatch.setParent(nodeElementMatch);
                int matchesCount = 0;
                if (validation.getVariableNames().contains(query.getSearchText())) {
                    matchesCount++;
                }
                for (ValidatorConfig config : validation.getGlobalConfigs()) {
                    String groovyCode = config.getParams().get(ValidatorDefinition.EXPRESSION_PARAM_NAME);
                    if (groovyCode != null && scriptMatcherByScriptingVariable.reset(groovyCode).find()) {
                        matchesCount++;
                    }
                }
                elementMatch.setMatchesCount(matchesCount);
                if (matchesCount > 0) {
                    query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
                }
            }
            if (formNode.hasFormScript() && searchTargets.contains(VariableSearchTarget.FORM_SCRIPT)) {
                IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getScriptFileName());
                ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM_SCRIPT);
                elementMatch.setParent(nodeElementMatch);
                List<Match> matches = findInFile(elementMatch, file, scriptMatcherByScriptingVariable);
                elementMatch.setMatchesCount(matches.size());
                if (!query.getVariable().getName().equals(query.getVariable().getScriptingName())) {
                    matches.addAll(findInFile(elementMatch, file, scriptMatcherByVariable));
                }
                elementMatch.setPotentialMatchesCount(matches.size() - elementMatch.getMatchesCount());
                for (Match match : matches) {
                    query.getSearchResult().addMatch(match);
                }
            }
            String swimlaneName = ((SwimlanedNode) formNode).getSwimlaneName();
            if (query.getSearchText().equals(swimlaneName) && searchTargets.contains(VariableSearchTarget.TASK_ROLE)) {
                nodeElementMatch.setMatchesCount(1);
                query.getSearchResult().addMatch(new Match(nodeElementMatch, 0, 0));
            }
            if (formNode instanceof TaskState) {
                TaskState taskState = (TaskState) formNode;
                if (taskState.getBotTaskLink() != null && searchTargets.contains(VariableSearchTarget.BOT_TASK)) {
                    ElementMatch elementMatch = new ElementMatch(taskState, null, ElementMatch.CONTEXT_BOT_TASK_LINK);
                    elementMatch.setParent(nodeElementMatch);
                    Map<String, String> parameters = ParamDefConfig.getAllParameters(taskState.getBotTaskLink().getDelegationConfiguration());
                    for (Map.Entry<String, String> entry : parameters.entrySet()) {
                        if (Objects.equal(query.getSearchText(), entry.getValue())) {
                            query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
                            elementMatch.setMatchesCount(elementMatch.getMatchesCount() + 1);
                        }
                    }
                } else {
                    String botName = taskState.getSwimlaneBotName();
                    if (botName != null) {
                        // if bot task exists with same as task name
                        BotTask botTask = BotCache.getBotTask(botName, taskState.getName());
                        if (botTask != null && botTask.getType() == BotTaskType.SIMPLE && searchTargets.contains(VariableSearchTarget.BOT_TASK)) {
                            ElementMatch elementMatch = new ElementMatch(taskState, BotCache.getBotTaskFile(botTask), ElementMatch.CONTEXT_BOT_TASK);
                            elementMatch.setParent(nodeElementMatch);
                            List<Match> matches = findInString(elementMatch, botTask.getDelegationConfiguration(), scriptMatcherByVariable);
                            elementMatch.setPotentialMatchesCount(matches.size());
                            for (Match match : matches) {
                                query.getSearchResult().addMatch(match);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception(formNode.toString(), e);
        }
    }

    private List<Match> findInFile(ElementMatch elementMatch, IFile file, Matcher matcher) throws CoreException, IOException {
        String text = IOUtils.readStream(file.getContents());
        return findInString(elementMatch, text, matcher);
    }

    private List<Match> findInString(ElementMatch elementMatch, CharSequence searchInput, Matcher matcher) throws CoreException, IOException {
        List<Match> matches = new ArrayList<Match>();
        matcher.reset(searchInput);
        int k = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (end != start) {
                matches.add(new Match(elementMatch, start + 1, end - start - 2));
            }
            if (k++ == 20) {
                if (progressMonitor.isCanceled()) {
                    throw new OperationCanceledException(SearchMessages.TextSearchVisitor_canceled);
                }
                k = 0;
            }
        }
        return matches;
    }

    private int findInVariableMappings(List<VariableMapping> mappings) {
        int matchesCount = 0;
        for (VariableMapping mapping : mappings) {
            if (mapping.isMultiinstanceLinkByRelation() && mapping.getName().contains("(" + query.getSearchText() + ")")) {
                // MultiSubprocess selector variable
                matchesCount++;
                continue;
            }
            if (mapping.isPropertySelector()) {
                if (mapping.getMappedName().equals(VariableUtils.wrapVariableName(query.getSearchText()))) {
                    matchesCount++;
                }
                continue;
            }
            if (mapping.isText()) {
                continue;
            }
            if (mapping.getName().equals(query.getSearchText())) {
                matchesCount++;
            }
        }
        return matchesCount;
    }

    private String getSearchedVariablesScriptingNames(Variable variable) {
        List<Variable> variables = new ArrayList<>();
        if (variable.isComplex()) {
            variables.addAll(VariableUtils.expandComplexVariable(variable, variable));
        } else {
            variables.add(variable);
        }
        String result = variables.stream().map(Variable::getScriptingName).collect(Collectors.joining("|"));
        return result.replaceAll("\\.", "|");
    }
}
