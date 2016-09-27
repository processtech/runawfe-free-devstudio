package ru.runa.gpd.extension.handler;

import ru.runa.gpd.lang.model.Delegable;
import ru.runa.gpd.util.XmlUtil;

public class ConfigBasedProvider extends ParamBasedProvider {
    @Override
    protected ParamDefConfig getParamConfig(Delegable delegable) {
        String xml = XmlUtil.getParamDefConfig(bundle, delegable.getDelegationClassName());
        return ParamDefConfig.parse(xml);
    }
}
