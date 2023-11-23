// Load all data range pickers.
function loadCalenders() {
    for (var i = 1; i < 6; i++) {


        // Get templace for the card.
        var template = getDateRangePickertemplate(i);

        // Date range instance storage in a global variable.
        var dateRange = new DateRange();
        dateRange.id = i;

        //binding daterange picker.
        dateRange.obj = $('#reportrange' + i).daterangepicker(template, updateDivAndReportParams);
        $('#reportrange' + i).on('show.daterangepicker', function (ev, picker) {
            var divId = this.id;
            var reportNumber = divId.substring(divId.indexOf('reportrange') + 'reportrange'.length);
            $('#card' + reportNumber + ' div.cc-cardReport-header').addClass('visible');
            $('#card' + reportNumber + ' div.cc-cardReport-footer').addClass('visible');
        });
        $('#reportrange' + i).on('hide.daterangepicker', function (ev, picker) {
            var divId = this.id;
            var reportNumber = divId.substring(divId.indexOf('reportrange') + 'reportrange'.length);
            $('#card' + reportNumber + ' div.cc-cardReport-header').removeClass('visible');
            $('#card' + reportNumber + ' div.cc-cardReport-footer').removeClass('visible');
        });


        dateRanges.push(dateRange);

        // Update the div and report params with default values.
        if (i_reports_obj.reports()[i - 1] && i_reports_obj.reports()[i - 1].reportParms &&
            i_reports_obj.reports()[i - 1].reportParms.FromDate && i_reports_obj.reports()[i - 1].reportParms.FromDate) {
            $('#reportrange' + i + ' span').html(i_reports_obj.reports()[i - 1].reportParms.FromDate +
                ' - ' + i_reports_obj.reports()[i - 1].reportParms.ToDate);
        }

        // Append custom year drop-down.
        appendYearDropDown(i);

    }
    loadCard6YearDropDowns();
}

// Update div and report params on selection of range.
function updateDivAndReportParams(start, end, label) {
    if (this.element[0] && this.element[0].id) {
        var divId = this.element[0].id;
        var reportNumber = divId.substring(divId.indexOf('reportrange') + 'reportrange'.length);
        if (label == 'Custom Range') {
            i_reports_obj.reports()[reportNumber - 1].reportParms.Duration = "DateRange";
        } else {
            i_reports_obj.reports()[reportNumber - 1].reportParms.Duration = "Period";
            i_reports_obj.reports()[reportNumber - 1].reportParms.Quarter = label;
        }
        if (IEdetection() && label !== 'Custom Range') {
            i_reports_obj.reports()[reportNumber - 1].reportParms.Year = dateRanges[reportNumber-1].selectedYear;
            i_reports_obj.reports()[reportNumber - 1].reportParms.FromDate = dateRanges[reportNumber-1].obj.data().ranges[label][0];
            i_reports_obj.reports()[reportNumber - 1].reportParms.ToDate = dateRanges[reportNumber-1].obj.data().ranges[label][1];
            start = i_reports_obj.reports()[reportNumber - 1].reportParms.FromDate;
            end = dateRanges[reportNumber-1].obj.data().ranges[label][1];
            this.setStartDate(start);
            this.setEndDate(end);
        } else {
            i_reports_obj.reports()[reportNumber - 1].reportParms.Year = start.year();
            i_reports_obj.reports()[reportNumber - 1].reportParms.FromDate = start.format(DATE_FROMAT_UI);
            i_reports_obj.reports()[reportNumber - 1].reportParms.ToDate = end.format(DATE_FROMAT_UI);
        }

        $('#' + divId + ' span').html(start.format(DATE_FROMAT_UI) + ' - ' + end.format(DATE_FROMAT_UI));
        loadReport(reportNumber, false, getReportSize(getReportBodyDivID(reportNumber), false, isReportFullSize(reportNumber), reportNumber));
    }
}

//Append year drop-down for the report.
function appendYearDropDown(cardNumber) {
    var currentYear = moment().year();
    var startYear = 0, endYear = 0;
    if (cardNumber == 2) {
        startYear = currentYear;
        endYear = currentYear + 2;
    } else {
        startYear = currentYear - NUMBER_OF_YEARS;
        endYear = currentYear;
    }
    var yearDropDown2 = '<select class="form-control" id="yearselection-dropdown' + cardNumber + '"><option>Year</option>';
    for (var j = startYear; j < currentYear; j++) {
        yearDropDown2 += '<option value="' + j + '">' + j + '</option>';
    }
    yearDropDown2 += '<option selected=selected value="' + currentYear + '">' + currentYear + '</option>';
    for (var j = currentYear + 1; j < endYear; j++) {
        yearDropDown2 += '<option value="' + j + '">' + j + '</option>';
    }

    yearDropDown2 += '</select>';
    $('#calenderdropdown' + cardNumber).prepend(yearDropDown2);
    $('#yearselection-dropdown' + cardNumber).on('change', function () {
        var idName = this.getAttribute('id');
        var rangeNumber = idName.substr(idName.indexOf("yearselection-dropdown") + "yearselection-dropdown".length);
        if (IEdetection()) {
            dateRanges[rangeNumber-1].selectedYear = this.value;
            updateRangeObject(this.value, dateRanges[rangeNumber-1].obj.data().ranges);
        } else {
            updateRangeObject(this.value, $('#reportrange' + rangeNumber).data('daterangepicker').ranges);
        }

    });
}

