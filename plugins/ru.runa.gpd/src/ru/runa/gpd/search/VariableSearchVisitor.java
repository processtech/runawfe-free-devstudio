package ru.runa.gpd.search;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.form.FormVariableAccess;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.BotTaskType;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.MessagingNode;
import ru.runa.gpd.lang.model.MultiTaskState;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.VariableMapping;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.gpd.validation.FormNodeValidation;
import ru.runa.gpd.validation.ValidatorConfig;
import ru.runa.gpd.validation.ValidatorDefinition;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

public class VariableSearchVisitor {
    private final VariableSearchQuery query;
    private IProgressMonitor progressMonitor;
    private int numberOfScannedElements;
    private int numberOfElementsToScan;
    private GraphElement currentElement = null;
    private final MultiStatus status;
    private final Matcher matcher;
    private final Matcher matcherWithBrackets;
    private Matcher matcherScriptingName;

    public VariableSearchVisitor(VariableSearchQuery query) {
        this.query = query;
        this.status = new MultiStatus(NewSearchUI.PLUGIN_ID, IStatus.OK, SearchMessages.TextSearchEngine_statusMessage, null);
        this.matcher = Pattern.compile(Pattern.quote(query.getSearchText())).matcher("");
        this.matcherScriptingName = Pattern.compile(Pattern.quote(query.getVariable().getScriptingName())).matcher("");
        this.matcherWithBrackets = Pattern.compile(Pattern.quote("\"" + query.getSearchText() + "\"")).matcher("");
    }

    public IStatus search(SearchResult searchResult, IProgressMonitor monitor) {
        Map<ProcessDefinition, IFile> map = Maps.newHashMap();
        map.put(query.getMainProcessDefinition(), query.getMainProcessdefinitionFile());
        numberOfElementsToScan = query.getMainProcessDefinition().getChildrenRecursive(GraphElement.class).size();
        for (SubprocessDefinition subprocessDefinition : query.getMainProcessDefinition().getEmbeddedSubprocesses().values()) {
            IFile subprocessDefinitionFile = ProcessCache.getProcessDefinitionFile(subprocessDefinition);
            map.put(subprocessDefinition, subprocessDefinitionFile);
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
            if (graphElement instanceof ITimed) {
                processTimedNode(definitionFile, (ITimed) graphElement);
            }
            if (graphElement instanceof Timer) {
                processTimer(definitionFile, (Timer) graphElement, graphElement);
            }
            if (graphElement instanceof Subprocess) {
                processSubprocessNode(definitionFile, (Subprocess) graphElement);
            }
            if (graphElement instanceof MessagingNode) {
                processMessagingNode(definitionFile, (MessagingNode) graphElement);
            }
            if (graphElement instanceof MultiTaskState) {
                processMultiTaskNode(definitionFile, (MultiTaskState) graphElement);
            }
        } catch (Exception e) {
            status.add(new Status(IStatus.ERROR, NewSearchUI.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
        } finally {
            numberOfScannedElements++;
        }
    }

    private void processDelegableNode(IFile definitionFile, Delegable delegable) throws Exception {
        Matcher delegableMatcher;
        if (matcherScriptingName != null && HandlerRegistry.SCRIPT_HANDLER_CLASS_NAMES.contains(delegable.getDelegationClassName())) {
            delegableMatcher = matcherScriptingName;
        } else {
            delegableMatcher = matcher;
        }
        String conf = delegable.getDelegationConfiguration();
        ElementMatch elementMatch = new ElementMatch((GraphElement) delegable, definitionFile);
        List<Match> matches = findInString(elementMatch, conf, delegableMatcher);
        elementMatch.setPotentialMatchesCount(matches.size());
        for (Match match : matches) {
            query.getSearchResult().addMatch(match);
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

    private void processMessagingNode(IFile definitionFile, MessagingNode messagingNode) throws Exception {
        int matchesCount = findInVariableMappings(messagingNode.getVariableMappings());
        if (matchesCount > 0) {
            ElementMatch elementMatch = new ElementMatch(messagingNode, definitionFile);
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
            if (formNode.hasForm()) {
                IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getFormFileName());
                Map<String, FormVariableAccess> formVariables = formNode.getFormVariables((IFolder) definitionFile.getParent());
                ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM);
                elementMatch.setParent(nodeElementMatch);
                int matchesCount = 0;
                if (formVariables.keySet().contains(query.getSearchText())) {
                    matchesCount++;
                }
                elementMatch.setMatchesCount(matchesCount);
                List<Match> matches = findInFile(elementMatch, file, matcherWithBrackets);
                elementMatch.setPotentialMatchesCount(matches.size() - matchesCount);
                for (Match match : matches) {
                    query.getSearchResult().addMatch(match);
                }
            }
            if (formNode.hasFormValidation()) {
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
                    if (groovyCode != null && groovyCode.contains(query.getVariable().getScriptingName())) {
                        matchesCount++;
                    }
                }
                elementMatch.setMatchesCount(matchesCount);
                if (matchesCount > 0) {
                    query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
                }
            }
            if (formNode.hasFormScript()) {
                IFile file = IOUtils.getAdjacentFile(definitionFile, formNode.getScriptFileName());
                Map<String, FormVariableAccess> formVariables = formNode.getFormVariables((IFolder) definitionFile.getParent());
                ElementMatch elementMatch = new ElementMatch(formNode, file, ElementMatch.CONTEXT_FORM_SCRIPT);
                elementMatch.setParent(nodeElementMatch);
                int matchesCount = 0;
                if (formVariables.keySet().contains(query.getSearchText())) {
                    matchesCount++;
                }
                elementMatch.setMatchesCount(matchesCount);
                List<Match> matches = findInFile(elementMatch, file, matcherScriptingName);
                elementMatch.setPotentialMatchesCount(matches.size() - matchesCount);
                for (Match match : matches) {
                    query.getSearchResult().addMatch(match);
                }
            }
            String swimlaneName = ((SwimlanedNode) formNode).getSwimlaneName();
            if (query.getSearchText().equals(swimlaneName)) {
                nodeElementMatch.setMatchesCount(1);
                query.getSearchResult().addMatch(new Match(nodeElementMatch, 0, 0));
            }
            if (formNode instanceof TaskState) {
                TaskState taskState = (TaskState) formNode;
                if (taskState.getBotTaskLink() != null) {
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
                        if (botTask != null && botTask.getType() == BotTaskType.SIMPLE) {
                            ElementMatch elementMatch = new ElementMatch(taskState, BotCache.getBotTaskFile(botTask), ElementMatch.CONTEXT_BOT_TASK);
                            elementMatch.setParent(nodeElementMatch);
                            List<Match> matches = findInString(elementMatch, botTask.getDelegationConfiguration(), matcher);
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
                matches.add(new Match(elementMatch, start, end - start));
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

}
