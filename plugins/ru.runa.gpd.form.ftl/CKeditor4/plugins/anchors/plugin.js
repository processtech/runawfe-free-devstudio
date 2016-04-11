CKEDITOR.plugins.add('anchors', {
	lang: 'en,ru',
	init: function (editor) {		
		editor.on('dataReady', function(event) {		
			$('a').each(function() {
				$(this).click(function() {
					var target = CKEDITOR.instances['editor'].document.$.getElementById($(this).attr('href').substring(1)) ;
					$(editor.editable().$).scrollTo(target, 800 );
		    	});
			});
		});
	}
});