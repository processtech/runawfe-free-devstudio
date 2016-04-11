var FTL_PLUGIN_NAME = "FtlComponents";
var OPEN_PARAMETERS_COMMAND = "OpenParametersDialog";
var TYPE_ATTRIBUTE = "type";

var FtlComponents = new Object();
FtlComponents.SelectedId = -1;

FtlComponents.ExecuteComponentCommand = function(command, type, id) {
	var url = "/editor/FtlComponentServlet?command=" + command;
	if (type) {
		url += "&type=" + type;
	}
	if (id) {
		url += "&id=" + id;
	}
    var httpRequest = (!window.XMLHttpRequest)? new ActiveXObject("Microsoft.XMLHTTP") : new XMLHttpRequest();
    httpRequest.open("GET", url, false);
    httpRequest.send(null);
	if (httpRequest.status == 200 || httpRequest.status == 304) {
		return httpRequest.responseText;
	} else {
		return "error";
	}
}

FtlComponents.ComponentSelected = function(componentId) {
	FtlComponents.SelectedId = componentId;
	return FtlComponents.ExecuteComponentCommand("ComponentSelected", null, componentId);
};

FtlComponents.ComponentDeselected = function() {
	FtlComponents.SelectedId = -1;
	return FtlComponents.ExecuteComponentCommand("ComponentDeselected");
};

FtlComponents.OpenParametersDialog = function() {
	return FtlComponents.ExecuteComponentCommand("OpenParametersDialog", null, FtlComponents.SelectedId);
};

FtlComponents.CreateComponent = function(type) {
	return FtlComponents.ExecuteComponentCommand("CreateComponent", type);
};

FtlComponents.createFakeParserElement = function(realElement) {
	var writer = new CKEDITOR.htmlParser.basicWriter();
	realElement.writeHtml(writer);
	var html = writer.getHtml();
	var id = realElement.attributes["id"];
	var attributes = {
	    id : id != "undefined" ? id : FtlComponents.CreateComponent(realElement.attributes[TYPE_ATTRIBUTE]),
		"class": "cke_ftl_component",
		"data-cke-real-node-type": CKEDITOR.NODE_ELEMENT,
		"cke-real-element-type": "ftl_component", 
		"data-cke-realelement": encodeURIComponent(html),
		"cke_resizable": false, 
		src: "/editor/FtlComponentServlet?command=GetImage&type=" + realElement.attributes[TYPE_ATTRIBUTE] + "&parameters=" + encodeURIComponent(realElement.attributes["parameters"]),
		style: "margin: 3px; border: 2px solid black;"
	};
	return new CKEDITOR.htmlParser.element("img", attributes);
};

CKEDITOR.plugins.add(FTL_PLUGIN_NAME, {
		lang: "en,ru",
		requires: "fakeobjects",
		init: function(editor) {
			editor.addCommand(OPEN_PARAMETERS_COMMAND, {
				exec: function() {
					FtlComponents.OpenParametersDialog();
				}
			});
			if (editor.addMenuItems) {	
				// If the 'menu' plugin is loaded, register the menu items.
				editor.addMenuItems({
					"ftl_component": {
						label : editor.lang.FtlComponents.Parameters,
						command : OPEN_PARAMETERS_COMMAND,
						group : "flash"
					}
				});
			}
			editor.on("doubleclick", function(evt) {
				var element = evt.data.element;
				if (element && element.getAttribute("cke-real-element-type") == "ftl_component") {
					FtlComponents.ComponentSelected(element.$.id);
					FtlComponents.OpenParametersDialog();
				}
			});
			if (editor.contextMenu) {
				// If the 'contextmenu' plugin is loaded, register the listeners.
				editor.contextMenu.addListener(function(element, selection) {
					if (element && element.getAttribute("cke-real-element-type") == "ftl_component") {
						return { "ftl_component": CKEDITOR.TRISTATE_OFF };
					}
				});
			}
		},
		afterInit: function(editor) {
			editor.on("selectionChange", function () {
				var selection = editor.getSelection();
				var selectedElement = selection.getSelectedElement();
				if (!selectedElement || selectedElement.$.className.indexOf("cke_ftl_component") == -1){
					FtlComponents.ComponentDeselected();
				} else {
					FtlComponents.ComponentSelected(selectedElement.$.id);
				}
			});
			var dataProcessor = editor.dataProcessor, dataFilter = dataProcessor && dataProcessor.dataFilter;
			if (dataFilter) {
				dataFilter.addRules( {
					elements: {
						"ftl_component" : function(element) {
							return FtlComponents.createFakeParserElement(element);
						}
					}
				}, {priority: 4, applyToAll: true} );
			}
		}
	}
);
