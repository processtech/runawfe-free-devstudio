package ru.runa.gpd.util.docx;

import com.google.common.base.Throwables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.InternalApplicationException;

public class DocxConfig {
    private static final Log log = LogFactory.getLog(DocxConfig.class);

    private boolean strictMode;

    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public void reportProblem(Exception e) {
        if (strictMode) {
            Throwables.propagate(e);
        }
        log.warn("", e);
    }

    public void reportProblem(String message) {
        if (strictMode) {
            throw new InternalApplicationException(message);
        }
        log.warn(message);
    }

    public void warn(String message) {
        log.warn(message);
    }
}
