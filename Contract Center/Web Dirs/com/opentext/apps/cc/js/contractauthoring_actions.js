// Contract authoring actions.

// Add new section.
function addSection(pos) {
	document.getElementById("defaultContainer").style.display = "none";
	var newSectionOrder = l_sectionandclause_model.SCMappingsToBind().length;
	var l_newSectionId = getNewSectionId();
	var section = {
		type: _SECTION,
		showContainer: 'true',
		cascade: ko.observable(_getCascadeFlagInherit(true, null)),
		inherited: ko.observable(true),
		nonumbering: ko.observable(false),
		numberingStyle: ko.observable(""),
		options: ko.observable(false),
		action: _NEW_SECTION,
		sectionName: ko.observable("Untitled"),
		sectionOrder: newSectionOrder,
		clauseOrder: newSectionOrder,
		isNew: true,
		containingSectionID: l_newSectionId,
		containerID: l_newSectionId,
		initialContainingSectionID: l_newSectionId,
		container: ko.observableArray([]),
		parentContainerID: ""
	};
	if (!isNaN(pos)) {
		l_sectionandclause_model.SCMappingsToBind.splice(pos, 0, section);
	} else {
		l_sectionandclause_model.SCMappingsToBind.splice(newSectionOrder + 1, 0, section);
	}

	l_sectionandclause_model.optionsCaption(undefined);
	var index = pos || (pos == 0) ? pos : newSectionOrder + 1
	for (var i = index; i < l_sectionandclause_model.SCMappingsToBind().length; i++) {
		if (!l_sectionandclause_model.SCMappingsToBind()[i].action) {
			l_sectionandclause_model.SCMappingsToBind()[i].action = _UPDATE_ORDER;
		} else if (l_sectionandclause_model.SCMappingsToBind()[i].type === _SECTION && l_sectionandclause_model.SCMappingsToBind()[i].action === _UPDATE_SECTION) {
			l_sectionandclause_model.SCMappingsToBind()[i].action = _UPDATE_SECTION_NAME_ORDER;
		} else if (l_sectionandclause_model.SCMappingsToBind()[i].type === _CLAUSE && l_sectionandclause_model.SCMappingsToBind()[i].action === _UPDATE_LINKED_CLAUSE) {
			l_sectionandclause_model.SCMappingsToBind()[i].action = _UPDATE_LINKED_CLAUSE_ORDER;
		} else if (l_sectionandclause_model.SCMappingsToBind()[i].type === _CLAUSE && l_sectionandclause_model.SCMappingsToBind()[i].action === _UPDATE_NONSTANDARD_CLAUSE) {
			l_sectionandclause_model.SCMappingsToBind()[i].action = _UPDATE_NONSTANDARD_CLAUSE_ORDER;
		} else if (l_sectionandclause_model.SCMappingsToBind()[i].type === _CLAUSE && l_sectionandclause_model.SCMappingsToBind()[i].action === _CONVERT_TO_NONSTANDARD) {
			l_sectionandclause_model.SCMappingsToBind()[i].action = _CONVERT_TO_NONSTANDARD_ORDER;
		}
	}
	isDirty = true;
	selectSection(l_newSectionId, true, true);
	disableSaveandCancel(false);
	disableInsertClauseBtn(false);
	updateNumberingStyleWithParentStyle(section, l_current_numberingFormat);
}

// Add new sub section
function addNestedSection(data, pos) {
	if (data.options) {
		data.options(false);
	}

	var newSectionOrder = data.container().length;
	if (!newSectionOrder) {
		newSectionOrder = 1 + (+newSectionOrder);
	}
	var l_newSectionId = getNewSectionId();
	newSectionOrder = l_sectionandclause_model.addSectionParentData().container ?
		l_sectionandclause_model.addSectionParentData().container().indexOf(l_sectionandclause_model.addSectionData())
		: l_sectionandclause_model.addSectionParentData().SCMappingsToBind().indexOf(l_sectionandclause_model.addSectionData());
	var section = {
		type: _SECTION,
		showContainer: 'true',
		options: ko.observable(false),
		cascade: ko.observable(_getCascadeFlagInherit(true, data)),
		inherited: ko.observable(true),
		nonumbering: ko.observable(false),
		numberingStyle: ko.observable(""),
		action: _NEW_SECTION,
		sectionName: ko.observable("Untitled"),
		sectionOrder: newSectionOrder,
		clauseOrder: newSectionOrder,
		isNew: true,
		containerID: l_newSectionId,
		containingSectionID: l_newSectionId,
		initialContainingSectionID: l_newSectionId,
		container: ko.observableArray([]),
		parentContainerID: data.containerID,
		parentContainer: data
	};
	if (!isNaN(pos)) {
		data.container.splice(pos, 0, section);
	} else {
		data.container.push(section);
	}

	if (l_sectionandclause_model.addSectionParentData().container) {
		l_sectionandclause_model.addSectionParentData().container().forEach((ele, ind) => {
			if (pos < ind) {
				_updateOrderActionOnSectionOrClause(ele);
			}
		});
	}

	isDirty = true;
	selectSection(l_newSectionId, true, true);
	disableSaveandCancel(false);
	disableInsertClauseBtn(false);
	updateNumberingStyleWithParentStyle(section, l_current_numberingFormat);
}


