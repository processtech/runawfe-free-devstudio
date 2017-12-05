CKEDITOR.plugins.add('tabs', {
	lang: 'en,ru',
	init: function (editor) {
		editor.ui.addButton('InsertTab', {
			label: editor.lang.tabs.InsertTab,
			command: 'InsertTab',
			icon: this.path + 'images/insertTab.png'
		});
		editor.ui.addButton('DeleteTab', {
			label: editor.lang.tabs.DeleteTab,
			command: 'DeleteTab',
			icon: this.path + 'images/deleteTab.png'
		});
		editor.addCommand('InsertTab', {
			exec: function (editor) {
				if (!editor.getSelection()) {
					return;
				}
				var selectionElement = editor.getSelection() && $(editor.getSelection().getStartElement().$);
				var tabFolder = selectionElement.closest('.tabs');
				if (tabFolder.length == 0) {
					var tabFolderId = (new Date()).getTime() - 10;
					var html = '<div id="' + tabFolderId + '" class="tabs"><ul></ul>';
					editor.insertHtml(html);
					addTab(editor, tabFolderId, true);
				} else {
					var tabFolderId = tabFolder.attr('id');
					addTab(editor, tabFolderId, false);
				}
			}
		});
		editor.addCommand('DeleteTab', {
			exec: function (editor) {
				if (!editor.getSelection()) {
					return;
				}
				var selectionElement = editor.getSelection() && $(editor.getSelection().getStartElement().$);
				var tabFolder = selectionElement.closest('.tabs');
				if (tabFolder.length == 1) {
					var activeTabLi = tabFolder.find("li.ui-tabs-active");
					if (activeTabLi.length == 1 && tabFolder.find("li").length > 1) {
						var tabId = activeTabLi.find("a").attr("href").substring(1);
						var activeTabDiv = tabFolder.find("div#" + tabId);
						activeTabLi.remove();
						activeTabDiv.remove();
						tabFolder.tabs('refresh');
					} else {
						tabFolder.remove();
					}
				}
			}
		});
		editor.on('dataReady', function(event) {
			$(".tabs").tabs();
			$(".tabs > ul > li > a").each(function() {
				$(this).parent().focus(function() {
					$(this).find("a").click();
					activeTab = $(this).find("a").attr('href');
				});
			});
			if (activeTab != null) {
				$("li a[href=" + activeTab + "]").click();
			}
		});
		editor.on('getData', function(e) {
			if ($('.tabs').length > 0) {
				var doc = $("<p>").append(e.data.dataValue);
				$(doc).find('.tabs').each(function() {
					$(this).removeAttr("class").addClass("tabs");
					$(this).find("a").removeAttr("id");
					$(this).find("ul,li,div,a").removeAttr("class tabIndex aria-live aria-busy aria-selected aria-labelledby aria-hidden aria-expanded role aria-controls");
				});
				e.data.dataValue = doc.html();
			}
		});
	}
});


var activeTab = null;
var tabTemplate = "<li><a href='#{href}'>#{label}</a></li>";
function addTab(editor, tabFolderId, initialize) {
	var tabId = (new Date()).getTime();
	var label = editor.lang.tabs.NewTabTitle;
	var id = "tabs-" + tabId;
	var li = $(tabTemplate.replace( /#\{href\}/g, "#" + id ).replace( /#\{label\}/g, label ));	
	var tabFolder = $('#' + tabFolderId);
	tabFolder.find('ul').append(li);
	tabFolder.append('<div id="' + id + '"><p>' + editor.lang.tabs.NewTabContent + '</p></div>');
	activeTab = "#" + id;
	$(li).on('focus', function() {
		$(this).find("a").click();
		activeTab = $(this).find("a").attr('href');
	});
	if (initialize) {
		tabFolder.tabs();
	} else {
		tabFolder.tabs('refresh');
	}
}