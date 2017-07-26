package ru.runa.gpd.search;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;

import com.google.common.base.Objects;

public class ElementMatch {
    public static String CONTEXT_SWIMLANE = "swimlane";
    public static String CONTEXT_TIMED_VARIABLE = "itimed_conf";
    public static String CONTEXT_FORM = "form";
    public static String CONTEXT_FORM_VALIDATION = "form_validation";
    public static String CONTEXT_FORM_SCRIPT = "form_script";
    public static String CONTEXT_BOT_TASK_LINK = "botTaskLink";
    public static String CONTEXT_BOT_TASK = "botTask";
    public static String CONTEXT_PROCESS_DEFINITION = "processDefinition";
    private String context;
    private IFile file;
    private GraphElement graphElement;
    private int matchesCount;
    private int potentialMatchesCount;
    private ElementMatch parent;

    public ElementMatch(GraphElement graphElement, IFile file, String context) {
        this.graphElement = graphElement;
        this.file = file;
        this.context = context;
    }

    public ElementMatch(GraphElement graphElement, IFile file) {
        this(graphElement, file, null);
    }

    public ElementMatch() {
        this(null, null);
    }

    public ElementMatch getParent() {
        return parent;
    }

    public void setParent(ElementMatch parent) {
        this.parent = parent;
    }

    public String getContext() {
        return context;
    }

    public IFile getFile() {
        return file;
    }

    public GraphElement getGraphElement() {
        return graphElement;
    }

    public int getMatchesCount() {
        return matchesCount;
    }

    public void setMatchesCount(int matchesCount) {
        this.matchesCount = matchesCount;
    }

    public int getPotentialMatchesCount() {
        return potentialMatchesCount;
    }

    public void setPotentialMatchesCount(int potentialMatchesCount) {
        this.potentialMatchesCount = potentialMatchesCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ElementMatch) {
            ElementMatch m = (ElementMatch) obj;
            return Objects.equal(graphElement, m.graphElement) && Objects.equal(context, m.context);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(graphElement, context);
    }
    
    public String toString(SearchResult searchResult) {
        String text;
        if (ElementMatch.CONTEXT_FORM.equals(context)) {
            text = Localization.getString("Search.formNode.form");
        } else if (ElementMatch.CONTEXT_FORM_VALIDATION.equals(context)) {
            text = Localization.getString("Search.formNode.validation");
        } else if (ElementMatch.CONTEXT_FORM_SCRIPT.equals(context)) {
            text = Localization.getString("Search.formNode.script");
        } else if (ElementMatch.CONTEXT_BOT_TASK.equals(context)) {
            text = Localization.getString("Search.taskNode.botTask");
        } else if (ElementMatch.CONTEXT_BOT_TASK_LINK.equals(context)) {
            text = Localization.getString("Search.taskNode.botTaskLink");
        } else if (graphElement instanceof NamedGraphElement) {
            text = ((NamedGraphElement) graphElement).getName();
        } else {
            text = graphElement.toString();
        }
        if (ElementMatch.CONTEXT_TIMED_VARIABLE.equals(context)) {
            text += " [" + Localization.getString("Timer.baseDate") + "]";
        }
        if (ElementMatch.CONTEXT_SWIMLANE.equals(context) && matchesCount > 0) {
            text += " [" + Localization.getString("default.swimlane.name") + "]";
        }
        int strictMatchCount = 0;
        int potentialMatchCount = 0;
        if (searchResult != null) {
            strictMatchCount = searchResult.getStrictMatchCount(this);
            potentialMatchCount = searchResult.getPotentialMatchCount(this) - strictMatchCount;
        }
        if (strictMatchCount == 0 && potentialMatchCount == 0) {
            return text;
        }
        String format;
        if (strictMatchCount > 0 && potentialMatchCount > 0) {
            format = "{0} (" + Localization.getString("Search.matches") + ": {1}, " + Localization.getString("Search.potentialMatches") + ": {2})";
            return MessageFormat.format(format, new Object[] { text, strictMatchCount, potentialMatchCount });
        } else if (strictMatchCount == 0) {
            format = "{0} (" + Localization.getString("Search.potentialMatches") + ": {1})";
            return MessageFormat.format(format, new Object[] { text, potentialMatchCount });
        } else {
            format = "{0} (" + Localization.getString("Search.matches") + ": {1})";
            return MessageFormat.format(format, new Object[] { text, strictMatchCount });
        }
    }
}
