package ru.runa.gpd.lang.model;

import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.extension.HandlerArtifact;
import ru.runa.gpd.extension.HandlerRegistry;
import ru.runa.gpd.extension.VariableFormatRegistry;
import ru.runa.gpd.extension.handler.ParamDef;
import ru.runa.gpd.extension.handler.ParamDefConfig;
import ru.runa.gpd.extension.handler.ParamDefGroup;
import ru.runa.gpd.extension.handler.XmlBasedConstructorProvider;
import ru.runa.gpd.util.XmlUtil;

import com.google.common.collect.Lists;

public class BotTask implements Delegable, Comparable<BotTask> {
    private BotTaskType type = BotTaskType.SIMPLE;
    private String id;
    private String name;
    private String delegationClassName = "";
    private String delegationConfiguration = "";
    private ParamDefConfig paramDefConfig;
    private final List<String> filesToSave;

	public BotTask(String station, String bot, String name) {
		this.id = String.format("%s/%s/%s", station, bot, name);
		this.name = name;
        filesToSave = Lists.newArrayList();
    }

    public BotTaskType getType() {
        return type;
    }

    public void setType(BotTaskType type) {
        this.type = type;
    }

    public List<String> getFilesToSave() {
        return filesToSave;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDelegationType() {
        return HandlerArtifact.TASK_HANDLER;
    }

    @Override
    public String getDelegationClassName() {
        return delegationClassName;
    }

    @Override
    public void setDelegationClassName(String delegateClassName) {
        this.delegationClassName = delegateClassName;
    }

    @Override
    public String getDelegationConfiguration() {
        return delegationConfiguration;
    }

    @Override
    public void setDelegationConfiguration(String configuration) {
        delegationConfiguration = configuration;
    }

    public boolean isDelegationConfigurationInXml() {
        if (HandlerRegistry.getProvider(delegationClassName) instanceof XmlBasedConstructorProvider) {
            return true;
        }
        return XmlUtil.isXml(delegationConfiguration);
    }

    /**
     * param-based config
     * 
     * @return null for simple bot task type
     */
    public ParamDefConfig getParamDefConfig() {
        return paramDefConfig;
    }

    /**
     * param-based config
     */
    public void setParamDefConfig(ParamDefConfig paramDefConfig) {
        this.paramDefConfig = paramDefConfig;
    }

    @Override
    public List<String> getVariableNames(boolean includeSwimlanes, String... typeClassNameFilters) {
        List<String> result = Lists.newArrayList();
        if (paramDefConfig != null) {
            for (ParamDefGroup group : paramDefConfig.getGroups()) {
                for (ParamDef paramDef : group.getParameters()) {
                    boolean applicable = typeClassNameFilters == null || typeClassNameFilters.length == 0;
                    if (!applicable && paramDef.getFormatFilters().size() > 0) {
                        for (String typeClassNameFilter : typeClassNameFilters) {
                            if (VariableFormatRegistry.isAssignableFrom(typeClassNameFilter, paramDef.getFormatFilters().get(0))) {
                                applicable = true;
                                break;
                            }
                        }
                    }
                    if (applicable) {
                        result.add(paramDef.getName());
                    }
                }
            }
        }
        return result;
    }

    @Override
    public int compareTo(BotTask o) {
        if (id == null || o == null || o.id == null) {
            return -1;
        }
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object obj) {
        BotTask botTask = (BotTask) obj;
        return id.equals(botTask.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return Localization.getString("property.botTaskName") + ": " + name;
    }
}
