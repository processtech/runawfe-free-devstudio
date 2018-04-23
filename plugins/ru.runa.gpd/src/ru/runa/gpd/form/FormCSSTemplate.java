package ru.runa.gpd.form;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class FormCSSTemplate {
    private final String name;
    private final String content;
    
    public FormCSSTemplate(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }
    
    public String getContent() {
        return content;
    }

    public InputStream getContentAsStream() {
        return new ByteArrayInputStream(content.getBytes());
    }
}
