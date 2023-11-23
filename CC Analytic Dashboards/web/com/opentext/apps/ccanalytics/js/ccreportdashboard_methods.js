
// Report general and util methods.

// On body resize do action.
function bodyResize(e) {
	if(window.actuate)
	{
		isReportFirstTimeLoading = true;
		if(!!document.documentMode) //for IE
		{
			loadReports();
		}
		else
		{
			setTimeout(loadReports,0);
		}
		isReportFirstTimeLoading = false;
	}
}

// Determine whether the card is full size or not.
function isReportFullSize(reportNumber) {
    return ($('#card' + reportNumber + 'ReportFooter').find('.icon-collapse').length == 1) ? true : false;
}

// Calculate div Id based on report number.
function getReportBodyDivID(reportNumber) {
    return 'card' + reportNumber + 'ReportBody';
}

// Generating a string array from given object array with specified property.
function formStringArray(array, propertyName) {
    var result = [];
    if (array && array.length) {
        for (var i = 0; i < array.length; i++) {
            result.push(array[i][propertyName]);
        }
    }
    return result;
}

// For calculating the report size based on parameters.
function getReportSize(divID, isChangeInReportSize, isReportFullSize, reportNumber) {
    var current = $('#' + divID);
    var parent = current.parent();
    var i_report_size = new ReportSize();
    if (isReportFullSize) {
        i_report_size.height = $('.full-body-transparent').height();
        i_report_size.width = $('.full-body-transparent').width() * 0.75;
        if (isChangeInReportSize) {
            g_report_size.height = parent.height();
            g_report_size.width = current.width() + 10;
        }
    } else if (!isChangeInReportSize) {
        if(reportNumber==1){
            g_report_size.height = parent.parent().height();
        }else{
        g_report_size.height = parent.height();
        }
        g_report_size.width = parent.width() + 10;
        i_report_size.height = g_report_size.height;
        i_report_size.width = g_report_size.width;
    }
    else {
        i_report_size.height = g_report_size.height;
        i_report_size.width = g_report_size.width;
    }

    if (isReportFirstTimeLoading) {
        i_report_size.width = parent.width();
        if (reportNumber == 1) {
            i_report_size.height = parent.parent().height();
        } else {
            i_report_size.height = parent.height();
        }

    }
    return i_report_size;
}

function getReportsSizeForSwapTiles(sourceCardNumber, targetCardNumber) {
    var swap_reports_sizes = [];
    var sourceCard = $('#' + getReportBodyDivID(sourceCardNumber));
    var targetCard = $('#' + getReportBodyDivID(targetCardNumber));
    var i_report_size = new ReportSize();
    i_report_size.height = sourceCard.parent().height();
    i_report_size.width = sourceCard.width() + 10;
    swap_reports_sizes.push(i_report_size);

    var i_report_size = new ReportSize();
    i_report_size.height = targetCard.parent().height();
    i_report_size.width = targetCard.width() + 10;
    swap_reports_sizes.push(i_report_size);
    return swap_reports_sizes;
}

// Get quarter number based on the label.
function getQuarterFilter(label) {
    switch (label) {
        case FILTER_ALL_QUARTER.label:
            return FILTER_ALL_QUARTER.value;
        case FILTER_FIRST_QUARTER.label:
            return FILTER_FIRST_QUARTER.value;
        case FILTER_SECOND_QUARTER.label:
            return FILTER_SECOND_QUARTER.value;
        case FILTER_THIRD_QUARTER.label:
            return FILTER_THIRD_QUARTER.value;
        case FILTER_FOURTH_QUARTER.label:
            return FILTER_FOURTH_QUARTER.value;
        default:
            return FILTER_ALL_QUARTER.value;
    }
}

/** For opening and closing settings*/

// Card1.
function openCard1Settings() {
    resetParameters1();
    $(".card-1-settings").css("display", "block");
    $(".card-1-report").css("display", "none");
    hideReportSummary(1);
}

// Card2.
function openCard2Settings() {
    resetParameters2();
    $(".card-2-settings").css("display", "block");
    $(".card-2-report").css("display", "none");
    hideReportSummary(2);
}

// Card3.
function openCard3Settings() {
    resetParameters3();
    $(".card-3-settings").css("display", "block");
    $(".card-3-report").css("display", "none");
    hideReportSummary(3);
}

// Card4.
function openCard4Settings() {
    resetParameters4();
    $(".card-4-settings").css("display", "block");
    $(".card-4-report").css("display", "none");
    hideReportSummary(4);
}

