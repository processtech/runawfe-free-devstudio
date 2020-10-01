package ru.runa.gpd.util.files;

import org.eclipse.core.resources.IFolder;

/**
 * @author Vitaly Alekseev
 *
 * @since Aug 1, 2019
 */
public interface FileImporter {
    
    IFolder importFile(FileImportInfo file) throws Exception;

}
