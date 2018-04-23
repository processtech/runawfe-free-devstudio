package ru.runa.gpd.extension.regulations.ui;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class CreateRegulationsAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            boolean success = RegulationsUtil.validate(processDefinition);
            if (success) {
                String html = RegulationsUtil.generate(processDefinition);
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.REGULATIONS_HTML_FILE_NAME);
                IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(html.getBytes()));
                IDE.openEditor(getWorkbenchPage(), file);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        ProcessDefinition processDefinition = getSelection();
        action.setEnabled(processDefinition != null && !processDefinition.isInvalid());
    }

}