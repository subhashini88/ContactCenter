package com.opentext.apps.cc.custom;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;


public class ContractManagementUtils extends ContractManagementUtilsBase
{
	public static final String BILLING_STATUS_INACTIVE = "InActive";
	private static final Pattern dateFormat = Pattern.compile("([0-9]{4})-([0-9]{2})-([0-9]{2})");
	public ContractManagementUtils()
	{
		this((BusObjectConfig)null);
	}

	public ContractManagementUtils(BusObjectConfig config)
	{
		super(config);
	}
	

	//Dont use this method untill this method should get align with getTCVandRCV method
	public static RemainingContractValueOutputObject GetAllContractValues(String contractItemId)
	{

		int readContractResponse=0,readContractRequestParams=0;
		// final int UpfrontFeeUnitDivisor = 72;// As discussed with Tejaswi,This BN specific contract will not more then 6 year i.e. 72 months
		RemainingContractValueOutputObject  rcv = new RemainingContractValueOutputObject();
		try {
			SOAPRequestObject readContractRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/Contract/operations", "ReadContract", null, null);
			readContractRequestParams = NomUtil.parseXML("<Contract-id><ItemId>"+contractItemId+"</ItemId></Contract-id>");
			readContractRequest.addParameterAsXml(readContractRequestParams);
			readContractResponse = readContractRequest.sendAndWait();
			//Contract Data 
			Double upfrontFee = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//UpfrontFees", readContractResponse),"0"));
			Double SWFee = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//SoftwareFees", readContractResponse),"0"));
			Double PSFee = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//ProfessionalServicesFees", readContractResponse),"0"));
			Double maintanceFee =Double.valueOf( Node.getDataWithDefault(NomUtil.getNode(".//MaintenanceFees", readContractResponse),"0"));
			Double maintanceFeeAdj = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//MaintFeeAdjDuringCurrentTerm", readContractResponse),"0"));
			Double SWMaintancefeeOrRenewalFee =  Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//SWMaintFeesForRenewalTerm", readContractResponse),"0"));
			Double TerminationFee =  Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//TerminationFees", readContractResponse),"0"));
			String minStartDate = Node.getDataWithDefault(NomUtil.getNode(".//MinStartdate", readContractResponse),null).replace("Z", "");
			String effectiveDate = Node.getDataWithDefault(NomUtil.getNode(".//StartDate", readContractResponse),null).replace("Z", "");
			String currenyId = Node.getDataWithDefault(NomUtil.getNode(".//Currency/Currency-id/ItemId", readContractResponse),null);
			String initialContractTenure = Node.getDataWithDefault(NomUtil.getNode(".//InitialContractTenure", readContractResponse),null);
			String nextExpirationDate = Node.getDataWithDefault(NomUtil.getNode(".//EndDate", readContractResponse),"").replace("Z", "");
			String renewalDuration = Node.getDataWithDefault(NomUtil.getNode(".//AutoRenewDuration", readContractResponse),null);
			boolean isAutoRenewal = Boolean.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//AutoRenew", readContractResponse),null));
			boolean isPerpetual = Boolean.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//Perpetual", readContractResponse),null));
			long presentMonth = getPresentMonth(minStartDate);
			double[] GridRevenueScheduleFeesUnit =  getRevenueScheduleGridTotalValue(presentMonth, contractItemId);
			double USDConversionUnit = (currenyId==null || currenyId.equals(""))?0:getConversionRateToUSD(currenyId);
			//Get TCV and RCV
			/* rcv = getTCVandRCV(minStartDate, nextExpirationDate, initialContractTenure, isAutoRenewal, renewalDuration, upfrontFee, maintanceFee, SWFee, PSFee, 
  	    			   TerminationFee, maintanceFeeAdj, SWMaintancefeeOrRenewalFee, contractItemId, isPerpetual,
  	    			 GridRevenueScheduleFeesUnit[0], GridRevenueScheduleFeesUnit[1], USDConversionUnit,effectiveDate);*/
		}
		finally{
			Utilities.cleanAll(readContractRequestParams,readContractResponse);
		}

		return rcv;
	}

	public static long getTotalMonthsFromMinstartDate(String minstartDate,String nextExpirationDate,String effectiveDate,String cancellationDate,String inActivationDate,String billingStatus){
		LocalDate minStartLocalDate = getValidLocalDate(minstartDate);
		if(Objects.isNull(minStartLocalDate))
			return -1L;
		//Date Details
		LocalDate localEffectiveDate = getValidLocalDate(effectiveDate);
		LocalDate localCancellationDate = getValidLocalDate(cancellationDate);
		LocalDate localInActivationDate = getValidLocalDate(inActivationDate);
		//if cancellation date is not null and cancellation date < effective date
		long totalMonthsFromMinstartDate = -1L;
		if(Objects.nonNull(localCancellationDate) )
		{
			if(Objects.nonNull(localEffectiveDate) && localCancellationDate.isBefore(localEffectiveDate) )
				return 0;
			else
				totalMonthsFromMinstartDate = ChronoUnit.MONTHS.between(minStartLocalDate,localCancellationDate);
		}
		else if(Objects.nonNull(localInActivationDate) && Objects.nonNull(billingStatus) && billingStatus.equalsIgnoreCase(BILLING_STATUS_INACTIVE)){
			totalMonthsFromMinstartDate = ChronoUnit.MONTHS.between(minStartLocalDate,localInActivationDate);
		}
		else{
			if(Objects.isNull(nextExpirationDate) || nextExpirationDate.equalsIgnoreCase(""))
				return 0L;
			totalMonthsFromMinstartDate = ChronoUnit.MONTHS.between(minStartLocalDate,LocalDate.parse(nextExpirationDate));
		}
		return totalMonthsFromMinstartDate < 0 ? totalMonthsFromMinstartDate : totalMonthsFromMinstartDate +1;
	}

