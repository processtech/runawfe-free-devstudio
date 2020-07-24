package ru.runa.gpd.ui.wizard;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.TreeMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredSelection;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.bot.BotTaskDeployCommand;
import ru.runa.gpd.bot.BotTaskExportCommand;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.IOUtils;

public class ExportBotTaskWizardPage extends ExportBotWizardPage {
    public ExportBotTaskWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportBotTaskWizardPage.page.title"));
        setDescription(Localization.getString("ExportBotTaskWizardPage.page.description"));
        this.exportObjectNameFileMap = new TreeMap<String, IResource>();
        for (IFile resource : IOUtils.getAllBotTasks()) {
            exportObjectNameFileMap.put(getSelectionResourceKey(resource), resource);
        }
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return resource.getProject().getName() + "/" + resource.getParent().getName() + "/" + resource.getName();
    }

    @Override
    protected String getFileName(String selectionName) {
        return selectionName.substring(selectionName.indexOf("/") + 1, selectionName.lastIndexOf("/")) + "."
                + selectionName.substring(selectionName.lastIndexOf("/") + 1) + getOutputSuffix();
    }

    @Override
    protected void exportToZipFile(IResource exportResource) throws Exception {
        String errorsDetails[] = { "" };
        Boolean docxTestResult = checkBotTaskParametersWithDocxTemplate(errorsDetails);
        if (null == docxTestResult) {
            Dialogs.error(Localization.getString("DialogEnhancement.docxCheckError"));
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
        } else if (docxTestResult) {
            getContainer().run(true, true, new BotTaskExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportSuccessful"));
        } else {
            switch (Dialogs.confirmWithAction(Localization.getString("DialogEnhancement.parametersNotCorrespondingWithDocxQ"),
                    Localization.getString("Update.from.docx.template"), errorsDetails[0], true)) {
            case IDialogConstants.PROCEED_ID:
                DialogEnhancement.updateBotTaskFromDocxTemplate(exportResource);
                PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
                break;
            case IDialogConstants.OK_ID:
                getContainer().run(true, true, new BotTaskExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportWithDocxErrors"));
                break;
            default:
                PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
            }

        }
    }

    @Override
    protected void deployToServer(IResource exportResource) throws Exception {
        String errorsDetails[] = { "" };
        Boolean docxTestResult = checkBotTaskParametersWithDocxTemplate(errorsDetails);
        if (null == docxTestResult) {
            Dialogs.error(Localization.getString("DialogEnhancement.docxCheckError"));
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
        } else if (!docxTestResult) {
            if (IDialogConstants.PROCEED_ID == Dialogs.errorWithAction(Localization.getString("DialogEnhancement.parametersNotCorrespondingWithDocx"),
                    Localization.getString("Update.from.docx.template"), errorsDetails[0], true)) {
                DialogEnhancement.updateBotTaskFromDocxTemplate(exportResource);
            }
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
        } else {
            getContainer().run(true, true, new BotTaskDeployCommand(exportResource, new ByteArrayOutputStream()));
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportSuccessful"));
        }
    }

    private Boolean checkBotTaskParametersWithDocxTemplate(String errorsDetails[]) {
        if (isDialogEnhancementMode() && null != exportResource && exportResource instanceof IFile) {
            IFile botTaskFile = (IFile) exportResource;
            BotTask botTask = null != botTaskFile ? BotCache.getBotTaskNotNull(botTaskFile) : null;
            if (null != botTask && 0 == botTask.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID)) {
                Object obj = DialogEnhancement.getConfigurationValue(botTask, DocxDialogEnhancementMode.InputPathId);
                String embeddedDocxTemplateFileName = null != obj && obj instanceof String ? (String) obj : "";
                if (!Strings.isNullOrEmpty(embeddedDocxTemplateFileName)) {
                    List<String> errors = Lists.newArrayList();
                    Boolean result = DialogEnhancement.checkBotTaskParametersWithDocxTemplate(botTask, embeddedDocxTemplateFileName, errors,
                            errorsDetails);
                    if (errors.size() > 0) {
                        botTask.logErrors(errors);
                    }
                    return result;
                }
            }
        }
        return true;
    }

    private boolean isDialogEnhancementMode() {
        return DialogEnhancement.isOn();
    }
}
