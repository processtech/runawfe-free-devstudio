package ru.runa.gpd.editor;

import com.google.common.base.Objects;
import java.util.List;
import org.eclipse.core.resources.IFolder;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.ui.actions.Clipboard;
import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.NamedGraphElement;

public class CopyBuffer {
    public static final String GROUP_ACTION_HANDLERS = Localization.getString("CopyBuffer.ActionHandler");
    public static final String GROUP_SWIMLANES = Localization.getString("CopyBuffer.Swimlane");
    public static final String GROUP_FORM_FILES = Localization.getString("CopyBuffer.FormFiles");
    public static final String GROUP_VARIABLES = Localization.getString("CopyBuffer.Variable");
    public static final String GROUP_DELEGABLE = Localization.getString("CopyBuffer.Delegable");

    private IFolder sourceFolder;
    private Language sourceLanguage;
    private List<NamedGraphElement> sourceNodes;
    private String editorId;
    private Point viewportLocation;

    public CopyBuffer() {
        Object contents = Clipboard.getDefault().getContents();
        if (contents != null && contents.getClass().isArray()) {
            Object[] array = (Object[]) contents;
            if (array.length == 5) {
                sourceFolder = (IFolder) array[0];
                sourceLanguage = (Language) array[1];
                sourceNodes = (List<NamedGraphElement>) array[2];
                editorId = (String) array[3];
                viewportLocation = (Point) array[4];
            }
        }
    }

    public CopyBuffer(IFolder sourceFolder, Language sourceLanguage, List<NamedGraphElement> sourceNodes, String editorId, Point viewportLocation) {
        this.sourceFolder = sourceFolder;
        this.sourceLanguage = sourceLanguage;
        this.sourceNodes = sourceNodes;
        this.editorId = editorId;
        this.viewportLocation = viewportLocation;
    }

    public void setToClipboard() {
        Clipboard.getDefault().setContents(new Object[] { sourceFolder, sourceLanguage, sourceNodes, editorId, viewportLocation });
    }

    public List<NamedGraphElement> getSourceNodes() {
        return sourceNodes;
    }

    public boolean isValid() {
        return sourceFolder != null;
    }

    public IFolder getSourceFolder() {
        return sourceFolder;
    }

    public Language getLanguage() {
        return sourceLanguage;
    }

    public String getEditorId() {
        return editorId;
    }

    public Point getViewportLocation() {
        return viewportLocation;
    }

    public static abstract class ExtraCopyAction {

        private final String groupName;
        private final String name;

        public ExtraCopyAction(String groupName, String name) {
            this.groupName = groupName;
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public abstract void execute() throws Exception;

        public abstract void undo() throws Exception;

        @Override
        public String toString() {
            return groupName + ": " + name;
        }

        @Override
        public boolean equals(Object obj) {
            ExtraCopyAction o = (ExtraCopyAction) obj;
            return groupName.equals(o.groupName) && name.equals(o.name);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(groupName, name);
        }

    }
}
