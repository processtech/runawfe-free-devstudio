package ru.runa.gpd.lang.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;

import ru.runa.gpd.PluginLogger;
import ru.runa.gpd.lang.par.ParContentProvider;
import ru.runa.gpd.util.IOUtils;

public class DeleteFormJSAction extends BaseModelActionDelegate {

    @Override
    public void run(IAction action) {
        try {
            IFile file = IOUtils.getAdjacentFile(getDefinitionFile(), ParContentProvider.FORM_JS_FILE_NAME);
            if (file.exists()) {
                file.delete(true, null);
            }
        } catch (Exception e) {
            PluginLogger.logError(e);
        }
    }
}
