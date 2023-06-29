package ru.runa.gpd.quick.formeditor.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.osgi.framework.Bundle;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.FtlFormType;
import ru.runa.gpd.formeditor.ftl.TemplateProcessor;
import ru.runa.gpd.lang.model.FormNode;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.quick.Messages;
import ru.runa.gpd.quick.extension.QuickTemplateRegister;
import ru.runa.gpd.quick.formeditor.QuickForm;
import ru.runa.gpd.quick.formeditor.QuickFormEditorUtil;
import ru.runa.gpd.quick.formeditor.QuickFormGpdProperty;
import ru.runa.gpd.quick.formeditor.QuickFormType;
import ru.runa.gpd.quick.tag.FormHashModelGpdWrap;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.WorkspaceOperations;
import ru.runa.wfe.var.MapDelegableVariableProvider;

public final class QuickFormConvertor {
    public interface ConverterSource {
        QuickForm getQuickForm();

        IFile getQuickFormFile();

        ProcessDefinition getProcessDefinition();

        FormNode getFormNode();
    }

    public static void convertQuickFormToSimple(ConverterSource converterSource) {
        String messageKey = "QuickFormConverting.warning.message";
        if (Dialogs.confirm(Messages.getString(messageKey))) {
            try {
                closeQuickFormEditor(converterSource);

                applyTemplateToForm(converterSource);

                deleteTemplate(converterSource);

                IFile newFile = updateFormToSimple(converterSource);

                WorkspaceOperations.refreshResource(newFile.getParent());

                IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), newFile, true);
            } catch (Exception e) {
                PluginLogger.logError("Error on converting template form: '" + converterSource.getQuickForm().getName() + "'", e);
            }
        }
    }

    private static void closeQuickFormEditor(ConverterSource converterSource) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorPart editor = page.findEditor(new FileEditorInput(converterSource.getQuickFormFile()));
        if (editor != null) {
            page.closeEditor(editor, false);
        }
    }

    private static void applyTemplateToForm(ConverterSource converterSource) throws CoreException {
        FormNode formNode = converterSource.getFormNode();
        Bundle bundle = QuickTemplateRegister.getBundle(formNode.getTemplateFileName());
        String templateHtml = QuickFormXMLUtil.getTemplateFromRegister(bundle, formNode.getTemplateFileName());

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("variables", converterSource.getQuickForm().getVariables());
        variables.put("task", "");
        for (QuickFormGpdProperty quickFormGpdProperty : converterSource.getQuickForm().getProperties()) {
            variables.put(quickFormGpdProperty.getName(), quickFormGpdProperty.getValue() == null ? "" : quickFormGpdProperty.getValue());
        }
        MapDelegableVariableProvider variableProvider = new MapDelegableVariableProvider(variables, null);
        FormHashModelGpdWrap model = new FormHashModelGpdWrap(null, variableProvider, null);

        String out = TemplateProcessor.process(templateHtml, model);
        ByteArrayInputStream stream = new ByteArrayInputStream(out.getBytes());
        converterSource.getQuickFormFile().setContents(stream, true, true, null);
    }

    private static IFile updateFormToSimple(ConverterSource converterSource) throws Exception {
        File file = converterSource.getQuickFormFile().getRawLocation().makeAbsolute().toFile();
        renameFileExtension(file.getAbsolutePath(), "ftl");
        String newFileName = converterSource.getFormNode().getFormFileName().replaceAll(QuickFormType.TYPE, FtlFormType.TYPE);
        converterSource.getFormNode().setFormFileName(newFileName);
        converterSource.getFormNode().setFormType("ftl");
        converterSource.getFormNode().setTemplateFileName(null);
        ProcessDefinition definition = converterSource.getProcessDefinition();
        WorkspaceOperations.saveProcessDefinition(definition);
        return IOUtils.getFile(newFileName);
    }

    private static boolean renameFileExtension(String source, String newExtension) {
        String target;
        String currentExtension = getFileExtension(source);

        if (currentExtension.equals("")) {
            target = source + "." + newExtension;
        } else {
            target = source.replaceFirst(Pattern.quote("." + currentExtension) + "$", Matcher.quoteReplacement("." + newExtension));
        }

        return new File(source).renameTo(new File(target));
    }

    private static String getFileExtension(String f) {
        String ext = "";
        int i = f.lastIndexOf('.');
        if (i > 0 && i < f.length() - 1) {
            ext = f.substring(i + 1);
        }
        return ext;
    }

    private static void deleteTemplate(ConverterSource converterSource) throws CoreException {
        if (!QuickFormEditorUtil.isTemplateUsingInForms(converterSource.getProcessDefinition(), converterSource.getFormNode(), converterSource
                .getFormNode().getTemplateFileName())) {
            IFolder folder = (IFolder) converterSource.getQuickFormFile().getParent();
            IFile templateFile = folder.getFile(converterSource.getFormNode().getTemplateFileName());
            templateFile.delete(true, null);
        }
    }
}
