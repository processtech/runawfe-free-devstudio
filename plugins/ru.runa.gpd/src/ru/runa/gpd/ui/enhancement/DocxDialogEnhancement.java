package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import ru.runa.gpd.Activator;
import ru.runa.gpd.BotCache;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.editor.BotTaskEditor;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.lang.model.GraphElement;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.gpd.util.docx.DocxVariableParser;

public class DocxDialogEnhancement {

    private static Map<String, Integer> actorFieldsMap;
    private static Map<String, Integer> groupFieldsMap;
    static {
        actorFieldsMap = new HashMap<String, Integer>();
        groupFieldsMap = new HashMap<String, Integer>();

        Class<?> objectClass = ru.runa.wfe.user.Executor.class;
        Field[] fieldsList = null != objectClass ? objectClass.getDeclaredFields() : null;
        if (null != fieldsList) {
            for (Field field : fieldsList) {
                actorFieldsMap.put(field.getName(), 0);
                groupFieldsMap.put(field.getName(), 0);
            }
        }
        objectClass = ru.runa.wfe.user.Actor.class;
        fieldsList = null != objectClass ? objectClass.getDeclaredFields() : null;
        if (null != fieldsList) {
            for (Field field : fieldsList) {
                actorFieldsMap.put(field.getName(), 0);
            }
        }
        actorFieldsMap.put("firstName", 0);
        actorFieldsMap.put("middleName", 0);
        actorFieldsMap.put("lastName", 0);
        objectClass = ru.runa.wfe.user.Group.class;
        fieldsList = null != objectClass ? objectClass.getDeclaredFields() : null;
        if (null != fieldsList) {
            for (Field field : fieldsList) {
                groupFieldsMap.put(field.getName(), 0);
            }
        }
    }

    private static String wrapToScriptName(Delegable delegable, String message) {
        return message + " (" + Localization.getString("DialogEnhancement.scriptTask") + " \"" + delegable.toString() + "\")";
    }