// Card5.
function openCard5Settings() {
    resetParameters5();
    $(".card-5-settings").css("display", "block");
    $(".card-5-report").css("display", "none");
    hideReportSummary(5);
}

// Card6.
function openCard6Settings() {
    resetParameters6();
    $(".card-6-settings").css("display", "block");
    $(".card-6-report").css("display", "none");
    hideReportSummary(6);
}


/** For applying the filters and submit the report.*/
// Card1.
function applyCard1Settings() {
    $(".card-1-report").css("display", "block");
    $(".card-1-settings").css("display", "none");
    if ($('#card1').hasClass('legend-card') && !isReportFullSize(1)) {
        showReportSummary(1);
    }
    if (i_reports_obj.reports()[0] && i_reports_obj.reports()[0].reportParms) {
        i_reports_obj.reports()[0].reportParms.Category(i_reports_obj.reports()[0].appliedFilters.Category());
        i_reports_obj.reports()[0].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[0].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[0].reportParms.IntentType.push(i_reports_obj.reports()[0].appliedFilters.IntentType()[index]);
        }
    }

    loadReport(1, false, getReportSize(getReportBodyDivID(1), false, isReportFullSize(1), 1));
}

// Card2.
function applyCard2Settings() {
    $(".card-2-report").css("display", "block");
    $(".card-2-settings").css("display", "none");
    if ($('#card2').hasClass('legend-card') && !isReportFullSize(2)) {
        showReportSummary(2);
    }
    if (i_reports_obj.reports()[1] && i_reports_obj.reports()[1].reportParms) {
        i_reports_obj.reports()[1].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[1].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[1].reportParms.IntentType.push(i_reports_obj.reports()[1].appliedFilters.IntentType()[index]);
        }
        i_reports_obj.reports()[1].reportParms.ContractTypes.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[1].appliedFilters.ContractTypes().length; index++) {
            i_reports_obj.reports()[1].reportParms.ContractTypes.push(i_reports_obj.reports()[1].appliedFilters.ContractTypes()[index]);
        }
        i_reports_obj.reports()[1].reportParms.Regions.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[1].appliedFilters.Regions().length; index++) {
            i_reports_obj.reports()[1].reportParms.Regions.push(i_reports_obj.reports()[1].appliedFilters.Regions()[index]);
        }
        i_reports_obj.reports()[1].reportParms.ValueRanges.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[1].appliedFilters.ValueRanges().length; index++) {
            i_reports_obj.reports()[1].reportParms.ValueRanges.push(i_reports_obj.reports()[1].appliedFilters.ValueRanges()[index]);
        }
        i_reports_obj.reports()[1].reportParms.IncludeTerminations = i_reports_obj.reports()[1].appliedFilters.IncludeTerminations();
        i_reports_obj.reports()[1].reportParms.IncludeAutorenewals = i_reports_obj.reports()[1].appliedFilters.IncludeAutorenewals();

    }
    loadReport(2, false, getReportSize(getReportBodyDivID(2), false, isReportFullSize(2)));
}

// Card3.
function applyCard3Settings() {
    $(".card-3-report").css("display", "block");
    $(".card-3-settings").css("display", "none");
    if ($('#card3').hasClass('legend-card') && !isReportFullSize(3)) {
        showReportSummary(3);
    }
    if (i_reports_obj.reports()[2] && i_reports_obj.reports()[2].reportParms) {
        i_reports_obj.reports()[2].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[2].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[2].reportParms.IntentType.push(i_reports_obj.reports()[2].appliedFilters.IntentType()[index]);
        }
        i_reports_obj.reports()[2].reportParms.ContractTypes.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[2].appliedFilters.ContractTypes().length; index++) {
            i_reports_obj.reports()[2].reportParms.ContractTypes.push(i_reports_obj.reports()[2].appliedFilters.ContractTypes()[index]);
        }
        i_reports_obj.reports()[2].reportParms.Regions.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[2].appliedFilters.Regions().length; index++) {
            i_reports_obj.reports()[2].reportParms.Regions.push(i_reports_obj.reports()[2].appliedFilters.Regions()[index]);
        }
        i_reports_obj.reports()[2].reportParms.ValueRanges.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[2].appliedFilters.ValueRanges().length; index++) {
            i_reports_obj.reports()[2].reportParms.ValueRanges.push(i_reports_obj.reports()[2].appliedFilters.ValueRanges()[index]);
        }
        i_reports_obj.reports()[2].reportParms.includeActive = i_reports_obj.reports()[2].appliedFilters.IncludeActive();

    }
    loadReport(3, false, getReportSize(getReportBodyDivID(3), false, isReportFullSize(3)));
}

