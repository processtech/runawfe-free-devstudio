package ru.runa.gpd.util.files;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 23, 2019
 */
public class ProcessDefinitionImportInfo implements FileImportInfo {
    private final String name;
    private final String path;
    private final InputStream inputStream;

    public ProcessDefinitionImportInfo(String name, String path, InputStream inputStream) {
        this.name = name;
        this.path = path;
        this.inputStream = inputStream;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPath() {
        if (Strings.isNullOrEmpty(path)) {
            return name;
        }
        return path + File.separator + name;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }
}
