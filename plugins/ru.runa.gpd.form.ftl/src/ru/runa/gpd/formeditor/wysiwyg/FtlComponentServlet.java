package ru.runa.gpd.formeditor.wysiwyg;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.gpd.EditorsPlugin;
import ru.runa.gpd.formeditor.ftl.Component;
import ru.runa.gpd.formeditor.ftl.ComponentType;
import ru.runa.gpd.formeditor.ftl.ComponentTypeRegistry;
import ru.runa.gpd.formeditor.ftl.conv.DesignUtils;
import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.base.Strings;

public class FtlComponentServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            StringBuffer resultHtml = new StringBuffer();
            String command = request.getParameter("command");
            if (command == null) {
                resultHtml.append("invalid request ...");
            }
            String componentType = request.getParameter("type");
            int componentId = TypeConversionUtil.convertTo(int.class, request.getParameter("id"));
            if ("GetImage".equals(command)) {
                response.setContentType("image/png;");
                String parametersString = request.getParameter("parameters");
                String[] parameters;
                if (Strings.isNullOrEmpty(parametersString)) {
                    parameters = new String[0];
                } else {
                    parameters = parametersString.split("\\" + DesignUtils.PARAMETERS_DELIM);
                }
                ComponentType type = ComponentTypeRegistry.getNotNull(componentType);
                byte[] imageData = type.getImageProvider().getImage(type, parameters);
                response.getOutputStream().write(imageData);
                response.getOutputStream().flush();
                return;
            }
            response.setContentType("text/xml; charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            if ("ComponentSelected".equals(command)) {
                FormEditor.getCurrent().componentSelected(componentId);
            } else if ("ComponentDeselected".equals(command)) {
                FormEditor.getCurrent().componentDeselected();
            } else if ("OpenParametersDialog".equals(command)) {
                FormEditor.getCurrent().openParametersDialog(componentId);
            } else {
                EditorsPlugin.logInfo("Unknown cmd: " + command);
            }
            response.getOutputStream().write(resultHtml.toString().getBytes("UTF-8"));
            response.getOutputStream().flush();
        } catch (Throwable th) {
            EditorsPlugin.logError("-- JS command error", th);
            response.setStatus(500);
        }
    }
}
