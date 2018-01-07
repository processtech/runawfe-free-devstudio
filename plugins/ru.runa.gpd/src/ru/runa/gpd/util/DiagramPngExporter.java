package ru.runa.gpd.util;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.editor.gef.GEFImageHelper;
import ru.runa.gpd.lang.model.ProcessDefinition;

public class DiagramPngExporter {

    public static void go(ProcessEditorBase editor, String filePath) throws Exception {
        ProcessDefinition pd = editor.getDefinition();
        boolean dirty = pd.isDirty();
        GEFImageHelper.save(editor.getGraphicalViewer(), pd, filePath);
        pd.setDirty(dirty);
    }
    
}
