
var FTL_PLUGIN_NAME = 'FreemarkerTags' ;
var FTL_METHOD_CMD = 'FreemarkerMethod' ;
var VISUAL_ELEMENT = 'img' ;


var FreemarkerTags = new Object() ;

FreemarkerTags.OpenParametersDialog = function() {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FtlComponentServlet?command=OpenParametersDialog&id=" + FreemarkerTags.SelectedId.id, false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

var OpenParametersDialog = function(name) {	
	this.Name = name;
}

OpenParametersDialog.prototype.Execute = function() {	
	FreemarkerTags.OpenParametersDialog();
}

OpenParametersDialog.prototype.GetState = function() {
	FCK.Focus();
    return 0;
}

// Register the related command.
FCKCommands.RegisterCommand( FTL_METHOD_CMD, new OpenParametersDialog(FTL_METHOD_CMD)) ;

// Create the "FTLTag" toolbar button.
var methodItem = new FCKToolbarButton( FTL_METHOD_CMD, FCKLang.MethodTitle ) ;
methodItem.IconPath = FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'toolbar.gif' ;
FCKToolbarItems.RegisterItem( FTL_METHOD_CMD, methodItem ) ;


FreemarkerTags.SelectedId = {id : -1};

// Add method
FreemarkerTags.AddMethod = function( tagName, params ) {
	var oSpan = FCK.CreateElement( VISUAL_ELEMENT );
	this.SetupSpan( oSpan, tagName, params );
	oSpan.setAttribute("parameters", params);
}

FreemarkerTags.SetupSpan = function( span, tagName, tagParams ) {
	span.setAttribute("src", "/editor/FtlSupportServlet?command=GetImage&type=" + tagName + "&parameters=" + encodeURIComponent(tagParams));
	span.setAttribute("type", tagName);
	span.setAttribute("style", "margin: 3px; border: 2px solid black;")
	// To avoid it to be resized.
	span.onresizestart = function() {
		FCK.EditorWindow.event.returnValue = false ;
		return false ;
	}
}

// On Gecko we must do this trick so the user select all the SPAN when clicking on it.
FreemarkerTags._SetupClickListener = function() {
	FreemarkerTags._ClickListener = function( e ) {
		if (e.target.type) {
			FCKSelection.SelectNode( e.target ) ;
		}
	}
	FCK.EditorDocument.addEventListener( 'click', FreemarkerTags._ClickListener, true ) ;
}

FCK.ContextMenu.RegisterListener( { AddItems : function( menu, tag, tagName ) {
	if ( tagName && tagName.toLowerCase() == VISUAL_ELEMENT ) {
		menu.AddSeparator();
	}
	if (tag && tag.getAttribute("parameters") != null) {
		menu.AddItem( FTL_METHOD_CMD, FCKLang.MethodTitle ) ;
	}
}});

FreemarkerTags.ComponentSelected = function( componentId ) {
  FreemarkerTags.SelectedId.id = componentId;
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FtlComponentServlet?command=ComponentSelected&id=" + componentId, false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};

FreemarkerTags.ComponentDeselected = function() {
  var oXmlHttp = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
  oXmlHttp.open( "GET", "/editor/FtlComponentServlet?command=ComponentDeselected", false ) ;
  oXmlHttp.send( null ) ;
  if ( oXmlHttp.status == 200 || oXmlHttp.status == 304 ) {
    return oXmlHttp.responseText ;
  } else {
    return "Error." ;
  }
};



