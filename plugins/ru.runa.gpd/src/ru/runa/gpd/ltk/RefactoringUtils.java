package ru.runa.gpd.ltk;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;

public class RefactoringUtils {
    public static CompositeChange createChangeUserTypeAttribute(String name, IProgressMonitor progressMonitor,
            List<RenameVariableRefactoring> refactorings, RefactoringStatus finalStatus) {
        CompositeChange compositeChange = new CompositeChange(name);
        for (RenameVariableRefactoring refactoring : refactorings) {
            compositeChange.merge(refactoring.createChange(progressMonitor));
            RefactoringStatus status = refactoring.checkFinalConditions(null);
            List<RefactoringStatusEntry> existingEntries = Arrays.asList(finalStatus.getEntries());
            for (RefactoringStatusEntry entry : status.getEntries()) {
                if (!existingEntries.contains(entry)) {
                    finalStatus.addEntry(entry);
                }
            }
        }
        return compositeChange;
    }
}
