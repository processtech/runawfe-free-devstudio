package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ide.IDE;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.form.FormCSSTemplate;
import ru.runa.gpd.form.FormCSSTemplateRegistry;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class CreateTemplatedFormCSSAction extends BaseModelDropDownActionDelegate {

    @Override
    protected void fillMenu(Menu menu) {
        for (FormCSSTemplate template : FormCSSTemplateRegistry.getTemplates()) {
            UseTemplateAction action = new UseTemplateAction(template.getName());
            ActionContributionItem item = new ActionContributionItem(action);
            item.fill(menu, -1);
        }
    }

    public class UseTemplateAction extends Action {
        private final String name;

        public UseTemplateAction(String name) {
            this.name = name;
            setText(name);
        }

        @Override
        public void run() {
            try {
                IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.FORM_CSS_FILE_NAME);
                FormCSSTemplate template = FormCSSTemplateRegistry.getTemplateNotNull(name);
                IOUtils.createOrUpdateFile(file, template.getContentAsStream());
                IDE.openEditor(getWorkbenchPage(), file, true);
            } catch (Exception e) {
                PluginLogger.logError(e);
            }
        }
    }

}