    public static Boolean updateBotFromDocxTemplate(IResource exportResource, boolean saveBotTaskEditor) throws Exception {
        Boolean changedResult = false;
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
                if (null != botTaskFile) {
                    Boolean changed = DocxDialogEnhancement.updateBotTaskFromDocxTemplate(botTaskFile, saveBotTaskEditor);
                    if (null == changed) {
                        changedResult = null;
                    } else if (changed && null != changedResult) {
                        changedResult = true;
                    }
                }
            }
        }
        return changedResult;
    }

    public static Boolean updateBotTaskFromDocxTemplate(IResource exportResource, boolean saveBotTaskEditor) throws Exception {
        Boolean changedResult = false;
        IFile botTaskFile = (IFile) exportResource;
        BotTask botTask = null != botTaskFile ? BotCache.getBotTaskNotNull(botTaskFile) : null;
        if (null != botTask && 0 == botTask.getDelegationClassName().compareTo(DocxDialogEnhancementMode.DocxHandlerID)) {
            Object obj = DialogEnhancement.getConfigurationValue(botTask, DocxDialogEnhancementMode.InputPathId);
            String embeddedDocxTemplateFileName = null != obj && obj instanceof String ? (String) obj : "";
            if (!Strings.isNullOrEmpty(embeddedDocxTemplateFileName)) {

                Boolean changed = DocxDialogEnhancement.updateBotTaskFromTemplate(botTask, embeddedDocxTemplateFileName);
                if (null == changed) {
                    changedResult = null;
                } else if (changed) {
                    IEditorPart editorPart = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), botTaskFile,
                            BotTaskEditor.ID, true);
                    if (null != editorPart && editorPart instanceof BotTaskEditor) {
                        BotTaskEditor botTaskEditor = (BotTaskEditor) editorPart;
                        botTaskEditor.setDirty(true);
                        botTaskEditor.setTableInput(ParamDefGroup.NAME_INPUT);
                        if (saveBotTaskEditor) {
                            if (!botTaskEditor.verySave(false)) {
                                changedResult = null;
                            }
                        }
                        if (null != changedResult) {
                            changedResult = true;
                        }
                    }
                }
            }
        }
        return changedResult;
    }

    // use EmbeddedFileUtils.getProcessFileName(embeddedDocxTemplateFileName) before call that function !!!
    public static Boolean checkScriptTaskParametersWithDocxTemplate(Delegable delegable, String embeddedDocxTemplateFileName, List<String> errors,
            List<Delegable> errorSources, String[] errorsDetails) {
        if (Activator.getPrefBoolean(PrefConstants.P_DISABLE_DOCX_TEMPLATE_VALIDATION)) {
            return true;
        }
        ProcessDefinition processDefinition = delegable instanceof GraphElement ? ((GraphElement) delegable).getProcessDefinition() : null;
        IFile file = Strings.isNullOrEmpty(embeddedDocxTemplateFileName) || null == processDefinition ? null
                : EmbeddedFileUtils.getProcessFile(processDefinition, embeddedDocxTemplateFileName);
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
            Map<String, Integer> variablesMap = getVariableNamesFromDocxTemplate(inputStream, true);
            if (null == variablesMap) {
                PluginLogger.logInfo(wrapToScriptName(delegable, Localization.getString("DialogEnhancement.cantParseDocxTemplate")));
                return null;
            }

            List<String> usedVariableList = delegable.getVariableNames(false);
            Map<String, Integer> variablesToCheck = new TreeMap<String, Integer>();
            boolean ok = true;
            List<Variable> processVaribles = processDefinition.getVariables(true, true);

            for (Map.Entry<String, Integer> entry : variablesMap.entrySet()) {
                String variable = entry.getKey();
                Variable var = ru.runa.gpd.util.VariableUtils.getVariableByName(processDefinition, variable);
                if (null == var) {
                    var = ru.runa.gpd.util.VariableUtils.getVariableByScriptingName(processVaribles, variable);
                }
                if (null == var) {
                    int dotIndex = variable.indexOf(".");
                    boolean check = true;
                    if (dotIndex > 0) {
                        String baseVarName = variable.substring(0, dotIndex);
                        String attrName = variable.substring(dotIndex + 1, variable.length());
                        Variable baseVar = ru.runa.gpd.util.VariableUtils.getVariableByName(processDefinition, baseVarName);
                        if (null == baseVar) {
                            baseVar = ru.runa.gpd.util.VariableUtils.getVariableByScriptingName(processVaribles, baseVarName);
                        }
                        if (null != baseVar && attrName.length() > 0) {
                            // for Actor/Role/Group/Executor
                            Map<String, Integer> fieldsMap = (baseVar.getFormat().compareTo("ru.runa.wfe.var.format.ExecutorFormat") == 0
                                    || baseVar.getFormat().compareTo("ru.runa.wfe.var.format.ActorFormat") == 0) ? actorFieldsMap
                                            : (baseVar.getFormat().compareTo("ru.runa.wfe.var.format.GroupFormat") == 0 ? groupFieldsMap : null);

                            // for Lists
                            if (null == fieldsMap) {
                                if (baseVar.getFormat().startsWith("ru.runa.wfe.var.format.ListFormat")) {
                                    // String TypeName = ru.runa.gpd.util.VariableUtils.getListVariableComponentFormat(baseVar);
                                    // checkTypeAttributes(TypeName, attrName);
                                    check = false;
                                }

                            }

                            if (check) {
                                check = null == fieldsMap || !fieldsMap.containsKey(attrName);
                            }
                        }
                    }

                    if (check) {
                        variablesToCheck.put(variable, 1);
                    }
                } else {
                    String varName = var.getName();
                    int dotIndex = varName.indexOf(".");
                    if (dotIndex > 0) {
                        String baseVarName = varName.substring(0, dotIndex);
                        variablesToCheck.put(baseVarName, 1);
                    }
                }

            }

            for (Map.Entry<String, Integer> entry : variablesToCheck.entrySet()) {
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
            PluginLogger.logErrorWithoutDialog(exception.getMessage(), exception);
            return null;
        }
    }

    private static String wrapToBotName(BotTask botTask, String message) {
        return message + " (" + Localization.getString("DialogEnhancement.botTask") + " \"" + botTask.getName() + "\")";
    }

    public static Boolean updateBotTaskFromTemplate(BotTask botTask, String embeddedFileName) throws IOException, CoreException {
        boolean changed = false;
        if (embeddedFileName == null || embeddedFileName.isEmpty() || !EmbeddedFileUtils.isBotTaskFile(embeddedFileName)) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.badEmbeddedDocxFile"));
            return null;
        }
        IFile file = EmbeddedFileUtils.getProcessFile(botTask, EmbeddedFileUtils.getBotTaskFileName(embeddedFileName));
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetFile", EmbeddedFileUtils.getBotTaskFileName(embeddedFileName)));
            return null;
        }
        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetInputStream"));
                return null;
            }
            Map<String, Integer> variablesMap = DocxDialogEnhancement.getVariableNamesFromDocxTemplate(inputStream, false);
            if (null == variablesMap) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantParseDocxTemplate"));
                return null;
            }
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

        if (Activator.getPrefBoolean(PrefConstants.P_DISABLE_DOCX_TEMPLATE_VALIDATION)) {
            return true;
        }

        if (!EmbeddedFileUtils.isBotTaskFile(embeddedDocxTemplateFileName)) {
            return true;
        }

        IFile file = EmbeddedFileUtils.getProcessFile(botTask, EmbeddedFileUtils.getBotTaskFileName(embeddedDocxTemplateFileName));

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
            Map<String, Integer> variablesMap = getVariableNamesFromDocxTemplate(inputStream, false);
            if (null == variablesMap) {
                PluginLogger.logInfo(wrapToBotName(botTask, Localization.getString("DialogEnhancement.cantParseDocxTemplate")));
                return null;
            }
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
            PluginLogger.logErrorWithoutDialog("Exception occured, see the stack trace!", exception);
            return null;
        }
    }

    public static Map<String, Integer> getVariableNamesFromDocxTemplate(InputStream templateInputStream, boolean scriptParseMode) {
        return DocxVariableParser.getVariableNamesFromDocxTemplate(templateInputStream, scriptParseMode);
    }

}
