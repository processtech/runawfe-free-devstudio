package ru.runa.gpd.editor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyBuffer.ExtraCopyAction;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.Swimlane;
import ru.runa.gpd.lang.model.SwimlanedNode;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.model.VariableUserType;
import ru.runa.gpd.ui.dialog.InfoWithDetailsDialog;
import ru.runa.gpd.ui.dialog.MultipleSelectionDialog;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.SwimlaneDisplayMode;
import ru.runa.gpd.util.VariableUtils;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CopyGraphCommand extends Command {
    private final ProcessEditorBase targetEditor;
    private final Point targetViewportLocation;
    private final ProcessDefinition targetDefinition;
    private final IFolder targetFolder;
    private final CopyBuffer copyBuffer;
    private final Map<String, NamedGraphElement> targetNodeMap = Maps.newHashMap();
    private final List<ExtraCopyAction> executedCopyActions = Lists.newArrayList();
    private final Map<String, String> nodeToSwimlaneNameMap = Maps.newHashMap();

    public CopyGraphCommand(ProcessEditorBase targetEditor, IFolder targetFolder) {
        this.targetEditor = targetEditor;
        this.targetViewportLocation = ((FigureCanvas) targetEditor.getGraphicalViewer().getControl()).getViewport().getViewLocation();
        this.targetDefinition = targetEditor.getDefinition();
        this.targetFolder = targetFolder;
        this.copyBuffer = new CopyBuffer();
    }

    @Override
    public boolean canExecute() {
        return copyBuffer.isValid();
    }

    @Override
    public String getLabel() {
        return Localization.getString("button.paste");
    }

    @Override
    public void execute() {
        try {
            if (!copyBuffer.getLanguage().equals(targetDefinition.getLanguage())) {
                (new InfoWithDetailsDialog(MessageDialog.WARNING,  Localization.getString("message.warning"), Localization.getString("CopyBuffer.DifferentVersion.warning"), null) {
                    @Override
                    protected void createButtonsForButtonBar(Composite parent) {
                        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
                    }
                }).open();
                return;
            }
            Set<ExtraCopyAction> copyActions = new HashSet<ExtraCopyAction>();
            List<NamedGraphElement> sourceNodeList = copyBuffer.getSourceNodes();
            // add nodes
            final List<NamedGraphElement> newElements = new ArrayList<>();
            for (NamedGraphElement node : sourceNodeList) {
                if (node instanceof StartState && targetDefinition.getChildren(StartState.class).size() != 0) {
                    continue;
                } else if (node instanceof EndState && targetDefinition instanceof SubprocessDefinition) {
                    continue;
                    // if swimlane is copied as graph element twice
                } else if (node instanceof Swimlane && targetDefinition.getSwimlaneByName(node.getName()) != null) {
                    continue;
                }
                NamedGraphElement copy = node.makeCopy(targetDefinition);
                adjustLocation(copy);
                newElements.add(copy);
                for (Variable variable : node.getUsedVariables(copyBuffer.getSourceFolder())) {
                    ExtraCopyAction copyAction;
                    if (variable instanceof Swimlane) {
                        copyAction = new CopySwimlaneAction((Swimlane) variable);
                    } else if (variable.isUserTypeAttribute()) {
                        copyAction = new CopyUserTypeAttributeAction(variable, node.getProcessDefinition());
                    } else {
                        copyAction = new CopyVariableAction(variable);
                    }
                    copyActions.add(copyAction);
                }
                targetNodeMap.put(node.getId(), copy);
                if (node instanceof FormNode) {
                    FormNode formNode = (FormNode) node;
                    if (formNode.hasForm() || formNode.hasFormValidation() || formNode.hasFormScript() || formNode.hasFormTemplate()) {
                        CopyFormFilesAction copyAction = new CopyFormFilesAction(formNode, (FormNode) copy);
                        copyAction.setSourceFolder(copyBuffer.getSourceFolder());
                        copyAction.setTargetFolder(targetFolder);
                        copyActions.add(copyAction);
                    }
                }
                if (node instanceof SwimlanedNode) {
                    Swimlane swimlane = ((SwimlanedNode) node).getSwimlane();
                    boolean ignoreSwimlane = targetDefinition instanceof SubprocessDefinition && node instanceof StartState;
                    if (swimlane != null && !ignoreSwimlane) {
                        CopySwimlaneAction copyAction = new CopySwimlaneAction(swimlane);
                        copyActions.add(copyAction);
                        nodeToSwimlaneNameMap.put(node.getId(), swimlane.getName());
                    }
                }
                if (node instanceof Delegable) {
                    Delegable source = (Delegable) node;
                    if (source.getDelegationClassName() != null) {
                        CopyDelegableElementAction copyAction = new CopyDelegableElementAction(node, copy);
                        copyAction.setSourceFolder(copyBuffer.getSourceFolder());
                        copyAction.setTargetFolder(targetFolder);
                        copyActions.add(copyAction);
                    }
                }
            }
            // add transitions
            for (NamedGraphElement node : sourceNodeList) {
                List<Transition> transitions = node.getChildren(Transition.class);
                for (Transition transition : transitions) {
                    NamedGraphElement source = targetNodeMap.get(transition.getSource().getId());
                    NamedGraphElement target = targetNodeMap.get(transition.getTarget().getId());
                    if (source != null && target != null) {
                        Transition copy = transition.makeCopy(source);
                        adjustLocation(copy);
                        copy.setTarget((Node) target);
                        newElements.add(copy);
                    }
                    for (Variable variable : transition.getUsedVariables(copyBuffer.getSourceFolder())) {
                        ExtraCopyAction copyAction;
                        if (variable instanceof Swimlane) {
                            copyAction = new CopySwimlaneAction((Swimlane) variable);
                        } else if (variable.isUserTypeAttribute()) {
                            copyAction = new CopyUserTypeAttributeAction(variable, node.getProcessDefinition());
                        } else {
                            copyAction = new CopyVariableAction(variable);
                        }
                        copyActions.add(copyAction);
                    }
                }
            }
            if (newElements.size() > 0) {
                if (copyBuffer.getLanguage() == Language.JPDL) {
                    List<EditPart> editParts = new ArrayList<>();
                    for (NamedGraphElement e : newElements) {
                        EditPart ep = (EditPart) targetEditor.getGraphicalViewer().getEditPartRegistry().get(e);
                        if (ep != null) {
                            editParts.add(ep);
                        }
                    }
                    if (editParts.size() > 0) {
                        targetEditor.getGraphicalViewer().deselectAll();
                        targetEditor.getGraphicalViewer().getSelectionManager().setSelection(new StructuredSelection(editParts));
                    }
                } else { // BPMN
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            List<PictogramElement> pictograms = new ArrayList<>();
                            for (NamedGraphElement e : newElements) {
                                PictogramElement[] pe = targetEditor.getDiagramEditorPage().getAllPictogramElementsForBusinessObject(e);
                                if (pe != null && pe.length > 0) {
                                    pictograms.addAll(Arrays.asList(pe));
                                }
                            }
                            if (pictograms.size() > 0) {
                                targetEditor.getGraphicalViewer().deselectAll();
                                targetEditor.getDiagramEditorPage().selectPictogramElements(pictograms.toArray(new PictogramElement[] {}));
                            }
                        }
                    });
                }
            }
            List<ExtraCopyAction> sortedCopyActions = Lists.newArrayList(copyActions);
            Collections.sort(sortedCopyActions);
            List<ExtraCopyAction> userConfirmedActions = new ArrayList<ExtraCopyAction>();
            for (ExtraCopyAction copyAction : sortedCopyActions) {
                if (copyAction.isUserConfirmationRequired()) {
                    copyAction.setEnabled(false);
                    userConfirmedActions.add(copyAction);
                }
            }
            if (userConfirmedActions.size() > 0) {
                // display dialog with collisions
                MultipleSelectionDialog dialog = new MultipleSelectionDialog(Localization.getString("CopyGraphRewriteDialog.title"),
                        userConfirmedActions);
                if (dialog.open() != IDialogConstants.OK_ID) {
                    for (ExtraCopyAction copyAction : userConfirmedActions) {
                        copyAction.setEnabled(false);
                    }
                }
            }
            // run copy actions
            for (ExtraCopyAction copyAction : sortedCopyActions) {
                if (copyAction.isEnabled()) {
                    PluginLogger.logInfo("Copying '" + copyAction + "'");
                    copyAction.execute();
                    executedCopyActions.add(copyAction);
                } else {
                    PluginLogger.logInfo("Ignored to copy '" + copyAction + "'");
                }
            }
            // set swimlanes
            for (Map.Entry<String, NamedGraphElement> entry : targetNodeMap.entrySet()) {
                if (entry.getValue() instanceof SwimlanedNode) {
                    boolean ignoreSwimlane = targetDefinition instanceof SubprocessDefinition && entry.getValue() instanceof StartState;
                    if (!ignoreSwimlane) {
                        Swimlane swimlane = targetDefinition.getSwimlaneByName(nodeToSwimlaneNameMap.get(entry.getKey()));
                        // this crazy line created because
                        // element.getConstraint() == null is checking of
                        // visibility for swimlane in many places
                        // TODO copy/paste eliminate swimlane.getConstraint() == null as logic base
                        if (swimlane != null && targetDefinition.getSwimlaneDisplayMode() == SwimlaneDisplayMode.none) {
                            swimlane.setConstraint(null);
                        }
                        ((SwimlanedNode) entry.getValue()).setSwimlane(swimlane);
                    }
                }
            }
        } catch (Exception e) {
            PluginLogger.logError("'Paste' operation failed", e);
        }
    }

    @Override
    public void undo() {
        // remove nodes
        for (NamedGraphElement node : targetNodeMap.values()) {
            targetDefinition.removeChild(node);
        }
        // undo actions
        for (ExtraCopyAction extraCopyAction : executedCopyActions) {
            try {
                extraCopyAction.undo();
            } catch (Exception e) {
                PluginLogger.logError("Unable undo operation for action " + extraCopyAction, e);
            }
        }
    }

    public List<NamedGraphElement> getFilteredElements() {
        return new ArrayList<NamedGraphElement>(targetNodeMap.values());
    }

    private void adjustLocation(GraphElement ge) {
        if (!targetEditor.toString().equals(copyBuffer.getEditorId())) {
            Rectangle constraint = ge.getConstraint();
            int deltaX = GEFConstants.GRID_SIZE - copyBuffer.getViewportLocation().x() + targetViewportLocation.x();
            int deltaY = GEFConstants.GRID_SIZE - copyBuffer.getViewportLocation().y() + targetViewportLocation.y();
            if (constraint != null) {
                Rectangle rect = constraint.getCopy();
                rect.setX(constraint.x() - deltaX);
                rect.setY(constraint.y() - deltaY);
                ge.setConstraint(rect);
            }
            if (ge instanceof Transition) {
                List<Point> bendPoints = ((Transition) ge).getBendpoints();
                for (Point bendPoint : bendPoints) {
                    bendPoint.setX(bendPoint.x() - deltaX);
                    bendPoint.setY(bendPoint.y() - deltaY);
                }
            }
        }
    }

    private class CopyFormFilesAction extends ExtraCopyAction {
        private final FormNode sourceFormNode;
        private final FormNode targetFormNode;
        private IFolder sourceFolder;
        private IFolder targetFolder;
        private IFile formFile;
        private IFile validationFile;
        private IFile scriptFile;
        private IFile templateFile;

        public CopyFormFilesAction(FormNode sourceFormNode, FormNode targetFormNode) {
            super(CopyBuffer.GROUP_FORM_FILES, sourceFormNode.getName() + " (" + sourceFormNode.getId() + ")");
            this.sourceFormNode = sourceFormNode;
            this.targetFormNode = targetFormNode;
        }

        public void setSourceFolder(IFolder sourceFolder) {
            this.sourceFolder = sourceFolder;
        }

        public void setTargetFolder(IFolder targetFolder) {
            this.targetFolder = targetFolder;
        }

        @Override
        protected String getChanges() {
            List<String> fileNames = Lists.newArrayList();
            if (targetFormNode.hasForm() && targetFolder.getFile(targetFormNode.getFormFileName()).exists()) {
                fileNames.add(targetFormNode.getFormFileName());
            }
            if (targetFormNode.hasFormValidation() && targetFolder.getFile(targetFormNode.getValidationFileName()).exists()) {
                fileNames.add(targetFormNode.getValidationFileName());
            }
            if (targetFormNode.hasFormScript() && targetFolder.getFile(targetFormNode.getScriptFileName()).exists()) {
                fileNames.add(targetFormNode.getScriptFileName());
            }
            if (fileNames.isEmpty()) {
                return super.getChanges();
            }
            return fileNames.toString();
        }

        @Override
        public void execute() throws CoreException {
            if (sourceFormNode.hasForm()) {
                formFile = copyFile(sourceFormNode.getFormFileName(), targetFormNode.getFormFileName());
            }
            if (sourceFormNode.hasFormValidation()) {
                validationFile = copyFile(sourceFormNode.getValidationFileName(), targetFormNode.getValidationFileName());
            }
            if (sourceFormNode.hasFormScript()) {
                scriptFile = copyFile(sourceFormNode.getScriptFileName(), targetFormNode.getScriptFileName());
            }
            if (targetFormNode.hasFormTemplate() && !targetFolder.getFile(targetFormNode.getTemplateFileName()).exists()) {
                templateFile = copyFile(sourceFormNode.getTemplateFileName(), targetFormNode.getTemplateFileName());
            }
        }

        private IFile copyFile(String sourceFileName, String targetFileName) throws CoreException {
            PluginLogger.logInfo("copying " + sourceFileName + " to " + targetFileName);
            InputStream is = sourceFolder.getFile(sourceFileName).getContents();
            IFile file = targetFolder.getFile(targetFileName);
            IOUtils.createOrUpdateFile(file, is);
            return file;
        }

        @Override
        public void undo() throws CoreException {
            if (formFile != null) {
                formFile.delete(true, null);
            }
            if (validationFile != null) {
                validationFile.delete(true, null);
            }
            if (scriptFile != null) {
                scriptFile.delete(true, null);
            }
            if (templateFile != null) {
                templateFile.delete(true, null);
            }
        }

    }

    private class CopyDelegableElementAction extends ExtraCopyAction {
        private final NamedGraphElement source;
        private final NamedGraphElement target;
        private IFolder sourceFolder;
        private IFolder targetFolder;

        public CopyDelegableElementAction(NamedGraphElement source, NamedGraphElement target) {
            super(CopyBuffer.GROUP_DELEGABLE, source.getName() + " (" + source.getId() + ")");
            this.source = source;
            this.target = target;
        }

        public void setSourceFolder(IFolder sourceFolder) {
            this.sourceFolder = sourceFolder;
        }

        public void setTargetFolder(IFolder targetFolder) {
            this.targetFolder = targetFolder;
        }

        @Override
        public void execute() throws CoreException {
            DelegableProvider provider = HandlerRegistry.getProvider(source.getDelegationClassName());
            provider.onCopy(sourceFolder, (Delegable) source, source.getId(), targetFolder, (Delegable) target, target.getId());
        }

        @Override
        public void undo() throws CoreException {
            DelegableProvider provider = HandlerRegistry.getProvider(source.getDelegationClassName());
            provider.onDelete((Delegable) target);
        }

    }

    private class CopySwimlaneAction extends ExtraCopyAction {
        private final Swimlane sourceSwimlane;
        private final Swimlane oldSwimlane;
        private Swimlane addedSwimlane;

        public CopySwimlaneAction(Swimlane sourceSwimlane) {
            super(CopyBuffer.GROUP_SWIMLANES, sourceSwimlane.getName());
            this.sourceSwimlane = sourceSwimlane;
            this.oldSwimlane = targetDefinition.getSwimlaneByName(sourceSwimlane.getName());
            if (oldSwimlane != null) {
                setEnabled(getChanges() != null);
            }
        }

        @Override
        protected String getChanges() {
            if (oldSwimlane == null) {
                return null;
            }
            if (!Objects.equal(oldSwimlane.getDelegationClassName(), sourceSwimlane.getDelegationClassName())) {
                return oldSwimlane.getDelegationClassName() + "/" + sourceSwimlane.getDelegationClassName();
            }
            if (!Objects.equal(oldSwimlane.getDelegationConfiguration(), sourceSwimlane.getDelegationConfiguration())) {
                return oldSwimlane.getDelegationConfiguration() + "/" + sourceSwimlane.getDelegationConfiguration();
            }
            return super.getChanges();
        }

        @Override
        public void execute() {
            if (oldSwimlane != null) {
                targetDefinition.removeChild(oldSwimlane);
            }
            addedSwimlane = (Swimlane) sourceSwimlane.makeCopy(targetDefinition);
            adjustLocation(addedSwimlane);
        }

        @Override
        public void undo() {
            targetDefinition.removeChild(addedSwimlane);
            if (oldSwimlane != null) {
                targetDefinition.addChild(oldSwimlane);
            }
        }

    }

    private class CopyVariableAction extends ExtraCopyAction {
        private final Variable sourceVariable;
        private final Variable oldVariable;
        private Variable addedVariable;
        private VariableUserType addedUserType;

        public CopyVariableAction(Variable sourceVariable) {
            super(CopyBuffer.GROUP_VARIABLES, sourceVariable.getName());
            this.sourceVariable = sourceVariable;
            this.oldVariable = VariableUtils.getVariableByName(targetDefinition, sourceVariable.getName());
            if (oldVariable != null) {
                setEnabled(getChanges() != null);
            }
        }

        @Override
        protected String getChanges() {
            if (oldVariable == null) {
                return null;
            }
            if (!Objects.equal(oldVariable.getFormat(), sourceVariable.getFormat())) {
                return oldVariable.getFormat() + "/" + sourceVariable.getFormat();
            }
            return super.getChanges();
        }

        @Override
        public void execute() {
            if (oldVariable != null) {
                targetDefinition.removeChild(oldVariable);
            }
            addedVariable = (Variable) sourceVariable.makeCopy(targetDefinition);
            adjustLocation(addedVariable);
            if (addedVariable.isComplex() && targetDefinition.getVariableUserType(addedVariable.getUserType().getName()) == null) {
                addedUserType = addedVariable.getUserType().getCopy();
                targetDefinition.addVariableUserType(addedUserType);
            }
        }

        @Override
        public void undo() {
            targetDefinition.removeChild(addedVariable);
            if (addedUserType != null) {
                targetDefinition.removeVariableUserType(addedUserType);
            }
            if (oldVariable != null) {
                targetDefinition.addChild(oldVariable);
            }
        }

    }

    private class CopyUserTypeAttributeAction extends ExtraCopyAction {
        private final Variable sourceVariable;
        private final Variable complexVariable;
        private final List<VariableUserType> usedUserTypes = Lists.newArrayList();
        private Variable addedVariable;
        private final List<VariableUserType> addedUserTypes = Lists.newArrayList();

        public CopyUserTypeAttributeAction(Variable sourceVariable, ProcessDefinition processDefinition) {
            super(CopyBuffer.GROUP_VARIABLES, sourceVariable.getName());
            this.sourceVariable = sourceVariable;
            this.complexVariable = VariableUtils.getComplexVariableByExpandedAttribute(processDefinition, sourceVariable.getName());
            this.usedUserTypes.addAll(VariableUtils.getUsedUserTypes(processDefinition, sourceVariable.getName()));
        }

        @Override
        public void execute() {
            Variable oldComplexVariable = VariableUtils.getVariableByName(targetDefinition, complexVariable.getName());
            if (oldComplexVariable != null && !Objects.equal(oldComplexVariable.getUserType().getName(), complexVariable.getUserType().getName())) {
                throw new RuntimeException(oldComplexVariable.getName() + " has incompatible formats: " + oldComplexVariable.getUserType().getName()
                        + "/" + complexVariable.getUserType().getName());
            }
            if (oldComplexVariable == null) {
                addedVariable = (Variable) complexVariable.makeCopy(targetDefinition);
                adjustLocation(addedVariable);
            }
            for (VariableUserType userType : usedUserTypes) {
                if (targetDefinition.getVariableUserType(userType.getName()) == null) {
                    VariableUserType addedUserType = userType.getCopy();
                    targetDefinition.addVariableUserType(addedUserType);
                    addedUserTypes.add(addedUserType);
                }
            }
        }

        @Override
        public void undo() {
            if (addedVariable != null) {
                targetDefinition.removeChild(addedVariable);
            }
            for (VariableUserType addedUserType : addedUserTypes) {
                targetDefinition.removeVariableUserType(addedUserType);
            }
        }
    }
}
