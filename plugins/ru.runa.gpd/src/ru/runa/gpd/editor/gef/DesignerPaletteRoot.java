package ru.runa.gpd.editor.gef;

import java.util.List;

import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.CreationToolEntry;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;

import ru.runa.gpd.editor.ProcessEditorBase;
import ru.runa.gpd.lang.Language;
import ru.runa.gpd.lang.NodeRegistry;
import ru.runa.gpd.lang.NodeTypeDefinition;
import ru.runa.gpd.lang.model.EndState;
import ru.runa.gpd.lang.model.SubprocessDefinition;
import ru.runa.gpd.lang.model.jpdl.ActionImpl;

public class DesignerPaletteRoot extends PaletteRoot {
    private static final String ACTION_IMPL_ENTRY_ID = "ActionImpl";
    private static final String END_STATE_ENTRY_ID = "EndState";
    private final ProcessEditorBase editor;

    public DesignerPaletteRoot(ProcessEditorBase editor) {
        this.editor = editor;
        addControls();
    }

    private void addControls() {
        PaletteGroup controls = new PaletteGroup("jpdl");
        controls.setId("jpdl");
        int idCounter = 1;
        for (NodeTypeDefinition type : NodeRegistry.getDefinitions()) {
            CreationFactory factory = new GEFElementCreationFactory(type, editor.getDefinition());
            if (type.getJpdlElementName() == null) {
                continue;
            }
            PaletteEntry entry = null;
            if (NodeTypeDefinition.TYPE_NODE.equals(type.getType())) {
                entry = new CreationToolEntry(type.getLabel(), null, factory, type.getImageDescriptor(Language.JPDL.getNotation()), null);
            }
            if (NodeTypeDefinition.TYPE_CONNECTION.equals(type.getType())) {
                entry = new ConnectionCreationToolEntry(type.getLabel(), null, factory, type.getImageDescriptor(Language.JPDL.getNotation()), null);
            }
            if (entry != null) {
                entry.setId(String.valueOf(idCounter++));
                if (type.getModelClass() == ActionImpl.class) {
                    entry.setId(ACTION_IMPL_ENTRY_ID);
                }
                if (type.getModelClass() == EndState.class) {
                    entry.setId(END_STATE_ENTRY_ID);
                }
                controls.add(entry);
            }
        }
        add(createDefaultControls());
        add(controls);
    }

    @SuppressWarnings("unchecked")
    public void refreshElementsVisibility() {
        for (PaletteGroup category : (List<PaletteGroup>) getChildren()) {
            for (PaletteEntry entry : (List<PaletteEntry>) category.getChildren()) {
                if (ACTION_IMPL_ENTRY_ID.equals(entry.getId())) {
                    entry.setVisible(editor.getDefinition().isShowActions());
                }
                if (END_STATE_ENTRY_ID.equals(entry.getId())) {
                    entry.setVisible(!(editor.getDefinition() instanceof SubprocessDefinition));
                }
            }
        }
    }

    private PaletteGroup createDefaultControls() {
        PaletteGroup controls = new PaletteGroup("Default Tools");
        controls.setId("DefaultTools");
        addSelectionTool(controls);
        return controls;
    }

    private void addSelectionTool(PaletteGroup controls) {
        ToolEntry tool = new SelectionToolEntry();
        tool.setId("Selection");
        controls.add(tool);
        setDefaultEntry(tool);
    }
}