// Add new non-standard clause.
function addNonStandardClause(_iIndex, _iData) {
	if (_iData.options) {
		_iData.options(false);
	}
	var l_clausesLength = 0;
	var l_newContClauseId = getNewContainingClauseId();
	var l_section = null;
	l_clausesLength = _iData.container().length;
	l_section = _iData;
	var l_clause = {
		type: _CLAUSE,
		showContainer: 'true',
		cascade: ko.observable(_getCascadeFlagInherit(true, _iData)),
		inherited: ko.observable(true),
		nonumbering: ko.observable(false),
		numberingStyle: ko.observable(""),
		action: _NEW_NONSTANDARD_CLAUSE,
		containingSectionID: l_section.containingSectionID,
		containingClauseID: l_newContClauseId,
		initialContainingClauseID: l_newContClauseId,
		initialContainingSectionID: l_section.initialContainingSectionID,
		sectionOrder: _iIndex + "",
		clauseOrder: l_clausesLength + "",
		clauseID: "",
		clauseItemID: "",
		generatedClauseID: "",
		clauseName: ko.observable(),
		clauseHTMLContent: "",
		isNew: true,
		isStandard: ko.observable(false),
		isDirty: ko.observable(true),
		showConvertToNonStandardBtn: ko.observable(false),
		showReplacewithEqClauseBtn: ko.observable(false),
		container: ko.observableArray([]),
		parentContainerID: l_section.containerID,
		parentContainer: l_section,
		containerID: l_newContClauseId
	};
	l_clause.sectionName = ko.computed(function () {
		return l_clause.clauseName();
	}, l_clause);

	l_section.container.splice(l_clausesLength, 0, l_clause);
	var _clauseHTMLContentID = l_clause.containerID + '_clauseHTMLContent';
	loadCKEditor(_clauseHTMLContentID);
	CKEDITOR.instances[_clauseHTMLContentID].on('instanceReady', function (_ev) {
		if ($(".cke_button__hideclauselist").css("display") == "none" && $(".cke_button__showclauselist").css("display") == "none") {
			$(".cke_button__showclauselist").hide();
			$(".cke_button__hideclauselist").show();
		}
	});
	disableSaveandCancel(false);
	$(".droptarget_section").css('border', '');
	$("#" + _clauseHTMLContentID).css({ 'border-style': 'solid', 'border-color': 'rgba(3, 122, 252, 0.89)' });
	isDirty = true;
	//updateDefaultNumberingStyle();
	updateNumberingStyleWithParentStyle(l_clause, l_current_numberingFormat);
}

