package ru.runa.gpd.office;

import java.io.InputStream;
import org.eclipse.swt.widgets.Composite;
import ru.runa.gpd.lang.model.BotTask;
import ru.runa.gpd.lang.model.ProcessDefinition;
import ru.runa.gpd.ui.custom.ProcessFileComposite;
import ru.runa.gpd.ui.enhancement.DialogEnhancementMode;
import ru.runa.gpd.util.EmbeddedFileUtils;
import ru.runa.wfe.commons.ClassLoaderUtil;

public class TemplateFileComposite extends ProcessFileComposite {
    private final String fileExtension;

    @Deprecated
    public TemplateFileComposite(Composite parent, String fileName, String fileExtension, DialogEnhancementMode dialogEnhancementMode) {
        super(parent, EmbeddedFileUtils.getProcessFile(fileName), dialogEnhancementMode);
        this.fileExtension = fileExtension;
    }

    public TemplateFileComposite(Composite parent, BotTask botTask, String fileName, String fileExtension,
            DialogEnhancementMode dialogEnhancementMode) {
        super(parent, EmbeddedFileUtils.getProcessFile(botTask, fileName), dialogEnhancementMode);
        this.fileExtension = fileExtension;
    }

    public TemplateFileComposite(Composite parent, ProcessDefinition processDefenition, String fileName, String fileExtension,
            DialogEnhancementMode dialogEnhancementMode) {
        super(parent, EmbeddedFileUtils.getProcessFile(processDefenition, fileName), dialogEnhancementMode);
        this.fileExtension = fileExtension;
    }

    @Override
    protected boolean hasTemplate() {
        return true;
    }

    @Override
    protected InputStream getTemplateInputStream() {
        return ClassLoaderUtil.getAsStreamNotNull("/metadata/template." + getFileExtension(), getClass());
    }

    @Override
    protected String getFileExtension() {
        return fileExtension;
    }

    @Override
    protected boolean isDeleteFileOperationSupported() {
        return true;
    }

}
