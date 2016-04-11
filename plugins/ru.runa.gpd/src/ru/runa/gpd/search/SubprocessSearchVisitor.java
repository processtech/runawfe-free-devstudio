package ru.runa.gpd.search;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.search.ui.text.Match;

import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Subprocess;

import com.google.common.base.Objects;

public class SubprocessSearchVisitor extends ProcessDefinitionsVisitor {
    
    public SubprocessSearchVisitor(SubprocessSearchQuery query) {
        super(query);
    }

    @Override
    protected void findInProcessDefinition(IFile definitionFile, ProcessDefinition processDefinition) {
        List<Subprocess> subprocesses = processDefinition.getChildren(Subprocess.class);
        for (Subprocess subprocess : subprocesses) {
            if (Objects.equal(subprocess.getSubProcessName(), query.getSearchText())) {
                ElementMatch elementMatch = new ElementMatch(subprocess, definitionFile, ElementMatch.CONTEXT_PROCESS_DEFINITION);
                query.getSearchResult().addMatch(new Match(elementMatch, 0, 0));
                elementMatch.setMatchesCount(elementMatch.getMatchesCount() + 1);
            }
        }
    }

}
