package ru.runa.gpd.aspects;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import ru.runa.gpd.lang.model.GraphElement;

public enum UserAction {

    //
    // Workbench Sessions (WS_*):
    //
    WS_Open,
    WS_Close,

    //
    // Main Menu (MM_*):
    //
    // File Menu
    //
    MM_NewProject,
    MM_NewFolder,
    MM_NewProcess,
    MM_NewBotStation,
    MM_NewBot,
    MM_NewBotTask,

    MM_Save,
    MM_SaveAll,

    MM_DeleteProject,
    MM_DeleteFolder,
    MM_DeleteProcess,
    MM_DeleteBotStation,
    MM_DeleteBot,
    MM_DeleteBotTask,
    MM_DataSource,

    MM_ShowRegulationsSequence,
    MM_ImportRegulations,

    MM_ImportProject,
    MM_ImportProcess,
    MM_ImportBotStation,
    MM_ImportBot,
    MM_ImportBotTask,

    MM_ExportProject,
    MM_ExportProcess,
    MM_ExportBotStation,
    MM_ExportBot,
    MM_ExportBotTask,

    MM_CheckUnlimitedTokens,

    MM_Exit,
    MM_About,
    CB_Close, // Workbench Window Close Button (CB_*)

    //
    // View Menu
    //
    MM_ShowGrid,
    MM_ApplayDefaultSizesToGraphElements,
    MM_ShowActions,
    MM_Antialiasing,
    MM_ResetEditorsLayout,

    //
    // Properties Menu
    //
    MM_Preferences,
    MM_CheckForUpdates,
    MM_InstallNewSoftware,

    //
    // Window Menu
    //
    MM_OpenErrorLog,
    MM_OpenSwtObjects,
    MM_OpenRegulationsNotes,
    MM_OpenProcessErrorsAndWarnings,
    MM_OpenDataSources,
    MM_OpenBots,
    MM_OpenExplorer,
    MM_OpenOutline,
    MM_OpenProperties,
    MM_OpenMiniatureView,
    MM_OpenFormComponents,

    //
    // [X] Views Close Buttons (CB_*):
    //
    CB_CloseErrorLog,
    CB_CloseSwtObjects,
    CB_CloseRegulationsNotes,
    CB_CloseProcessErrorsAndWarnings,
    CB_CloseDataSources,
    CB_CloseBots,
    CB_CloseExplorer,
    CB_CloseOutline,
    CB_CloseProperties,
    CB_CloseMiniatureView,
    CB_CloseFormComponents,

    //
    // Toolbar Buttons (TB_*):
    //
    TB_NewProject,
    TB_NewProcess,
    TB_NewBotStation,
    TB_NewBot,
    TB_NewBotTask,
    TB_SaveSelectedElement,
    TB_SaveAllElements,
    TB_DeleteSelectedElement,
    TB_Import,
    TB_Export,
    TB_ImportProcess,
    TB_ExportProcess,
    TB_ShowRegulationsSequence,
    TB_HideContextButtons,
    TB_ZoomOut,
    TB_ZoomIn,
    TB_Undo,
    TB_Redo,
    TB_Copy,
    TB_Paste,

    //
    // Context Menus (CM_*):
    //
    // Processes View
    //
    CM_NewProject,
    CM_NewFolder,
    CM_NewProcess,
    CM_ImportProcess,
    CM_RefreshProject,
    CM_RefreshFolder,
    CM_RefreshProcess,
    CM_DeleteProject,
    CM_DeleteFolder,
    CM_DeleteProcess,
    //
    // Process Specifics:
    //
    CM_OpenProcess,
    CM_Copy,
    CM_ExportProcess,
    CM_Rename,
    CM_FindReferences,
    CM_ShowSaveHistory,
    //
    // Bots View
    //
    CM_NewBotStation,
    CM_NewBot,
    CM_NewBotTask,
    CM_ImportBotStation,
    CM_ImportBot,
    CM_ImportBotTask,
    CM_ExportBotStation,
    CM_ExportBot,
    CM_ExportBotTask,
    CM_EditBotStation,
    CM_RenameBot,
    CM_RenameBotTask,
    CM_CopyBotTask,
    CM_RefreshBotStation,
    CM_RefreshBot,
    CM_RefreshBotTask,
    CM_DeleteBotStation,
    CM_DeleteBot,
    CM_DeleteBotTask,
    CM_FindBotReference,
    CM_FindBotTaskReference,

    DC_OpenBotTask, // Double Click

    //
    // Data Sources View
    //
    CM_NewDataSource,
    CM_ImportDataSource,
    CM_ExportDataSource,
    CM_EditDataSource,
    CM_CopyDataSource,
    CM_RefreshDataSource,
    CM_DeleteDataSource,

    //
    // Dialog Specifics - Dialog Bottons (DB_*):
    //
    OpenDialog,
    DB_Close, // Dialog button
    DB_Ok,
    DB_Cancel,

    //
    // Process Editor Tabs (PE_*):
    //
    PE_Diagram,
    PE_Swimlanes,
    PE_Variables,
    PE_UserTypes,
    PE_Xml,

    LC_SelectElement, // Left Click Button (LC_*)

    LC_ActivateProcessEditor,
    CB_CloseProcessEditor,

    Exception,

    //
    // Left Click (Mouse Button):
    //
    LC_Drug,
    LC_Drop,

    //
    // Editing Sessions (ES_*):
    //
    ES_Open,
    ES_Close,
    //
    // Graph Element Property Changes
    //
    GE_ChangeProperty,
    //
    // Graph Element(s) Selection Specifics
    //
    GE_Select,
    GE_AddToSelection,

    DoNothing;

    private static Map<String, Object> map = new LinkedHashMap<>(10);

    public String asString(GraphElement graphElement) {
        map.clear();
        map.put("element", graphElement.getClass().getSimpleName());
        map.put("id", graphElement.getId());
        return this + " " + map.toString();
    }

    public String asString(GraphElement graphElement, String id, String propertyName, Object oldValue, Object newValue) {
        map.clear();
        map.put("element", graphElement.getClass().getSimpleName());
        map.put("id", id);
        map.put("property", propertyName);
        map.put("oldValue", oldValue);
        map.put("newValue", newValue);
        return this + " " + map.toString();
    }

    public String asString(String something) {
        return this + " " + something;
    }

    public String asString(Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return this + " " + sw.toString();
    }

    public String asString() {
        return this.toString();
    }

}
