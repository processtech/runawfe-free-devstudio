package ru.runa.gpd.search;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.IEditorPart;

import ru.runa.gpd.SharedImages;

public class SearchResult extends AbstractTextSearchResult implements IEditorMatchAdapter, IFileMatchAdapter {
    private static final int MAX_LABEL_LENGTH = 66;

    private final BaseSearchQuery query;

    public SearchResult(BaseSearchQuery query) {
        this.query = query;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return SharedImages.getImageDescriptor("/images/search.gif");
    }

    @Override
    public String getLabel() {
        StringBuilder label = new StringBuilder(query.getSearchText());
        if (label.length() > MAX_LABEL_LENGTH) {
            label.delete(MAX_LABEL_LENGTH + 1, label.length() + 1);
            label.append("...");
        }
        Object[] args = { label.toString(), query.getContext(), getMatchCount() };
        return MessageFormat.format("\"{0}\" - \"{1}\":{2}", args);
    }

    @Override
    public String getTooltip() {
        return getLabel();
    }

    @Override
    public ISearchQuery getQuery() {
        return query;
    }

    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return this;
    }

    @Override
    public Match[] computeContainedMatches(AbstractTextSearchResult result, IEditorPart editor) {
        return new Match[0];
    }

    @Override
    public boolean isShownInEditor(Match match, IEditorPart editor) {
        return false;
    }

    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return this;
    }

    @Override
    public Match[] computeContainedMatches(AbstractTextSearchResult result, IFile file) {
        return new Match[0];
    }

    public void merge(SearchResult searchResult) {
        Object[] elements = searchResult.getElements();
        for (Object element : elements) {
            addMatches(searchResult.getMatches(element));
        }
    }

    @Override
    public int getMatchCount(Object element) {
        int count = 0;
        for (Match match : getMatches(element)) {
            ElementMatch elementMatch = (ElementMatch) match.getElement();
            count += elementMatch.getMatchesCount() + elementMatch.getPotentialMatchesCount();
        }
        return count;
    }

    public int getPotentialMatchCount(Object element) {
        int count = 0;
        for (Match match : getMatches(element)) {
            ElementMatch elementMatch = (ElementMatch) match.getElement();
            count += elementMatch.getPotentialMatchesCount();
        }
        return count;
    }

    public int getStrictMatchCount(Object element) {
        int count = 0;
        for (Match match : getMatches(element)) {
            ElementMatch elementMatch = (ElementMatch) match.getElement();
            count += elementMatch.getMatchesCount();
        }
        return count;
    }

    @Override
    public IFile getFile(Object element) {
        return ((ElementMatch) element).getFile();
    }
}
