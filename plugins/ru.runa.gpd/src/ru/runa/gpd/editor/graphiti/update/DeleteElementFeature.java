package ru.runa.gpd.editor.graphiti.update;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.features.context.IMultiDeleteInfo;
import org.eclipse.graphiti.features.context.impl.DeleteContext;
import org.eclipse.graphiti.internal.command.GenericFeatureCommandWithContext;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.ui.editor.DiagramBehavior;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;
import org.eclipse.graphiti.ui.internal.command.GefCommandWrapper;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.graphiti.CustomUndoRedoFeature;
import ru.runa.gpd.editor.graphiti.HasTextDecorator;
import ru.runa.gpd.lang.model.Action;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.NamedGraphElement;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.Transition;
import ru.runa.gpd.lang.model.bpmn.TextDecorationNode;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.XmlUtil;
import ru.runa.wfe.definition.IFileDataProvider;

public class DeleteElementFeature extends DefaultDeleteFeature implements CustomUndoRedoFeature {

    private static final String NAME = Localization.getString("DeleteElementFeature_1");

    private GraphElement element;
    private List<Transition> transitions;
    private Map<Transition, Integer> transitionIndexes;

    public DeleteElementFeature(IFeatureProvider provider) {
        super(provider);
    }

    @Override
    public String getName() {
        return NAME + " " + element;
    }

