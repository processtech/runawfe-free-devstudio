package ru.runa.gpd.extension.handler;

import java.util.List;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.model.Delegable;
import ru.runa.wfe.user.Executor;

public class AddReadProcessPermissionsProvider extends ParamBasedProvider {
    private static Config config = new Config();

    @Override
    protected ParamDefConfig getParamConfig(Delegable delegable) {
        return config;
    }

    public static class Config extends ParamDefConfig {
        public Config() {
            ParamDef p;
            ParamDefGroup inputGroup = new ParamDefGroup(ParamDefGroup.NAME_INPUT);
            p = new ParamDef("executors", Localization.getString("Param.Executors"));
            p.getFormatFilters().add(Executor.class.getName());
            p.getFormatFilters().add(List.class.getName());
            inputGroup.getParameters().add(p);
            getGroups().add(inputGroup);
        }
    }

}