// Get custom tamplate for data range picker.
function getDateRangePickertemplate(reportNumber) {
    var template = {
        "showDropdowns": "false",
        "opens": "center",
        ranges: formRangeObject(),
        "alwaysShowCalendars": "true",
        "startDate": i_reports_obj.reports()[reportNumber - 1].reportParms.FromDate,
        "endDate": i_reports_obj.reports()[reportNumber - 1].reportParms.ToDate,
        "drops": calenderOrientaion[reportNumber - 1],
        "template":
            '<div class="daterangepicker">' +
            '<div class="drp-buttons">' +
            '<span class="drp-selected custom-drp-selected"></span>' +
            '<button class="btn btn-lg btn-success btn-link applyBtn" disabled="disabled" type="button"></button>' +
            '<button class="btn btn-lg btn-danger btn-link cancelBtn" type="button"></button>' +
            '</div>' +
            '<div class="ranges" id="calenderdropdown' + reportNumber + '">' +
            '</div>'
            + '<div class="drp-calendar left">' + '<div class="calendar-table"></div>'
            + '<div class="calendar-time"></div>' + '</div>' + '<div class="drp-calendar right">'
            + '<div class="calendar-table"></div>' + '<div class="calendar-time"></div>' + '</div>' + '</div>',
        "locale": {
            "format": DATE_FROMAT_UI,
            "separator": " - ",
            "applyLabel": '<span class="ok-settings"></span>',
            "cancelLabel": '<span class="cancel-settings"></span>'
        }
    }
    if (reportNumber == 2) {
        template['minDate'] = moment();
        template['maxDate'] = moment().year(moment().year() + 1).endOf('year');
    } else {
        template['maxDate'] = moment();
    }
    if (!(reportNumber == 1 || reportNumber == 3)) {
        template.maxSpan = { 'Years': '1' };
    } else {
        template.maxSpan = { 'Years': '20' };
    }
    return template;
}

// Forming a ranges object used in template.
function formRangeObject(year) {
    if (!year)
        year = moment().year();
    var obj = {};
    obj[FILTER_ALL_QUARTER.label] = [moment().year(year).startOf('year'), moment().year(year).endOf('year')];
    obj[FILTER_FIRST_QUARTER.label] = [moment().year(year).month(0).startOf('month'), moment().year(year).month(2).endOf('month')];
    obj[FILTER_SECOND_QUARTER.label] = [moment().year(year).month(3).startOf('month'), moment().year(year).month(5).endOf('month')];
    obj[FILTER_THIRD_QUARTER.label] = [moment().year(year).month(6).startOf('month'), moment().year(year).month(8).endOf('month')];
    obj[FILTER_FOURTH_QUARTER.label] = [moment().year(year).month(9).startOf('month'), moment().year(year).month(11).endOf('month')];
    return obj;
}

// Update the custom date ranges based on the year.
function updateRangeObject(year, ranges) {
    if (!year)
        year = moment().year();
    ranges[FILTER_ALL_QUARTER.label] = [moment().year(year).startOf('year'), moment().year(year).endOf('year')];
    ranges[FILTER_FIRST_QUARTER.label] = [moment().year(year).month(0).startOf('month'), moment().year(year).month(2).endOf('month')];
    ranges[FILTER_SECOND_QUARTER.label] = [moment().year(year).month(3).startOf('month'), moment().year(year).month(5).endOf('month')];
    ranges[FILTER_THIRD_QUARTER.label] = [moment().year(year).month(6).startOf('month'), moment().year(year).month(8).endOf('month')];
    ranges[FILTER_FOURTH_QUARTER.label] = [moment().year(year).month(9).startOf('month'), moment().year(year).month(11).endOf('month')];
}

// Laoding card6 calender years.
function loadCard6YearDropDowns() {
    var currentYear = moment().year();

    for (var j = currentYear - NUMBER_OF_YEARS; j <= currentYear; j++) {
        $('#card6year1').append($("<option></option>").attr("value", j).text(j));
        $('#card6year2').append($("<option></option>").attr("value", j).text(j));
    }

    $('#card6year1').on('change', function () {               
        i_reports_obj.reports()[5].reportParms.FirstYear(this.value);
        loadReport(6, false, getReportSize(getReportBodyDivID(6), false, isReportFullSize(6), 6));
    });
    $('#card6year2').on('change', function () {
               
        i_reports_obj.reports()[5].reportParms.SecondYear(this.value);
        loadReport(6, false, getReportSize(getReportBodyDivID(6), false, isReportFullSize(6), 6));
    });

    $("#card6year1").val(i_reports_obj.reports()[5].reportParms.FirstYear());
    $("#card6year2").val(i_reports_obj.reports()[5].reportParms.SecondYear());    

}