package ru.runa.gpd.extension.regulations.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import ru.runa.gpd.Activator;
import ru.runa.gpd.Localization;
import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.extension.regulations.RegulationsRegistry;
import ru.runa.gpd.extension.regulations.RegulationsUtil;
import ru.runa.gpd.lang.action.BaseModelDropDownActionDelegate;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.lang.model.ProcessRegulations;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

import com.google.common.base.Charsets;

public class CreateRegulationsAction extends BaseModelDropDownActionDelegate {
    
    public class UseTemplateAction extends Action {
        private final Path templatePath;
        private final Path cssPath;
        private final Bundle bundle;

        public UseTemplateAction(
                String name,
                Path templatePath,
                Path cssPath,
                Bundle bundle) {
            this.templatePath = templatePath;
            this.cssPath = cssPath;
            this.bundle = bundle;
            setText(name);
        }
        
        private String getContent(Bundle bundle, Path path) throws IOException {
            if (path == null) {
                return null;
            }
            URL url = FileLocator.find(bundle, path, Collections.emptyMap());
            URL fileUrl = FileLocator.toFileURL(url);
            InputStream input = fileUrl.openConnection().getInputStream();
            return IOUtils.readStream(input);
        }

        @Override
        public void run() {
            try {
                ProcessDefinition processDefinition = getActiveDesignerEditor().getDefinition();
            
                if (processDefinition.getDefaultProcessRegulations().equals(ProcessRegulations.DEFAULT)) {
                    RegulationsUtil.defaultRegulation(processDefinition);
                }
            
                boolean success = RegulationsUtil.validate(processDefinition); 
            
                if (success) {
                    String templateContent = getContent(this.bundle, this.templatePath);
                    RegulationsRegistry.setTemplate(templateContent);
                    String cssContent = getContent(this.bundle, this.cssPath);
                    RegulationsRegistry.setCssStyles(cssContent);
                    String html = RegulationsUtil.generate(processDefinition, templateContent);
                    IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.REGULATIONS_HTML_FILE_NAME);
                    IOUtils.createOrUpdateFile(file, new ByteArrayInputStream(html.getBytes(Charsets.UTF_8)));
                    IDE.openEditor(getWorkbenchPage(), file);
                }
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }
    
    private void createDefault(Menu menu) {
        UseTemplateAction action = null;
        ActionContributionItem item = null;
        
        action = new UseTemplateAction(
                Localization.getString("CreateRegulations.property.default"),
                new Path("template/regulations.ftl"),
                new Path("template/regulations.css"),
                Activator.getDefault().getBundle());
        
        item = new ActionContributionItem(action);
        item.fill(menu, -1);
    }

    @Override
    protected void fillMenu(Menu menu) {
        IExtension[] extensions = Platform.getExtensionRegistry().getExtensionPoint("ru.runa.gpd.regulations").getExtensions();
        IConfigurationElement[] configure = null;
        UseTemplateAction action = null;
        ActionContributionItem item = null;
        
        createDefault(menu);
        
        for (int i = 0; i < extensions.length; i++) {
            configure = extensions[i].getConfigurationElements();
            
            if (configure.length > 0) {
                action = new UseTemplateAction(
                        configure[i].getAttribute("name"),
                        new Path(configure[i].getAttribute("template")),
                        new Path(configure[i].getAttribute("css")),
                        Platform.getBundle(extensions[i].getNamespaceIdentifier()));
                
                item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
        }
    }
}