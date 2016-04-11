package ru.runa.gpd.htmleditor;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;

import ru.runa.gpd.EditorsPlugin;

import com.google.common.collect.Maps;

/**
 * The main plugin class to be used in the desktop.
 * 
 * @author Naoki Takezoe
 */
public class HTMLPlugin {
    private static ResourceBundle resourceBundle;
    static {
        resourceBundle = ResourceBundle.getBundle("ru.runa.gpd.htmleditor.HTMLPluginResources");
    }

    public static final String ICON_HTML = "_icon_html";
    public static final String ICON_XML = "_icon_xml";
    public static final String ICON_JSP = "_icon_jsp";
    public static final String ICON_CSS = "_icon_css";
    public static final String ICON_WEB = "_icon_web";
    public static final String ICON_FILE = "_icon_file";
    public static final String ICON_TAG = "_icon_tag";
    public static final String ICON_ATTR = "_icon_attribute";
    public static final String ICON_VALUE = "_icon_value";
    public static final String ICON_FOLDER = "_icon_folder";
    public static final String ICON_BUTTON = "_icon_button";
    public static final String ICON_TEXT = "_icon_text";
    public static final String ICON_RADIO = "_icon_radio";
    public static final String ICON_CHECK = "_icon_check";
    public static final String ICON_SELECT = "_icon_select";
    public static final String ICON_TEXTAREA = "_icon_textarea";
    public static final String ICON_TABLE = "_icon_table";
    public static final String ICON_COLUMN = "_icon_column";
    public static final String ICON_LABEL = "_icon_label";
    public static final String ICON_PASS = "_icon_pass";
    public static final String ICON_LIST = "_icon_list";
    public static final String ICON_PANEL = "_icon_panel";
    public static final String ICON_LINK = "_icon_link";
    public static final String ICON_HIDDEN = "_icon_hidden";
    public static final String ICON_OUTPUT = "_icon_output";
    public static final String ICON_CSS_RULE = "_icon_css_rule";
    public static final String ICON_CSS_PROP = "_icon_css_prop";
    public static final String ICON_PROPERTY = "_icon_property";
    public static final String ICON_FORWARD = "_icon_forward";
    public static final String ICON_BACKWARD = "_icon_backword";
    public static final String ICON_REFRESH = "_icon_refresh";
    public static final String ICON_RUN = "_icon_run";
    public static final String ICON_TAG_HTML = "_icon_html";
    public static final String ICON_TITLE = "_icon_title";
    public static final String ICON_FORM = "_icon_form";
    public static final String ICON_IMAGE = "_icon_image";
    public static final String ICON_COMMENT = "_icon_comment";
    public static final String ICON_BODY = "_icon_body";
    public static final String ICON_DOCTYPE = "_icon_doctype";
    public static final String ICON_ELEMENT = "_icon_element";
    public static final String ICON_ATTLIST = "_icon_attlist";
    public static final String ICON_NOTATE = "_icon_notate";
    public static final String ICON_ENTITY = "_icon_entity";
    public static final String ICON_FUNCTION = "_icon_function";
    public static final String ICON_VARIABLE = "_icon_variable";
    public static final String ICON_CLASS = "_icon_class";
    public static final String ICON_TEMPLATE = "_icon_template";
    public static final String ICON_JAVASCRIPT = "_icon_javascript";
    public static final String ICON_XSD = "_icon_xsd";
    public static final String ICON_DTD = "_icon_dtd";
    public static final String ICON_PALETTE = "_icon_palette";
    public static final String ICON_ERROR = "_icon_error";
    public static final String ICON_JAR = "_icon_jar";
    public static final String ICON_JAR_EXT = "_icon_jar_ext";

