// customs plug-in url.
function getUrlforCustomPlugin() {
	var url = window.location.href;
	var index = url.indexOf("apps/cc");
	url = url.substring(0, index + 8) + "js/ckeditor-plugins/";
	return url;
}

// Destroy ck editor instance.
function destroyCkEditorInstance( iContainerId) {
	var editorToDestroy = CKEDITOR.instances[iContainerId + "_clauseHTMLContent"];
	if (editorToDestroy) {
		editorToDestroy.destroy(true);
	}
}

// Destroy all ck editor instances.
function _destroyAllCKInstances(){
	for(name in CKEDITOR.instances)
	{
		CKEDITOR.instances[name].destroy(true);
	}
}

// Load ck editor.
function loadCKEditor(iClauseId) {
	if ((l_sectionandclause_model.contract_LifecycleState == "Draft" || l_sectionandclause_model.contract_LifecycleState == "Negotiation" || l_sectionandclause_model.contract_LifecycleState == "Pre-Execution") && (l_sectionandclause_model.contract_Z_INT_Status() == "Draft" || l_sectionandclause_model.contract_Z_INT_Status() == "Negotiation" || l_sectionandclause_model.contract_Z_INT_Status() == "Pre-Execution" || l_sectionandclause_model.contract_Z_INT_Status() == "RequestedForNegClosure" || l_sectionandclause_model.contract_Z_INT_Status() == "SentForNegotiation")) {
		CKEDITOR.config.skin = 'moono-lisa';
		CKEDITOR.config.contentsCss = '../css/cc_ckeditor.css';

		if (CKEDITOR.env.quirks) {
			CKEDITOR.env.quirks = false;
		}
		var pluginUrl = getUrlforCustomPlugin();
		CKEDITOR.plugins.addExternal('commonbar', pluginUrl, 'commonbar.js');
		CKEDITOR.plugins.addExternal('customliststyles', pluginUrl, 'plugin_customliststyles.js');
		CKEDITOR.plugins.addExternal('custommenu', pluginUrl, 'plugin_custommenu.js');
		
		editor = CKEDITOR.inline(iClauseId, {
			extraPlugins: 'font,colorbutton,commonbar,customliststyles,custommenu',
			commonbarDiv: 'div_ckEditorActions',
			forcePasteAsPlainText: true,
			toolbar: [
				{ name: 'document', items: ['Source', '-', 'Save', 'NewPage', 'Preview', 'Print', '-', 'Templates'] },
				{ name: 'clipboard', items: ['Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord', '-', 'Undo', 'Redo'] },
				{ name: 'editing', items: ['Find', 'Replace', '-', 'SelectAll', '-', 'Scayt'] },
				{ name: 'forms', items: ['Form', 'Checkbox', 'Radio', 'TextField', 'Textarea', 'Select', 'Button', 'ImageButton', 'HiddenField'] },
				{ name: 'basicstyles', items: ['Bold', 'Italic', 'Underline', 'Strike', 'Subscript', 'Superscript', '-', 'CopyFormatting', 'RemoveFormat'] },
				{ name: 'paragraph', items: ['NumberedList','customlisstyles','BulletedList','Outdent', 'Indent', '-', 'Blockquote', 'CreateDiv', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock', '-', 'BidiLtr', 'BidiRtl', 'Language'] },
				{ name: 'insert', items: ['Image', 'Flash', 'Table', 'HorizontalRule', 'Smiley', 'SpecialChar', 'PageBreak', 'Iframe'] },
				{ name: 'styles', items: ['Styles', 'Format', 'Font', 'FontSize'] },
				{ name: 'colors', items: ['TextColor', 'BGColor'] },
				{ name: 'about', items: ['About'] },
				{ name: 'collapse', items: ['collapse'] },
				{ name: 'expand', items: ['expand'] },
				{ name: 'showClause', items: ['showClause'] },
				{ name: 'hideClause', items: ['hideClause'] },
				{ name: 'viewAllComments', items: ['viewAllComments'] },
				{ name: 'undoChanges', items: ['undoChanges'] },
				{ name: 'cccustom', items: [] },
			],
			removeButtons: 'Save,NewPage,Preview,Print,Templates,Cut,Copy,Paste,PasteFromWord,Find,Undo,Redo,Strike,Replace,SelectAll,About,Scayt,Form,Checkbox,Radio,TextField,Textarea,Select,Button,ImageButton,HiddenField,Subscript,Superscript,CopyFormatting,Blockquote,CreateDiv,JustifyLeft,JustifyCenter,JustifyRight,JustifyBlock,Language,BidiRtl,BidiLtr,Link,Unlink,Anchor,Image,Flash,Table,HorizontalRule,Smiley,SpecialChar,PageBreak,Iframe,Maximize,ShowBlocks,Format',
			removePlugins: 'iframe,pagebreak,flash,stylescombo,print,preview,save,smiley,paste,pastefromword,elementspath,magicline',
			extraAllowedContent: 'ol[class](cc-ol-lower-alpha,cc-ol-upper-alpha,cc-ol-lower-roman,cc-ol-upper-roman,cc-ol-lower-alpha-nested,cc-ol-upper-alpha-nested,cc-ol-decimal-nested,cc-ol-lower-roman-nested,cc-ol-upper-roman-nested)'
		});
	
		 CKEDITOR.config.ccmenu = {
				'customlisstyles': {
					items: [
						{
							name: 'insertdecimal',
							label: '1, 2, 3, ...',
							command: 'insertdecimal'
						},
						{
							name: 'insertlowerroman',
							label: 'i, ii, iii, ...',
							command: 'insertlowerroman'
						},
						{
							name: 'insertupperroman',
							label: 'I, II, III, ...',
							command: 'insertupperroman'
						},
						{
							name: 'insertloweralpha',
							label: 'a, b, c, ...',
							command: 'insertloweralpha'
						},
						{
							name: 'insertupperalpha',
							label: 'A, B, C, ...',
							command: 'insertupperalpha'
						},
						{
							name: 'insertdecimalnested',
							label: '1, 1.1, 1.1.1, ...',
							command: 'insertdecimalnested'
						},
						{
							name: 'insertlowerromannested',
							label: 'i, i.i, i.i.i, ...',
							command: 'insertlowerromannested'
						},
						{
							name: 'insertupperromannested',
							label: 'I, I.I, I.I.I, ...',
							command: 'insertupperromannested'
						},
						{
							name: 'insertloweralphanested',
							label: 'a, a.a, a.a.a, ...',
							command: 'insertloweralphanested',
						},
						{
							name: 'insertupperalphanested',
							label: 'A, A.A, A.A.A, ...',
							command: 'insertupperalphanested',
						},
						{
							name: 'removelist',
							label: 'None',
							command: 'removelist',
						}],
					label: {
						text: 'Number style',
						width: 45,
						visible: false //default value
					},
					icon: getUrlforCustomPlugin() + 'icons/numbered_bullet.png'
				}
			}
			
		editor.on("focus", function(...params){
			if(params[0].editor.element.$.getAttribute('isStandard')!=="true"){
				_enableAllCKButtons();
			}else{
				_disableAllCKButtons();
			}
		});	
		editor.on("blur", function(...params){
			ko.dataFor(params[0].editor.element.$).clauseHTMLContent=CKEDITOR.instances[params[0].editor.element.$.getAttribute("id")].getData()
			_disableAllCKButtons();
		});	
		editor.on("change", function(...params){
			ko.dataFor(params[0].editor.element.$).clauseHTMLContent=CKEDITOR.instances[params[0].editor.element.$.getAttribute("id")].getData()
		});	
		function _disableAllCKButtons(){
			$(".cke_button").addClass("cke_button_disabled");
			$(".cke_combo").addClass("cke_combo_disabled");	
		}
		function _enableAllCKButtons(){
			$(".cke_button").removeClass("cke_button_disabled");
			$(".cke_combo").removeClass("cke_combo_disabled");
		}
		editor.on('instanceReady', function (ev) {
			var editor = ev.editor;
			var isStandard = ev.editor.element.$.getAttribute('isStandard')==="true" ? true : false;
			var cke_numberbutton =  $(".cke_button__numberedlist");
			if(cke_numberbutton){
				cke_numberbutton.hide();
			}	
			_disableAllCKButtons();
			if (navigator.userAgent.search("Edge") >= 0) {
				//Hide Bold, Italic, Underline, Font and color buttons.
				$(".cke_button__bold").hide();
				$(".cke_button__italic").hide();
				$(".cke_button__underline").hide();
				$(".cke_button__removeformat").hide();
				$(".cke_combo__font").hide();
				$(".cke_combo__fontsize").hide();
				$(".cke_button__textcolor").hide();
				$(".cke_button__bgcolor").hide();
				$(".cke_button__pastetext").hide();
				$(".cke_button__bold").parent().parent().remove();
			}
			editor.setReadOnly(isStandard);
			$(".cke_chrome").css("border", "0px");
			$(".cke_top").css("border-bottom", "0px");
			$(".cke_editable").css("max-height", "none");
			$(".cke_button__hideclauselist").hide();
			$(".cke_button__expandAll").hide();
			$(".cke_button__showclauselist").closest(".cke_toolbar").css({ "float": "right" });
			$(".cke_button__hideclauselist").closest(".cke_toolbar").css({ "float": "right" });
			$(".cke_button__viewAllComments").closest(".cke_toolbar").css({ "float": "right" });
			$(".cke_button__expandAll").closest(".cke_toolbar").css({ "float": "right" });
			$(".cke_button__collapseAll").closest(".cke_toolbar").css({ "float": "right" });
			$(".cke_button__saveclause_icon").css("background-size", "15px");
			$(".cke_button__cancelclause_icon").css("background-size", "14px");
			$(".cke_button__addsection_icon").css("margin-top", "2px");
			$(".cke_button__showclauselist_icon").css("background-size", "19px");
			$(".cke_button__showclauselist_icon").css("width", "19px");
			$(".cke_button__showclauselist_icon").css("height", "25px");
			$(".cke_button__hideclauselist_icon").css("background-size", "19px");
			$(".cke_button__hideclauselist_icon").css("width", "19px");
			$(".cke_button__hideclauselist_icon").css("height", "25px");
			$(".cke_button__viewAllComments_icon").css("background-size", "19px");
			$(".cke_button__viewAllComments_icon").css("width", "19px");
			$(".cke_button__viewAllComments_icon").css("height", "25px");
			$('#' + editor.name).attr('contentEditable', !isStandard);
		});
		editor.on('change', function (evt) {
			var clause=	ko.dataFor(evt.editor.element.$);
			if (clause) {
				clause.isDirty(true);
				if (!clause.action) {
					clause.action = _UPDATE_NONSTANDARD_CLAUSE;
				}else if (clause.action === _UPDATE_ORDER) {
					clause.action = _UPDATE_NONSTANDARD_CLAUSE_ORDER;
				}
			}
			isDirty = true;
		});
		editor.on('afterCommandExec', function (event) {
			var commandName = event.data.name;
			// For 'indent' commmand
			if (commandName == 'indent') {
				var element = getListElement(editor, 'ol');
				if (element && element.getParent() && element.getParent().getParent()) {
					element.addClass(element.getParent().getParent().getAttribute("class"));
				}
			}
		});
	} else {
		$("#div_sectionsAndClausesHeader").css("height", "37px");
		$("#div_sectionActions").css("bottom", "0px");
		$("#div_sectionsAndClausesHeader").css("height", "37px");
		$("#div_sectionNamesDropdown").css("bottom", "0px");
		$("#ShowHideclauseListId").css("bottom", "0px");
	}
	
}