package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

import ru.runa.gpd.form.FormTypeProvider;
import ru.runa.gpd.lang.model.FormNode;

public class OpenVisualFormEditorDelegate extends OpenFormEditorDelegate {

    @Override
    protected void openInEditor(IFile file, FormNode formNode) throws CoreException {
        // open form
        FormTypeProvider.getFormType(formNode.getFormType()).openForm(file, formNode);
    }

}