// Card4.
function applyCard4Settings() {
    $(".card-4-report").css("display", "block");
    $(".card-4-settings").css("display", "none");
    if ($('#card4').hasClass('legend-card') && !isReportFullSize(4)) {
        showReportSummary(4);
    }
    if (i_reports_obj.reports()[3] && i_reports_obj.reports()[3].reportParms) {
        i_reports_obj.reports()[3].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[3].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[3].reportParms.IntentType.push(i_reports_obj.reports()[3].appliedFilters.IntentType()[index]);
        }
        i_reports_obj.reports()[3].reportParms.ContractTypes.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[3].appliedFilters.ContractTypes().length; index++) {
            i_reports_obj.reports()[3].reportParms.ContractTypes.push(i_reports_obj.reports()[3].appliedFilters.ContractTypes()[index]);
        }
        i_reports_obj.reports()[3].reportParms.Regions.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[3].appliedFilters.Regions().length; index++) {
            i_reports_obj.reports()[3].reportParms.Regions.push(i_reports_obj.reports()[3].appliedFilters.Regions()[index]);
        }
        i_reports_obj.reports()[3].reportParms.ValueRanges.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[3].appliedFilters.ValueRanges().length; index++) {
            i_reports_obj.reports()[3].reportParms.ValueRanges.push(i_reports_obj.reports()[3].appliedFilters.ValueRanges()[index]);
        }
        i_reports_obj.reports()[3].reportParms.IncludeAutorenewals = i_reports_obj.reports()[3].appliedFilters.IncludeAutorenewals();
    }
    loadReport(4, false, getReportSize(getReportBodyDivID(4), false, isReportFullSize(4)));
}

// Card5.
function applyCard5Settings() {
    $(".card-5-report").css("display", "block");
    $(".card-5-settings").css("display", "none");
    if ($('#card5').hasClass('legend-card') && !isReportFullSize(5)) {
        showReportSummary(5);
    }
    if (i_reports_obj.reports()[4] && i_reports_obj.reports()[4].reportParms) {
        i_reports_obj.reports()[4].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[4].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[4].reportParms.IntentType.push(i_reports_obj.reports()[4].appliedFilters.IntentType()[index]);
        }
        i_reports_obj.reports()[4].reportParms.ContractTypes.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[4].appliedFilters.ContractTypes().length; index++) {
            i_reports_obj.reports()[4].reportParms.ContractTypes.push(i_reports_obj.reports()[4].appliedFilters.ContractTypes()[index]);
        }
        i_reports_obj.reports()[4].reportParms.Regions.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[4].appliedFilters.Regions().length; index++) {
            i_reports_obj.reports()[4].reportParms.Regions.push(i_reports_obj.reports()[4].appliedFilters.Regions()[index]);
        }
        i_reports_obj.reports()[4].reportParms.ValueRanges.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[4].appliedFilters.ValueRanges().length; index++) {
            i_reports_obj.reports()[4].reportParms.ValueRanges.push(i_reports_obj.reports()[4].appliedFilters.ValueRanges()[index]);
        }
        i_reports_obj.reports()[4].reportParms.IncludeAutorenewals = i_reports_obj.reports()[4].appliedFilters.IncludeAutorenewals();
    }
    loadReport(5, false, getReportSize(getReportBodyDivID(5), false, isReportFullSize(5)));
}

// Card6.
function applyCard6Settings() {
    $(".card-6-report").css("display", "block");
    $(".card-6-settings").css("display", "none");
    if ($('#card6').hasClass('legend-card') && !isReportFullSize(6)) {
        showReportSummary(6);
    }
    if (i_reports_obj.reports()[5] && i_reports_obj.reports()[5].reportParms) {
        i_reports_obj.reports()[5].reportParms.IntentType.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[5].appliedFilters.IntentType().length; index++) {
            i_reports_obj.reports()[5].reportParms.IntentType.push(i_reports_obj.reports()[5].appliedFilters.IntentType()[index]);
        }
        i_reports_obj.reports()[5].reportParms.ContractTypes.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[5].appliedFilters.ContractTypes().length; index++) {
            i_reports_obj.reports()[5].reportParms.ContractTypes.push(i_reports_obj.reports()[5].appliedFilters.ContractTypes()[index]);
        }
        i_reports_obj.reports()[5].reportParms.Regions.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[5].appliedFilters.Regions().length; index++) {
            i_reports_obj.reports()[5].reportParms.Regions.push(i_reports_obj.reports()[5].appliedFilters.Regions()[index]);
        }
        i_reports_obj.reports()[5].reportParms.ValueRanges.removeAll();
        for (var index = 0; index < i_reports_obj.reports()[5].appliedFilters.ValueRanges().length; index++) {
            i_reports_obj.reports()[5].reportParms.ValueRanges.push(i_reports_obj.reports()[5].appliedFilters.ValueRanges()[index]);
        }
        i_reports_obj.reports()[5].reportParms.DataType = i_reports_obj.reports()[5].appliedFilters.DataType();
    }
    loadReport(6, false, getReportSize(getReportBodyDivID(6), false, isReportFullSize(6)));
}


