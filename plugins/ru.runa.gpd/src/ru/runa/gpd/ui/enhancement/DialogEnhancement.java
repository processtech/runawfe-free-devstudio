package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.extension.DelegableProvider;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.par.ProcessDefinitionValidator;
import ru.runa.gpd.util.EmbeddedFileUtils;

public class DialogEnhancement {

    public static boolean isOn() {
        return dialogEnhancementMode;
    }

    public static Object getConfigurationValue(Delegable delegable, String valueId) {
        Object obj = null;
        try {
            obj = HandlerRegistry.getProvider(delegable.getDelegationClassName()).getConfigurationValue(delegable, valueId);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return obj;
    }

    public static String showConfigurationDialog(Delegable delegable) {
        DelegableProvider provider = HandlerRegistry.getProvider(delegable.getDelegationClassName());
        String newConfig = provider.showConfigurationDialog(delegable,
                DocxDialogEnhancementMode.isScriptDocxHandlerEnhancement(delegable) ? new DocxDialogEnhancementMode(true, 0) {
                    private String templateFilePath;

                    @Override
                    public void invoke(long flags) {
                        if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_RELOAD_FROM_TEMPLATE)) {
                            List<String> errors = Lists.newArrayList();
                            List<Delegable> errorSources = Lists.newArrayList();
                            DialogEnhancement.checkScriptTaskParametersWithDocxTemplate(delegable, templateFilePath, errors, errorSources, null);

                            if (delegable instanceof GraphElement && errors.size() > 0) {
                                ProcessDefinition processDefinition = ((GraphElement) delegable).getProcessDefinition();
                                ProcessDefinition mainProcessDefinition = null != processDefinition ? processDefinition.getMainProcessDefinition()
                                        : null;
                                if (null != mainProcessDefinition) {
                                    ProcessDefinitionValidator.logErrors(mainProcessDefinition, errors, errorSources, true);
                                }
                            }
                        } else if (DialogEnhancementMode.check(flags, DialogEnhancementMode.DOCX_SET_PROCESS_FILEPATH)) {
                            templateFilePath = this.defaultFileName;
                        }
                    }
                } : null);
        return newConfig;
    }

    private static String wrapToScriptName(Delegable delegable, String message) {
        return message + " (" + Localization.getString("DialogEnhancement.scriptTask") + " \"" + delegable.toString() + "\")";
    }

    public static boolean updateBotFromDocxTemplate(IResource exportResource) throws Exception {
        boolean result = false;
        if (null != exportResource && exportResource instanceof IFolder) {
            IFolder processDefinitionFolder = (IFolder) exportResource;
            List<BotTask> botTaskList = BotCache.getBotTasks(processDefinitionFolder.getName());
            ListIterator<BotTask> botTaskListIterator = botTaskList.listIterator();
            while (botTaskListIterator.hasNext()) {
                BotTask botTask = botTaskListIterator.next();
                if (null == botTask || !botTask.getDelegationClassName().equals(DocxDialogEnhancementMode.DocxHandlerID)) {
                    continue;
                }
                IFile botTaskFile = BotCache.getBotTaskFile(botTask);
                if (null != botTaskFile && DialogEnhancement.updateBotTaskFromDocxTemplate(botTaskFile)) {
                    result = true;
                }
            }
        }
        return result;
    }

    public static boolean updateBotTaskFromDocxTemplate(IResource exportResource) throws Exception {
        IFile botTaskFile = (IFile) exportResource;
        BotTask botTask = null != botTaskFile ? BotCache.getBotTaskNotNull(botTaskFile) : null;
        if (null != botTask && 0 == botTask.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID)) {
            Object obj = DialogEnhancement.getConfigurationValue(botTask, DocxDialogEnhancementMode.InputPathId);
            String embeddedDocxTemplateFileName = null != obj && obj instanceof String ? (String) obj : "";
            if (!Strings.isNullOrEmpty(embeddedDocxTemplateFileName)) {

                if (DialogEnhancement.updateBotTaskFromTemplate(botTask, embeddedDocxTemplateFileName)) {
                    IEditorPart editorPart = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), botTaskFile,
                            BotTaskEditor.ID, true);
                    if (null != editorPart && editorPart instanceof BotTaskEditor) {
                        BotTaskEditor botTaskEditor = (BotTaskEditor) editorPart;
                        botTaskEditor.setDirty(true);
                        botTaskEditor.setTableInput(ParamDefGroup.NAME_INPUT);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // use EmbeddedFileUtils.getProcessFileName(embeddedDocxTemplateFileName) before call that function !!!
    public static Boolean checkScriptTaskParametersWithDocxTemplate(Delegable delegable, String embeddedDocxTemplateFileName, List<String> errors,
            List<Delegable> errorSources, String[] errorsDetails) {

        ProcessDefinition processDefinition = delegable instanceof GraphElement ? ((GraphElement) delegable).getProcessDefinition() : null;

        IFile file = null;
        try {
            file = Strings.isNullOrEmpty(embeddedDocxTemplateFileName) || null == processDefinition ? null
                    : EmbeddedFileUtils.getProcessFile(processDefinition, embeddedDocxTemplateFileName);
        } catch (Throwable exception) {
            exception.printStackTrace();
        }

        if (null == file || !file.exists()) {
            String error = Localization.getString("DialogEnhancement.cantGetFile",
                    null == embeddedDocxTemplateFileName ? "NULL" : embeddedDocxTemplateFileName);

            if (null != errorsDetails && errorsDetails.length > 0) {
                if (!errorsDetails[0].isEmpty()) {
                    errorsDetails[0] += "\n";
                }
                errorsDetails[0] += wrapToScriptName(delegable, error);
            }
            if (null != errors) {
                errors.add(error);
            }
            if (null != errorSources) {
                errorSources.add(delegable);
            }

            return false;
        }

        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(wrapToScriptName(delegable, Localization.getString("DialogEnhancement.cantGetInputStream")));
                return null;
            }
            Map<String, Integer> variablesMap = getVariableNamesFromDocxTemplate(inputStream);
            List<String> usedVariableList = delegable.getVariableNames(false);
            boolean ok = true;
            for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                String variable = entry.getKey();
                ListIterator<String> iterator = usedVariableList.listIterator();
                boolean exists = false;
                while (iterator.hasNext()) {
                    if (iterator.next().compareTo(variable) == 0) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    String error = Localization.getString("DialogEnhancement.noParameterForDocx", variable);
                    if (null != errorsDetails && errorsDetails.length > 0) {
                        if (!errorsDetails[0].isEmpty()) {
                            errorsDetails[0] += "\n";
                        }
                        errorsDetails[0] += wrapToScriptName(delegable, error);
                    }
                    if (null != errors) {
                        errors.add(error);
                    }
                    if (null != errorSources) {
                        errorSources.add(delegable);
                    }
                    ok = false;
                }
            }
            return ok;
        } catch (Throwable exception) {
            exception.printStackTrace();
            PluginLogger.logErrorWithoutDialog("Exception occured, see the stack trace!", exception);
            return null;
        }
    }

    private static String wrapToBotName(BotTask botTask, String message) {
        return message + " (" + Localization.getString("DialogEnhancement.botTask") + " \"" + botTask.getName() + "\")";
    }

    public static boolean updateBotTaskFromTemplate(BotTask botTask, String embeddedFileName) throws IOException, CoreException {
        boolean changed = false;
        if (embeddedFileName == null || embeddedFileName.isEmpty() || !EmbeddedFileUtils.isBotTaskFile(embeddedFileName)) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.badEmbeddedDocxFile"));
            return false;
        }
        IFile file = EmbeddedFileUtils.getProcessFile(botTask, EmbeddedFileUtils.getBotTaskFileName(embeddedFileName));
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetFile", EmbeddedFileUtils.getBotTaskFileName(embeddedFileName)));
            return false;
        }
        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetInputStream"));
                return false;
            }
            Map<String, Integer> variablesMap = DialogEnhancement.getVariableNamesFromDocxTemplate(inputStream);
            for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                if (ParamDefGroup.NAME_INPUT.equals(group.getName())) {
                    String inputFileParamName = DocxDialogEnhancementMode.getInputFileParamName();
                    List<ParamDef> params = group.getParameters();
                    ListIterator<ParamDef> paramsIterator = params.listIterator();
                    while (paramsIterator.hasNext()) {
                        ParamDef pd = paramsIterator.next();
                        String paramName = pd.getName();
                        if (paramName.equals(inputFileParamName)) {
                            if (pd.getFormatFilters().size() > 0
                                    && pd.getFormatFilters().get(0).compareTo(DocxDialogEnhancementMode.FILE_VARIABLE_FORMAT) == 0) {
                                continue;
                            }
                        }
                        if (!variablesMap.containsKey(paramName)) {
                            paramsIterator.remove();
                            changed = true;
                        }
                    }
                    for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                        ListIterator<ParamDef> paramsIter = params.listIterator();
                        boolean exists = false;
                        while (paramsIter.hasNext()) {
                            if (paramsIter.next().getName().compareTo(entry.getKey()) == 0) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            ParamDef pd = new ParamDef(entry.getKey(), entry.getKey());
                            List<String> formats = pd.getFormatFilters();
                            formats.add("java.lang.Object");
                            params.add(pd);
                            changed = true;
                        }
                    }
                    break;
                }
            }
        }

        return changed;
    }

    public static Boolean checkBotTaskParametersWithDocxTemplate(BotTask botTask, String embeddedDocxTemplateFileName, List<String> errors,
            String[] errorsDetails) {

        if (!EmbeddedFileUtils.isBotTaskFile(embeddedDocxTemplateFileName)) {
            return true;
        }

        IFile file = null;

        try {
            file = EmbeddedFileUtils.getProcessFile(botTask, EmbeddedFileUtils.getBotTaskFileName(embeddedDocxTemplateFileName));
        } catch (Throwable exception) {
            exception.printStackTrace();
        }

        if (null == file || !file.exists()) {
            String error = Localization.getString("DialogEnhancement.cantGetFile",
                    EmbeddedFileUtils.isBotTaskFile(embeddedDocxTemplateFileName) ? EmbeddedFileUtils.getBotTaskFileName(embeddedDocxTemplateFileName)
                            : embeddedDocxTemplateFileName);
            if (null != errorsDetails && errorsDetails.length > 0) {
                if (!errorsDetails[0].isEmpty()) {
                    errorsDetails[0] += "\n";
                }
                errorsDetails[0] += wrapToBotName(botTask, error);
            }
            if (null != errors) {
                errors.add(error);
            }
            return false;
        }

        boolean ok = true;
        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(wrapToBotName(botTask, Localization.getString("DialogEnhancement.cantGetInputStream")));
                return null;
            }
            Map<String, Integer> variablesMap = getVariableNamesFromDocxTemplate(inputStream);
            for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                String variable = entry.getKey();
                boolean finded = false;
                for (ParamDefGroup group : botTask.getParamDefConfig().getGroups()) {
                    if (ParamDefGroup.NAME_INPUT.equals(group.getName())) {
                        List<ParamDef> params = group.getParameters();
                        ListIterator<ParamDef> paramsIterator = params.listIterator();
                        while (paramsIterator.hasNext()) {
                            ParamDef pd = paramsIterator.next();
                            String paramName = pd.getName();
                            if (paramName.equals(variable)) {
                                finded = true;
                                break;
                            }
                        }
                    }
                }

                if (!finded) {
                    String error = Localization.getString("DialogEnhancement.noParameterForDocx", variable);
                    if (null != errorsDetails && errorsDetails.length > 0) {
                        if (!errorsDetails[0].isEmpty()) {
                            errorsDetails[0] += "\n";
                        }
                        errorsDetails[0] += wrapToBotName(botTask, error);
                    }
                    if (null != errors) {
                        errors.add(error);
                    }
                    ok = false;
                }
            }

            return ok;
        } catch (Throwable exception) {
            exception.printStackTrace();
            PluginLogger.logErrorWithoutDialog("Exception occured, see the stack trace!", exception);
            return null;
        }
    }

    public static Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream) {
        Map<String, Integer> variablesMap = new HashMap<String, Integer>();
        try (XWPFDocument document = new XWPFDocument(templateInputStream)) {
            for (XWPFHeader header : document.getHeaderList()) {
                DocxDialogEnhancementMode.getVariableNamesFromDocxBodyElements(header.getBodyElements(), variablesMap);
            }
            DocxDialogEnhancementMode.getVariableNamesFromDocxBodyElements(document.getBodyElements(), variablesMap);
            for (XWPFFooter footer : document.getFooterList()) {
                DocxDialogEnhancementMode.getVariableNamesFromDocxBodyElements(footer.getBodyElements(), variablesMap);
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return variablesMap;
    }

    private static boolean dialogEnhancementMode = true;
}