// Insert new standard clause.
function addSelectedStandardClauses() {
	document.getElementById("defaultContainer").style.display = "none";
	if (l_sectionandclause_model.addSectionData()) {
		var l_clausesLength = l_sectionandclause_model.addSectionData().container().length;
		var id = null;
		for (var claus in clauseselectedarray) {
			var l_listElement = clauseselectedarray[claus];
			if (l_listElement) {
				var l_newContClauseId = getNewContainingClauseId();
				var l_sectionOrClause = l_sectionandclause_model.addSectionData();
				var l_clauseName = l_listElement.name;
				var l_clauseHTMLContent = l_listElement.htmlcontent ? l_listElement.htmlcontent : "";
				var l_clauseID = l_listElement.id;
				var l_clauseItemID = l_listElement.itemid;
				var l_generatedClauseID = l_listElement.clauseid;
				var l_clause = {
					type: _CLAUSE,
					showContainer: 'true',
					action: _NEW_LINKED_CLAUSE,
					parentContainerID: l_sectionOrClause.containerID,
					parentContainer: l_sectionOrClause,
					containingClauseID: l_newContClauseId + "",
					containerID: l_newContClauseId,
					initialContainingClauseID: l_newContClauseId,
					sectionOrder: sectionList.selectedSectionOrder + "",
					clauseOrder: l_clausesLength + "",
					clauseID: l_clauseID + "",
					cascade: ko.observable(_getCascadeFlagInherit(true, l_sectionandclause_model.addSectionData())),
					inherited: ko.observable(true),
					nonumbering: ko.observable(false),
					numberingStyle: ko.observable(""),
					clauseItemID: l_clauseItemID + "",
					generatedClauseID: l_generatedClauseID + "",
					clauseName: ko.observable(l_clauseName + ""),
					clauseHTMLContent: l_clauseHTMLContent + "",
					isNew: true,
					isStandard: ko.observable(true),
					isDirty: ko.observable(true),
					showConvertToNonStandardBtn: ko.observable(true),
					showReplacewithEqClauseBtn: ko.observable(true),
					container: ko.observableArray([])
				};
				l_clause.sectionName = ko.computed(function () {
					return l_clause.clauseName();
				}, l_clause);
				updateNumberingStyleWithParentStyle(l_clause, l_current_numberingFormat);
				l_sectionandclause_model.addSectionData().container.splice(l_clausesLength, 0, l_clause);
				var _clauseHTMLContentID = l_clause.containerID + '_clauseHTMLContent';
				loadCKEditor(_clauseHTMLContentID);
				CKEDITOR.instances[_clauseHTMLContentID].on('instanceReady', function (_ev) {
					$(".cke_button__showclauselist").hide();
					$(".cke_button__hideclauselist").show();
				});
				l_clausesLength++;
			}
		}
	} else {
		var l_clausesLength = l_sectionandclause_model.SCMappingsToBind().length;
		var id = null;
		for (var claus in clauseselectedarray) {
			var l_listElement = clauseselectedarray[claus];
			if (l_listElement) {
				var l_newContClauseId = getNewContainingClauseId();
				var l_clauseName = l_listElement.name;
				var l_clauseHTMLContent = l_listElement.htmlcontent ? l_listElement.htmlcontent : "";
				var l_clauseID = l_listElement.id;
				var l_clauseItemID = l_listElement.itemid;
				var l_generatedClauseID = l_listElement.clauseid;
				var l_clause = {
					type: _CLAUSE,
					showContainer: 'true',
					action: _NEW_LINKED_CLAUSE,
					cascade: ko.observable(_getCascadeFlagInherit(true, l_sectionandclause_model)),
					inherited: ko.observable(true),
					nonumbering: ko.observable(false),
					numberingStyle: ko.observable(""),
					parentContainerID: "",
					containingClauseID: l_newContClauseId + "",
					containerID: l_newContClauseId,
					initialContainingClauseID: l_newContClauseId,
					sectionOrder: sectionList.selectedSectionOrder + "",
					clauseOrder: l_clausesLength + "",
					clauseID: l_clauseID + "",
					clauseItemID: l_clauseItemID + "",
					generatedClauseID: l_generatedClauseID + "",
					clauseName: ko.observable(l_clauseName + ""),
					clauseHTMLContent: l_clauseHTMLContent + "",
					isNew: true,
					isStandard: ko.observable(true),
					isDirty: ko.observable(true),
					showConvertToNonStandardBtn: ko.observable(true),
					showReplacewithEqClauseBtn: ko.observable(true),
					container: ko.observableArray([])
				};
				l_clause.sectionName = ko.computed(function () {
					return l_clause.clauseName();
				}, l_clause);
				updateNumberingStyleWithParentStyle(l_clause, l_current_numberingFormat);
				l_sectionandclause_model.SCMappingsToBind.splice(l_clausesLength, 0, l_clause);
				var _clauseHTMLContentID = l_clause.containerID + '_clauseHTMLContent';
				loadCKEditor(_clauseHTMLContentID);
				CKEDITOR.instances[_clauseHTMLContentID].on('instanceReady', function (_ev) {
					$(".cke_button__showclauselist").hide();
					$(".cke_button__hideclauselist").show();
				});
				l_clausesLength++;
			}
		}
	}
	uncheck_modal();
	delete clauseselectedarray;
	clauseselectedarray = [];
	disableSaveandCancel(false);
	$("#insert_selected_btn").prop("disabled", true);
	isDirty = true;
}


