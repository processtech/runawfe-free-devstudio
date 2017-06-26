package ru.runa.gpd.extension.regulations.ui;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.settings.CommonPreferencePage;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class CreateProcessRegulations extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            IFile definitionFile = getActiveDesignerEditor().getDefinitionFile();
            List<ValidationError> validationErrors = Lists.newArrayList();
            boolean success = RegulationsUtil.validate(definitionFile, processDefinition, validationErrors);
            if (success) {
                String html = RegulationsUtil.generate(definitionFile, processDefinition);
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), "regulations.html");
                IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
                IDE.openEditor(getWorkbenchPage(), file);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        ProcessDefinition definition = getSelection();
        boolean enabled = definition != null && CommonPreferencePage.isRegulationsMenuItemsEnabled()
                && getSelection().getClass().equals(ProcessDefinition.class) && !definition.isInvalid();
        action.setEnabled(enabled);
    }

}