	public static long getPresentMonth(String startDate){
		if(Objects.isNull(startDate) || startDate.equalsIgnoreCase(""))
			return -1L;
		LocalDate localStartDate = LocalDate.parse(startDate);
		LocalDate currentDate = LocalDate.now();
		long presentMonth = ChronoUnit.MONTHS.between(localStartDate,currentDate);
		return (presentMonth < 0 )? 0 : presentMonth+1;
	}

	public static String getRemainingDuration(String nextExpirationDate){
		if(Objects.isNull(nextExpirationDate))
			return "P0Y0M0DT0H0M10.000S";
		LocalDate localnextExpirationDate = LocalDate.parse(nextExpirationDate);
		LocalDate currentDate = LocalDate.now();
		return Period.between(currentDate, localnextExpirationDate).toString();
	}

	public static String getRemainingDurationForDeadlines(String dueDate) {
		StringBuilder result = new StringBuilder("P0M");
		if (Objects.nonNull(dueDate) && !dueDate.isEmpty()) {
			dueDate = dueDate + "T00:00:00";
			LocalDateTime currentDateTime = LocalDateTime.now();
			LocalDateTime localdueDateTime = LocalDateTime.parse(dueDate);
			if (localdueDateTime.compareTo(currentDateTime) > 0) {
				Duration duration = java.time.Duration.between(currentDateTime, localdueDateTime);
				if (duration.getSeconds() > 0) {
					duration.plusMinutes(1);
				}
				final long days = duration.toDays();
				final long hours = duration.toHours() % 24;
				final long minuts = duration.toMinutes() % 60;
				result.append(days).append("D").append("T").append(hours).append("H").append(minuts).append("M");
			} else {
				result.append("0DT0H2M");
			}
		} else {
			result.append("0DT0H2M");
		}
		return result.toString();

	}

	public static int compareDateWithToday(String date) {
		LocalDate dateObj = LocalDate.parse(date);
		LocalDate currentDate = LocalDate.now();
		return dateObj.compareTo(currentDate);
	}

	public static String getNextDatePEFormat(String curDate) throws ParseException {
		String nextDate = curDate;
		if (Objects.nonNull(curDate)) {
			String inputDate = (curDate.endsWith("Z"))?curDate:(curDate+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'Z'HH:mm:ss");
			final Date date = inputFormat.parse(inputDate + "00:02:00");
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, 1);
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			nextDate = outputFormat.format(calendar.getTime());

		}
		return nextDate;
	}

