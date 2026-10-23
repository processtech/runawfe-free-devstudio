package ru.runa.gpd.ltk;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextEditBasedChange;
import org.eclipse.ltk.core.refactoring.TextEditBasedChangeGroup;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ui.dialog.ErrorDialog;

public abstract class TextCompareChange<E> extends TextEditBasedChange {
    protected final Object targetObject;
    protected final E currentObject;
    protected final E replacementObject;

    public TextCompareChange(Object element, E currentObject, E replacementObject) {
        super(element.toString());
        this.targetObject = element;
        this.currentObject = currentObject;
        this.replacementObject = replacementObject;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == TextEditChangeNode.class) {
            return adapter.cast(new ChangeNode(this, targetObject));
        }
        return super.getAdapter(adapter);
    }

    @Override
    public String getCurrentContent(IProgressMonitor pm) throws CoreException {
        return toPreviewContent(currentObject);
    }

    @Override
    public String getCurrentContent(IRegion region, boolean arg1, int arg2, IProgressMonitor pm) throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPreviewContent(IProgressMonitor pm) throws CoreException {
        return toPreviewContent(replacementObject);
    }

    @Override
    public String getPreviewContent(TextEditBasedChangeGroup[] groups, IRegion region, boolean arg2, int arg3, IProgressMonitor pm)
            throws CoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getModifiedElement() {
        return targetObject;
    }

    @Override
    public void initializeValidationData(IProgressMonitor pm) {
    }

    @Override
    public RefactoringStatus isValid(IProgressMonitor pm) {
        return RefactoringStatus.createInfoStatus("Ok");
    }

    protected abstract String toPreviewContent(E object);

    @Override
    public Change perform(IProgressMonitor pm) throws CoreException {
        Display.getDefault().asyncExec(() -> {
            try {
                performInUIThread();
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("Unable to perform change in " + targetObject, e);
                ErrorDialog.open(Localization.getString("RenameVariableException") + targetObject + ": " + e.getLocalizedMessage(), e);
            }
        });
        return new NullChange();
    }

    protected abstract void performInUIThread();

}
