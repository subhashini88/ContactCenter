var resizeId;
window.addEventListener('resize', function () {
    clearTimeout(resizeId);
    resizeId = setTimeout(bodyResize, 500);
});

jQuery(document).ready(function ($) {

    var frameEle = window.frameElement;
    if (frameEle) {
        frameEle.style.border = "none";
        if (frameEle.parentElement) {
            if (frameEle.parentElement.parentElement) {
                if (frameEle.parentElement.parentElement.parentElement) {
                    frameEle.parentElement.parentElement.parentElement.style.border = "none";
                }
            }
        }
    }
	
	var i_locale = getlocale();
	translateLabels("com/opentext/apps/contractcenter/CCAnalyticsDashboard/CCAnalyticsDashboard", i_locale);
	var rtl_css = '../../../../../com/opentext/apps/utils/css/rtlappscommon.css';
	loadRTLIfRequired(i_locale,rtl_css);

    document.body.onresize = bodyResize;

    // Initilize report objects;
    initilizeReports();

    // Load report configuration;
    init();



    //Toggle fullscreen
    $(".expand-button").click(function (e) {

        e.preventDefault();
        var $this = $(this);
        var isReportFullSize = false;
        var reportNumber = parseInt($(this).closest('.cc-card').attr('cardid'));

        if ($this.children('i').hasClass('icon-expand')) {
            $this.children('i').removeClass('icon-expand');
            $this.children('i').addClass('icon-collapse');
            $('#card' + reportNumber + ' div.title-header').addClass('fs24');
            isReportFullSize = true;
        }
        else if ($this.children('i').hasClass('icon-collapse')) {
            $this.children('i').removeClass('icon-collapse');
            $this.children('i').addClass('icon-expand');
            $('#card' + reportNumber + ' div.title-header').removeClass('fs24');
        }

        var reportSize = getReportSize(getReportBodyDivID(reportNumber), true, isReportFullSize, reportNumber);
        $(this).closest('.cc-card').toggleClass('card-fullscreen');

        if (isReportFullSize) {
            if (reportNumber != 6) {
                $('#reportrange' + reportNumber).data('daterangepicker').drops = 'down';
            } else {
                $('#showSpanonExpand').css("display", "inline-block")
            }
            $("#card" + reportNumber + "ReportBodyonExpand").css("display", "block");
            $("#btn_card" + reportNumber + "Settings").css("display", "none");
            if (reportNumber == 1) {
                $('#card1ReportExpandSummary').css('display', 'block');
            } else if (reportNumber == 2) {
                $('#card2ReportExpandSummary').css('display', 'block');
            }
            hideReportSummary(reportNumber);
        } else {
            if (reportNumber != 6) {
                $('#reportrange' + reportNumber).data('daterangepicker').drops = calenderOrientaion[reportNumber - 1];
            } else {
                $('#showSpanonExpand').css("display", "none")
            }
            $("#card" + reportNumber + "ReportBodyonExpand").css("display", "none");
            $("#btn_card" + reportNumber + "Settings").css("display", "block");
            if (reportNumber == 1) {
                $('#card1ReportExpandSummary').css('display', 'none');
            } else if (reportNumber == 2) {
                $('#card2ReportExpandSummary').css('display', 'none');
            }

            if ($('#card' + reportNumber).hasClass('legend-card')) {
                showReportSummary(reportNumber);
            }
        }
        // Reload report.
		if(!!document.documentMode) //for IE
		{
			loadReports(reportNumber, false, reportSize);
		}
		else
		{
			setTimeout(loadReport,0,reportNumber, false, reportSize);
		}
    });


    // Click on toggle.
    $('#radiobutton1,#radiobutton2,#radiobutton3,#radiobutton4,#radiobutton5,#radiobutton6').change(function () {
        var reportNumber = parseInt($(this).closest('.cc-card').attr('cardid'));
        var reportSize = getReportSize(getReportBodyDivID(reportNumber), false, isReportFullSize(reportNumber), reportNumber);

        // Reload report.
        loadReport(reportNumber, true, reportSize);
    });


    // For taggle button switch CSS change.
    $('#radioBtn1 a,#radioBtn2 a,#radioBtn3 a,#radioBtn4 a,#radioBtn5 a,#radioBtn6 a').on('click', function () {
        var sel = $(this).data('title');
        var tog = $(this).data('toggle')
        if (sel != $('#' + tog).val()) {
            $('#' + tog).val(sel).trigger('change');
        }
        $('a[data-toggle="' + tog + '"]').not('[data-title="' + sel + '"]').removeClass('active').addClass('notActive');
        $('a[data-toggle="' + tog + '"][data-title="' + sel + '"]').removeClass('notActive').addClass('active');
    })
    ko.applyBindings(l_contractTypes_model, document.getElementById("contractTypesDiv"));
    ko.applyBindings(l_regions_model, document.getElementById("regionsDiv"));
    ko.applyBindings(l_contractValueRanges_model, document.getElementById("contractValueRangesDiv"));

    // Load Calender.
    loadCalenders();

    ko.applyBindings(i_reports_obj, document.getElementById("reports_main_div"));
});