	public static String getSameDatePEFormat(String curDate) throws ParseException {
		String sameDate = curDate;
		if (Objects.nonNull(curDate)) {
			String inputDate = (curDate.endsWith("Z"))?curDate:(curDate+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'Z'HH:mm:ss");
			final Date date = inputFormat.parse(inputDate + "00:02:00");
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sameDate = outputFormat.format(calendar.getTime());
		}
		return sameDate;
	}

	public static String getSameDateTimePEFormat(String curDateTime) throws ParseException {
		String sameDateTime = curDateTime;
		if (Objects.nonNull(curDateTime)) {
			String inputDate = (curDateTime.endsWith("Z"))?curDateTime:(curDateTime+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final Date date = inputFormat.parse(inputDate);
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sameDateTime = outputFormat.format(calendar.getTime());
		}
		return sameDateTime;
	}

	public static String getDateTimePEFormatAddParam(String curDateTime,String addType, int addValue) throws ParseException {
		String sameDateTime = curDateTime;
		if (Objects.nonNull(curDateTime)) {
			String inputDate = (curDateTime.endsWith("Z"))?curDateTime:(curDateTime+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final Date date = inputFormat.parse(inputDate);
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			//currently written for adding minutes can extend for diff types
			if(addType.equals("MINUTE") && Objects.nonNull(addValue))
			{
				calendar.add(Calendar.MINUTE,addValue);
			}
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			sameDateTime = outputFormat.format(calendar.getTime());
		}
		return sameDateTime;
	}

	public static String getAddOrRemoveDaysPEFormat(String curDate, int days) throws ParseException {
		String resultDate = curDate;
		if (Objects.nonNull(curDate)) {
			String inputDate = (curDate.endsWith("Z"))?curDate:(curDate+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'Z'HH:mm:ss");
			final Date date = inputFormat.parse(inputDate + "00:02:00");
			final Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, days);
			outputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			resultDate = outputFormat.format(calendar.getTime());

		}
		return resultDate;
	}

	public static String getAddOrRemoveDays(String curDate, int days) throws ParseException {
		String resultDate = curDate;
		if (Objects.nonNull(curDate)) {
			String inputDate = (curDate.endsWith("Z"))?curDate:(curDate+"Z");
			final SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'Z'");
			final Calendar calendar = Calendar.getInstance();
			final Date date = outputFormat.parse(inputDate);
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_YEAR, days);
			resultDate = outputFormat.format(calendar.getTime());
		}
		return resultDate;
	}

	public static com.opentext.apps.cc.custom.RemainingContractValueOutputObject getTCVandRCV(
			String minStartDate, String nextExpirationDate, String initialContractTenure, boolean isAutoRenewal, String renewalDuration, double upfrontFee, double maintanceFee, 
			double SWFee, double PSFee, double TerminationFee, double maintanceFeeAdj, double SWMaintancefeeOrRenewalFee, String contractItemId, boolean isPerpetual,
			double revenueScheduleGridTCV, double revenueScheduleGridRCV, double currencyConversionRate,String effectiveDate,String cancellationDate,String inActivationDate,String billingStatus )
	{
		Double GridRevenueScheduleFeesUnitForTCV =  (Objects.isNull(revenueScheduleGridTCV))? 0 : revenueScheduleGridTCV;
		RemainingContractValueOutputObject  rcv = new RemainingContractValueOutputObject();
		// with out minStartDate, we can't calculate RCV and TCV
		if(Objects.isNull(minStartDate) || minStartDate.isEmpty() || !checkDateFormate(minStartDate)){
			return prepareRCV(rcv,0d,0d,0d,0d);
		}
		//TODO : if currency value is not given then for now we are marking currency value is 1 ~ to USD value
		Double USDConversionUnit =  (Objects.isNull(currencyConversionRate))? 1 : currencyConversionRate;

		//Total Contract Value (TCV) Calculation
		Double totalContractValue = SWFee + maintanceFee +PSFee+upfrontFee+TerminationFee+GridRevenueScheduleFeesUnitForTCV;
		Double tcvInUSD = (totalContractValue*USDConversionUnit);

		//if TCV = 0 then RCV = 0
		if(totalContractValue == 0){
			return prepareRCV(rcv,0d,0d,0d,0d);
		}

		//Date Details
		LocalDate localCancellationDate = getValidLocalDate(cancellationDate);
		LocalDate localInActivationDate = getValidLocalDate(inActivationDate);
		//if cancellation date is not null and cancellation date < effective date
		LocalDate localNextExpirationDate = null;
		if(Objects.nonNull(localCancellationDate) )
		{
			LocalDate localEffectiveDate = getValidLocalDate(effectiveDate); 
			if(Objects.nonNull(localEffectiveDate) && localCancellationDate.isBefore(localEffectiveDate) )
				return prepareRCV(rcv,totalContractValue,tcvInUSD,0d,0d);
			else
				localNextExpirationDate = localCancellationDate;
		}
		else if(Objects.nonNull(localInActivationDate) && Objects.nonNull(billingStatus) && billingStatus.equalsIgnoreCase(BILLING_STATUS_INACTIVE)){//TODO: will "InActive" string to CC Constant Utils
			localNextExpirationDate = localInActivationDate;
		}
		else{
			localNextExpirationDate = getValidLocalDate(nextExpirationDate);
		}
		LocalDate minStartLocalDate = LocalDate.parse(minStartDate);
		initialContractTenure = getP0M0DFormat(initialContractTenure);
		long totalMonthsInInitialContractTenure = (Objects.isNull(initialContractTenure)) ? 0 : Period.parse(initialContractTenure).toTotalMonths();
		long totalMonthsFromMinStartDateToNextExpirationDate = (Objects.isNull(localNextExpirationDate) ? 0 : (ChronoUnit.MONTHS.between(minStartLocalDate,localNextExpirationDate)) + 1L );
		Double GridRevenueScheduleFeesUnitForRCV =  (Objects.isNull(revenueScheduleGridRCV))? 0 : revenueScheduleGridRCV;
		long presentMonth = getPresentMonth(minStartDate);
		//initialContractTenure = (isPerpetual || (getP0M0DFormat(initialContractTenure) == null )) ? "P12M" : getP0M0DFormat(initialContractTenure);
		/*long maxtotalMonthsInInitialContractTenure  =  (totalMonthsFromMinStartDateToNextExpirationDate < totalMonthsInInitialContractTenure )
    			   									? totalMonthsFromMinStartDateToNextExpirationDate
    			   											: totalMonthsInInitialContractTenure;*/
		//Fee calculation
		final int UpfrontFeeUnitDivisor = 72; 
		Double upfrontFeeUnit = (presentMonth > 72 ) ? 0 : ((UpfrontFeeUnitDivisor-presentMonth)+1) * (upfrontFee /UpfrontFeeUnitDivisor);
		Double MaintanceFeeUnit = maintanceFee/ totalMonthsInInitialContractTenure; 

		//Calculate Remaining Contract Value (RCV)
		Double remainingContractValue = 0d,remainingContractValueInUSD = 0d;
		renewalDuration = getP0M0DFormat(renewalDuration);
		Period renewalTerm = (Objects.isNull(renewalDuration)) ? null : Period.parse(renewalDuration);
		Double SWMaintancefeeOrRenewalFeeUnit = 0d;
		Double SWRenewalFeeCalUnit = 0d;

		if(Objects.nonNull(localNextExpirationDate)){

			if(localNextExpirationDate.isBefore(LocalDate.now())) {
				remainingContractValue = 0d;//contract expired
			}
			else if(presentMonth == 1 ){
				remainingContractValue = SWFee + PSFee + maintanceFeeAdj + upfrontFeeUnit + maintanceFee + GridRevenueScheduleFeesUnitForRCV;
			}
			else if(presentMonth <= totalMonthsInInitialContractTenure && presentMonth <=totalMonthsFromMinStartDateToNextExpirationDate) {
				remainingContractValue = upfrontFeeUnit + (
						((totalMonthsFromMinStartDateToNextExpirationDate-presentMonth)+1) * MaintanceFeeUnit ) 
						+ GridRevenueScheduleFeesUnitForRCV;
			}
			else if(presentMonth > totalMonthsInInitialContractTenure && isAutoRenewal ){
				//renewal base calculation
				if(renewalTerm!=null){
					long renewalTenureInMonths = renewalTerm.toTotalMonths();
					long totalRenewalDurationInMonths = totalMonthsFromMinStartDateToNextExpirationDate-totalMonthsInInitialContractTenure;
					long remainingRenewalDuration = (renewalTenureInMonths-(totalRenewalDurationInMonths%renewalTenureInMonths));
					SWMaintancefeeOrRenewalFeeUnit = SWMaintancefeeOrRenewalFee/renewalTenureInMonths;
					SWRenewalFeeCalUnit = (remainingRenewalDuration * SWMaintancefeeOrRenewalFeeUnit);
				}
				remainingContractValue = upfrontFeeUnit+ GridRevenueScheduleFeesUnitForRCV + SWRenewalFeeCalUnit;
			}

		}
		else {
			final int TWELVE_MONTHS = 12;
			MaintanceFeeUnit = maintanceFee/ TWELVE_MONTHS;
			if(presentMonth == 1 ){
				remainingContractValue = SWFee + PSFee + maintanceFeeAdj + upfrontFeeUnit + maintanceFee + GridRevenueScheduleFeesUnitForRCV;
			}
			else if(presentMonth <= 12 ) {
				remainingContractValue = upfrontFeeUnit + ((TWELVE_MONTHS-presentMonth+1) * MaintanceFeeUnit)+ GridRevenueScheduleFeesUnitForRCV;
			}
			else {
				SWMaintancefeeOrRenewalFeeUnit = SWMaintancefeeOrRenewalFee/TWELVE_MONTHS;
				long totalRenewalDurationInMonths = presentMonth - TWELVE_MONTHS;
				long remainingRenewalDuration = TWELVE_MONTHS - (totalRenewalDurationInMonths%TWELVE_MONTHS);
				SWRenewalFeeCalUnit = (remainingRenewalDuration * SWMaintancefeeOrRenewalFeeUnit);
				remainingContractValue = upfrontFeeUnit+ GridRevenueScheduleFeesUnitForRCV + SWRenewalFeeCalUnit;
			}
		}
		remainingContractValueInUSD = remainingContractValue * USDConversionUnit;	 
		return prepareRCV(rcv, totalContractValue, tcvInUSD, remainingContractValue, remainingContractValueInUSD);
	}
	private static RemainingContractValueOutputObject  prepareRCV(RemainingContractValueOutputObject  rcv,Double totalContractValue,Double tcvInUSD,Double remainingContractValue,Double remainingContractValueInUSD){
		//prepare result
		DecimalFormat roundingTo2Decimal = new DecimalFormat("##.00");
		rcv.setTotalContractValue(Double.valueOf(roundingTo2Decimal.format(totalContractValue)));
		rcv.setTotalContractValueInUSD(Double.valueOf(roundingTo2Decimal.format(tcvInUSD)));
		rcv.setRemainingContractValue(Double.valueOf(roundingTo2Decimal.format(remainingContractValue)));
		rcv.setRemainingContractValueInUSD(Double.valueOf(roundingTo2Decimal.format(remainingContractValueInUSD)));
		return rcv;
	}

	public static BusObjectIterator<com.opentext.apps.cc.custom.ContractManagementUtils> getContractManagementUtilsObjects(com.cordys.cpc.bsf.query.Cursor cursor)
	{
		// TODO implement body
		return null;
	}

	public static String getRenewalDuration(java.util.Date startDate, java.util.Date endDate, String initailTenure, String rewalduration)
	{
		Period originalDuration = Period.parse(initailTenure);
		Period renewalTenure = Period.parse(rewalduration);
		LocalDate localEndDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate localstartDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate prasentDate = LocalDate.now();
		Period newTenure = null;
		//if the Contract End date is a past date
		if(prasentDate.isAfter(localEndDate)){
			long daysAfterEndDate = ChronoUnit.DAYS.between(localEndDate, prasentDate);
			LocalDate nextRenewalDate = localEndDate.plus(renewalTenure);
			long renewalDurationInDays = ChronoUnit.DAYS.between(localEndDate, nextRenewalDate);
			long reminder = (daysAfterEndDate < renewalDurationInDays) ? 0 : daysAfterEndDate % renewalDurationInDays;
			if(reminder <=0) {
				newTenure = originalDuration.plus(renewalTenure);
			}
			if(reminder> 0 && reminder < renewalDurationInDays){
				newTenure = Period.between(localstartDate, prasentDate)
						.minus(Period.of(0,0, (int)(reminder)))
						.plus(renewalTenure);
			}
		}
		else { // contract end date is in future date
			newTenure = originalDuration.plus(renewalTenure);
		}
		if(newTenure != null)
		{
			return newTenure.toString();
		}
		else {
			return null;
		}
	}
	public void onInsert()
	{
	}

	public void onUpdate()
	{
	}

	public void onDelete()
	{
	}

	public static Double getConversionRateToUSD(final String currencyId){
		int currencyResponse=0,currencyCodeNode=0;
		try
		{
			SOAPRequestObject currencyRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/Currency/operations", "ReadCurrency", null, null);
			currencyCodeNode = NomUtil.parseXML("<Currency-id><ItemId>"+currencyId+"</ItemId></Currency-id>");
			currencyRequest.addParameterAsXml(currencyCodeNode);
			currencyResponse = currencyRequest.sendAndWait();
			String ConversionRateToUSD=Node.getDataWithDefault(NomUtil.getNode(".//ConversionRateToUSD", currencyResponse),null);
			return Double.valueOf(ConversionRateToUSD);
		}
		finally{
			Utilities.cleanAll(currencyResponse,currencyCodeNode);
		}
	}

	public  static double[] getRevenueScheduleGridTotalValue(long presentMonth,String contractItemId){
		int getRevenueSchedulesResponse=0,contractItemIdNode=0;
		double[] revenueScheduleValues = new double[2];
		try
		{
			SOAPRequestObject getRevenueSchedulesRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenter/Contract/operations", "GetRevenueSchedule", null, null);
			contractItemIdNode = NomUtil.parseXML("<Contract-id><ItemId>"+contractItemId+"</ItemId></Contract-id>");
			getRevenueSchedulesRequest.addParameterAsXml(contractItemIdNode);
			getRevenueSchedulesResponse = getRevenueSchedulesRequest.sendAndWait();
			int[] nodes = NomUtil.getNodeList("RevenueSchedule", getRevenueSchedulesResponse);
			Double totalRevenueValueForTCV = 0d;
			Double totalRevenueValueForRCV = 0d;
			for(int node : nodes)
			{
				//TODO: change Double to BigDecimal for accurate values
				int fromMonth=0,toMonth = 0;Double totalValueForRCV = 0d,totalValueForTCV = 0d, processFee=0d,RTSOrPSSupportFees=0d,DedicationSupport=0d;
				toMonth = Integer.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//MonthTo", node),null));
				fromMonth = Integer.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//MonthFrom", node),null));
				processFee = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//ProcessFees", node),"0"));
				RTSOrPSSupportFees = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//RTSOrPSSupportFees", node),"0"));
				DedicationSupport = Double.valueOf(Node.getDataWithDefault(NomUtil.getNode(".//DedicationSupport", node),"0"));
				int billingTypeValue = getBillingTypeValue(Node.getDataWithDefault(NomUtil.getNode(".//BillingType", node),null));
				Double totalValuePart2 = ((processFee+RTSOrPSSupportFees+DedicationSupport)/billingTypeValue);
				if(fromMonth <= presentMonth && presentMonth <= toMonth){ 
					int totalValuePart1 = (fromMonth == 1 || presentMonth ==1 ) ? toMonth : ((toMonth-(int)presentMonth)+1) ;
					totalValueForRCV = totalValuePart1 * totalValuePart2 ;
				}
				//RCV
				totalRevenueValueForRCV+= totalValueForRCV;
				//TCV
				totalValueForTCV = (toMonth-fromMonth+1) * totalValuePart2;
				totalRevenueValueForTCV+= totalValueForTCV;
			}
			revenueScheduleValues[0] = totalRevenueValueForTCV;
			revenueScheduleValues[1] = totalRevenueValueForRCV;
			return revenueScheduleValues;
		}
		finally{
			Utilities.cleanAll(getRevenueSchedulesResponse,contractItemIdNode);
		}
	}

	public static int getBillingTypeValue(String BillingType){
		switch(BillingType)
		{
		case "Annual" :
			return 12;
		case "Monthly" :
			return 1;
		case "Quarterly" :
			return 3;
		}
		return 1;
	}

	public static String getP0M0DFormat(String term){
		if(Objects.isNull(term)) return null;
		int indexOfT = term.indexOf("T");
		term = (indexOfT == -1) ? term : ((indexOfT == 1)? null: term.substring(0, indexOfT));
		return (term == null || term.equalsIgnoreCase("") || term.equalsIgnoreCase("P0D") || term.equalsIgnoreCase("P0M") || term.equalsIgnoreCase("P0M0D") || term.equalsIgnoreCase("P0M0DT0H0M") ) 
				? null  : term;
	}
	public static com.opentext.apps.cc.custom.ContractDates getContractDates(String effectiveDate, String initialContractTerm, String renewalDuration, boolean isAutoRenew, int renewalCycle, String nextExpiryDate, String cancellationDate, String inActivationDate, String billingStatus, String minStartDate, int noticePeriodInDays)
	{

		ContractDates contractDates = new ContractDates();
		initialContractTerm = getP0M0DFormat(initialContractTerm);
		if(Objects.isNull(initialContractTerm)) {
			return contractDates;
		}
		else if(Objects.isNull(minStartDate) || minStartDate.isEmpty() || !checkDateFormate(minStartDate)){
			return contractDates;
		}
		LocalDate localCancellationDate = getValidLocalDate(cancellationDate),localInActivationDate=getValidLocalDate(inActivationDate),newNextExpiryDate = null,newCurrentEndDate=null;
		boolean isNextExpDateExist = false,isInActivationDateNotExist = false;
		if(Objects.nonNull(localCancellationDate) )
		{
			LocalDate localEffectiveDate = getValidLocalDate(effectiveDate);
			if(Objects.nonNull(localEffectiveDate) && localCancellationDate.isBefore(localEffectiveDate)) {
				return contractDates;//if cancellation date is before effective date
			}
			newNextExpiryDate = localCancellationDate;
			isNextExpDateExist = true;
		}
		else if(Objects.nonNull(billingStatus) && billingStatus.equalsIgnoreCase(BILLING_STATUS_INACTIVE)){//TODO: will "InActive" string to CC Constant Utils
			if(Objects.nonNull(localInActivationDate)) {
				newNextExpiryDate = localInActivationDate;
				isNextExpDateExist = true;
			}else {
				newNextExpiryDate = null;
				isInActivationDateNotExist = true;
			}
		}
		//Date Details
		LocalDate localMinStartDate,initialExpDate,newCurrentStartDate = null,presentDate = LocalDate.now();
		Period initialTerm,renewalTerm,totalContractTermValue = null;
		localMinStartDate = LocalDate.parse(minStartDate);
		initialTerm = Period.parse(initialContractTerm);
		renewalTerm = (getP0M0DFormat(renewalDuration) ==null) ? null : Period.parse(getP0M0DFormat(renewalDuration));
		initialExpDate = localMinStartDate.plus(initialTerm).minusDays(1);
		long newRenewalCycle = 0;
		if(presentDate.isBefore(initialExpDate)){
			newCurrentEndDate = isNextExpDateExist ? newNextExpiryDate :initialExpDate;
			newNextExpiryDate = (isNextExpDateExist || isInActivationDateNotExist ) ? newNextExpiryDate : initialExpDate;
			newCurrentStartDate = localMinStartDate;
			totalContractTermValue = initialTerm;
		}
		else if((presentDate.isAfter(initialExpDate) || presentDate.isEqual(initialExpDate)) && isAutoRenew == true && renewalTerm !=null)
		{
			LocalDate tempInitailExpDate = localMinStartDate.plus(initialTerm);
			if(isInActivationDateNotExist && Objects.isNull(newNextExpiryDate)) {
				LocalDate tempExpDate =  LocalDate.now();
				do {
					tempInitailExpDate = tempInitailExpDate.plus(renewalTerm);
					if(tempInitailExpDate.isAfter(tempExpDate)) {
						break;
					}
					newRenewalCycle++;
				}while(presentDate.isAfter(tempInitailExpDate.minusDays(1)) || presentDate.isEqual(tempInitailExpDate.minusDays(1)));
				newCurrentStartDate = tempInitailExpDate.minus(renewalTerm);
				newCurrentEndDate = tempInitailExpDate.minusDays(1);
				totalContractTermValue = Period.between(localMinStartDate, tempInitailExpDate);
			}
			else {
				do {
					tempInitailExpDate = tempInitailExpDate.plus(renewalTerm);
					if(isNextExpDateExist && tempInitailExpDate.isAfter(newNextExpiryDate)) {
						break;
					}
					newRenewalCycle++;
				}while(presentDate.isAfter(tempInitailExpDate.minusDays(1)) || presentDate.isEqual(tempInitailExpDate.minusDays(1)));
				newNextExpiryDate = isNextExpDateExist ? newNextExpiryDate:tempInitailExpDate.minusDays(1);
				newCurrentStartDate = tempInitailExpDate.minus(renewalTerm);
				totalContractTermValue = Period.between(localMinStartDate, tempInitailExpDate);
				newCurrentEndDate = newNextExpiryDate;
			}
		}else {
			newCurrentEndDate = isNextExpDateExist ? newNextExpiryDate:initialExpDate;
			newNextExpiryDate = (isNextExpDateExist || isInActivationDateNotExist ) ? newNextExpiryDate : initialExpDate;
			newCurrentStartDate = localMinStartDate;
			totalContractTermValue = initialTerm;
		}
		if(Objects.isNull(localCancellationDate) && Objects.isNull(localInActivationDate) && noticePeriodInDays!=0 && Objects.nonNull(newNextExpiryDate)) {
			LocalDate noticePeriodStartDate = null;
			if(newNextExpiryDate != null)
			{
				noticePeriodStartDate = newNextExpiryDate.minusDays(noticePeriodInDays);
				contractDates.setTerminationNoticePeriodDate(noticePeriodStartDate.toString());
				if(presentDate.isAfter(noticePeriodStartDate.minusDays(1)) && presentDate.isBefore(newNextExpiryDate.plusDays(1)))
				{
					if(isAutoRenew == true && renewalTerm !=null) {
						newNextExpiryDate = newNextExpiryDate.plus(renewalTerm);
						totalContractTermValue = totalContractTermValue.plus(renewalTerm);
						newRenewalCycle++;
					}
				}
			}
		}
		String totalContracTerm=null;
		if(Objects.nonNull(totalContractTermValue)) {
			// the value of above  variable tempContractTermValue will be in PyYmMdD format, but PS 16.3 duration property will only allow PmMdD format and align with PS16.3 duration property 
			//  we have to changed tempContractTermValue variable value to PmMdD as in below line
			totalContractTermValue = Period.of(0, (int) totalContractTermValue.toTotalMonths(), totalContractTermValue.getDays());
			totalContracTerm = totalContractTermValue.toString();
		}
		contractDates.setCurrentStartDate(newCurrentStartDate.toString());
		contractDates.setRenewalCycle((int)newRenewalCycle);
		contractDates.setInitialExpiryDate(initialExpDate.toString());
		if(newNextExpiryDate != null)
		{
			contractDates.setNextExpiryDate( Objects.nonNull(newNextExpiryDate)? newNextExpiryDate.toString():"");
		}
		contractDates.setTotalContractTenure(totalContracTerm);
		if(newCurrentEndDate != null)
		{
			contractDates.setCurrentEndDate(newCurrentEndDate.toString());
		}
		return contractDates;
	}

	public static LocalDate getLocalDateFromDate(Date date){

		return Objects.isNull(date)? null: date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay().toLocalDate();
	}

	public static String getUpdatedComments(String oldComments, String newComments) {
		String updatedComments = null;
		if(oldComments == null || oldComments.trim().isEmpty()) {
			updatedComments = newComments;
		} else {
			updatedComments = oldComments.replaceAll("(.*)\\.\\.\\.Biller Notes.*\\.\\.\\.(.*)", newComments);
		}
		return updatedComments;
	}
	public static boolean checkDateFormate(String date) {
		return dateFormat.matcher(date).matches();
	}
	public static LocalDate getValidLocalDate(String date) {
		return (Objects.nonNull(date) && !date.isEmpty() && checkDateFormate(date)) ? LocalDate.parse(date) : null; 
	}

	// Obligation recurrence code

	public static String findNextRecurrDate(String recurType, String recurEndType, String recurStartDate,
			int rcurrPeriod, String dayOfWeek, int dayOfMonth, int noOfOccur, String recurEndDate, int recurCycle) {

		String nextRecurrDate = null;
		try {
			switch (recurType) {

			case "REPEATMONTHLY":
				nextRecurrDate = monthlyRecur(recurEndType, parseInputDate(recurStartDate), rcurrPeriod, dayOfMonth,
						noOfOccur, parseInputDate(recurEndDate), recurCycle);
				break;
			case "REPEATWEEKLY":
				nextRecurrDate = weeklyRecur(recurEndType, parseInputDate(recurStartDate), rcurrPeriod,
						getDayOfWeek(dayOfWeek), noOfOccur, parseInputDate(recurEndDate), recurCycle);
				break;
			case "REPEATDAILY":
				nextRecurrDate = dailyRecur(recurEndType, parseInputDate(recurStartDate), rcurrPeriod, noOfOccur,
						parseInputDate(recurEndDate), recurCycle);
				break;
			default:
				return nextRecurrDate;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}

		return nextRecurrDate;
	}

	private static String monthlyRecur(String r_EndType, Date r_startDate, int everyNmonths, int dayOfMonth,
			int noOfOccur, Date r_endDate, int recurCycle) {

		LocalDate localCurrDate = LocalDate.now();
		LocalDate localStartDate = LocalDate.ofInstant(r_startDate.toInstant(), ZoneId.systemDefault());
		int startingDOM = localStartDate.getDayOfMonth();
		LocalDate nextRecurDate = setDayOfMonth(localStartDate, dayOfMonth);
		if (startingDOM > dayOfMonth) {
			nextRecurDate = nextRecurDate.plusMonths(1);
		}

		if (r_EndType.equals("AFTEROCCUR") && recurCycle < noOfOccur) {
			nextRecurDate = nextRecurDate.plusMonths(recurCycle * everyNmonths);
			if (nextRecurDate.compareTo(localCurrDate) >= 0 && nextRecurDate.compareTo(localStartDate) >= 0) {
				return getFormmattedLocalDate(nextRecurDate);
			} else {
				return "Completed";
			}

		} else if (r_EndType.equals("ONTHISDATE") || r_EndType.equals("LIFEOFCONTRACT")) {
			if(Objects.nonNull(r_endDate)) {
				LocalDate localEndDate = LocalDate.ofInstant(r_endDate.toInstant(), ZoneId.systemDefault());
				if (localEndDate.compareTo(localCurrDate) > 0) {
					nextRecurDate = nextRecurDate.plusMonths(recurCycle * everyNmonths);		
					if (localEndDate.compareTo(nextRecurDate) >= 0 && nextRecurDate.compareTo(localCurrDate) >= 0
							&& nextRecurDate.compareTo(localStartDate) >= 0) {
						return getFormmattedLocalDate(nextRecurDate);
					} else {
						return "Completed";
					}
	
				} else {
					return "Completed";
				}
			}
			else {
				nextRecurDate = nextRecurDate.plusMonths(recurCycle * everyNmonths);	
				if (nextRecurDate.compareTo(localCurrDate) >= 0 && nextRecurDate.compareTo(localStartDate) >= 0) {
					return getFormmattedLocalDate(nextRecurDate);
				} else {
					return "Completed";
				}
			}
		}
		return "Completed";
	}

	private static String weeklyRecur(String r_EndType, Date r_startDate, int everyNweeks, int dayOfWeek, int noOfOccur,
			Date r_endDate, int recurCycle) {

		LocalDate localCurrDate = LocalDate.now();
		LocalDate localStartDate = LocalDate.ofInstant(r_startDate.toInstant(), ZoneId.systemDefault());
		LocalDate nextRecurDate = localStartDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(dayOfWeek)));
		if (r_EndType.equals("AFTEROCCUR") && recurCycle < noOfOccur) {
			nextRecurDate = nextRecurDate.plusWeeks(recurCycle * everyNweeks);
			if (nextRecurDate.compareTo(localCurrDate) >= 0 && nextRecurDate.compareTo(localStartDate) >= 0) {
				return getFormmattedLocalDate(nextRecurDate);
			} else {
				return "Completed";
			}

		} else if (r_EndType.equals("ONTHISDATE") || r_EndType.equals("LIFEOFCONTRACT")) {
			if(Objects.nonNull(r_endDate)) {
				LocalDate localEndDate = LocalDate.ofInstant(r_endDate.toInstant(), ZoneId.systemDefault());
				if (localEndDate.compareTo(localCurrDate) > 0) {
					nextRecurDate = nextRecurDate.plusWeeks(recurCycle * everyNweeks);
					if (localEndDate.compareTo(nextRecurDate) >= 0 && nextRecurDate.compareTo(localCurrDate) >= 0
							&& nextRecurDate.compareTo(localStartDate) >= 0) {
						return getFormmattedLocalDate(nextRecurDate);
					} else {
						return "Completed";
					}
	
				} else {
					return "Completed";
				}
			}
			else {
				nextRecurDate = nextRecurDate.plusWeeks(recurCycle * everyNweeks);
				if (nextRecurDate.compareTo(localCurrDate) >= 0 && nextRecurDate.compareTo(localStartDate) >= 0) {
					return getFormmattedLocalDate(nextRecurDate);
				} else {
					return "Completed";
				}
			}
		}
		return "Completed";
	}

	private static String dailyRecur(String r_EndType, Date r_startDate, int everyNdays, int noOfOccur, Date r_endDate,
			int recurCycle) {

		LocalDate localCurrDate = LocalDate.now();
		LocalDate localStartDate = LocalDate.ofInstant(r_startDate.toInstant(), ZoneId.systemDefault());
		if (r_EndType.equals("AFTEROCCUR") && recurCycle < noOfOccur) {
			localStartDate = localStartDate.plusDays(recurCycle * everyNdays);
			if (localStartDate.compareTo(localCurrDate) >= 0) {
				return getFormmattedLocalDate(localStartDate);
			} else {
				return "Completed";
			}

		} else if (r_EndType.equals("ONTHISDATE") || r_EndType.equals("LIFEOFCONTRACT")) {
			if(Objects.nonNull(r_endDate)) {
				LocalDate localEndDate = LocalDate.ofInstant(r_endDate.toInstant(), ZoneId.systemDefault());
				if (localEndDate.compareTo(localCurrDate) >= 0) {
					localStartDate = localStartDate.plusDays(recurCycle * everyNdays);
					if (localEndDate.compareTo(localStartDate) >= 0 && localStartDate.compareTo(localCurrDate) >= 0) {
						return getFormmattedLocalDate(localStartDate);
					} else {
						return "Completed";
					}
	
				} else {
					return "Completed";
				}
			}
			else {
				localStartDate = localStartDate.plusDays(recurCycle * everyNdays);
				if (localStartDate.compareTo(localCurrDate) >= 0) {
					return getFormmattedLocalDate(localStartDate);
				} else {
					return "Completed";
				}
			}
			
		}
		return "Completed";
	}	

	private static int getDayOfWeek(String dayOfWeek) {
		int day = 1;
		if (dayOfWeek.contains("7")) {
			day = 7;
		} else if (dayOfWeek.contains("6")) {
			day = 6;
		} else if (dayOfWeek.contains("5")) {
			day = 5;
		} else if (dayOfWeek.contains("4")) {
			day = 4;
		} else if (dayOfWeek.contains("3")) {
			day = 3;
		} else if (dayOfWeek.contains("2")) {
			day = 2;
		}
		return day;
	}


	private static String getFormmattedLocalDate(LocalDate triggerTime) {
		if (Objects.nonNull(triggerTime)) {
			DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
			return outputFormat.format(triggerTime.atTime(00, 00, 00));
		}
		return null;
	}

	private static Date parseInputDate(String inputDate) throws ParseException {
		if (Objects.nonNull(inputDate) && !inputDate.isEmpty()) {
			inputDate = (inputDate.endsWith("Z")) ? inputDate.replace("Z", "") : inputDate;

			final SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
			return inputFormat.parse(inputDate);

		}
		return null;
	}

	private static LocalDate setDayOfMonth(LocalDate date, int dayOfMonth) {
		try {
			date = date.withDayOfMonth(dayOfMonth);
		}catch (Exception e) {
			date = date.with(TemporalAdjusters.lastDayOfMonth());
		}

		return date;
	}
}
