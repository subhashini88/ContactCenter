$.cordys.json.defaults.removeNamespacePrefix = true;

const completionType = Object.freeze({
	MET: {label: 'Met', action:'OBL-Met-Success', isMet: true, value:'MET'},
	NOTMET: {label: 'Not met', action:'OBL-Not-Met', isMet: false, value:'NOTMET'}
});

var CommentsDataModel = function () {
	var self = this;
	self.comments = ko.observable('');
	self.contractID = ko.observable();
	self.isMet = ko.observable();
	self.nonCompState = ko.observable('');
	self.nonCompStateItemId = ko.observable('');
	self.completionType = ko.observable('');
	self.completionTypes = ko.observableArray(Object.values(completionType));
	self.hasErrors = ko.observable(false);

	self.updateIsMet = (iItem)=>{
		if(iItem.completionType().length>0){
			self.hasErrors(false);
			self.isMet(completionType[iItem.completionType()].isMet);
		}
	}

	self.openNonComplianceModal = () =>{
		nonCompLookup.searchText('');
		$("#div_nonCompModal").modal({
			backdrop: 'static',
			keyboard: false
		});
		getNonComplianceStates(nonCompLookup);
	
		$("#btn_selectYes").on("click", function(){
			self.nonCompState(nonCompLookup.selectedNonCompState());
			self.nonCompStateItemId(nonCompLookup.selectedNonCompStateItemId());
	
		})
	}
	
	self.clearopenNonComplianceValue = ()=> {
		self.nonCompState('');
		self.nonCompStateItemId('');
	}
}

var nonCompLookUpModel = function(){
	var self = this;
	self.searchText = ko.observable('');
	self.nonComplianceStates = ko.observableArray([]);
	self.selectedNonCompState = ko.observable();
	self.selectedNonCompStateItemId = ko.observable();

	self.searchResults = ()=>{
		getNonComplianceStates(nonCompLookup,self.searchText());
	}
	
	self.selectViewRadioButton = function (iItem, event) {
		$(event.currentTarget.parentElement).find('.cc-select-column').removeClass("cc-radio-on");
		$(event.currentTarget).find('.cc-select-column').addClass("cc-radio-on");
	
		self.selectedNonCompStateItemId(iItem["OBLNonCompliance-id"].ItemId);
		self.selectedNonCompState(iItem.Name);
	  }

}

var commentsModel = new CommentsDataModel();
var nonCompLookup = new nonCompLookUpModel();

$(function () {
	var i_locale = getlocale();
	translateLabels("com/opentext/apps/contractcenter/ContractCenter/ContractCenter", i_locale);
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	loadRTLIfRequired(i_locale, rtl_css);
	const modalElement = $('[src*="complete_obligationTask.htm"]', window.parent.document);
	modalElement.height("500px");
	modalElement.parent().css('overflow', 'hidden');
	cInstanceId = getUrlParameterValue("instanceId", null, true);
	ko.applyBindings(commentsModel, document.getElementById("commentsDiv"));
	ko.applyBindings(nonCompLookup, document.getElementById("div_nonCompModal"));

	platformDialogModifications("Submit", completeOblTask);
	//getOblComments();
});

function completeOblTask(){
	if(validateMandatoryFields()){
	$.cordys.ajax(
		{
			namespace: "http://schemas.opentext.com/apps/cc/configworkflow/20.2",
			method: "CompleteOblReminder",
			parameters: {
				GCActivityInstanceItemId: cInstanceId,
				action: completionType[commentsModel.completionType()].action,
				NonComplianceStateItemId:commentsModel.nonCompStateItemId(),
				Comments: commentsModel.comments(), 
			},
			success: function (data) {
				window.parent.location.reload();
		}});
	}
}

function validateMandatoryFields(){
	const valid = commentsModel.completionType() != undefined;
	commentsModel.hasErrors(!valid);
	return valid;
}

function getNonComplianceStates(iModel,searchText="") {
		$.cordys.ajax(
			{
				namespace: "http://schemas/OpenTextContractCenter/OBLNonCompliance/operations",
				method: "GetNonComplianceStates",
				parameters: {
					SearchName: searchText,
					Cursor: { "@limit": "200" }
				},

				success: function (data) {
					if (data) {
						iModel.nonComplianceStates.removeAll();
						let allStates = data.OBLNonCompliance;
						if(allStates){
							if(Array.isArray(allStates)){
								allStates.forEach(state => iModel.nonComplianceStates.push(state));
							}else{
								iModel.nonComplianceStates.push(allStates);
							}
						}
					}
				},
				error: function (responseFailure) {
					showOrHideErrorInfo("div_modalErrorInfoArea", true, getTranslationMessage("An error occurred while fetching non compliance states. Contact your administrator."), 10000);
					return false;
				}
			});
}

function getOblComments() {
	CommentsDataModel = $.cordys.ajax(
		{
			namespace: "http://schemas/OpenTextContractCenter/Contract/operations",
			method: "",
			parameters:
				{},
			success: function (data) {
				if (data) {
					commentsModel.comments = data.Contract.Comments;
					commentsModel.contractID = data.Contract["Contract-id"].Id;
				}
			}
		});
}


function platformDialogModifications(i_newButtonName, i_newButtonAction) {
    //hide OK button
    hideOKButton();

    //Save changes action in footer
    var newBtn = document.createElement("Button");
    newBtn.innerHTML = i_newButtonName;
    newBtn.className = "btn btn-primary btn-translate";
    newBtn.onclick = i_newButtonAction;

    $('ai-dialog-footer .btn-primary', window.parent.document).before(newBtn);

    $('ai-dialog', window.parent.document).animate({
        'max-height': '100vh',
        'max-width': '60vw',
        'width': '51vw',
        'height': '71vh'
    }, 500);

    //Dialog content style enhancements            
    $('ai-dialog-body iframe', window.parent.document).css({
        'width': '100%',
        'height': 'calc(100% - 6px)'
    });

    $('ai-dialog-body', window.parent.document).css({
        'max-height': 'calc(75vh - 7.5em)',
        'height': '100%',
    });

    $('.layout-panel .panel-container', window.parent.document).css({
        'padding-left': '0px'
    });

    $('panel-container iframe', window.parent.document).css({
        'height': 'calc(100% - 6px)',
        'width': '100%',
        'border': '0px'
    });
}