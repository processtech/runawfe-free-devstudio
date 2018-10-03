package ru.runa.gpd.formeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.formeditor.ftl.ui.FormComponentsView;
import ru.runa.gpd.jointformeditor.JointFormEditor;
import ru.runa.gpd.lang.model.FormNode;

public class FtlJointFormType extends FtlFormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(FormComponentsView.ID, null, IWorkbenchPage.VIEW_VISIBLE);
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, JointFormEditor.ID, true);
    }

}
