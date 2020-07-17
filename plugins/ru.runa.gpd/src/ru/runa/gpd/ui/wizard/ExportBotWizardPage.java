package ru.runa.gpd.ui.wizard;

import com.google.common.collect.Lists;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.bot.BotDeployCommand;
import ru.runa.gpd.bot.BotExportCommand;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.ui.custom.Dialogs;
import ru.runa.gpd.ui.enhancement.DialogEnhancement;
import ru.runa.gpd.ui.enhancement.DocxDialogEnhancementMode;
import ru.runa.gpd.util.IOUtils;

public class ExportBotWizardPage extends ExportBotElementWizardPage {
    public ExportBotWizardPage(IStructuredSelection selection) {
        super(selection);
        setTitle(Localization.getString("ExportBotWizardPage.page.title"));
        setDescription(Localization.getString("ExportBotWizardPage.page.description"));
        this.exportObjectNameFileMap = new TreeMap<String, IResource>();
        for (IFolder resource : IOUtils.getAllBotFolders()) {
            exportObjectNameFileMap.put(getKey(resource.getProject(), resource), resource);
        }
    }

    @Override
    protected String getOutputSuffix() {
        return ".bot";
    }

    @Override
    protected String getSelectionResourceKey(IResource resource) {
        return getKey(resource.getProject(), resource);
    }

    @Override
    protected void exportToZipFile(IResource exportResource) throws Exception {
        String errorsDetails[] = { "" };
        Boolean docxTestResult = checkBotTaskParametersWithDocxTemplate(errorsDetails);
        if (null == docxTestResult) {
            Dialogs.error(Localization.getString("DialogEnhancement.docxCheckError"));
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
        } else if (docxTestResult
                || Dialogs.confirm(Localization.getString("DialogEnhancement.parametersNotCorrespondingWithDocxQ"), errorsDetails[0], true)) {
            getContainer().run(true, true, new BotExportCommand(exportResource, new FileOutputStream(getDestinationValue())));
            if (docxTestResult) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportSuccessful"));
            } else {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportWithDocxErrors"));
            }
        } else {
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
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
            Dialogs.error(Localization.getString("DialogEnhancement.parametersNotCorrespondingWithDocx"), errorsDetails[0], true);
            PluginLogger.logErrorWithoutDialog(Localization.getString("DialogEnhancement.exportCanceled"));
            return;
        } else {
            getContainer().run(true, true, new BotDeployCommand(exportResource, new ByteArrayOutputStream()));
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.exportSuccessful"));
        }
    }

    private Boolean checkBotTaskParametersWithDocxTemplate(String errorsDetails[]) {
        if (isDialogEnhancementMode() && null != exportResource && exportResource instanceof IFolder) {
            IFolder processDefinitionFolder = (IFolder) exportResource;
            List<BotTask> botTaskList = BotCache.getBotTasks(processDefinitionFolder.getName());
            ListIterator<BotTask> botTaskListIterator = botTaskList.listIterator();
            boolean wasExceptions = false;
            boolean wasErrors = false;
            while (botTaskListIterator.hasNext()) {
                BotTask botTask = botTaskListIterator.next();
                if (!botTask.getDelegationClassName().equals(DocxDialogEnhancementMode.DocxHandlerID)) {
                    continue;
                }
                Object obj = DialogEnhancement.getConfigurationValue(botTask, DocxDialogEnhancementMode.InputPathId);
                String embeddedDocxTemplateFileName = null != obj && obj instanceof String ? (String) obj : "";
                List<String> errors = Lists.newArrayList();
                Boolean result = DialogEnhancement.checkBotTaskParametersWithDocxTemplate(botTask, embeddedDocxTemplateFileName, errors,
                        errorsDetails);
                if (errors.size() > 0) {
                    botTask.logErrors(errors);
                }
                if (null == result) {
                    wasExceptions = true;
                } else if (!result) {
                    wasErrors = true;
                }
            }
            return wasExceptions ? null : !wasErrors;
        }
        return true;
    }

    private boolean isDialogEnhancementMode() {
        return DialogEnhancement.isOn();
    }
}
