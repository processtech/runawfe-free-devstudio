package ru.runa.gpd.editor;

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
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.CopyBuffer.ExtraCopyAction;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.lang.Language;
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
        return Localization.getString("button.paste") + " " + this.getClass().getSimpleName();
    }

    @Override
    public void execute() {
        try {
            if (targetDefinition instanceof SubprocessDefinition) {
                IFile definitionFile = ((SubprocessDefinition) targetDefinition).getMainProcessDefinition().getFile();
                if (EditorUtils.getOpenedEditor(definitionFile) == null) {
                    new InfoWithDetailsDialog(MessageDialog.WARNING, Localization.getString("message.warning"),
                            Localization.getString("CopyBuffer.MainProcessDefinition.closed.warning"), null).open();
                    return;
                }
            }
            if (!copyBuffer.getLanguage().equals(targetDefinition.getLanguage())) {
                new InfoWithDetailsDialog(MessageDialog.WARNING, Localization.getString("message.warning"),
                        Localization.getString("CopyBuffer.DifferentVersion.warning"), null).open();
                return;
            }
            targetNodeMap.clear();
            executedCopyActions.clear();
            nodeToSwimlaneNameMap.clear();
            Set<ExtraCopyAction> copyActions = new HashSet<ExtraCopyAction>();
            List<NamedGraphElement> sourceNodeList = copyBuffer.getSourceNodes();
            // add nodes
            final List<NamedGraphElement> newElements = new ArrayList<>();
            for (NamedGraphElement node : sourceNodeList) {
                if (!(node.getParent() instanceof ProcessDefinition)) {
                    continue;
                } else if (node instanceof StartState && targetDefinition.getChildren(StartState.class).size() != 0) {
                    continue;
                } else if (node instanceof EndState && (targetDefinition instanceof SubprocessDefinition
                        && ((SubprocessDefinition) targetDefinition).getBehavior() == Behavior.GraphPart)) {
                    continue;
                    // if swimlane is copied as graph element twice
                } else if (node instanceof Swimlane && targetDefinition.getSwimlaneByName(node.getName()) != null) {
                    continue;
                }
                NamedGraphElement copy = (NamedGraphElement) node.makeCopy(targetDefinition);
                adjustLocation(copy);
                newElements.add(copy);
                List<Variable> usedVariables = node.getUsedVariables(copyBuffer.getSourceFolder());
                Set<String> usedVariableNames = usedVariables.stream().map(v -> v.getName()).collect(Collectors.toSet());
                for (Variable variable : usedVariables) {
                    if (variable instanceof Swimlane) {
                        copyActions.add(new CopySwimlaneAction((Swimlane) variable));
                    } else {
                        String rootVariableName = variable.getName().split(Pattern.quote(UserType.DELIM))[0];
                        CopyVariableAction copyVariableAction = new CopyVariableAction(node.getProcessDefinition(),
                                VariableUtils.getVariableByName(node.getProcessDefinition(), rootVariableName), usedVariableNames);
                        if (copyActions.contains(copyVariableAction)) {
                            copyActions.stream().filter(a -> a.equals(copyVariableAction))
                                    .anyMatch(a -> ((CopyVariableAction) a).usedVariableNames.addAll(usedVariableNames));
                        } else {
                            copyActions.add(copyVariableAction);
                        }
                    }
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
                Transition timerTransition = null;
                Timer timer = null;
                if (node instanceof ITimed) {
                    timer = ((ITimed) node).getTimer();
                    if (timer != null) {
                        Transition transition = timer.getFirstChild(Transition.class);
                        if (transition != null) {
                            timerTransition = transition;
                            timer = ((ITimed) targetNodeMap.get(node.getId())).getTimer();
                            transitions.add(transition);
                        }
                    }
                }
                CatchEventNode catchEvent = null;
                if (node instanceof IBoundaryEventContainer) {
                    catchEvent = node.getFirstChild(CatchEventNode.class);
                    if (catchEvent != null) {
                        Transition transition = catchEvent.getFirstChild(Transition.class);
                        if (transition != null) {
                            catchEvent = targetNodeMap.get(node.getId()).getFirstChild(CatchEventNode.class);
                            transitions.add(transition);
                        }
                    }
                }
                for (Transition transition : transitions) {
                    NamedGraphElement source = targetNodeMap.get(transition.getSource().getId());
                    if (source == null) {
                        if (transition.equals(timerTransition)) {
                            source = timer;
                        } else {
                            source = catchEvent;
                        }
                    }
                    NamedGraphElement target = targetNodeMap.get(transition.getTarget().getId());
                    if (source != null && target != null) {
                        Transition copy = transition.makeCopy(source);
                        adjustLocation(copy);
                        copy.setTarget((Node) target);
                        newElements.add(copy);
                    }
                    List<Variable> usedVariables = transition.getUsedVariables(copyBuffer.getSourceFolder());
                    Set<String> usedVariableNames = usedVariables.stream().map(v -> v.getName()).collect(Collectors.toSet());
                    for (Variable variable : usedVariables) {
                        if (variable instanceof Swimlane) {
                            copyActions.add(new CopySwimlaneAction((Swimlane) variable));
                        } else {
                            String rootVariableName = variable.getName().split(Pattern.quote(UserType.DELIM))[0];
                            CopyVariableAction copyVariableAction = new CopyVariableAction(node.getProcessDefinition(),
                                    VariableUtils.getVariableByName(node.getProcessDefinition(), rootVariableName), usedVariableNames);
                            if (copyActions.contains(copyVariableAction)) {
                                copyActions.stream().filter(a -> a.equals(copyVariableAction))
                                        .anyMatch(a -> ((CopyVariableAction) a).usedVariableNames.addAll(usedVariableNames));
                            } else {
                                copyActions.add(copyVariableAction);
                            }
                        }
                    }
                }
            }
            // select new elements
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
            // run copy actions
            for (ExtraCopyAction copyAction : copyActions) {
                copyAction.execute();
                executedCopyActions.add(copyAction);
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
        int shiftInCaseOfTheSameDiagram = targetEditor.toString().equals(copyBuffer.getEditorId()) ? -GEFConstants.GRID_SIZE : 0;
        Rectangle constraint = ge.getConstraint();
        int deltaX = shiftInCaseOfTheSameDiagram + copyBuffer.getViewportLocation().x() - targetViewportLocation.x();
        int deltaY = shiftInCaseOfTheSameDiagram + copyBuffer.getViewportLocation().y() - targetViewportLocation.y();
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
        }

        @Override
        public void execute() {
            if (oldSwimlane == null) {
                addedSwimlane = (Swimlane) sourceSwimlane.makeCopy(targetDefinition);
                adjustLocation(addedSwimlane);
            }
        }

        @Override
        public void undo() {
            if (addedSwimlane != null) {
                targetDefinition.removeChild(addedSwimlane);
            }
        }

    }

    private class CopyVariableAction extends ExtraCopyAction {
        private final ProcessDefinition sourceProcessDefinition;
        private final Variable sourceVariable;
        private final Variable oldVariable;
        private Variable addedVariable;
        private Map<VariableUserType, List<Variable>> changedUserTypes = new HashMap<>();
        private Set<String> usedVariableNames;

        public CopyVariableAction(ProcessDefinition sourceProcessDefinition, Variable sourceVariable, Set<String> usedVariableNames) {
            super(CopyBuffer.GROUP_VARIABLES, sourceVariable.getName());
            this.sourceProcessDefinition = sourceProcessDefinition;
            this.sourceVariable = sourceVariable;
            this.oldVariable = VariableUtils.getVariableByName(targetDefinition, sourceVariable.getName());
            this.usedVariableNames = usedVariableNames;
        }

        @Override
        public void execute() {
            copyReferencedUserTypes(sourceVariable, true);
            if (oldVariable == null) {
                addedVariable = (Variable) sourceVariable.makeCopy(targetDefinition);
            }
        }

        private void copyReferencedUserTypes(Variable srcVar, boolean isCopyNeeded) {
            if (srcVar.isComplex()) {
                VariableUserType sourceUserType = isCopyNeeded ? srcVar.getUserType().getCopy() : srcVar.getUserType();
                VariableUserType userType = targetDefinition.getVariableUserType(srcVar.getUserType().getName());
                if (userType == null) {
                    // full copy
                    userType = sourceUserType;
                    targetDefinition.addVariableUserType(userType);
                    changedUserTypes.put(userType, null);
                    for (Variable v : sourceUserType.getAttributes()) {
                        if (v.isComplex() || VariableUtils.isContainerVariable(v)) {
                            copyReferencedUserTypes(v, false);
                        }
                    }
                } else {
                    // copy only referenced attributes
                    List<Variable> addedAttributes = new ArrayList<>();
                    List<Variable> userTypeAttributes = userType.getAttributes();
                    for (Variable v : sourceUserType.getAttributes()) {
                        if (!userTypeAttributes.contains(v) && usedVariableNames.contains(srcVar.getName() + UserType.DELIM + v.getName())) {
                            if (v.isComplex() || VariableUtils.isContainerVariable(v)) {
                                copyReferencedUserTypes(v, false);
                            }
                            userType.addAttribute(v);
                            addedAttributes.add(v);
                        }
                    }
                    if (!changedUserTypes.containsKey(userType)) {
                        changedUserTypes.put(userType, addedAttributes);
                    }
                }
            } else if (VariableUtils.isContainerVariable(srcVar)) {
                String[] componentNames = srcVar.getFormatComponentClassNames();
                for (String componentName : componentNames) {
                    if (VariableUtils.isValidUserTypeName(componentName)) {
                        VariableUserType sourceUserType = sourceProcessDefinition.getVariableUserType(componentName);
                        if (sourceUserType != null && targetDefinition.getVariableUserType(sourceUserType.getName()) == null) {
                            // full copy
                            VariableUserType addedUserType = sourceUserType.getCopy();
                            targetDefinition.addVariableUserType(addedUserType);
                            changedUserTypes.put(addedUserType, null);
                            for (Variable v : sourceUserType.getAttributes()) {
                                if (v.isComplex() || VariableUtils.isContainerVariable(v)) {
                                    copyReferencedUserTypes(v, false);
                                }
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void undo() {
            if (addedVariable != null) {
                targetDefinition.removeChild(addedVariable);
            }
            for (Map.Entry<VariableUserType, List<Variable>> entry : changedUserTypes.entrySet()) {
                if (entry.getValue() == null) {
                    targetDefinition.removeVariableUserType(entry.getKey());
                } else {
                    for (Variable addedAttribute : entry.getValue()) {
                        entry.getKey().removeAttribute(addedAttribute);
                    }
                }
            }
        }

    }

}
