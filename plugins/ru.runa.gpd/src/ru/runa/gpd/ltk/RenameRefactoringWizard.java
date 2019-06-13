package ru.runa.gpd.ltk;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.internal.ui.refactoring.AbstractChangeNode;
import org.eclipse.ltk.internal.ui.refactoring.CompositeChangeNode;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.SharedImages;

public class RenameRefactoringWizard extends RefactoringWizard {

    public RenameRefactoringWizard(Refactoring refactoring) {
        super(refactoring, WIZARD_BASED_USER_INTERFACE);
    }

    @Override
    protected void addUserInputPages() {
    }

    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        // TODO avoid reflection
        try {
            IWizardPage[] pages = getPages();
            PreviewWizardPage pvp = ((PreviewWizardPage) pages[pages.length - 1]);
            Field treeViewerField = pvp.getClass().getDeclaredField("fTreeViewer");
            treeViewerField.setAccessible(true);
            TreeViewer treeViewer = (TreeViewer) treeViewerField.get(pvp);
            treeViewer.setLabelProvider(new ChangeElementLabelProvider());
            treeViewer.refresh(true);
        } catch (Exception e) {
            PluginLogger.logErrorWithoutDialog("refactoring internals error", e);
        }
    }

    private boolean fuseTextFileEditsByFile(Refactoring refactoring) {
        Map<IFile, MultiTextEdit> textFileEditCache = new HashMap<>();
        try {
            for (Change change : ((CompositeChange) refactoring.createChange(null)).getChildren()) {
                if (change instanceof CompositeChange) {
                    for (Change childChange : ((CompositeChange) change).getChildren()) {
                        TextFileChange textFileChange = (TextFileChange) childChange;
                        MultiTextEdit multiTextEdit = (MultiTextEdit) textFileChange.getEdit();
                        IFile file = textFileChange.getFile();
                        if (textFileEditCache.containsKey(file)) {
                            for (TextEdit textEdit : multiTextEdit.getChildren()) {
                                multiTextEdit.removeChild(textEdit);
                                textFileEditCache.get(file).addChild(textEdit);
                            }
                        } else {
                            textFileEditCache.put(file, multiTextEdit);
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            PluginLogger.logError("refactoring error", e);
            return false;
        } finally {
            textFileEditCache.clear();
        }
    }

    @Override
    public boolean performFinish() {
        if (fuseTextFileEditsByFile(getRefactoring())) {
            return super.performFinish();
        } else {
            return false;
        }
    }

    private static class ChangeElementLabelProvider extends LabelProvider {
        @Override
        public Image getImage(Object object) {
            if (object instanceof CompositeChangeNode) {
                return SharedImages.getImage(((CompositeChangeNode) object).getImageDescriptor());
            }
            if (object instanceof ChangeNode) {
                ChangeNode node = (ChangeNode) object;
                return node.getImage();
            }
            return SharedImages.getImage("icons/file_change.gif");
        }

        @Override
        public String getText(Object object) {
            return ((AbstractChangeNode) object).getText();
        }
    }
}
