
var VAR_TAG_PLUGIN_NAME = 'RunaVarTags' ;
var VAR_TAG_CMD = 'VarTag' ;
var VAR_TAG_VUALE = 'SPAN' ;
var VAR_TAG_ELEMENT = 'customtag' ;
var NAME_ATTR = 'var' ;
var TYPE_ATTR = 'delegation' ;

// Register the related command.
// FCKPlugins.Items[VAR_TAG_PLUGIN_NAME].Path + 'fck_placeholder.html'
FCKCommands.RegisterCommand( VAR_TAG_CMD, new FCKDialogCommand( VAR_TAG_CMD, FCKLang.VarTagDlgTitle, "/editor/RunaVarTags.java?method=GetVarTags", 400, 200 ) ) ;

// Create the "VarTag" toolbar button.
var oVarTagItem = new FCKToolbarButton( VAR_TAG_CMD, FCKLang.VarTagBtn ) ;
oVarTagItem.IconPath = FCKPlugins.Items[VAR_TAG_PLUGIN_NAME].Path + 'toolbar.gif' ;
FCKToolbarItems.RegisterItem( VAR_TAG_CMD, oVarTagItem ) ;


// The object used for all Placeholder operations.
var VarTags = new Object() ;

// Add a new placeholder at the actual selection.
VarTags.Add = function( name, type ) {
	var oSpan = FCK.CreateElement( VAR_TAG_VUALE ) ;
	this.SetupSpan( oSpan, name, type ) ;
}

VarTags.SetupSpan = function( span, name, type ) {
	if (this.IsTagHaveImage( type )) {
		span.style.backgroundImage = 'url("/editor/RunaVarTags.java?method=GetTagImage&type=' + type + '")' ;
	} else {
		span.style.backgroundColor = '#ffff00' ;
		span.style.color = '#000000' ;
		span.style.overflow = 'hidden';
		span.innerHTML = '[ ' + this.GetTagVisibleName( type ) + ' ]' ;
	}
	span.style.width = this.GetTagWidth( type ) ;
	span.style.height = this.GetTagHeight( type ) ;
	span.style.border = '1px solid green' ;

	if ( FCKBrowserInfo.IsGecko ) {
		span.style.position = 'relative' ;
		span.style.display = 'table-cell' ;
	} else {
		span.style.display = 'inline' ;
	}

	span.varTagName = name ;
	span.varTagType = type;
	span.contentEditable = false ;

	// To avoid it to be resized.
	span.onresizestart = function() {
		FCK.EditorWindow.event.returnValue = false ;
		return false ;
	}
}

// On Gecko we must do this trick so the user select all the SPAN when clicking on it.
VarTags._SetupClickListener = function() {
	VarTags._ClickListener = function( e ) {
		if ( e.target.varTagName ) {
			FCKSelection.SelectNode( e.target ) ;
		}
	}
	
	FCK.EditorDocument.addEventListener( 'click', VarTags._ClickListener, true ) ;
}

FCK.ContextMenu.RegisterListener( {
        AddItems : function( menu, tag, tagName ) {
                // under what circumstances do we display this option
                if ( tagName && tag && tagName == VAR_TAG_VUALE && tag.varTagName ) {
                        // when the option is displayed, show a separator  the command
                        menu.AddSeparator() ;
                        // the command needs the registered command name, the title for the context menu, and the icon path
                        menu.AddItem( VAR_TAG_CMD, FCKLang.VarTagDlgTitle ) ;
                }
        }}
);

VarTags.Redraw = function() {
	if ( FCK.EditMode != FCK_EDITMODE_WYSIWYG )
		return ;

	var aNodes = FCK.EditorDocument.getElementsByTagName(VAR_TAG_ELEMENT);
	var len = aNodes.length;
	for ( var i = 0 ; i < len ; i++ ) {
		var name = aNodes[0].getAttribute(NAME_ATTR);
		var type = aNodes[0].getAttribute(TYPE_ATTR);
		var oSpan = FCK.EditorDocument.createElement( VAR_TAG_VUALE ) ;
		VarTags.SetupSpan( oSpan, name, type ) ;
		aNodes[0].parentNode.insertBefore( oSpan, aNodes[0] ) ;
		aNodes[0].parentNode.removeChild( aNodes[0] ) ;
	}
}

VarTags.GetTagVisibleName = function( tagType ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=GetTagVisibleName&type=" + tagType, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "error" ;
	}
}

VarTags.GetTagWidth = function( tagType ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=GetVarTagWidth&type=" + tagType, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return 300 ;
	}
}

VarTags.GetTagHeight = function( tagType ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=GetVarTagHeight&type=" + tagType, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return 300 ;
	}
}

VarTags.IsTagHaveImage = function( tagType ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=IsTagHaveImage&type=" + tagType, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return (oXmlHttp.responseText == "true");
	} else {
		return false ;
	}
}

VarTags.GetTagImage = function( tagType ) {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=GetTagImage&type=" + tagType, false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "error" ;
	}
}

VarTags.IsAvailable = function() {
	var oXmlHttp = FCKTools.CreateXmlObject( 'XmlHttp' ) ;
	oXmlHttp.open( "GET", "/editor/RunaVarTags.java?method=IsAvailable", false ) ;
	oXmlHttp.send( null ) ;
	if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
		return oXmlHttp.responseText ;
	} else {
		return "false";
	}
}

if (VarTags.IsAvailable() != "true") {
	FCKCommands.GetCommand( VAR_TAG_CMD ).Execute = function() { return false; };
	FCKCommands.GetCommand( VAR_TAG_CMD ).GetState = function() { return FCK_TRISTATE_DISABLED; } ;
}


FCK.Events.AttachEvent( 'OnAfterSetHTML', VarTags.Redraw ) ;

// The "Redraw" method must be called on startup.
// VarTags.Redraw() ;

// We must process the SPAN tags to replace then with the real resulting value of the placeholder.
FCKXHtml.TagProcessors['span'] = function( node, htmlNode ) {
	if ( htmlNode.varTagName ) {
		node = FCKXHtml.XML.createElement( VAR_TAG_ELEMENT ) ;
		FCKXHtml._AppendAttribute( node, NAME_ATTR, htmlNode.varTagName ) ;
		FCKXHtml._AppendAttribute( node, TYPE_ATTR, htmlNode.varTagType ) ;
	} else {
		FCKXHtml._AppendChildNodes( node, htmlNode, false ) ;
	}
	return node;
}