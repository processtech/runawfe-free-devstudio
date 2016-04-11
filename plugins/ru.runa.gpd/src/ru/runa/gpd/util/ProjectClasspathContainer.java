package ru.runa.gpd.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class ProjectClasspathContainer implements IClasspathContainer {
    private static IClasspathEntry[] libraryEntries;
    private final IPath path;

    public ProjectClasspathContainer(IJavaProject javaProject, IPath path) {
        this.path = path;
    }

    @Override
    public IClasspathEntry[] getClasspathEntries() {
        if (libraryEntries == null) {
            libraryEntries = createJbpmLibraryEntries();
        }
        return libraryEntries;
    }

    @Override
    public String getDescription() {
        return "RunaWFE Library";
    }

    @Override
    public int getKind() {
        return IClasspathContainer.K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return path;
    }

    private IClasspathEntry[] createJbpmLibraryEntries() {
        List<IClasspathEntry> classPathEntries = new ArrayList<IClasspathEntry>();
        Enumeration<URL> enumUrls = Platform.getBundle("ru.runa.gpd").findEntries("/", "*.jar", true);
        while (enumUrls.hasMoreElements()) {
            URL bundleUrl = enumUrls.nextElement();
            try {
                URL fileUrl = FileLocator.toFileURL(bundleUrl);
                Path jarPath = new Path(fileUrl.getPath());
                classPathEntries.add(JavaCore.newLibraryEntry(jarPath, null, null));
            } catch (IOException e) {
                throw new RuntimeException("Error loading classpath library from: " + bundleUrl);
            }
        }
        return classPathEntries.toArray(new IClasspathEntry[classPathEntries.size()]);
    }
}
