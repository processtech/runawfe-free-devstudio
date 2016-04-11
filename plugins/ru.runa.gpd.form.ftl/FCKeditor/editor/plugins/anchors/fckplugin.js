var FTL_PLUGIN_NAME = 'anchors' ;

function afterSetHTML( editorInstance )
{
	$(editorInstance.EditorDocument).find('a').each(function() {
		$(this).click(function() {
			var target = $(FCK.EditorDocument).find($(this).attr('href'));			
			if(FCKBrowserInfo.IsIE && target.length > 0) {
				var top = target.offset().top;
				var left = target.offset().left;
				$(FCK.EditorDocument).scrollTo({ top: "+=" + top, left: "+=" + left}, 800 );
			} else {
				$(FCK.EditorDocument).scrollTo(target, 800 );
			}
    	});
	});
}

FCK.Events.AttachEvent( 'OnAfterSetHTML', afterSetHTML ) ;
