package ru.runa.gpd.quick.formeditor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.quick.jointformeditor.QuickJointFormEditor;

public class QuickJointFormType extends QuickFormType {

    @Override
    public IEditorPart openForm(IFile formFile, FormNode formNode) throws CoreException {
        return IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), formFile, QuickJointFormEditor.ID, true);
    }

}
