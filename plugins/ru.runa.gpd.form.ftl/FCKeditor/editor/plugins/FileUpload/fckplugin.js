
var PLUGIN_NAME = 'FileUpload' ;
var CMD = 'UploadFile' ;

// Register the related command.
// FCKPlugins.Items[VAR_TAG_PLUGIN_NAME].Path + 'fck_placeholder.html'
FCKCommands.RegisterCommand( CMD, new FCKDialogCommand( CMD, FCKLang.DlgTitle, FCKPlugins.Items[PLUGIN_NAME].Path + 'upload_dialog.html', 400, 200 ) ) ;

// Create the toolbar button.
var oVarTagItem = new FCKToolbarButton( CMD, FCKLang.BtnName ) ;
oVarTagItem.IconPath = FCKPlugins.Items[PLUGIN_NAME].Path + 'toolbar.gif' ;
FCKToolbarItems.RegisterItem( CMD, oVarTagItem ) ;


// The object used for all Placeholder operations.
var tagHandler = new Object() ;

// Add a new placeholder at the actual selection.
tagHandler.Add = function( name ) {
	FCK.InsertHtml("<input type='file' name='"+name+"'/>");	
}

// Open the Placeholder dialog on double click.
tagHandler.OnDoubleClick = function( span ) {
	if ( span.type == "file" ) {
		FCKCommands.GetCommand( CMD ).Execute() ;
	}
}

FCK.ContextMenu.RegisterListener( {
        AddItems : function( menu, tag, tagName ) {
                // under what circumstances do we display this option
                if ( tagName == 'INPUT' && tag.type == 'file' ) {
                        menu.AddSeparator() ;
                        menu.AddItem( CMD, FCKLang.DlgTitle ) ;
                }
        }}
);

tagHandler.Redraw = function() {
	//VarTags._SetupClickListener() ;
}

//FCK.Events.AttachEvent( 'OnAfterSetHTML', VarTags.Redraw ) ;

// The "Redraw" method must be called on startup.
tagHandler.Redraw() ;
