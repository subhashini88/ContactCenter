//Add Section Plugin
CKEDITOR.plugins.add( 'addSection', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'addSection', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				addSection();
			}
		});
		
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'addSection', {
			label: getTranslationMessage("Add Section"),
			command: 'addSection',
			toolbar: 'document',
			icon : this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/addSection.svg'
		});
	}
});

//Save Plugin
CKEDITOR.plugins.add( 'saveClause', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'saveClause', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				updateSectionandClauseMapping();
			}
		});
		
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'saveClause', {
			label: getTranslationMessage("Save"),
			command: 'saveClause',
			toolbar: 'about',
			icon : this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_save.svg'
		});
	}
});

//Cancel Plugin
CKEDITOR.plugins.add( 'cancelClause', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'cancelClause', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				onCancel();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'cancelClause', {
			label: getTranslationMessage("Cancel"),
			command: 'cancelClause',
			toolbar: 'about',
			icon : this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_cancel.svg'
		});
	}
});

// Show Clause List Plugin
CKEDITOR.plugins.add( 'showClauseList', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'showClauseList', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				showClauseLibrary();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'showClauseList', {
			label: getTranslationMessage("Show clause list"),
			command: 'showClauseList',
			toolbar: 'showClause',
			icon : this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_show_clause.svg'
		});
	}
});

//Hide Clause List Plugin
CKEDITOR.plugins.add( 'hideClauseList', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'hideClauseList', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				hideClauseLibrary();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'hideClauseList', {
			label: getTranslationMessage("Hide clause list"),
			command: 'hideClauseList',
			toolbar: 'hideClause',
			icon :  this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_hide_clause.svg'
		});
	}
});

//View all comments Plugin
CKEDITOR.plugins.add( 'viewAllComments', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'viewAllComments', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				viewAllComments();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'viewAllComments', {
			label: 'View all comments',
			command: 'viewAllComments',
			toolbar: 'viewAllComments',
			icon :  this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/view_comments.svg'
		});
	}
});

//Collapse All Plugin
CKEDITOR.plugins.add( 'collapseAll', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'collapseAll', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				collapseAllSections();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'collapseAll', {
			label: getTranslationMessage("Collapse all"),
			command: 'collapseAll',
			toolbar: 'collapse',
			icon :  this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_arrow_up.svg'
		});
	}
});

//Expand All Plugin
CKEDITOR.plugins.add( 'expandAll', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'expandAll', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				expandAllSections();
			}
		});
		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'expandAll', {
			label: getTranslationMessage("Expand all"),
			command: 'expandAll',
			toolbar: 'expand',
			icon :  this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/action_arrow_down.svg'
		});
	}
});

CKEDITOR.plugins.add( 'insertterms', {
	init: function( editor ) {

		// Define the editor command that inserts a timestamp.
		editor.addCommand( 'insertTermsCommand', {

			// Define the function that will be fired when the command is executed.
			exec: function( editor ) {
				callOpenTermsList(editor.name);
			}
		});

		// Create the toolbar button that executes the above command.
		editor.ui.addButton( 'insertTerms', {
			label: getTranslationMessage("Insert Terms"),
			command: 'insertTermsCommand',
			toolbar: 'insertterms',
			icon : this.path.substring(0,this.path.indexOf("js/ckeditor-plugins/"))+'img/addSection.svg'
		});
	}
});