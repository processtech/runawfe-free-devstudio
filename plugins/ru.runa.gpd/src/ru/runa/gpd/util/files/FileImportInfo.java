package ru.runa.gpd.util.files;

import java.io.Closeable;
import java.io.InputStream;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 1, 2019
 */
public interface FileImportInfo extends Closeable {

    String getName();

    default String getPath() {
        return getName();
    }

    InputStream getInputStream();
}
