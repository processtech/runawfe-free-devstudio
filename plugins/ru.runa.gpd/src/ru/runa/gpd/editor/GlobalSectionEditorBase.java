package ru.runa.gpd.editor;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.ProcessCache;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;

public abstract class GlobalSectionEditorBase extends ProcessEditorBase {
    // TODO seems copy-paste with parent class (fields), method init - also duplicated
    protected ProcessDefinition definition;
    protected IFile definitionFile;
    protected SwimlaneEditorPage swimlanePage;
    protected VariableEditorPage variablePage;
    protected VariableTypeEditorPage variableTypeEditorPage;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        definitionFile = ((FileEditorInput) input).getFile();
        definition = ProcessCache.getProcessDefinition(definitionFile);
        definition.setDirty(false);
        definition.addPropertyChangeListener(this);

        setPartName(definition.getName());

        super.init(site, input);
        getSite().getPage().addSelectionListener(this);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
    }

    @Override
    protected void createPages() {
        try {
            if (!(definition instanceof SubprocessDefinition)) {
                swimlanePage = super.addNewPage(new SwimlaneEditorPage((ProcessEditorBase) this), "DesignerEditor.title.swimlanes");
                variablePage = super.addNewPage(new VariableEditorPage((ProcessEditorBase) this), "DesignerEditor.title.variables");
                variableTypeEditorPage = super.addNewPage(new VariableTypeEditorPage((ProcessEditorBase) this), "VariableUserType.collection");
            }
            ProcessDefinitionValidator.validateDefinition(definition);
        } catch (PartInitException e) {
            PluginLogger.logError(Localization.getString("DesignerEditor.error.can_not_create_graphical_viewer"), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            ProcessDefinitionValidator.validateDefinition(definition);
            WorkspaceOperations.saveProcessDefinition(definition);
            definition.setDirty(false);
            ProcessSaveHistory.addSavepoint(definitionFile);
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
        ProcessDefinition mainProcessDefinition = definition.getMainProcessDefinition();
        try {
            Set<String> usedFormFiles = new HashSet<String>();
            usedFormFiles.add(ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
            usedFormFiles.add(ParContentProvider.FORM_JS_FILE_NAME);
            usedFormFiles.add(ParContentProvider.REGULATIONS_HTML_FILE_NAME);
            fetchUsedFormFiles(usedFormFiles, mainProcessDefinition);
            for (SubprocessDefinition subprocessDefinition : mainProcessDefinition.getEmbeddedSubprocesses().values()) {
                fetchUsedFormFiles(usedFormFiles, subprocessDefinition);
            }
            IFolder folder = (IFolder) definitionFile.getParent();
            IResource[] children = folder.members(true);
            for (IResource resource : children) {
                boolean interested = IOUtils.looksLikeFormFile(resource.getName());
                if (interested && !usedFormFiles.contains(resource.getName())) {
                    try {
                        PluginLogger.logInfo("Deleting unused " + resource);
                        resource.delete(true, monitor);
                    } catch (ResourceException e) {
                        PluginLogger.logErrorWithoutDialog(resource + " is being used by another process");
                    }
                }
                if (ParContentProvider.PROCESS_IMAGE_OLD_FILE_NAME.equals(resource.getName())) {
                    PluginLogger.logInfo("Deleting jpg graph image: " + resource);
                    resource.delete(true, null);
                }
            }
            IOUtils.eraseDeletedFiles(folder);
        } catch (CoreException e) {
            PluginLogger.logErrorWithoutDialog("Cleaning unused form files", e);
        }
    }

}
