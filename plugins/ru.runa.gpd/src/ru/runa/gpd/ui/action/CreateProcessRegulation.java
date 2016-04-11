package ru.runa.gpd.ui.action;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.action.BaseModelActionDelegate;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.EndTokenState;
import ru.runa.gpd.lang.model.Node;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.StartState;
import ru.runa.gpd.lang.model.TaskState;
import ru.runa.gpd.lang.model.Timer;
import ru.runa.gpd.lang.model.Variable;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;
import ru.runa.gpd.util.TextEditorInput;

import com.google.common.base.Charsets;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

public class CreateProcessRegulation extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {

        try {
            ProcessDefinition proccDefinition = getActiveDesignerEditor().getDefinition();
            String html = generateRegulation(proccDefinition);

            TextEditorInput input = new TextEditorInput(proccDefinition.getName() + ".rgl", html);
            IDE.openEditor(getWorkbenchPage(), input, "ru.runa.gpd.wysiwyg.RegulationHTMLEditor");
        } catch (Exception e) {
            PluginLogger.logError(e);
        }

    }

    private String generateRegulation(ProcessDefinition definition) throws Exception {

        Configuration config = new Configuration();

        config.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        config.setDefaultEncoding(Charsets.UTF_8.name());
        config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        // TODO need localization
        Path path = new Path("template/regulation.ftl");

        Bundle bundl = Activator.getDefault().getBundle();
        URL url = FileLocator.find(bundl, path, Collections.EMPTY_MAP);
        URL fileUrl = FileLocator.toFileURL(url);
        InputStream input = fileUrl.openConnection().getInputStream();
        Template template = new Template("regulation", new StringReader(IOUtils.readStream(input)), config);

        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("proc", definition);

        IFile htmlDefinition = IOUtils.getAdjacentFile(getActiveDesignerEditor().getDefinitionFile(), ParContentProvider.PROCESS_DEFINITION_DESCRIPTION_FILE_NAME);
        if (htmlDefinition.exists()) {
            map.put("brief", IOUtils.readStream(htmlDefinition.getContents()));
        }

        // freeMarker can't work with params like StartState.class directly
        HashMap<String, Object> model = new HashMap<String, Object>();
        map.put("model", model);
        model.put("start", StartState.class);
        model.put("task", TaskState.class);
        model.put("node", Node.class);
        model.put("end", EndState.class);
        model.put("variable", Variable.class);
        model.put("timer", Timer.class);
        model.put("endToken", EndTokenState.class);

        Writer writer = new StringWriter();
        template.process(map, writer);
        return writer.toString();

    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        if (getSelection() != null && getSelection().getClass().equals(ProcessDefinition.class)) {
            action.setEnabled(!getActiveDesignerEditor().getDefinition().isInvalid());
        } else {
            action.setEnabled(false);
        }
    }

}
