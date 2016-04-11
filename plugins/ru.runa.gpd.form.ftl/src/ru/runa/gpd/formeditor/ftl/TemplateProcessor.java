package ru.runa.gpd.formeditor.ftl;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.gpd.EditorsPlugin;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

import freemarker.Mode;
import freemarker.core.Environment;
import freemarker.log.Logger;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateModel;

public class TemplateProcessor {
    static {
        Mode.setDesignerMode();
        try {
            if (!EditorsPlugin.DEBUG) {
                Logger.selectLoggerLibrary(Logger.LIBRARY_NONE);
            }
        } catch (ClassNotFoundException e) {
        }
    }

    public static String process(byte[] templateData, TemplateModel model) throws Exception {
        return process(new String(templateData, Charsets.UTF_8), model);
    }

    public static String process(String templateData, TemplateModel model) {
        if (Strings.isNullOrEmpty(templateData)) {
            return templateData;
        }
        try {
            Configuration configuration = new Configuration();
            configuration.setObjectWrapper(new DefaultObjectWrapper());
            configuration.setLocalizedLookup(false);
            configuration.setStrictBeanModels(false);
            configuration.setTemplateExceptionHandler(new CustomTemplateExceptionHandler());
            Template template = new Template("template", new StringReader(templateData), configuration, Charsets.UTF_8.name());
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
