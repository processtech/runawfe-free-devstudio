package ru.runa.gpd.editor.graphiti;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.InternalTransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IPasteContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.AbstractPasteFeature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyBuffer;
import ru.runa.gpd.editor.GEFConstants;
import ru.runa.gpd.editor.IPasteGraph;
import ru.runa.gpd.editor.PasteGraphDelegate;
import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.AbstractTransition;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.EmbeddedSubprocess.Behavior;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ITimed;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.lang.model.bpmn.CatchEventNode;
import ru.runa.gpd.lang.model.bpmn.IBoundaryEventContainer;
import ru.runa.gpd.ui.dialog.InfoWithDetailsDialog;
import ru.runa.gpd.util.EditorUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableUtils;
import ru.runa.wfe.var.UserType;

public class PasteFeature extends AbstractPasteFeature implements CustomUndoRedoFeature, IRedoProtected {
    private final IPasteGraph pasteGraphDelegate;
    protected DrawAfterPasteCommand dapc;
    public static final String PROPERTY_EDITOR = "editor";

    public PasteFeature(IFeatureProvider fp, ProcessEditorBase editor) {
        super(fp);
        pasteGraphDelegate = new PasteGraphDelegate(fp, editor);
    }

    @Override
    public void paste(IPasteContext context) {
        pasteGraphDelegate.pasteGraph();
        drawAfterPaste();
    }

    @Override
    public boolean canPaste(IPasteContext context) {
        return pasteGraphDelegate.bufferIsValid() && Language.BPMN.equals(pasteGraphDelegate.getTargetDefinition().getLanguage())
                && pasteGraphDelegate.getDiagramEditorPage() != null;
    }

    @Override
    public boolean canUndo(IContext context) {
        if (dapc == null) {
            return false;
        }
        return (dapc.canUndo() && pasteGraphDelegate.canUndo());
    }

    @Override
    public void postUndo(IContext context) {
        dapc.undo();
        pasteGraphDelegate.undo();
    }

    @Override
    public boolean canRedo(IContext context) {
        return pasteGraphDelegate.canRedo();
    }

    @Override
    public void postRedo(IContext context) {
        pasteGraphDelegate.redo();
    }

    

    private void drawAfterPaste() {
        dapc = new DrawAfterPasteCommand(pasteGraphDelegate.getFilteredElements(), pasteGraphDelegate.getTargetDefinition(),
                pasteGraphDelegate.getDiagramEditorPage());
        TransactionalEditingDomain domain = pasteGraphDelegate.getDiagramEditorPage().getEditingDomain();
        InternalTransactionalEditingDomain ited = (InternalTransactionalEditingDomain) domain;
        if (ited.getActiveTransaction() == null) {
            Transaction tx = null;
            try {
                tx = ited.startTransaction(false, null);
                dapc.execute();
            } catch (InterruptedException e) {
                PluginLogger.logError(e);
                Thread.currentThread().interrupt();
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
}