// Card1.
function cancelCard1Settings() {
    resetParameters1();
    $(".card-1-report").css("display", "block");
    $(".card-1-settings").css("display", "none");
    if ($('#card1').hasClass('legend-card') && !isReportFullSize(1)) {
        showReportSummary(1);
    }
}


// Card2.
function cancelCard2Settings() {
    resetParameters2();
    $(".card-2-report").css("display", "block");
    $(".card-2-settings").css("display", "none");
    if ($('#card2').hasClass('legend-card') && !isReportFullSize(2)) {
        showReportSummary(2);
    }

}

// Card3.
function cancelCard3Settings() {
    resetParameters3();
    $(".card-3-report").css("display", "block");
    $(".card-3-settings").css("display", "none");
    if ($('#card3').hasClass('legend-card') && !isReportFullSize(3)) {
        showReportSummary(3);
    }
}

// Card4.
function cancelCard4Settings() {
    resetParameters4();
    $(".card-4-report").css("display", "block");
    $(".card-4-settings").css("display", "none");
    if ($('#card4').hasClass('legend-card') && !isReportFullSize(4)) {
        showReportSummary(4);
    }

}

// Card5.
function cancelCard5Settings() {
    resetParameters5();
    $(".card-5-report").css("display", "block");
    $(".card-5-settings").css("display", "none");
    if ($('#card5').hasClass('legend-card') && !isReportFullSize(5)) {
        showReportSummary(5);
    }

}

// Card6.
function cancelCard6Settings() {
    resetParameters6();
    $(".card-6-report").css("display", "block");
    $(".card-6-settings").css("display", "none");
    if ($('#card6').hasClass('legend-card') && !isReportFullSize(6)) {
        showReportSummary(6);
    }

}

/** For resetting the filters on cancel. */

function resetParameters1() {
    i_reports_obj.reports()[0].appliedFilters.Category(i_reports_obj.reports()[0].reportParms.Category());
    if (i_reports_obj.reports()[0].reportParms.IntentType()) {
        i_reports_obj.reports()[0].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[0].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[0].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[0].reportParms.IntentType()[i]));
        }
    }
}

function resetParameters2() {
    if (i_reports_obj.reports()[1].reportParms.IntentType()) {
        i_reports_obj.reports()[1].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[1].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[1].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[1].reportParms.IntentType()[i]));
        }
    }
    if (i_reports_obj.reports()[1].reportParms.ContractTypes()) {
        i_reports_obj.reports()[1].appliedFilters.ContractTypes.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[1].reportParms.ContractTypes().length; i++) {
            i_reports_obj.reports()[1].appliedFilters.ContractTypes.push(
                i_reports_obj.reports()[1].reportParms.ContractTypes()[i]);
        }
    }
    if (i_reports_obj.reports()[1].reportParms.Regions()) {
        i_reports_obj.reports()[1].appliedFilters.Regions.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[1].reportParms.Regions().length; i++) {
            i_reports_obj.reports()[1].appliedFilters.Regions.push(
                i_reports_obj.reports()[1].reportParms.Regions()[i]);
        }
    }
    if (i_reports_obj.reports()[1].reportParms.ValueRanges()) {
        i_reports_obj.reports()[1].appliedFilters.ValueRanges.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[1].reportParms.ValueRanges().length; i++) {
            i_reports_obj.reports()[1].appliedFilters.ValueRanges.push(
                i_reports_obj.reports()[1].reportParms.ValueRanges()[i]);
        }
    }
    i_reports_obj.reports()[1].appliedFilters.IncludeTerminations(i_reports_obj.reports()[1].reportParms.IncludeTerminations);
    i_reports_obj.reports()[1].appliedFilters.IncludeAutorenewals(i_reports_obj.reports()[1].reportParms.IncludeAutorenewals);
}

