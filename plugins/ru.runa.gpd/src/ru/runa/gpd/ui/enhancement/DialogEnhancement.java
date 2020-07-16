package ru.runa.gpd.ui.enhancement;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
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
                                    ProcessDefinitionValidator.logErrors(mainProcessDefinition, errors, errorSources);
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

    // use EmbeddedFileUtils.getProcessFileName(embeddedDocxTemplateFileName) before call that function !!!
    public static Boolean checkScriptTaskParametersWithDocxTemplate(Delegable delegable, String embeddedDocxTemplateFileName, List<String> errors,
            List<Delegable> errorSources, String[] errorsDetails) {
        ProcessDefinition processDefinition = delegable instanceof GraphElement ? ((GraphElement) delegable).getProcessDefinition() : null;

        IFile file = Strings.isNullOrEmpty(embeddedDocxTemplateFileName) || null == processDefinition ? null
                : EmbeddedFileUtils.getProcessFile(processDefinition, embeddedDocxTemplateFileName);
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetFile"));
            return null;
        }

        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(Localization.getString("DialogEnhancement.cantGetInputStream"));
                return null;
            }
            Map<String, Integer> variablesMap = DocxDialogEnhancementMode.getVariableNamesFromDocxTemplate(inputStream);
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
                    String error = wrapToScriptName(delegable, Localization.getString("DialogEnhancement.noParameterForDocx", variable));
                    if (null != errorsDetails && errorsDetails.length > 0) {
                        if (!errorsDetails[0].isEmpty()) {
                            errorsDetails[0] += "\n";
                        }
                        errorsDetails[0] += error;
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
        } catch (IOException | CoreException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String wrapToBotName(BotTask botTask, String message) {
        return message + " (" + Localization.getString("DialogEnhancement.botTask") + " \"" + botTask.getName() + "\")";
    }

    public static Boolean checkBotTaskParametersWithDocxTemplate(BotTask botTask, String embeddedDocxTemplateFileName, List<String> errors,
            String[] errorsDetails) {
        IFile file = EmbeddedFileUtils.getProcessFile(botTask, EmbeddedFileUtils.getBotTaskFileName(embeddedDocxTemplateFileName));
        if (null == file || !file.exists()) {
            PluginLogger.logInfo(wrapToBotName(botTask, Localization.getString("DialogEnhancement.cantGetFile")));
            return null;
        }

        boolean ok = true;
        try (InputStream inputStream = file.getContents()) {
            if (null == inputStream) {
                PluginLogger.logInfo(wrapToBotName(botTask, Localization.getString("DialogEnhancement.cantGetInputStream")));
                return null;
            }
            Map<String, Integer> variablesMap = DocxDialogEnhancementMode.getVariableNamesFromDocxTemplate(inputStream);
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
                    String error = wrapToBotName(botTask, Localization.getString("DialogEnhancement.noParameterForDocx", variable));
                    if (null != errorsDetails && errorsDetails.length > 0) {
                        if (!errorsDetails[0].isEmpty()) {
                            errorsDetails[0] += "\n";
                        }
                        errorsDetails[0] += error;
                    }
                    if (null != errors) {
                        errors.add(error);
                    }
                    ok = false;
                }
            }

            return ok;
        } catch (IOException | CoreException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean dialogEnhancementMode = true;
}
