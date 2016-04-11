package ru.runa.gpd.editor.gef.part.graph;

import java.util.List;

import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;

import ru.runa.gpd.lang.model.Subprocess;
import ru.runa.gpd.util.WorkspaceOperations;

public class SubprocessGraphicalEditPart extends LabeledNodeGraphicalEditPart {
    @Override
    public Subprocess getModel() {
        return (Subprocess) super.getModel();
    }

    @Override
    public void performRequest(Request request) {
        if (request.getType() == RequestConstants.REQ_OPEN) {
            WorkspaceOperations.openSubprocessDefinition(getModel());
        } else {
            super.performRequest(request);
        }
    }

    @Override
    protected void fillFigureUpdatePropertyNames(List<String> list) {
        super.fillFigureUpdatePropertyNames(list);
        list.add(PROPERTY_SUBPROCESS);
    }
}
