package ru.runa.gpd.formeditor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.mortbay.http.handler.ResourceHandler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.WebApplicationContext;
import org.mortbay.util.InetAddrPort;
import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.formeditor.resources.Messages;
import ru.runa.gpd.formeditor.settings.PreferencePage;
import ru.runa.gpd.util.IOUtils;

public class WebServerUtils {
    private static Server server;
    private static int SERVER_PORT;
    private static String lastUsedEditor = null;

    public static File getEditorDirectory() {
    	File result = new File(new File(getStateLocation().toFile(), getEditorDirectoryName()), "editor");
        if (!result.exists()) {
            result.mkdirs();
        }
        return result;
    }

    public static String getEditorURL() {
        String url = "http://localhost:" + SERVER_PORT + "/editor.html";
        
        if (EditorsPlugin.DEBUG) {
            PluginLogger.logInfo("Editor url: " + url);
        }
        return url;
    }

    public static String getRegulationsViewerUrl() {
        return "http://localhost:" + SERVER_PORT + "/regulation.html";
    }

    public static File copyEditor(IProgressMonitor monitor, int allProgressCount) throws Exception {
        IPath location = getStateLocation();
        File editorFolder = new File(location.toFile(), getEditorDirectoryName());
        if (!getEditorDirectoryName().equals(lastUsedEditor)) {
            lastUsedEditor = getEditorDirectoryName();
            editorFolder.mkdir();
            copyFolderWithProgress(location, getEditorDirectoryName(), monitor, allProgressCount);
            if (isWebServerStarted()) {
                server.stop();
            }
        }
        return editorFolder;
    }

    public static void startWebServer(IProgressMonitor monitor, int allProgressCount) throws Exception {
        monitor.subTask(Messages.getString("editor.subtask.copy_resources"));
        File editorFolder = copyEditor(monitor, allProgressCount);
        monitor.subTask(Messages.getString("editor.subtask.start_server"));
        if (!isWebServerStarted()) {
            server = new Server();
            WebApplicationContext applicationContext = new WebApplicationContext(editorFolder.getAbsolutePath());
            applicationContext.setContextPath("/");
            applicationContext.addHandler(new ResourceHandler());
            server.addContext(applicationContext);
            try {
                SERVER_PORT = EditorsPlugin.getDefault().getPreferenceStore().getInt(PreferencePage.P_FORM_WEB_SERVER_PORT);
            } catch (Exception e) {
                PluginLogger.logErrorWithoutDialog("SERVER_PORT: " + e);
                SERVER_PORT = 48780;
            }
            server.addListener(new InetAddrPort(SERVER_PORT));
            server.start();
        }
        monitor.worked(1);
    }

    public static void stopWebServer() throws Exception {
        if (isWebServerStarted()) {
            server.stop();
        }
    }

    private static String getEditorDirectoryName() {
        return "CKeditor4";
    }

    private static IPath getStateLocation() {
        return EditorsPlugin.getDefault().getStateLocation();
    }

    private static boolean isWebServerStarted() {
        return server != null && server.isStarted();
    }

    private static void copyFolderWithProgress(IPath root, String path, IProgressMonitor monitor, int allProgressCount) throws IOException {
        int allFilesCount = countFiles(path);
        int filesForUnitWork = allFilesCount / allProgressCount;
        copyFolder(root, path, monitor, filesForUnitWork, 0);
    }

    private static int copyFolder(IPath root, String path, IProgressMonitor monitor, int filesForUnitWork, int currentUnitFilesCount)
            throws IOException {
        File folder = new File(root.toFile(), path);
        folder.mkdir();
        Enumeration<String> e = EditorsPlugin.getDefault().getBundle().getEntryPaths(path);
        int filesSize = currentUnitFilesCount;
        while (e != null && e.hasMoreElements()) {
            String child = e.nextElement();
            if (child.endsWith("/")) {
                filesSize = copyFolder(root, child, monitor, filesForUnitWork, filesSize);
            } else {
                InputStream in = EditorsPlugin.getDefault().getBundle().getEntry(child).openStream();
                File targetFile = new File(root.toFile(), child);
                OutputStream out = new FileOutputStream(targetFile);
                IOUtils.copyStream(in, out);
                filesSize++;
                if (filesSize == filesForUnitWork) {
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    filesSize = 0;
                }
            }
        }
        return filesSize;
    }

    private static int countFiles(String path) throws IOException {
        int result = 0;
        Enumeration<String> e = EditorsPlugin.getDefault().getBundle().getEntryPaths(path);
        while (e != null && e.hasMoreElements()) {
            String child = e.nextElement();
            if (child.endsWith("/")) {
                result += countFiles(child);
            } else {
                result++;
            }
        }
        return result;
    }

}
