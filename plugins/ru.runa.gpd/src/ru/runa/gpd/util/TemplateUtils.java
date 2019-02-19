package ru.runa.gpd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import ru.runa.wfe.InternalApplicationException;

public abstract class TemplateUtils {

    public static InputStream getFormTemplateAsStream() {
        return TemplateUtils.class.getResourceAsStream("/conf/form.template.js");
    }

    public static String getFormTemplateAsString() {
        try {
            return CharStreams.toString(new InputStreamReader(getFormTemplateAsStream(), Charsets.UTF_8));
        } catch (IOException e) {
            throw new InternalApplicationException(e);
        }
    }

}