function resetParameters3() {
    if (i_reports_obj.reports()[2].reportParms.IntentType()) {
        i_reports_obj.reports()[2].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[2].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[2].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[2].reportParms.IntentType()[i]));
        }
    }
    if (i_reports_obj.reports()[2].reportParms.ContractTypes()) {
        i_reports_obj.reports()[2].appliedFilters.ContractTypes.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[2].reportParms.ContractTypes().length; i++) {
            i_reports_obj.reports()[2].appliedFilters.ContractTypes.push(
                i_reports_obj.reports()[2].reportParms.ContractTypes()[i]);
        }
    }
    if (i_reports_obj.reports()[2].reportParms.Regions()) {
        i_reports_obj.reports()[2].appliedFilters.Regions.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[2].reportParms.Regions().length; i++) {
            i_reports_obj.reports()[2].appliedFilters.Regions.push(
                i_reports_obj.reports()[2].reportParms.Regions()[i]);
        }
    }
    if (i_reports_obj.reports()[2].reportParms.ValueRanges()) {
        i_reports_obj.reports()[2].appliedFilters.ValueRanges.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[2].reportParms.ValueRanges().length; i++) {
            i_reports_obj.reports()[2].appliedFilters.ValueRanges.push(
                i_reports_obj.reports()[2].reportParms.ValueRanges()[i]);
        }
    }
    i_reports_obj.reports()[2].appliedFilters.IncludeActive(i_reports_obj.reports()[2].reportParms.includeActive);
}

function resetParameters4() {
    if (i_reports_obj.reports()[3].reportParms.IntentType()) {
        i_reports_obj.reports()[3].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[3].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[3].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[3].reportParms.IntentType()[i]));
        }
    }
    if (i_reports_obj.reports()[3].reportParms.ContractTypes()) {
        i_reports_obj.reports()[3].appliedFilters.ContractTypes.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[3].reportParms.ContractTypes().length; i++) {
            i_reports_obj.reports()[3].appliedFilters.ContractTypes.push(
                i_reports_obj.reports()[3].reportParms.ContractTypes()[i]);
        }
    }
    if (i_reports_obj.reports()[3].reportParms.Regions()) {
        i_reports_obj.reports()[3].appliedFilters.Regions.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[3].reportParms.Regions().length; i++) {
            i_reports_obj.reports()[3].appliedFilters.Regions.push(
                i_reports_obj.reports()[3].reportParms.Regions()[i]);
        }
    }
    if (i_reports_obj.reports()[3].reportParms.ValueRanges()) {
        i_reports_obj.reports()[3].appliedFilters.ValueRanges.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[3].reportParms.ValueRanges().length; i++) {
            i_reports_obj.reports()[3].appliedFilters.ValueRanges.push(
                i_reports_obj.reports()[3].reportParms.ValueRanges()[i]);
        }
    }
    i_reports_obj.reports()[3].appliedFilters.IncludeAutorenewals(i_reports_obj.reports()[3].reportParms.IncludeAutorenewals);
}

function resetParameters5() {
    if (i_reports_obj.reports()[4].reportParms.IntentType()) {
        i_reports_obj.reports()[4].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[4].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[4].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[4].reportParms.IntentType()[i]));
        }
    }
    if (i_reports_obj.reports()[4].reportParms.ContractTypes()) {
        i_reports_obj.reports()[4].appliedFilters.ContractTypes.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[4].reportParms.ContractTypes().length; i++) {
            i_reports_obj.reports()[4].appliedFilters.ContractTypes.push(
                i_reports_obj.reports()[4].reportParms.ContractTypes()[i]);
        }
    }
    if (i_reports_obj.reports()[4].reportParms.Regions()) {
        i_reports_obj.reports()[4].appliedFilters.Regions.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[4].reportParms.Regions().length; i++) {
            i_reports_obj.reports()[4].appliedFilters.Regions.push(
                i_reports_obj.reports()[4].reportParms.Regions()[i]);
        }
    }
    if (i_reports_obj.reports()[4].reportParms.ValueRanges()) {
        i_reports_obj.reports()[4].appliedFilters.ValueRanges.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[4].reportParms.ValueRanges().length; i++) {
            i_reports_obj.reports()[4].appliedFilters.ValueRanges.push(
                i_reports_obj.reports()[4].reportParms.ValueRanges()[i]);
        }
    }
    i_reports_obj.reports()[4].appliedFilters.IncludeAutorenewals(i_reports_obj.reports()[4].reportParms.IncludeAutorenewals);
}

