
CKEDITOR.plugins.myLists = {
	init: function (editor) {

		// Define the editor command that inserts class into ol.
		editor.addCommand('insertdecimalnested', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					applyForAllNodesInList(element, "cc-ol-decimal-nested");
				}
			}
		});

		// Define the editor command that inserts class into ol.
		editor.addCommand('insertlowerromannested', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					applyForAllNodesInList(element, "cc-ol-lower-roman-nested");
				}
			}
		});
		// Define the editor command that inserts class into ol.
		editor.addCommand('insertupperromannested', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					applyForAllNodesInList(element, "cc-ol-upper-roman-nested");
				}
			}
		});


		// Define the editor command that inserts class into ol.
		editor.addCommand('insertloweralphanested', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					applyForAllNodesInList(element, "cc-ol-lower-alpha-nested");
				}
			}
		});

		// Define the editor command that inserts class into ol.
		editor.addCommand('insertupperalphanested', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					applyForAllNodesInList(element, "cc-ol-upper-alpha-nested");
				}
			}
		});

		// Define the editor command that inserts lower alpha ol.
		editor.addCommand('insertloweralpha', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					if (!element.hasClass("cc-ol-lower-alpha")) {
						removeAllListStyles(element);
						element.addClass("cc-ol-lower-alpha");
					}
					removeNestedForChilds(element);
				}
			}
		});

		// Define the editor command that inserts a lower roman ol.
		editor.addCommand('insertlowerroman', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					if (!element.hasClass("cc-ol-lower-roman")) {
						removeAllListStyles(element);
						element.addClass("cc-ol-lower-roman");
					}
					removeNestedForChilds(element);
				}
			}
		});


		// Define the editor command that inserts upper alpha ol.
		editor.addCommand('insertupperalpha', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					if (!element.hasClass("cc-ol-upper-alpha")) {
						removeAllListStyles(element);
						element.addClass("cc-ol-upper-alpha");
					}
					removeNestedForChilds(element);
				}
			}
		});

		// Define the editor command that inserts upper roman ol.
		editor.addCommand('insertupperroman', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					if (!element.hasClass("cc-ol-upper-roman")) {
						removeAllListStyles(element);
						element.addClass("cc-ol-upper-roman");
					}
					removeNestedForChilds(element);
				}
			}
		});

		// Define the editor command that inserts decimal.
		editor.addCommand('insertdecimal', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (!element) {
					editor.execCommand('numberedlist');
					element = getListElement(editor, 'ol');
				}
				if (element) {
					if (!element.hasClass("cc-ol-decimal")) {
						removeAllListStyles(element);
						element.addClass("cc-ol-decimal");
					}
					removeNestedForChilds(element);
				}
			}
		});

		// Define the editor command that removes list and its style.
		editor.addCommand('removelist', {
			exec: function (editor) {
				element = getListElement(editor, 'ol');
				if (element) {
					editor.execCommand('numberedlist');
				}
			}
		});
	},
	icons: 'insertdecimalnested,insertlowerromannested,insertupperromannested,insertupperalphanested,insertloweralphanested,insertloweralpha,insertupperalpha,insertlowerroman,insertupperroman,insertdecimal,removelist', // %REMOVE_LINE_CORE%,
	hidpi: true, // %REMOVE_LINE_CORE%


}
CKEDITOR.plugins.add('customliststyles', CKEDITOR.plugins.myLists);

function getListElement(editor, listTag) {
	var range;
	try {
		range = editor.getSelection().getRanges()[0];
	} catch (e) {
		return null;
	}

	range.shrink(CKEDITOR.SHRINK_TEXT);
	return editor.elementPath(range.getCommonAncestor()).contains(listTag, 1);
}

const list_styles = [
	'cc-ol-lower-alpha',
	'cc-ol-upper-alpha',
	'cc-ol-lower-roman',
	'cc-ol-upper-roman',
	'cc-ol-lower-alpha-nested',
	'cc-ol-upper-alpha-nested',
	'cc-ol-decimal-nested',
	'cc-ol-lower-roman-nested',
	'cc-ol-upper-roman-nested',
	'cc-ol-decimal'
];

function removeAllListStyles(element) {
	for (var i = 0; i < list_styles.length; i++) {
		element.removeClass(list_styles[i]);
	}
}

function getRootOLList(element) {
	var rootElement = element;
	var currentElement = element;
	while (currentElement && !currentElement.hasClass('m_secandcls-ol')) {
		rootElement = currentElement;
		currentElement = currentElement.getAscendant('ol', false);
	}
	return rootElement;
}

function applyOnChildsOLList(element, styleName) {
	if (element) {
		element.setAttribute('class', styleName);
		element.find('ol').$.forEach(function (el) {
			el.className = styleName;
		});
		var ulList = element.find('ul');
		var count = ulList ? ulList.count() : 0;
		for (var i = 0; i < count; i++) {
			var item = ulList.getItem(i);
			if (item) {
				item.renameNode('ol');
				item.setAttribute('class', styleName);
			}
		}
	}
}
function applyForAllNodesInList(element, styleName) {
	var rootElement = getRootOLList(element);
	applyOnChildsOLList(rootElement, styleName);
}

function removeNestedForChilds(element) {
	if (element) {
		element.find('ol').$.forEach(function (el) {
			el.className = el.className.replace('-nested', '');;
		});
	}
}