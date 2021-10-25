package ru.runa.gpd.quick.tag;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

public class FormHashModelGpdWrap extends SimpleHash {

    private static final long serialVersionUID = 1L;

    private final User user;
    private final VariableProvider variableProvider;
    private final WebHelper webHelper;

    public FormHashModelGpdWrap(User user, VariableProvider variableProvider, WebHelper webHelper) {
    	super(ObjectWrapper.BEANS_WRAPPER);
        this.user = user;
        this.variableProvider = variableProvider;
        this.webHelper = webHelper;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        try {
            FreemarkerConfigurationGpdWrap configuration = FreemarkerConfigurationGpdWrap.getInstance();
            FreemarkerTagGpdWrap tag = configuration.getTag(key);
            if (tag != null) {
                tag.init(user, webHelper, variableProvider);
                return tag;
            }
        } catch (Exception e) {
            throw new TemplateModelException(e);
        }
        Object variableValue = variableProvider.getValue(key);
        if (variableValue != null) {
            return wrap(variableValue);
        }
        return null;
    }
}