    @Override
    // based on default implementation: DefaultDeleteFeature#getUserDecision(IDeleteContext)
    protected boolean getUserDecision(IDeleteContext context) {
        if (!Activator.getPrefBoolean(PrefConstants.P_CONFIRM_DELETION)) {
            return true;
        }
        String msg;
        IMultiDeleteInfo multiDeleteInfo = context.getMultiDeleteInfo();
        if (multiDeleteInfo != null) {
            msg = MessageFormat.format(Localization.getString("DeleteElementFeature_2"), multiDeleteInfo.getNumber());
        } else {
            if (((GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement())) instanceof TextDecorationNode) {
                return true;
            }
            String deleteName = getDeleteName(context);
            if (deleteName != null && deleteName.length() > 0) {
                msg = MessageFormat.format(Localization.getString("DeleteElementFeature_3"), deleteName);
            } else {
                msg = Localization.getString("DeleteElementFeature_4");
            }
        }
        return MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                Localization.getString("DeleteElementFeature_5"), msg);
    }

    @Override
    protected String getDeleteName(IDeleteContext context) {
        GraphElement ge = ((GraphElement) getBusinessObjectForPictogramElement(context.getPictogramElement()));
        if (ge instanceof NamedGraphElement) {
            String name = ((NamedGraphElement) ge).getName();
            if (!Strings.isNullOrEmpty(name)) {
                return name;
            }
        }
        return ge.getId();
    }

    private DeleteElementFeature textDecorationDeleteFeature;

    @SuppressWarnings("restriction")
    @Override
    protected void deleteBusinessObject(Object bo) {
        if (bo == null) {
            return;
        }
        if (element == null) {
            element = (GraphElement) bo;
        }
        if (bo instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) bo;
            textDecoration.getTarget().getParent().removeChild(textDecoration.getTarget());
            return;
        } else if (element instanceof HasTextDecorator) {
            // TODO rm1090: Возможно не очень красивое решение с полем textDecorationDeleteFeature. Нужен свежий взгляд, как лучше отрефакторить
            final HasTextDecorator withDefinition = (HasTextDecorator) element;
            final IDeleteContext delContext = new DeleteContext(
                    withDefinition.getTextDecoratorEmulation().getDefinition().getUiContainer().getOwner());
            if (textDecorationDeleteFeature == null) {
                textDecorationDeleteFeature = (DeleteElementFeature) getFeatureProvider().getDeleteFeature(delContext);
                final DiagramBehavior db = (DiagramBehavior) getDiagramBehavior();
                db.getEditDomain().getCommandStack().execute(
                        new GefCommandWrapper(new GenericFeatureCommandWithContext(textDecorationDeleteFeature, delContext), db.getEditingDomain()));
            } else {
                textDecorationDeleteFeature.postRedo(delContext);
            }
        } else if (element instanceof Transition) {
            Transition transition = (Transition) element;
            transition.getSource().removeLeavingTransition(transition);
            return;
        }
        if (element instanceof Node) {
            if (element instanceof FormNode) {
                removeFormFiles((FormNode) element);
            }
            removeAndStoreTransitions((Node) element);
        }
        if (element instanceof Delegable) {
            Delegable delegable = (Delegable) element;
            removeProcessFiles(delegable);
        }
        element.getParent().removeChild(element);
    }

    @Override
    public void postUndo(IContext context) {
        if (element == null) {
            return;
        }
        if (element instanceof Transition) {
            Transition transition = (Transition) element;
            transition.getSource().addLeavingTransition(transition);
            getDiagramBehavior().refresh();
            return;
        } else if (element instanceof TextDecorationNode) {
            TextDecorationNode textDecoration = (TextDecorationNode) element;
            textDecoration.getTarget().getParent().addChild(textDecoration.getTarget());
            restoreTransitions();
            return;
        } else {
            element.getParent().addChild(element);
            if (element instanceof FormNode) {
                restoreFormFiles();
            }
        }
        if (element instanceof Node) {
            restoreTransitions();
        }
        if (element instanceof Delegable) {
            restoreProcessFiles();
        }
    }

    @Override
    public boolean canRedo(IContext context) {
        return element != null;
    }

    @Override
    public void postRedo(IContext context) {
        deleteBusinessObject(element);
    }

    private void removeAndStoreTransitions(Node node) {
        transitions = Stream.concat(node.getLeavingTransitions().stream(), node.getArrivingTransitions().stream()).collect(Collectors.toList());
        transitionIndexes = Maps.newHashMap();
        transitions.stream().forEach(transition -> {
            Node source = transition.getSource();
            transitionIndexes.put(transition, source.getElements().indexOf(transition));
            source.removeLeavingTransition(transition);
        });
        getDiagramBehavior().refresh();
    }

    private void restoreTransitions() {
        if (transitions != null) {
            Collections.reverse(transitions);
            transitions.stream().forEach(transition -> transition.getSource().addChild(transition, transitionIndexes.get(transition)));
        }
        getDiagramBehavior().refresh();
    }

    @Override
    public void preDelete(IDeleteContext context) {
        super.preDelete(context);
        if (getBusinessObjectForPictogramElement(context.getPictogramElement()) instanceof Action) {
            PictogramElement pe = context.getPictogramElement();
            context.putProperty("action-container",
                    pe instanceof ConnectionDecorator ? ((ConnectionDecorator) context.getPictogramElement()).getConnection()
                            : ((Shape) context.getPictogramElement()).getContainer());
        }
    }

    @Override
    public void postDelete(IDeleteContext context) {
        super.postDelete(context);
        if (element instanceof Action) {
            layoutPictogramElement((PictogramElement) context.getProperty("action-container"));
        }
    }

    private IPath formFilePath;
    private IPath scriptFilePath;
    private IPath validationFilePath;

    private void removeFormFiles(FormNode formNode) {
        IFile processDefinitionFile = formNode.getProcessDefinition().getFile();
        if (formNode.hasFormValidation()) {
            validationFilePath = removeFile(processDefinitionFile, formNode.getValidationFileName(), validationFilePath == null);
        }
        if (formNode.hasFormScript()) {
            scriptFilePath = removeFile(processDefinitionFile, formNode.getScriptFileName(), scriptFilePath == null);
        }
        if (formNode.hasForm()) {
            formFilePath = removeFile(processDefinitionFile, formNode.getFormFileName(), formFilePath == null);
        }
    }

    private void restoreFormFiles() {
        if (formFilePath != null) {
            restoreFile(formFilePath);
        }
        if (scriptFilePath != null) {
            restoreFile(scriptFilePath);
        }
        if (validationFilePath != null) {
            restoreFile(validationFilePath);
        }
    }

    private IPath removeFile(IFile adjacentFile, String fileName, boolean keepHistory) {
        IPath removedFilePath = null;
        try {
            IFile file = IOUtils.getAdjacentFile(adjacentFile, fileName);
            if (file.exists()) {
                file.delete(true, keepHistory, null);
                removedFilePath = file.getFullPath();
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
        return removedFilePath;
    }

    private void restoreFile(IPath filePath) {
        try {
            IFile file = (IFile) ((Workspace) element.getProcessDefinition().getFile().getWorkspace()).newResource(filePath, IResource.FILE);
            IFileState[] fileStates = file.getHistory(null);
            if (!file.exists() && fileStates.length > 0) {
                file.create(fileStates[0].getContents(), true, null);
            }
        } catch (CoreException e) {
            PluginLogger.logError(e);
        }
    }

    private List<IPath> processFilePaths = Lists.newArrayList();

    private void removeProcessFiles(Delegable delegable) {
        String config = delegable.getDelegationConfiguration();
        if (XmlUtil.isXml(config)) {
            processFilePaths.clear();
            IFile processDefinitionFile = ((GraphElement) delegable).getProcessDefinition().getFile();
            Document document = XmlUtil.parseWithoutValidation(config);
            for (Element inputElement : (List<Element>) document.getRootElement().elements("input")) {
                String path = inputElement.attributeValue("path");
                if (EmbeddedFileUtils.isProcessFile(path)) {
                    String fileName = path.substring(IFileDataProvider.PROCESS_FILE_PROTOCOL.length());
                    processFilePaths.add(removeFile(processDefinitionFile, fileName, true));
                }
            }
        }
    }

    private void restoreProcessFiles() {
        processFilePaths.stream().forEach(path -> restoreFile(path));
    }

}
