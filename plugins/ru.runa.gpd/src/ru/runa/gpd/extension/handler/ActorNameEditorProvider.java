package ru.runa.gpd.extension.handler;

import java.util.List;
import java.util.Map;

import ru.runa.gpd.Localization;
import ru.runa.gpd.lang.ValidationError;
import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

public class ActorNameEditorProvider extends ParamBasedProvider {
    private static final String ACTOR_CODE = "actorCode";
    private static final String ACTOR_LOGIN = "actorLogin";
    private static Config config = new Config();

    @Override
    protected ParamDefConfig getParamConfig(IDelegable iDelegable) {
        return config;
    }

    public static class Config extends ParamDefConfig {
        public Config() {
            ParamDef p;
            ParamDefGroup inputGroup = new ParamDefGroup(ParamDefGroup.NAME_INPUT);
            p = new ParamDef(ACTOR_CODE, Localization.getString("ActorNameEditorProvider.param.actorCode"));
            p.setOptional(true);
            p.getFormatFilters().add(String.class.getName());
            p.getFormatFilters().add(Long.class.getName());
            p.getFormatFilters().add(Executor.class.getName());
            p.getFormatFilters().add(Actor.class.getName());
            inputGroup.getParameters().add(p);
            p = new ParamDef(ACTOR_LOGIN, Localization.getString("ActorNameEditorProvider.param.actorLogin"));
            p.setOptional(true);
            p.getFormatFilters().add(String.class.getName());
            inputGroup.getParameters().add(p);
            p = new ParamDef("format", Localization.getString("ActorNameEditorProvider.param.format"));
            p.setUseVariable(false);
            p.setComboItems(new String[] { "full name", "name", "email", "code", "description" });
            inputGroup.getParameters().add(p);
            ParamDefGroup outputGroup = new ParamDefGroup(ParamDefGroup.NAME_OUTPUT);
            p = new ParamDef("result", Localization.getString("ActorNameEditorProvider.param.result"));
            p.getFormatFilters().add(String.class.getName());
            outputGroup.getParameters().add(p);
            getGroups().add(inputGroup);
            getGroups().add(outputGroup);
        }

        @Override
        public boolean validate(IDelegable iDelegable, List<ValidationError> errors) {
            super.validate(iDelegable, errors);
            Map<String, String> props = parseConfiguration(iDelegable.getDelegationConfiguration());
            return isValid(props.get(ACTOR_CODE)) || isValid(props.get(ACTOR_LOGIN));
        }
    }
}
