package ru.runa.gpd.extension.handler;

import ru.runa.gpd.lang.model.IDelegable;
import ru.runa.gpd.util.XmlUtil;

public class ConfigBasedProvider extends ParamBasedProvider {
    @Override
    protected ParamDefConfig getParamConfig(IDelegable iDelegable) {
        String xml = XmlUtil.getParamDefConfig(bundle, iDelegable.getDelegationClassName());
        return ParamDefConfig.parse(xml);
    }
}
