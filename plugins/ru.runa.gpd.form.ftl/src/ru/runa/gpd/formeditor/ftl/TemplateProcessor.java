package ru.runa.gpd.formeditor.ftl;

import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateProcessor {
    private static final Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
    static {
        configuration.setObjectWrapper(new DefaultObjectWrapper());
        configuration.setLocalizedLookup(false);
        configuration.setStrictBeanModels(false);
        configuration.setTemplateExceptionHandler(new CustomTemplateExceptionHandler());
    }

    public static String process(String templateName, byte[] templateData, TemplateModel model) {
        return process(templateName, new String(templateData), model);
    }

    public static String process(String templateName, String templateData, TemplateModel model) {
        if (Strings.isNullOrEmpty(templateData)) {
            return templateData;
        }
        try {
            Template template = new Template("template", new StringReader(templateData), configuration);
            StringWriter out = new StringWriter();
            template.process(model, out);
            out.flush();
            return out.toString();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static class CustomTemplateExceptionHandler implements TemplateExceptionHandler {
        private static final Pattern PATTERN = Pattern.compile(".*\\$\\{(.*)\\}.*", Pattern.DOTALL | Pattern.MULTILINE);

        @Override
        public void handleTemplateException(TemplateException te, Environment env, Writer out) throws TemplateException {
            // in freemarker 2.3.21 it should be available as property
            Matcher matcher = PATTERN.matcher(te.getFTLInstructionStack());
            if (matcher.matches()) {
                String expression = matcher.group(1);
                try {
                    out.write("${" + expression + "}");
                } catch (IOException e) {
                }
            }
        }

    }

}