function resetParameters6() {
    if (i_reports_obj.reports()[5].reportParms.IntentType()) {
        i_reports_obj.reports()[5].appliedFilters.IntentType.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[5].reportParms.IntentType().length; i++) {
            i_reports_obj.reports()[5].appliedFilters.IntentType.push(
                String(i_reports_obj.reports()[5].reportParms.IntentType()[i]));
        }
    }
    if (i_reports_obj.reports()[5].reportParms.ContractTypes()) {
        i_reports_obj.reports()[5].appliedFilters.ContractTypes.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[5].reportParms.ContractTypes().length; i++) {
            i_reports_obj.reports()[5].appliedFilters.ContractTypes.push(
                i_reports_obj.reports()[5].reportParms.ContractTypes()[i]);
        }
    }
    if (i_reports_obj.reports()[5].reportParms.Regions()) {
        i_reports_obj.reports()[5].appliedFilters.Regions.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[5].reportParms.Regions().length; i++) {
            i_reports_obj.reports()[5].appliedFilters.Regions.push(
                i_reports_obj.reports()[5].reportParms.Regions()[i]);
        }
    }
    if (i_reports_obj.reports()[5].reportParms.ValueRanges()) {
        i_reports_obj.reports()[5].appliedFilters.ValueRanges.removeAll();
        for (var i = 0; i < i_reports_obj.reports()[5].reportParms.ValueRanges().length; i++) {
            i_reports_obj.reports()[5].appliedFilters.ValueRanges.push(
                i_reports_obj.reports()[5].reportParms.ValueRanges()[i]);
        }
    }
    i_reports_obj.reports()[5].appliedFilters.DataType(i_reports_obj.reports()[5].reportParms.DataType);

}

/** For opening models. */
// Contract types model.
function openContractTypes(cardNumber) {
    loadJqueryIfRequired();
    $("#id_contractTypesDialog").modal();
    l_contractTypes_model.cardIndex = cardNumber - 1;
    l_contractTypes_model.ContractTypesSelectedList.removeAll();
    for (var i = 0; i < i_reports_obj.reports()[cardNumber - 1].appliedFilters.ContractTypes().length; i++) {
        l_contractTypes_model.ContractTypesSelectedList.push(
            i_reports_obj.reports()[cardNumber - 1].appliedFilters.ContractTypes()[i]);
    }
    loadContractTypes();
}

// Regions model.
function openRegions(cardNumber) {
    loadJqueryIfRequired();
    $("#id_regionsDialog").modal();
    l_regions_model.cardIndex = cardNumber - 1;
    l_regions_model.RegionsSelectedList.removeAll();
    for (var i = 0; i < i_reports_obj.reports()[cardNumber - 1].appliedFilters.Regions().length; i++) {
        l_regions_model.RegionsSelectedList.push(
            i_reports_obj.reports()[cardNumber - 1].appliedFilters.Regions()[i]);
    }
    loadRegions();
}

// Contract value ranges model.
function openContractValueRanges(cardNumber) {
    loadJqueryIfRequired();
    $("#id_contractValueRangesDialog").modal();
    l_contractValueRanges_model.cardIndex = cardNumber - 1;
    l_contractValueRanges_model.ContractValueRangeSelectedList.removeAll();
    for (var i = 0; i < i_reports_obj.reports()[cardNumber - 1].appliedFilters.ValueRanges().length; i++) {
        l_contractValueRanges_model.ContractValueRangeSelectedList.push(
            i_reports_obj.reports()[cardNumber - 1].appliedFilters.ValueRanges()[i]);
    }
    loadContractValueRanges();
}

function loadJqueryIfRequired() {
    if (IEdetection() && $().jquery !== "2.1.4") {
        var tag = document.createElement("script");
        tag.src = "/cordys/thirdparty/jquery/jquery.js";
        document.getElementsByTagName("head")[0].appendChild(tag);
        var tag = document.createElement("script");
        tag.src = "/cordys/thirdparty/bootstrap/js/bootstrap.min.js";
        document.getElementsByTagName("head")[0].appendChild(tag);
        var tag = document.createElement("script");
        tag.src = "/cordys/html5/cordys.html5sdk.js";
        document.getElementsByTagName("head")[0].appendChild(tag);
    }
}

/** Update filters with selected values from model view.*/
// Update contract types.
function updateContractTypeFilters(cardIndex, selectedTypes) {
    i_reports_obj.reports()[cardIndex].appliedFilters.ContractTypes.removeAll();
    if (selectedTypes) {
        for (var i = 0; i < selectedTypes.length; i++) {
            i_reports_obj.reports()[cardIndex].appliedFilters.ContractTypes.push(selectedTypes[i]);
        }
    }
}

