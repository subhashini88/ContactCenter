( function() {

	'use strict';

	CKEDITOR.plugins.add( 'commonbar', {
		init: function( editor ) {
			editor.on( 'loaded', function() {
			var target = editor.config.commonbarDiv;
			var innerHtml,space;
			if ( typeof target == 'string' ) {
				target = CKEDITOR.document.getById( target );
			} else {
				target = new CKEDITOR.dom.element( target );
			}
			if ( target ) {
				innerHtml = editor.fire( 'uiSpace', { space: 'top', html: '' } ).html;
				if ( innerHtml ) {
					editor.on( 'uiSpace', function( ev ) {
						if ( ev.data.space == 'top' )
							ev.cancel();
					}, null, null, 1 );
					space = target.append( CKEDITOR.dom.element.createFromHtml('<div id="cke_'+editor.name+'" class="cke '+editor.id+' cke_reset_all cke_chrome cke_editor_'+editor.name+' cke_shared cke_detached cke_'+editor.lang.dir+'0 ' + CKEDITOR.env.cssClass + '"'+' dir="'+editor.lang.dir+'" title="' + ( CKEDITOR.env.gecko ? ' ' : '' ) + '" lang="'+editor.langCode+'" role="presentation"><div class="cke_inner"><div id="'+editor.ui.spaceId('top' )+'" class="cke_top role="presentation">'+innerHtml+'</div></div></div>' ) );
					if ( target.getCustomData( 'cke_hasshared' ) )
						space.hide();
					else
						target.setCustomData( 'cke_hasshared', 1 );
					space.unselectable();

					space.on( 'mousedown', function( evt ) {
						evt = evt.data;
						if ( !evt.getTarget().hasAscendant( 'a', 1 ) )
							evt.preventDefault();
					} );
					editor.focusManager.add( space, 1 );
					editor.on( 'focus', function() {
						for ( var i = 0, sibling, children = target.getChildren(); ( sibling = children.getItem( i ) ); i++ ) {
							if ( sibling.type == CKEDITOR.NODE_ELEMENT &&
								!sibling.equals( space ) &&
								sibling.hasClass( 'cke_shared' ) ) {
								sibling.hide();
							}
						}

						space.show();
					} );

					editor.on( 'destroy', function() {
						space.remove();
					} );
				}
			}
			}, null, null, 9 );
		}
	} );
} )();
