package ru.runa.gpd.editor.graphiti;

import org.eclipse.core.resources.IFolder;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.InternalTransactionalEditingDomain;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyGraphCommand;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;

public class CopyGraphAndDrawAfterPasteCommand extends CopyGraphCommand {

    protected ProcessEditorBase targetEditor;
    protected DrawAfterPasteCommand dapc;

    public CopyGraphAndDrawAfterPasteCommand(ProcessEditorBase targetEditor, IFolder targetFolder) {
        super(targetEditor, targetFolder);
        this.targetEditor = targetEditor;
    }

    @Override
    public String getLabel() {
        return this.getClass().getSimpleName();
    }

    @Override
    public boolean canExecute() {
        return super.canExecute() && Language.BPMN.equals(targetEditor.getDefinition().getLanguage()) && targetEditor.getDiagramEditorPage() != null;
    }

    @Override
    public void execute() {
        super.execute();
        dapc = new DrawAfterPasteCommand(getFilteredElements(), targetEditor.getDefinition(), targetEditor.getDiagramEditorPage());
        TransactionalEditingDomain domain = targetEditor.getDiagramEditorPage().getEditingDomain();
        InternalTransactionalEditingDomain ited = (InternalTransactionalEditingDomain) domain;
        if (ited.getActiveTransaction() == null) {
            Transaction tx = null;
            try {
                tx = ited.startTransaction(false, null);
                dapc.execute();
            } catch (InterruptedException e) {
                PluginLogger.logError(e);
            } finally {
                if (tx != null) {
                    try {
                        tx.commit();
                    } catch (RollbackException e) {
                        tx.rollback();
                        PluginLogger.logError(e);
                    }
                }
            }
        } else {
            dapc.execute();
        }
    }

    @Override
    public void undo() {
        dapc.undo();
        super.undo();
    }

}
