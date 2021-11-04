package ru.runa.gpd.quick.tag;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.Serializable;
import java.util.List;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

@SuppressWarnings("unchecked")
public abstract class FreemarkerTagGpdWrap implements TemplateMethodModelEx, Serializable {
    private static final long serialVersionUID = 1L;
    protected User user;
    protected VariableProvider variableProvider;
    protected WebHelper webHelper;
    private List<TemplateModel> arguments;

    public void init(User user, WebHelper webHelper, VariableProvider variableProvider) {
        this.user = user;
        this.webHelper = webHelper;
        this.variableProvider = variableProvider;
    }

    public void initChained(FreemarkerTagGpdWrap parent) {
        init(parent.user, parent.webHelper, parent.variableProvider);
        arguments = parent.arguments;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final Object exec(List arguments) throws TemplateModelException {
        try {
            this.arguments = arguments;
            return executeTag();
        } catch (Throwable th) {
            LogFactory.getLog(getClass()).error(arguments.toString(), th);
            return "<div style=\"background-color: #ffb0b0; border: 1px solid red; padding: 3px;\">" + th.getMessage() + "</div>";
        }
    }

    protected abstract Object executeTag() throws Exception;

    protected <T> T getParameterAs(Class<T> clazz, int i) throws TemplateModelException {
        Object paramValue = null;
        if (i < arguments.size()) {
            paramValue = BeansWrapper.getDefaultInstance().unwrap(arguments.get(i));
        }
        return TypeConversionUtil.convertTo(clazz, paramValue);
    }

    protected <T> T getParameterVariableNotNull(Class<T> clazz, int i) throws TemplateModelException {
        String variableName = getParameterAs(String.class, i);
        return variableProvider.getValueNotNull(clazz, variableName);
    }

    protected <T> T getParameterVariable(Class<T> clazz, int i, T defaultValue) throws TemplateModelException {
        String variableName = getParameterAs(String.class, i);
        T value = variableProvider.getValue(clazz, variableName);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }
    
    public int getArgumentsSize() {
    	return arguments == null ? 0 : arguments.size();
    }

}
