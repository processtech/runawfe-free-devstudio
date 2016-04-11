/**
 * @license Copyright (c) 2003-2013, CKSource - Frederico Knabben. All rights reserved.
 * For licensing, see LICENSE.md or http://ckeditor.com/license
 */

CKEDITOR.editorConfig = function( config ) {
	// Define changes to default configuration here. For example:
	// config.language = 'fr';
	// config.uiColor = '#AADC6E';
	
	config.extraPlugins = "divarea,FtlComponents,tabs,anchors";
	config.contentsCss = "form.css";
	config.bodyClass = "taskform";
	config.height = "500px";
	config.allowedContent = true;

	config.toolbar = "Runa";
	config.toolbar_Runa=[
		['Source','-','NewPage','-','Templates'],
		['Cut','Copy','Paste','PasteText','PasteFromWord','-','Print','SpellChecker','Scayt'],
		['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
		['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
		['InsertTab','DeleteTab'],
		'/',
		['Bold','Italic','Underline','Strike','-','Subscript','Superscript'],
		['NumberedList','BulletedList','-','Outdent','Indent','Blockquote'],
		['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'],
		['Link','Unlink','Anchor'],
		['Image','Flash','Table','HorizontalRule','Smiley','SpecialChar','PageBreak'],
		'/',
		['Styles','Format','Font','FontSize'],
		['TextColor','BGColor'],['Maximize','ShowBlocks','-','About']
	];
	
};
