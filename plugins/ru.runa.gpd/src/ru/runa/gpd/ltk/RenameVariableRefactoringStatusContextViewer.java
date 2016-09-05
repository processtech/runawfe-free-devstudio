package ru.runa.gpd.ltk;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.ltk.ui.refactoring.TextStatusContextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public class RenameVariableRefactoringStatusContextViewer extends TextStatusContextViewer {

    @Override
    public void setInput(RefactoringStatusContext input) {
        if (input instanceof RenameVariableRefactoringStatusContext) {
            StringBuilder str = new StringBuilder();
            Exception e = (Exception) ((RenameVariableRefactoringStatusContext) input).getCorrespondingElement();
            str.append(e.getClass().getName() + ": " + e.getLocalizedMessage() + "\r\n");
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                str.append("\tat " + stackTraceElement.toString() + "\r\n");
            }
            setInput(new Document(str.toString()), null);
        }
    }

    @Override
    protected SourceViewer createSourceViewer(Composite parent) {
        return new SourceViewer(parent, null, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
    }
}