// Update regions.
function updateRegionFilters(cardIndex, selectedRegions) {
    i_reports_obj.reports()[cardIndex].appliedFilters.Regions.removeAll();
    if (selectedRegions) {
        for (var i = 0; i < selectedRegions.length; i++) {
            i_reports_obj.reports()[cardIndex].appliedFilters.Regions.push(selectedRegions[i]);
        }
    }
}

//Update value ranges.
function updateValueRangeFilters(cardIndex, selectedValueRanges) {
    i_reports_obj.reports()[cardIndex].appliedFilters.ValueRanges.removeAll();
    if (selectedValueRanges) {
        for (var i = 0; i < selectedValueRanges.length; i++) {
            i_reports_obj.reports()[cardIndex].appliedFilters.ValueRanges.push(selectedValueRanges[i]);
        }
    }
}

// Return false is not exists. Return element if exists.
function existsInArray(array, id) {
    for (var _i = 0; _i < array.length; _i++) {
        if (array[_i].id == id) {
            return array[_i];
        }
    }
    return false;
}
// Get Ids from objs.
function getIdAsArray(array) {
    var result = [];
    if (array && array.length && array.length > 0) {
        for (var i = 0; i < array.length; i++) {
            result.push(array[i].id);
        }
    }
    return result;
}


//** Loading the model of model-views.*/
// Contract types list in model.
function addDataToContractTypesView(iElementList, iModel) {
    iModel.ContractTypesList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            for (var i = 0; i < iElementList.length; i++) {
                var iElement = iElementList[i];
                var id = iElement['GCType-id'].Id;
                var foundElement = existsInArray(l_contractTypes_model.ContractTypesSelectedList(), id);
                if (foundElement) {
                    iModel.ContractTypesList.push(foundElement);
                } else {
                    iElement.id = id;
                    iElement.displayName = iElement.Name;
                    iModel.ContractTypesList.push(iElement);
                }
            }
        }
        else {
            var id = iElementList['GCType-id'].Id;
            var foundElement = existsInArray(l_contractTypes_model.ContractTypesSelectedList(), id);
            if (foundElement) {
                iModel.ContractTypesList.push(foundElement);
            } else {
                iElementList.id = id;
                iElementList.displayName = iElementList.Name;
                iModel.ContractTypesList.push(iElementList);
            }
        }
    }
}

// Regions list in model.
function addDataToRegionsView(iElementList, iModel) {
    iModel.RegionsList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            for (var i = 0; i < iElementList.length; i++) {
                var iElement = iElementList[i];
                var id = iElement['Region-id'].Id;
                var foundElement = existsInArray(l_regions_model.RegionsSelectedList(), id);
                if (foundElement) {
                    iModel.RegionsList.push(foundElement);
                } else {
                    iElement.id = id;
                    iElement.displayName = iElement.Name;
                    iModel.RegionsList.push(iElement);
                }
            }
        }
        else {
            var id = iElementList['Region-id'].Id;
            var foundElement = existsInArray(l_regions_model.RegionsSelectedList(), id);
            if (foundElement) {
                iModel.RegionsList.push(foundElement);
            } else {
                iElementList.id = id;
                iElementList.displayName = iElement.Name;
                iModel.RegionsList.push(iElementList);
            }

        }
    }
}

// Valule ranges list in model.
function addDataToContractValueRangesView(iElementList, iModel) {
    iModel.ContractValueRangesList.removeAll();
    if (iElementList) {
        if (iElementList.length) {
            for (var i = 0; i < iElementList.length; i++) {
                var iElement = iElementList[i];
                var id = iElement['GCValueRange-id'].Id;
                var foundElement = existsInArray(l_contractValueRanges_model.ContractValueRangeSelectedList(), id);
                if (foundElement) {
                    iModel.ContractValueRangesList.push(foundElement);
                } else {
                    iElement.id = id;
                    iElement.displayName = iElement.Name;
                    iModel.ContractValueRangesList.push(iElement);
                }
            }
        }
        else {
            var id = iElementList['GCValueRange-id'].Id;
            var foundElement = existsInArray(l_contractValueRanges_model.ContractValueRangeSelectedList(), id);
            if (foundElement) {
                iModel.ContractValueRangesList.push(foundElement);
            } else {
                iElementList.id = id;
                iElementList.displayName = iElementList.Name;
                iModel.ContractValueRangesList.push(iElementList);
            }
        }
    }
}

// Remove filter from selected list.
function removeFilterFromList(cardNumber, listName, item) {
    if (i_reports_obj.reports()[cardNumber] && i_reports_obj.reports()[cardNumber].appliedFilters &&
        i_reports_obj.reports()[cardNumber][listName]) {
        i_reports_obj.reports()[cardNumber][listName].remove(item);
    }
}

