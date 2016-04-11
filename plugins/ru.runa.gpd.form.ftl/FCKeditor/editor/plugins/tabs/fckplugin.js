var FTL_PLUGIN_NAME = 'tabs' ;
var FTL_METHOD_INSERT_CMD = 'InsertTab' ;
var FTL_METHOD_DELETE_CMD = 'DeleteTab' ;

var folderId = -1;
var tabTemplate = "<li><a href='#{href}'><div>#{label}</div></a></li>";

function addTab(tabFolderId, initialize) {
	var tabId = (new Date()).getTime();
	var label = FCKLang.NewTabTitle;
	var id = "tabs-" + tabId;
	var li = $(tabTemplate.replace( /#\{label\}/g, label ));
	li.find('a').attr('href', "#" + id);
	li.attr('id', tabId);
	li.attr('aria-controls', id);
	folderId = tabFolderId;
	var tabFolder = $(FCK.EditorDocument).find('#' + tabFolderId);	
	tabFolder.find('ul').append(li);
	tabFolder.append('<div id="' + id + '"><p>' + FCKLang.NewTabContent + '</p></div>');	
	$(li).on('focus', function() {
		$(this).find("a").click();	
	});	
		
	if (initialize) {
		tabFolder.tabs({
			create:function(event,ui){ 
				$(FCK.EditorDocument).find("#"+tabFolderId).click(function() {
		    		folderId = $(this).closest('.tabs').attr('id');
		    	});
			}
		});	
	} else {
		tabFolder.tabs('refresh');	
		tabFolder.find('div[class^="tabs-"]').addClass('ui-tabs-panel ui-widget-content ui-corner-bottom');	
	}
}

function afterSetHTML( editorInstance )
{
	$(editorInstance.EditorDocument).find('.tabs').tabs();
	$(editorInstance.EditorDocument).find('.tabs').each(function() {
		$(this).click(function() {
    		folderId = $(this).closest('.tabs').attr('id');
    	});
	});
	$(editorInstance.EditorDocument).find('div[class^="tabs-"]').addClass('ui-tabs-panel ui-widget-content ui-corner-bottom');
	$(editorInstance.EditorDocument).find('li a[href^=#tabs]').each(function() {
		$(this).parent().focus(function() {
			$(this).find('a').click();
		});
	});
}

var FCKInsertTabCommand = function()
{	
}

FCKInsertTabCommand.prototype.Execute = function()
{	
	var range = "";
	if(FCKSelection.GetSelectedElement()) {
		range = $(FCKSelection.GetSelectedElement());
	} else if(FCKSelection.GetParentElement()) {
		range = $(FCKSelection.GetParentElement());
	}	
	if (range == "" || range.closest('.tabs').length == 0) {
		var tabFolderId = (new Date()).getTime() - 10;
		var html = '<div id="' + tabFolderId + '" class="tabs"><ul></ul>';	
		$(FCK.EditorDocument).find('.taskform').append(html);		
		addTab(tabFolderId, true);	
	} else {
		var tabFolderId = range.closest('.tabs').attr('id');
		addTab(tabFolderId, false);
	}
}

FCKInsertTabCommand.prototype.GetState = function()
{
	FCK.Focus();
    return 0;
}

var FCKDeleteTabCommand = function()
{	
}

FCKDeleteTabCommand.prototype.Execute = function() {
			
	var tabActiveId = -1;
	if($(FCKSelection.GetSelectedElement()).parent().find('a').attr("href")) {
		tabActiveId = $(FCKSelection.GetSelectedElement()).parent().find('a').attr("href").split("#tabs-")[1];
	} 
	var tabFolder = $(FCK.EditorDocument).find('.tabs#' + folderId);
	if (tabFolder.length == 1) {
		var activeTabLi = tabFolder.find("li#" + tabActiveId);
		if (activeTabLi.length == 1 && tabFolder.find("li").length > 1) {
			var activeTabDiv = tabFolder.find("div#tabs-" + tabActiveId);
			var siblings = activeTabLi.siblings();
			activeTabLi.remove();
			activeTabDiv.remove();				
			tabFolder.tabs('refresh');
		} else {
			tabFolder.remove();
		}
	} 
}

FCKDeleteTabCommand.prototype.GetState = function()
{
    return 0;
}

FCKCommands.RegisterCommand( FTL_METHOD_INSERT_CMD, new FCKInsertTabCommand() ) ;
FCKCommands.RegisterCommand( FTL_METHOD_DELETE_CMD, new FCKDeleteTabCommand() ) ;

var insertTabItem = new FCKToolbarButton( FTL_METHOD_INSERT_CMD, FCKLang.InsertTab ) ;
insertTabItem.IconPath = FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'images/insertTab.png' ;
FCKToolbarItems.RegisterItem( FTL_METHOD_INSERT_CMD, insertTabItem ) ;

var deleteTabItem = new FCKToolbarButton( FTL_METHOD_DELETE_CMD, FCKLang.DeleteTab ) ;
deleteTabItem.IconPath = FCKPlugins.Items[FTL_PLUGIN_NAME].Path + 'images/deleteTab.png' ;
FCKToolbarItems.RegisterItem( FTL_METHOD_DELETE_CMD, deleteTabItem ) ;

FCK.Events.AttachEvent( 'OnAfterSetHTML', afterSetHTML ) ; 
