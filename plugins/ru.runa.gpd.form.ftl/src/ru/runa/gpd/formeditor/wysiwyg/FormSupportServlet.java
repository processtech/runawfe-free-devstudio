package ru.runa.gpd.formeditor.wysiwyg;

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ru.runa.gpd.EditorsPlugin;

public class FormSupportServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        try {
            StringBuffer resultHtml = new StringBuffer();
            String command = request.getParameter("command");
            if (command == null) {
                resultHtml.append("invalid request ...");
            }
            response.setContentType("text/xml; charset=UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            if ("GetVariableNames".equals(command)) {
                String filterClassName = Object.class.getName();
                if ("checkbox".equals(request.getParameter("elementType"))) {
                    filterClassName = Boolean.class.getName();
                }
                if ("file".equals(request.getParameter("elementType"))) {
                    filterClassName = "ru.runa.wfe.var.file.FileVariable";
                }
                List<String> variableNames = FormEditor.getCurrent().getVariableNames(filterClassName);
                Collections.sort(variableNames);
                for (String variableName : variableNames) {
                    if (resultHtml.length() > 0) {
                        resultHtml.append("|");
                    }
                    resultHtml.append(variableName);
                }
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