// Drag and drop of cards.

function dragCard(ev, cardNumber) {
    //ev.dataTransfer.setData("cardid", ev.target.id);
    //ev.dataTransfer.setData("cardNumber", cardNumber);
    ev.dataTransfer.setData("text", '' + ev.target.id + '$$$' + cardNumber);
}

function dropCard(ev, targetCardNumber) {
    ev.preventDefault();
    var targetCardId = 'card' + targetCardNumber; //ev.target.id;
    var parmaValues = ev.dataTransfer.getData("text").split('$$$')
    var draggedCardId = parmaValues[0];
    var draggedCardNumber = parmaValues[1];
    var targetCardNode = $('#' + targetCardId);
    var draggedCardNode = $('#' + draggedCardId);
    if (targetCardNumber != draggedCardNumber &&
        (targetCardNode.hasClass('legend-card') || draggedCardNode.hasClass('legend-card'))) {
        var targetParentNode = targetCardNode.parent();
        var draggedCardParentNode = draggedCardNode.parent();
        var reportSizes = getReportsSizeForSwapTiles(draggedCardNumber, targetCardNumber);

        // Calender orientation change if required.

        var tempVar = calenderOrientaion[targetCardNumber - 1];
        calenderOrientaion[targetCardNumber - 1] = calenderOrientaion[draggedCardNumber - 1];
        calenderOrientaion[draggedCardNumber - 1] = tempVar;
        if ($('#reportrange' + draggedCardNumber).data('daterangepicker') && $('#reportrange' + draggedCardNumber).data('daterangepicker').drops) {
            $('#reportrange' + draggedCardNumber).data('daterangepicker').drops = calenderOrientaion[draggedCardNumber - 1];
        }
        if ($('#reportrange' + targetCardNumber).data('daterangepicker') && $('#reportrange' + targetCardNumber).data('daterangepicker').drops) {
            $('#reportrange' + targetCardNumber).data('daterangepicker').drops = calenderOrientaion[targetCardNumber - 1];
        }


        if (targetCardNode.hasClass('legend-card')) {
            targetCardNode.removeClass('legend-card');
            draggedCardNode.addClass('legend-card');
        } else if (draggedCardNode.hasClass('legend-card')) {
            draggedCardNode.removeClass('legend-card');
            targetCardNode.addClass('legend-card');
        }

        if (targetCardNode.hasClass('bottom-full')) {
            targetCardNode.removeClass('bottom-full');
            draggedCardNode.addClass('bottom-full');
        } else if (draggedCardNode.hasClass('bottom-full')) {
            draggedCardNode.removeClass('bottom-full');
            targetCardNode.addClass('bottom-full');
        }

        if (draggedCardNode.hasClass('legend-card')) {
            hideReportSummary(targetCardNumber);
            showReportSummary(draggedCardNumber);
        } else if (targetCardNode.hasClass('legend-card')) {
            showReportSummary(targetCardNumber);
            hideReportSummary(draggedCardNumber);
        }
        draggedCardParentNode.append(targetCardNode);
        targetParentNode.append(draggedCardNode);

        loadReport(draggedCardNumber, false, reportSizes[1]);
        loadReport(targetCardNumber, false, reportSizes[0]);
    }
}

function allowDrop(ev) {
    ev.preventDefault();
}

// 
var formatCurrency = function (amount) {
    if (!amount || isNaN(amount)) {
        amount = 0;
    }
    var nf = new Intl.NumberFormat('en-US', { minimumFractionDigits: 0 });
    return nf.format(amount);
}

function currencyRounding(num, locale) {
    if (!locale) {
        locale = 'en';
    }
    // Nine Zeroes for Billions
    return formatCurrency(Math.round(Math.abs(Number(num)) / 1.0e+6)) + " M";
}

function hideReportSummary(reportNumber) {
    $("#card" + reportNumber + "ReportSummary").css("display", "none");
}

function showReportSummary(reportNumber) {
    $("#card" + reportNumber + "ReportSummary").css("display", "block");
}

function IEdetection() {
    var ua = window.navigator.userAgent;
    var msie = ua.indexOf('MSIE ');
    if (msie > 0) {
        // IE 10 or older, return version number 
        return true;
    }
    var trident = ua.indexOf('Trident/');
    if (trident > 0) {
        // IE 11, return version number
        return true;
    }
    var edge = ua.indexOf('Edge/');
    if (edge > 0) {
        //Edge (IE 12+), return version number 
        return true;
    }
    // User uses other browser 
    return false;
} 