    public static final String PREF_COLOR_TAG = "_pref_color_tag";
    public static final String PREF_COLOR_COMMENT = "_pref_color_comment";
    public static final String PREF_COLOR_STRING = "_pref_color_string";
    public static final String PREF_COLOR_DOCTYPE = "_pref_color_doctype";
    public static final String PREF_COLOR_SCRIPT = "_pref_color_scriptlet";
    public static final String PREF_COLOR_CSSPROP = "_pref_color_cssprop";
    public static final String PREF_COLOR_CSSCOMMENT = "_pref_color_csscomment";
    public static final String PREF_COLOR_CSSVALUE = "_pref_color_cssvalue";
    public static final String PREF_EDITOR_TYPE = "_pref_editor_type";
    public static final String PREF_DTD_URI = "_pref_dtd_uri";
    public static final String PREF_DTD_PATH = "_pref_dtd_path";
    public static final String PREF_DTD_CACHE = "_pref_dtd_cache";
    public static final String PREF_ASSIST_AUTO = "_pref_assist_auto";
    public static final String PREF_ASSIST_CHARS = "_pref_assist_chars";
    public static final String PREF_ASSIST_TIMES = "_pref_assist_times";
    public static final String PREF_ASSIST_CLOSE = "_pref_assist_close";
    public static final String PREF_PALETTE_ITEMS = "_pref_palette_items";
    public static final String PREF_USE_SOFTTAB = "_pref_use_softtab";
    public static final String PREF_SOFTTAB_WIDTH = "_pref_softtab_width";
    public static final String PREF_COLOR_BG = "AbstractTextEditor.Color.Background";
    public static final String PREF_COLOR_BG_DEF = "AbstractTextEditor.Color.Background.SystemDefault";
    public static final String PREF_COLOR_FG = "__pref_color_foreground";
    public static final String PREF_TLD_URI = "__pref_tld_uri";
    public static final String PREF_TLD_PATH = "__pref_tld_path";
    public static final String PREF_JSP_COMMENT = "__pref_jsp_comment";
    public static final String PREF_JSP_KEYWORD = "__pref_jsp_keyword";
    public static final String PREF_JSP_STRING = "__pref_jsp_string";
    public static final String PREF_PAIR_CHAR = "__pref_pair_character";
    public static final String PREF_COLOR_JSSTRING = "__pref_color_jsstring";
    public static final String PREF_COLOR_JSKEYWORD = "__pref_color_jskeyword";
    public static final String PREF_COLOR_JSCOMMENT = "__pref_color_jscomment";
    public static final String PREF_CUSTOM_ATTRS = "__pref_custom_attributes";
    public static final String PREF_CUSTOM_ELEMENTS = "__pref_custom_elements";
    public static final String PREF_TASK_TAGS = "__pref_task_tags";

    public static final String[] SUPPORTED_IMAGE_TYPES = { "gif", "png", "jpg", "jpeg", "bmp" };

    private static Map<String, String> innerDTD = Maps.newHashMap();
    static {
        innerDTD.put("http://java.sun.com/j2ee/dtds/web-app_2_2.dtd", "/metadata/metadata/DTD/web-app_2_2.dtd");
        innerDTD.put("http://java.sun.com/metadata/DTD/web-app_2_3.dtd", "/metadata/DTD/web-app_2_3.dtd");
        innerDTD.put("http://java.sun.com/j2ee/dtds/web-jsptaglibrary_1_1.dtd", "/metadata/DTD/web-jsptaglibrary_1_1.dtd");
        innerDTD.put("http://java.sun.com/metadata/DTD/web-jsptaglibrary_1_2.dtd", "/metadata/DTD/web-jsptaglibrary_1_2.dtd");
        innerDTD.put("XMLSchema.dtd", "/metadata/DTD/XMLSchema.dtd");
        innerDTD.put("datatypes.dtd", "/metadata/DTD/datatypes.dtd");

        innerDTD.put("http://java.sun.com/xml/ns/j2ee", "/metadata/metadata/XSD/web-app_2_4.xsd");
        innerDTD.put("j2ee_1_4.xsd", "/metadata/XSD/j2ee_1_4.xsd");
        innerDTD.put("j2ee_web_services_1_1.xsd", "/metadata/XSD/j2ee_web_services_1_1.xsd");
        innerDTD.put("j2ee_web_services_client_1_1.xsd", "/metadata/XSD/j2ee_web_services_client_1_1.xsd");
        innerDTD.put("jsp_2_0.xsd", "/metadata/XSD/jsp_2_0.xsd");
        innerDTD.put("jspxml.xsd", "/metadata/XSD/jspxml.xsd");
        innerDTD.put("web-app_2_4.xsd", "/metadata/XSD/web-app_2_4.xsd");
        innerDTD.put("web-jsptablibrary_2_0.xsd", "/metadata/XSD/web-jsptablibrary_2_0.xsd");
        innerDTD.put("xml.xsd", "/metadata/XSD/xml.xsd");
    }

