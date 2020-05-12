package ru.runa.gpd.sync;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.settings.PrefConstants;
import ru.runa.gpd.ui.custom.Dialogs;

public class LdapServerExecutorImporter implements PrefConstants {
    private static final String OBJECT_CLASS_ATTR_NAME = "objectClass";
    private static final String OBJECT_CLASS_ATTR_USER_VALUE = "user";
    private static final String OBJECT_CLASS_ATTR_GROUP_VALUE = "group";
    private static final String SAM_ACCOUNT_NAME = "SamAccountName";
    private static LdapServerExecutorImporter instance = new LdapServerExecutorImporter();
    private Map<String, Boolean> data;

    protected LdapServerConnector getConnector() {
        return LdapServerConnector.getInstance();
    }

    public static LdapServerExecutorImporter getInstance() {
        return instance;
    }

    public Map<String, Boolean> getData() {
        if (data == null) {
            if (!getConnector().isConfigured()) {
                return null;
            }
            synchronize();
        }
        return data;
    }

    public final void synchronize() {
        Shell shell = Display.getCurrent() != null ? Display.getCurrent().getActiveShell() : null;
        final ProgressMonitorDialog monitorDialog = new ProgressMonitorDialog(shell);
        monitorDialog.setCancelable(true);
        final IRunnableWithProgress runnable = new IRunnableWithProgress() {
            @Override
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                try {
                    long start = System.currentTimeMillis();
                    monitor.beginTask(Localization.getString("task.SynchronizeData"), 120);
                    monitor.subTask(Localization.getString("task.Connect") + " " + getConnector().getName());
                    getConnector().connect();
                    monitor.worked(10);
                    monitor.subTask(Localization.getString("task.LoadData"));
                    data = null;
                    data = loadRemoteData(monitor);
                    monitor.subTask(Localization.getString("task.SaveData"));
                    monitor.done();
                    long end = System.currentTimeMillis();
                    PluginLogger.logInfo(getClass() + " sync took " + (end - start) + "millis");
                } catch (Exception e) {
                    PluginLogger.logErrorWithoutDialog("error.Synchronize", e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            monitorDialog.run(true, false, runnable);
        } catch (InvocationTargetException ex) {
            Dialogs.error(Localization.getString("error.Synchronize"), ex.getTargetException());
        } catch (InterruptedException consumed) {
        }
    }

    private Map<String, Boolean> loadRemoteData(IProgressMonitor monitor) throws Exception {
        Map<String, Boolean> data = new TreeMap<>();
        for (String userName : getGroupList()) {
            data.put(userName, true);
        }
        monitor.worked(50);
        for (String userName : getActorList()) {
            data.put(userName, false);
        }
        monitor.worked(50);
        return data;
    }

    private List<String> getActorList() throws NamingException {
        String[] ouNames = Activator.getPrefString(P_LDAP_CONNECTION_OU).split(";");
        List<String> actors = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_USER_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration<SearchResult> list = getConnector().getDirContext().search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = list.next();
                String name = getConnector().getDirContext().getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get().toString();
                actors.add(name);
            }
        }
        return actors;
    }

    private List<String> getGroupList() throws NamingException {
        String[] ouNames = Activator.getPrefString(P_LDAP_CONNECTION_OU).split(";");
        List<String> groups = new ArrayList<String>();
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS_ATTR_NAME, OBJECT_CLASS_ATTR_GROUP_VALUE);
        for (String ou : ouNames) {
            NamingEnumeration<SearchResult> list = getConnector().getDirContext().search(ou, attrs);
            while (list.hasMore()) {
                NameClassPair nc = list.next();
                String name = getConnector().getDirContext().getAttributes(nc.getName() + "," + ou).get(SAM_ACCOUNT_NAME).get().toString();
                groups.add(name);
            }
        }
        return groups;
    }
}
