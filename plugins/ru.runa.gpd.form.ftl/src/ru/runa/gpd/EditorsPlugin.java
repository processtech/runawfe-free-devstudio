package ru.runa.gpd;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ru.runa.gpd.formeditor.WebServerUtils;
import ru.runa.gpd.htmleditor.ColorProvider;
import ru.runa.gpd.htmleditor.HTMLPlugin;
import ru.runa.gpd.jseditor.launch.JavaScriptLaunchUtil;

/**
 * The main plugin class to be used in the desktop.
 */
public class EditorsPlugin extends AbstractUIPlugin {
    private static EditorsPlugin plugin;
    public static final boolean DEBUG = "true".equals(System.getProperty("ru.runa.gpd.form.ftl.debug"));
    // Color Provider
    private ColorProvider colorProvider;

    public EditorsPlugin() {
        plugin = this;
    }

    public ColorProvider getColorProvider() {
        return this.colorProvider;
    }

    public static void log(int severity, int code, String message, Throwable exception) {
        getDefault().getLog().log(new Status(severity, getDefault().getBundle().getSymbolicName(), code, message, exception));
    }

    public static void logInfo(String message) {
        log(IStatus.INFO, IStatus.OK, message, null);
    }

    public static void logError(String message, Throwable exception) {
        log(IStatus.ERROR, IStatus.OK, message, exception);
    }

    public String getLocalizedProperty(String key) {
        return Platform.getResourceString(getBundle(), "%" + key);
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        colorProvider = new ColorProvider(getPreferenceStore());
        WebServerUtils.copyEditor(null, 1);
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry registry) {
        super.initializeImageRegistry(registry);
        registry.put(HTMLPlugin.ICON_HTML, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/html.png")));
        registry.put(HTMLPlugin.ICON_XML, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/xml.png")));
        registry.put(HTMLPlugin.ICON_JSP, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jsp.png")));
        registry.put(HTMLPlugin.ICON_CSS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/css.png")));
        registry.put(HTMLPlugin.ICON_WEB, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/web.gif")));
        registry.put(HTMLPlugin.ICON_FILE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/file.gif")));
        registry.put(HTMLPlugin.ICON_TAG, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/tag.gif")));
        registry.put(HTMLPlugin.ICON_ATTR, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/attribute.gif")));
        registry.put(HTMLPlugin.ICON_VALUE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/value.gif")));
        registry.put(HTMLPlugin.ICON_FOLDER, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/folder.gif")));
        registry.put(HTMLPlugin.ICON_BUTTON, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/button.gif")));
        registry.put(HTMLPlugin.ICON_TEXT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/text.gif")));
        registry.put(HTMLPlugin.ICON_RADIO, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/radio.gif")));
        registry.put(HTMLPlugin.ICON_CHECK, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/checkbox.gif")));
        registry.put(HTMLPlugin.ICON_SELECT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/select.gif")));
        registry.put(HTMLPlugin.ICON_TEXTAREA, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/textarea.gif")));
        registry.put(HTMLPlugin.ICON_TABLE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/table.gif")));
        registry.put(HTMLPlugin.ICON_COLUMN, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/column.gif")));
        registry.put(HTMLPlugin.ICON_LABEL, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/label.gif")));
        registry.put(HTMLPlugin.ICON_PASS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/password.gif")));
        registry.put(HTMLPlugin.ICON_LIST, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/list.gif")));
        registry.put(HTMLPlugin.ICON_PANEL, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/panel.gif")));
        registry.put(HTMLPlugin.ICON_LINK, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/link.gif")));
        registry.put(HTMLPlugin.ICON_HIDDEN, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/hidden.gif")));
        registry.put(HTMLPlugin.ICON_OUTPUT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/output.gif")));
        registry.put(HTMLPlugin.ICON_CSS_RULE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/css_rule.gif")));
        registry.put(HTMLPlugin.ICON_CSS_PROP, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/css_prop.gif")));
        registry.put(HTMLPlugin.ICON_PROPERTY, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/properties.gif")));
        registry.put(HTMLPlugin.ICON_FORWARD, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/forward.gif")));
        registry.put(HTMLPlugin.ICON_BACKWARD, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/backward.gif")));
        registry.put(HTMLPlugin.ICON_REFRESH, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/refresh.gif")));
        registry.put(HTMLPlugin.ICON_RUN, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/run.gif")));
        registry.put(HTMLPlugin.ICON_BODY, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/body.gif")));
        registry.put(HTMLPlugin.ICON_FORM, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/form.gif")));
        registry.put(HTMLPlugin.ICON_TAG_HTML, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/html.gif")));
        registry.put(HTMLPlugin.ICON_IMAGE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/image.gif")));
        registry.put(HTMLPlugin.ICON_TITLE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/title.gif")));
        registry.put(HTMLPlugin.ICON_COMMENT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/comment.gif")));
        registry.put(HTMLPlugin.ICON_DOCTYPE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/doctype.gif")));
        registry.put(HTMLPlugin.ICON_ENTITY, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/entity.gif")));
        registry.put(HTMLPlugin.ICON_ATTLIST, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/attlist.gif")));
        registry.put(HTMLPlugin.ICON_ELEMENT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/element.gif")));
        registry.put(HTMLPlugin.ICON_NOTATE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/notation.gif")));
        registry.put(HTMLPlugin.ICON_FUNCTION, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/function.gif")));
        registry.put(HTMLPlugin.ICON_VARIABLE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/var.gif")));
        registry.put(HTMLPlugin.ICON_CLASS, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/class.gif")));
        registry.put(HTMLPlugin.ICON_TEMPLATE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/template.gif")));
        registry.put(HTMLPlugin.ICON_JAVASCRIPT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/javascript.gif")));
        registry.put(HTMLPlugin.ICON_XSD, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/xsd.gif")));
        registry.put(HTMLPlugin.ICON_DTD, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/dtd.gif")));
        registry.put(HTMLPlugin.ICON_PALETTE, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/palette.gif")));
        registry.put(HTMLPlugin.ICON_ERROR, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/error.gif")));
        registry.put(HTMLPlugin.ICON_JAR, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jar.gif")));
        registry.put(HTMLPlugin.ICON_JAR_EXT, ImageDescriptor.createFromURL(getBundle().getEntry("/icons/jar_ext.gif")));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        WebServerUtils.stopWebServer();
        JavaScriptLaunchUtil.removeLibraries();
        colorProvider.dispose();
        super.stop(context);
        plugin = null;
    }

    public static EditorsPlugin getDefault() {
        return plugin;
    }

}