    private static Map<String, String> innerTLD = Maps.newHashMap();
    static {
        innerTLD.put("http://java.sun.com/jstl/core_rt", "/metadata/metadata/TLD/c-1_0-rt.tld");
        innerTLD.put("http://java.sun.com/jstl/core", "/metadata/TLD/c-1_0.tld");
        innerTLD.put("http://java.sun.com/jsp/jstl/core", "/metadata/TLD/c.tld");
        innerTLD.put("http://java.sun.com/jstl/fmt_rt", "/metadata/TLD/fmt-1_0-rt.tld");
        innerTLD.put("http://java.sun.com/jstl/fmt", "/metadata/TLD/fmt-1_0.tld");
        innerTLD.put("http://java.sun.com/jsp/jstl/fmt", "/metadata/TLD/fmt.tld");
        // innerTLD.put("http://java.sun.com/jsp/jstl/functions","/metadata/TLD/fn.tld");
        innerTLD.put("http://java.sun.com/jstl/sql_rt", "/metadata/TLD/sql-1_0-rt.tld");
        innerTLD.put("http://java.sun.com/jstl/sql", "/metadata/TLD/sql-1_0.tld");
        innerTLD.put("http://java.sun.com/jsp/jstl/sql", "/metadata/TLD/sql.tld");
        innerTLD.put("http://java.sun.com/jstl/xml_rt", "/metadata/TLD/x-1_0-rt.tld");
        innerTLD.put("http://java.sun.com/jstl/xml", "/metadata/TLD/x-1_0.tld");
        innerTLD.put("http://java.sun.com/jsp/jstl/xml", "/metadata/TLD/x.tld");
    }

    public static Map<String, String> getInnerDTD() {
        return innerDTD;
    }

    public static Map<String, String> getInnerTLD() {
        return innerTLD;
    }

    public static Image getImage(String key) {
        return EditorsPlugin.getDefault().getImageRegistry().get(key);
    }
    
    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        try {
            return (resourceBundle != null) ? resourceBundle.getString(key) : key;
        } catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public static ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Generates a message from a template and parameters. Replace template
     * {0}{1}.. with parameters�B
     * 
     * @param message
     *            message
     * @param params
     *            parameterd
     * @return generated message
     */
    public static String createMessage(String message, String[] params) {
        for (int i = 0; i < params.length; i++) {
            message = message.replaceAll("\\{" + i + "\\}", params[i]);
        }
        return message;
    }

    /**
     * Logging debug information.
     * 
     * @param message
     *            message
     */
    public static void logDebug(String message) {
        ILog log = EditorsPlugin.getDefault().getLog();
        IStatus status = new Status(IStatus.INFO, "EditorsPlugin", 0, message, null);
        log.log(status);
    }

    /**
     * Logging error information.
     * 
     * @param message
     *            message
     */
    public static void logError(String message) {
        ILog log = EditorsPlugin.getDefault().getLog();
        IStatus status = new Status(IStatus.ERROR, "EditorsPlugin", 0, message, null);
        log.log(status);
    }

    /**
     * Logging exception information.
     * 
     * @param ex
     *            exception
     */
    public static void logException(Throwable ex) {
        ILog log = EditorsPlugin.getDefault().getLog();
        IStatus status = null;
        if (ex instanceof CoreException) {
            status = ((CoreException) ex).getStatus();
        } else {
            status = new Status(IStatus.ERROR, "EditorsPlugin", 0, ex.toString(), ex);
        }
        log.log(status);
    }

}