function addSelectedStandardClausesBefore() {
	addSelectedStandardClausesAfterBefore(0);
	toggleOptions('insertStandardClbtnCont', 'open');
}

function addSelectedStandardClausesAfter() {
	addSelectedStandardClausesAfterBefore(1);
	toggleOptions('insertStandardClbtnCont', 'open');
}

function addSelectedStandardClausesAfterBefore(pos) {
	if (l_sectionandclause_model.addSectionData() && l_sectionandclause_model.addSectionParentData()) {
		var index = 0;
		var l_clausesLength = 0;
		for (var claus in clauseselectedarray) {
			l_clausesLength = l_sectionandclause_model.addSectionParentData().container ? l_sectionandclause_model.addSectionParentData().container().indexOf(l_sectionandclause_model.addSectionData()) : l_sectionandclause_model.addSectionParentData().SCMappingsToBind().indexOf(l_sectionandclause_model.addSectionData());
			l_clausesLength = l_clausesLength + index;
			var l_listElement = clauseselectedarray[claus];
			if (l_listElement) {
				var l_clause = _prepareStdClause(l_listElement, l_sectionandclause_model.addSectionParentData(), l_clausesLength);
				updateNumberingStyleWithParentStyle(l_clause, l_current_numberingFormat);
				if (l_sectionandclause_model.addSectionParentData().container) {
					l_sectionandclause_model.addSectionParentData().container.splice(l_clausesLength + pos, 0, l_clause);
				} else {
					l_sectionandclause_model.addSectionParentData().SCMappingsToBind.splice(l_clausesLength + pos, 0, l_clause);
				}
				var _clauseHTMLContentID = l_clause.containerID + '_clauseHTMLContent';
				loadCKEditor(_clauseHTMLContentID);
				CKEDITOR.instances[_clauseHTMLContentID].on('instanceReady', function (_ev) {
					$(".cke_button__showclauselist").hide();
					$(".cke_button__hideclauselist").show();
				});
				// l_clausesLength++;
				if (pos > 0) { //After 
					index++;
				}
			}
		}

		if (l_sectionandclause_model.addSectionParentData().container) {
			l_sectionandclause_model.addSectionParentData().container().forEach((ele, ind) => {
				if (l_clausesLength + pos < ind) {
					_updateOrderActionOnSectionOrClause(ele);
				}
			});
		} else {
			l_sectionandclause_model.addSectionParentData().SCMappingsToBind().forEach((ele, ind) => {
				if (l_clausesLength + pos < ind) {
					_updateOrderActionOnSectionOrClause(ele);
				}
			});
		}

		uncheck_modal();
		delete clauseselectedarray;
		clauseselectedarray = [];
		disableSaveandCancel(false);
		$("#insert_selected_btn").prop("disabled", true);
		isDirty = true;
	}

}


// Convert standard clause to non-standard clause.
function convertToNonStandard(l_sectionOrder, l_clauseOrder) {
	if (l_input_conditionInstMap.has(l_sectionandclause_model.addSectionData().containerID)) {
		return;
	}
	$("#convertToNonStandard").modal();
	$('button#convertToNonStandardOk').off("click");
	$('button#convertToNonStandardOk').on('click', function (_event) {
		// var l_section = l_sectionandclause_model.SCMappingsToBind()[l_sectionOrder];
		var l_clause = l_sectionandclause_model.addSectionData();
		var selectedClauseDivID = l_clause.containerID + '_clauseHTMLContent';
		if (!l_clause.action || l_clause.action == _UPDATE_LINKED_CLAUSE || l_clause.action == _UPDATE_LINKED_CLAUSE_ORDER) {
			l_clause.action = _CONVERT_TO_NONSTANDARD;
		}
		else if (l_clause.action == _NEW_LINKED_CLAUSE) {
			l_clause.action = _NEW_NONSTANDARD_CLAUSE;
		}
		l_clause.isDirty(true);
		l_clause.isStandard(false);
		l_clause.showConvertToNonStandardBtn(false);
		l_clause.showReplacewithEqClauseBtn(false);
		CKEDITOR.instances[selectedClauseDivID].readOnly = true;
		CKEDITOR.instances[selectedClauseDivID].setReadOnly(false);
		//updateDefaultNumberingStyle();
	});
	isDirty = true;
}
