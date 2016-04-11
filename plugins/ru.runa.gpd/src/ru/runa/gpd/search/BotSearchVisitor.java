package ru.runa.gpd.search;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.text.Match;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.TaskState;

import com.google.common.base.Objects;

public class BotSearchVisitor extends ProcessDefinitionsVisitor {
    public BotSearchVisitor(BotSearchQuery query) {
        super(query);
    }

    @Override
    protected void findInProcessDefinition(IFile definitionFile, ProcessDefinition processDefinition) {
        List<TaskState> taskStates = processDefinition.getChildren(TaskState.class);
        for (TaskState taskState : taskStates) {
            ElementMatch elementMatch = new ElementMatch(taskState, definitionFile);
            if (Objects.equal(taskState.getSwimlaneBotName(), query.getSearchText())) {
                query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
                elementMatch.setMatchesCount(elementMatch.getMatchesCount() + 1);
            }
        }
    }
}
