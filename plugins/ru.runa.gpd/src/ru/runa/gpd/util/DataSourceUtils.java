package ru.runa.gpd.util;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.DataSourceType;

public class DataSourceUtils {

    public static IProject getDataSourcesProject() {
        return ResourcesPlugin.getWorkspace().getRoot().getProject("DataSources");
    }

    public static List<IFile> getAllDataSources() {
        List<IFile> fileList = new ArrayList<>();
        IProject dsProject = getDataSourcesProject();
        if (dsProject.exists()) {
            try {
                for (IResource resource : dsProject.members()) {
                    if (resource instanceof IFile && ((IFile) resource).getName().endsWith(DataSourceStuff.DATA_SOURCE_FILE_SUFFIX)) {
                        fileList.add((IFile) resource);
                    }
                }
            } catch (CoreException e) {
                throw new InternalApplicationException(e);
            }
        }
        return fileList;
    }

    public static List<IFile> getDataSourcesByType(DataSourceType... types) {
        List<IFile> fileList = new ArrayList<>();
        List<DataSourceType> typeList = Arrays.asList(types);
        for (IFile dsFile : getAllDataSources()) {
            try (InputStream is = dsFile.getContents()) {
                if (typeList.contains(DataSourceType.valueOf(XmlUtil.parseWithoutValidation(is).getRootElement().attribute("type").getValue()))) {
                    fileList.add(dsFile);
                }
            } catch (IOException | CoreException e) {
                Throwables.propagate(e);
            }
        }
        return fileList;
    }

    private DataSourceUtils() {
        // All-static class
    }

}
