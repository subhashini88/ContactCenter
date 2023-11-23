package com.opentext.apps.cc.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateFormatUtils;

import com.cordys.cpc.bsf.busobject.BSF;
import com.cordys.cpc.bsf.busobject.BusObject;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.busobject.QueryObject;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.cordys.restclient.SAMLArtifactProvider;
import com.eibus.security.identity.UserIdentityFactory;
import com.eibus.util.system.EIBProperties;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.XMLException;
import com.eibus.xml.xpath.XPath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.apps.cc.analytics.ContractCenterAlertMessages;
import com.opentext.apps.cc.analytics.aggregator.sql.DbUtil;
import com.opentext.apps.cc.analytics.aggregator.sql.DefaultSqlReportFactory;
import com.opentext.apps.cc.analytics.aggregator.sql.DefaultSqlReportFactory.DefaultReports;
import com.opentext.apps.cc.analytics.aggregator.sql.ISqlReport;
import com.opentext.apps.cc.analytics.aggregator.sql.ProcessReportSql;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.DBType;
import com.opentext.apps.cc.analytics.aggregator.sql.builder.QueryBuilderFactory;
import com.opentext.apps.cc.analytics.aggregator.sql.dao.CCCustomJsonDao;
import com.opentext.apps.cc.analytics.aggregator.util.data.UserInfo;
import com.opentext.apps.cc.analytics.exception.ContractCenterApplicationException;
import com.opentext.apps.cc.analytics.nom.NomUtil;
import com.opentext.apps.cc.analytics.nom.Utilities;

public class ReportProcessor {

	public void process() {
		List<ReportDashboardConfig> configurations = getAllReportConfiguration();
		// ExecutorService executorService = Executors.newFixedThreadPool(3);
		configurations.forEach(reportConfig -> {
			// Runnable runnable = () -> {
			processReportDasboardConfig(reportConfig);
			// };
			// executorService.submit(runnable);
		});
	}

	protected static volatile Map<String, UserInfo> userWorkListMap = new ConcurrentHashMap<>();

	public String processReportDasboardConfig(ReportDashboardConfig reportConfig) {

		String json = null;
		if (reportConfig.getProcessorType() == ProcessorType.SQL) {
			json = processSqlReportData(reportConfig);
		} else if (reportConfig.getProcessorType() == ProcessorType.DEFAULTSQL) {
			json = processDefaultSqlReportData(reportConfig);
		} else {
			processListReportData(reportConfig);
		}
		return json;
	}

	public String processReportDasboardConfig(ReportDashboardConfig reportConfig, boolean isExecuteOnly) {

		String json = null;
		if (reportConfig.getProcessorType() == ProcessorType.SQL) {
			json = processSqlReportData(reportConfig, isExecuteOnly);
		} else if (reportConfig.getProcessorType() == ProcessorType.DEFAULTSQL) {
			json = processDefaultSqlReportData(reportConfig, isExecuteOnly);
		} else {
			processListReportData(reportConfig);
		}
		return json;
	}

	public String processDefaultSqlReportData(ReportDashboardConfig configuration) {
		DefaultSqlReportFactory.DefaultReports reportType = getDefaultReportType(configuration);
		ISqlReport report = DefaultSqlReportFactory.getDefaultReport(reportType,
				(DbUtil.getDbType().equals(QueryBuilderFactory.DB_POSTGRES) ? DBType.POSTGRES : DBType.MSSQL),
				configuration);
		String sql = report.getSql();
		CCCustomJsonDao ccCustomJsonDao = new CCCustomJsonDao();
		String res = ccCustomJsonDao.getDbJsonObjects(sql);
		String jsonData = "";
		if (res.toString().isEmpty()) {
			jsonData = "{\"data\":[]}";
		} else {
			jsonData = "{\"time\":\"" + Calendar.getInstance().getTimeInMillis() + "\" , \"data\":" + res + "}";
		}
		saveReportProcessData(jsonData, configuration);
		return jsonData;
	}

	public String processDefaultSqlReportData(ReportDashboardConfig configuration, boolean isExecuteOnly) {
		DefaultSqlReportFactory.DefaultReports reportType = getDefaultReportType(configuration);
		ISqlReport report = DefaultSqlReportFactory.getDefaultReport(reportType,
				(DbUtil.getDbType().equals(QueryBuilderFactory.DB_POSTGRES) ? DBType.POSTGRES : DBType.MSSQL),
				configuration);
		String sql = report.getSql();
		CCCustomJsonDao ccCustomJsonDao = new CCCustomJsonDao();
		String res = ccCustomJsonDao.getDbJsonObjects(sql);
		String jsonData = "";
		if (res.toString().isEmpty()) {
			jsonData = "{\"data\":[]}";
		} else {
			jsonData = "{\"time\":\"" + Calendar.getInstance().getTimeInMillis() + "\" , \"data\":" + res + "}";
		}
		if (!isExecuteOnly) {
			saveReportProcessData(jsonData, configuration);
		}
		return jsonData;
	}

	private DefaultReports getDefaultReportType(ReportDashboardConfig configuration) {
		if (configuration.getName().equalsIgnoreCase("Upcoming contract terminations")) {
			return DefaultSqlReportFactory.DefaultReports.Termination;
		}
		return DefaultSqlReportFactory.DefaultReports.Renewal;
	}

	private List<ReportDashboardConfig> getAllReportConfiguration() {
		int reportsReponseNode = 0;
		int[] reportNodes = null;
		List<ReportDashboardConfig> reportsList = new ArrayList<>();
		try {
			SOAPRequestObject getAllInAppReports = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/ccanalyticdashboards/23.3", "getAllInAppReportTiles", null, null);
			reportsReponseNode = getAllInAppReports.execute();
			reportNodes = NomUtil.getNodeList(".//Response/FindZ_INT_AllInAppTilesResponse/RelatedReportNode",
					reportsReponseNode);
			for (int rep : reportNodes) {
				reportsList.add(prepareReportDashBoard(rep));
			}
		} finally {
			Utilities.cleanAll(reportsReponseNode);
		}
		return reportsList;
	}

	public String processSavedReport(String reportItemId1) {
		ReportDashboardConfig config = getUserReportConfig(reportItemId1);
		if (!userWorkListMap.containsKey(config.getUserIdentityId())) {
			getWorklistsforUser(config.getUserIdentityId());
		}
		UserInfo userData = userWorkListMap.get(config.getUserIdentityId());
		config.getUser().setIsAdmin(userData.getIsAdmin());
		config.getUser().setTargetWorklist(userData.getTargetWorklist());
		return processReportDasboardConfig(config);
	}

	public String processSavedReportIndividual(String reportItemId1) {
		ReportDashboardConfig config = getUserReportConfig(reportItemId1);
		userWorkListMap.remove(config.getUserIdentityId());
		getWorklistsforUser(config.getUserIdentityId());
		UserInfo userData = userWorkListMap.get(config.getUserIdentityId());
		config.getUser().setIsAdmin(userData.getIsAdmin());
		config.getUser().setTargetWorklist(userData.getTargetWorklist());
		return processReportDasboardConfig(config);
	}

	public String processSavedReportMyDashboard(String chartReportItemId1) {
		GCChartDto chartDto = readGCChart(chartReportItemId1);
		ReportDashboardConfig config = readSavedReportConfig(chartDto);
		userWorkListMap.remove(config.getUserIdentityId());
		getWorklistsforUser(config.getUserIdentityId());
		UserInfo userData = userWorkListMap.get(config.getUserIdentityId());
		config.getUser().setIsAdmin(userData.getIsAdmin());
		config.getUser().setTargetWorklist(userData.getTargetWorklist());
		config.getUser().setIsAdmin(userData.getIsAdmin());
//		config.getUser().setIsAdmin(true);
		String json = processReportDasboardConfig(config, true);
		updateGCChart(chartReportItemId1, json);
		return json;
	}

	private GCChartDto readGCChart(String reportItemId) {
		int chartNodeReponse = 0;
		int reportConfiNode = 0;
		GCChartDto chartDto = null;
		try {
			SOAPRequestObject getAllInAppReports = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/basiccomponents/20.2", "GetFilterGCChartDetails", null, null);
			int CTRAddlPropsId = NomUtil.parseXML("<Id>" + reportItemId.split("\\.")[1] + "</Id>");
//			Node.setDataElement(CTRAddlPropsId, "Id", reportItemId);
			getAllInAppReports.addParameterAsXml(CTRAddlPropsId);
			chartNodeReponse = getAllInAppReports.execute();
			String configId = NomUtil.getData(".//Response//FindZ_INT_ChartDetailsResponse//GCChart//RelatedConfigId",
					chartNodeReponse);
			String defaultChartDuration = NomUtil.getData(
					".//Response//FindZ_INT_ChartDetailsResponse//GCChart//DefaultChartDuration", chartNodeReponse);
			String defaultChartType = NomUtil.getData(
					".//Response//FindZ_INT_ChartDetailsResponse//GCChart//DefaultChartType", chartNodeReponse);
			String defaultGroupByCol = NomUtil.getData(
					".//Response//FindZ_INT_ChartDetailsResponse//GCChart//DefaultGroupByCol", chartNodeReponse);
			String userIdentityId = NomUtil.getData(
					".//Response//FindZ_INT_ChartDetailsResponse//GCChart//Tracking//CreatedBy//Identity-id//Id",
					chartNodeReponse);
			chartDto = new GCChartDto();
			chartDto.setRelatedConfigItemId(configId);
			chartDto.setDefaultChartDuration(defaultChartDuration);
			chartDto.setDefaultChartType(defaultChartType);
			chartDto.setDefaultGroupByCol(defaultGroupByCol);
			chartDto.setUserIdentityId(userIdentityId);
		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(chartNodeReponse, reportConfiNode);
		}
		return chartDto;
	}

	private void updateGCChart(String reportItemId, String processedData) {
		int chartNodeReponse = 0;
		int reportConfiNode = 0;
		try {
			SOAPRequestObject getAllInAppReports = new SOAPRequestObject(
					"http://schemas/OpenTextBasicComponents/GCChart/operations", "UpdateGCChart", null, null);
			int CTRAddlPropsId = NomUtil.parseXML("<GCChart-id></GCChart-id>");
			int gcChartUpdateNode = NomUtil.parseXML("<GCChart-update></GCChart-update>");
			Node.setDataElement(CTRAddlPropsId, "ItemId", reportItemId);
			Node.setDataElement(gcChartUpdateNode, "ProcessedReportData", processedData);
			getAllInAppReports.addParameterAsXml(CTRAddlPropsId);
			getAllInAppReports.addParameterAsXml(gcChartUpdateNode);
			chartNodeReponse = getAllInAppReports.execute();
		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(chartNodeReponse, reportConfiNode);
		}
	}

	private ReportDashboardConfig getUserReportConfig(String reportItemId1) {
		int reportNodeReponse = 0;
		int reportConfiNode = 0;
		ReportDashboardConfig configuration = null;
		try {

			SOAPRequestObject getAllInAppReports = new SOAPRequestObject(
					"http://schemas/OpenTextCCAnalyticDashboards/CCUserReportsDashboard.RelatedReportNode/operations",
					"ReadRelatedReportNode", null, null);
			int CTRAddlPropsId = NomUtil.parseXML("<RelatedReportNode-id></RelatedReportNode-id>");
			Node.setDataElement(CTRAddlPropsId, "ItemId1", reportItemId1);
			getAllInAppReports.addParameterAsXml(CTRAddlPropsId);
			reportNodeReponse = getAllInAppReports.execute();
			String configId = NomUtil.getData(".//RelatedConfig/CCUserSavedReports-id/Id", reportNodeReponse);
			reportConfiNode = getSavedReportConfig(configId);
			String reportName = NomUtil.getData(".//Name", reportConfiNode);
			String xCol = NomUtil.getData(".//XColumn", reportConfiNode);
			String yCol = NomUtil.getData(".//YColumn", reportConfiNode);
			String aggCol = NomUtil.getData(".//AggregatorColumn", reportConfiNode);
			String dataSet = NomUtil.getData(".//DataSet", reportConfiNode);
			String charType = NomUtil.getData(".//ChartType", reportConfiNode);
			String agg = NomUtil.getData(".//Aggregator", reportConfiNode);
			String configType = NomUtil.getData(".//ConfigType", reportConfiNode);
			String defaultChartType = NomUtil.getData(".//DefaultChartType", reportNodeReponse);
			String defaultChartDuration = NomUtil.getData(".//DefaultChartDuration", reportNodeReponse);
			String defaultGroupBy = NomUtil.getData(".//DefaultGroupByCol", reportNodeReponse);
			String userIdentityId = NomUtil.getData(".//Tracking//CreatedBy//Identity-id//Id", reportNodeReponse);
			String dataSetGroup = NomUtil.getData(".//DataSetGroup", reportConfiNode);
			configuration = new ReportDashboardConfig(reportItemId1, configId, reportName, xCol, yCol, agg, aggCol,
					dataSet, charType, getConfigType(configType), configType, userIdentityId, null, true,
					getDataSetGroup(dataSetGroup));
			configuration.setDefaultChartDuration(defaultChartDuration);
			configuration.setDefaultChartType(defaultChartType);
			configuration.setDefaultGroupBy(defaultGroupBy);
		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(reportNodeReponse, reportConfiNode);
		}
		return configuration;
	}

	private DataSetGroup getDataSetGroup(String dataSetGroup) {
		return null != dataSetGroup && dataSetGroup.equals("OBLIGATION") ? DataSetGroup.OBLIGATION
				: DataSetGroup.CONTRACT;
	}

	private ReportDashboardConfig readSavedReportConfig(GCChartDto chartDto) {
		int reportNodeReponse = 0;
		int reportConfiNode = 0;
		ReportDashboardConfig configuration = null;
		try {
			reportConfiNode = getSavedReportConfig(chartDto.getRelatedConfigId());
			String reportName = NomUtil.getData(".//Name", reportConfiNode);
			String xCol = NomUtil.getData(".//XColumn", reportConfiNode);
			String yCol = NomUtil.getData(".//YColumn", reportConfiNode);
			String aggCol = NomUtil.getData(".//AggregatorColumn", reportConfiNode);
			String dataSet = NomUtil.getData(".//DataSet", reportConfiNode);
			String charType = NomUtil.getData(".//ChartType", reportConfiNode);
			String agg = NomUtil.getData(".//Aggregator", reportConfiNode);
			String configType = NomUtil.getData(".//ConfigType", reportConfiNode);
			String defaultChartType = chartDto.getDefaultChartType();
			String defaultChartDuration = chartDto.getDefaultChartDuration();
			String defaultGroupBy = chartDto.getDefaultGroupByCol();
			String userIdentityId = chartDto.getUserIdentityId();
			String dataSetGroup = NomUtil.getData(".//DataSetGroup", reportConfiNode);
			configuration = new ReportDashboardConfig("", chartDto.getRelatedConfigId(), reportName, xCol, yCol, agg,
					aggCol, dataSet, charType, getConfigType(configType), configType, userIdentityId, null, true,
					getDataSetGroup(dataSetGroup));
			configuration.setDefaultChartDuration(defaultChartDuration);
			configuration.setDefaultChartType(defaultChartType);
			configuration.setDefaultGroupBy(defaultGroupBy);

		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(reportNodeReponse, reportConfiNode);
		}
		return configuration;
	}

	private ProcessorType getConfigType(String configType) {
		return (null != configType && configType.equalsIgnoreCase("DEFAULTSQL")) ? ProcessorType.DEFAULTSQL
				: ProcessorType.SQL;
	}

	private int getSavedReportConfig(String configId) {
		int reportsReponseNode = 0;
		SOAPRequestObject getAllInAppReports = new SOAPRequestObject(
				"http://schemas/OpenTextCCAnalyticDashboards/CCUserSavedReports/operations", "ReadCCUserSavedReports",
				null, null);
		int CTRAddlPropsId = NomUtil.parseXML("<CCUserSavedReports-id></CCUserSavedReports-id>");
		Node.setDataElement(CTRAddlPropsId, "Id", configId);
		getAllInAppReports.addParameterAsXml(CTRAddlPropsId);
		reportsReponseNode = getAllInAppReports.execute();
		return reportsReponseNode;
	}

	private ReportDashboardConfig prepareReportDashBoard(int rep) {

		ReportDashboardConfig configuration = new ReportDashboardConfig(
				NomUtil.getData(".//RelatedReportNode-id/ItemId1", rep), NomUtil.getData(".//RelatedConfig-id/Id", rep),
				NomUtil.getData(".//Name", rep), NomUtil.getData(".//RelatedConfig/XColumn", rep),
				NomUtil.getData(".//RelatedConfig/YColumn", rep), NomUtil.getData(".//RelatedConfig/Aggregator", rep),
				NomUtil.getData(".//RelatedConfig/AggregatorColumn", rep),
				NomUtil.getData(".//RelatedConfig/DataSet", rep), NomUtil.getData(".//RelatedConfig/ChartType", rep),
				ProcessorType.SQL, "", null, null, true, DataSetGroup.CONTRACT);
		return configuration;
	}

	private static void updateCTRAddlPropsJSON(String relatedReportItemId1, String reportData) {
		int CTRAddlPropsNode = 0, relatedContractNode = 0, contractIDNode = 0;

		try {
			SOAPRequestObject UpdateCTRAddlPropsRequest = new SOAPRequestObject(
					"http://schemas/OpenTextCCAnalyticDashboards/CCUserReportsDashboard.RelatedReportNode/operations",
					"UpdateRelatedReportNode", null, null);

			int CTRAddlPropsId = NomUtil.parseXML("<RelatedReportNode-id></RelatedReportNode-id>");
			Node.setDataElement(CTRAddlPropsId, "ItemId1", relatedReportItemId1);

			CTRAddlPropsNode = NomUtil.parseXML("<RelatedReportNode-update></RelatedReportNode-update>");
			Node.setDataElement(CTRAddlPropsNode, "ProcessedReportData", reportData);
			String dateStr = DateFormatUtils.format(new Date(), "yyyy-MM-dd'T'HH:mm:ss'Z'");
			Node.setDataElement(CTRAddlPropsNode, "DataUpdateTime", dateStr);
			UpdateCTRAddlPropsRequest.addParameterAsXml(CTRAddlPropsId);
			UpdateCTRAddlPropsRequest.addParameterAsXml(CTRAddlPropsNode);
			UpdateCTRAddlPropsRequest.sendAndWait();

		} catch (Exception e) {
		} finally {
			Utilities.cleanAll(contractIDNode, relatedContractNode, CTRAddlPropsNode);
		}
	}

	public String processSqlReportData(ReportDashboardConfig configuration) {
		ProcessReportSql processReportSql = new ProcessReportSql();
		String jsonData = processReportSql.processReport(configuration);
		saveReportProcessData(jsonData, configuration);
		return jsonData;
	}

	public String processSqlReportData(ReportDashboardConfig configuration, boolean isExecuteOnly) {
		ProcessReportSql processReportSql = new ProcessReportSql();
		String jsonData = processReportSql.processReport(configuration);
		if (!isExecuteOnly) {
			saveReportProcessData(jsonData, configuration);
		}
		return jsonData;
	}

	private void saveReportProcessData(String jsonData, ReportDashboardConfig configuration) {
		updateCTRAddlPropsJSON(configuration.getItemId1(), jsonData);
	}

	public void processListReportData(ReportDashboardConfig configuration) {

	}

	public String getWorklistsforUser(String userIdentityID) {
		String workLists = "";
		if (userWorkListMap.containsKey(userIdentityID)) {
			workLists = userWorkListMap.get(userIdentityID).getTargetWorklistStr();
		} else {
			UserInfo user = new UserInfo(userIdentityID, true);
			updateUserDatafromIdentityID(userIdentityID, user);
			String orgIDParamforSQL = getActiveMembershipQueryParam(user.getUserId());
			String queryText = null;
			try {
				queryText = "SELECT Z_INT_WorklistID FROM " + getTableName("OpenTextBasicComponents", "GCOrganization")
						+ " where " + orgIDParamforSQL;
			} catch (Exception e) {
			}
			String data = null;
			try {
				QueryObject query = new QueryObject(queryText);
				BusObjectIterator<BusObject> orgRecords = query.getObjects();
				while (orgRecords.hasMoreElements()) {
					BusObject orgRecord = orgRecords.nextElement();
					org.w3c.dom.NodeList list = orgRecord.getObjectData().getChildNodes();
					org.w3c.dom.Node node = list.item(0);
					data = node.getTextContent();
					user.getTargetWorklist().add(data);
				}
			} catch (Exception e) {
			}
			userWorkListMap.put(userIdentityID, user);
		}

		return workLists;
	}

	private void updateUserDatafromIdentityID(String userIdentityID, UserInfo user) {
		SOAPRequestObject getUserData = new SOAPRequestObject(
				"http://schemas.opentext.com/apps/ccanalyticdashboards/23.3", "GetUserData", null, null);
		int userIDParam = parseXML("<userIdentityID>" + userIdentityID + "</userIdentityID>");
		getUserData.addParameterAsXml(userIDParam);
		int getUserDataResponse = getUserData.sendAndWait();
		user.setUserId(NomUtil.getData(".//userID", getUserDataResponse));
		user.setIsAdmin(Boolean.parseBoolean(NomUtil.getData(".//isCTRAdmin", getUserDataResponse)));
	}

	private String getActiveMembershipQueryParam(String userID) {
		String orgIDParamforSQL = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date today = Calendar.getInstance().getTime();
		String todayAsString = df.format(today);
		SOAPRequestObject getMembershipOrg = new SOAPRequestObject(
				"http://schemas/OpenTextBasicComponents/GCOrganization.OrganizationMembers/operations",
				"GetMemSpelAcssOfUser", null, null);
		int userIDParam = parseXML("<UserID>" + userID + "</UserID>");
		int currDateParam = parseXML("<currentDate>" + todayAsString + "</currentDate>");
		getMembershipOrg.addParameterAsXml(userIDParam);
		getMembershipOrg.addParameterAsXml(currDateParam);
		int getMembershipOrgResponse = getMembershipOrg.sendAndWait();
		int[] orgMemNodes = getNodeList(".//OrganizationMembers", getMembershipOrgResponse);
		for (int i = 0; i < orgMemNodes.length; i++) {
			String orgID = Node.getDataWithDefault(getNode(".//Owner//GCOrganization-id//Id", orgMemNodes[i]), null);
			if (Objects.nonNull(orgID)) {
				orgIDParamforSQL += getOrgParam(orgID);
			}
			if (i < (orgMemNodes.length - 1)) {
				orgIDParamforSQL += " OR ";
			}
		}
		return orgIDParamforSQL;
	}

	public static String getTableName(String solutionname, String entityname)
			throws ContractCenterApplicationException {
		HttpURLConnection conn = null;
		String nodeUrl = EIBProperties.getProperty("com.cordys.node.url");
		// nodeUrl = "http://ccdevint.lab.opentext.com:5050";
		String orgDN = BSF.getOrganization();
		String orgName = orgDN.substring(orgDN.indexOf('=') + 1, orgDN.indexOf(','));

		if (nodeUrl != null && !nodeUrl.isEmpty()) {
			int responseCode = 0;
			URL url;
			String documentUrl = nodeUrl + "/home/" + orgName + "/app/entityRestService/api/" + solutionname
					+ "/entities/" + entityname + "/tableName";

			try {
				url = new URL(documentUrl);
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoOutput(true);
				conn.setRequestMethod("GET");
				conn.setRequestProperty("SAMLart", getSamlArtifact(BSF.getUser()));

				InputStreamReader inputStreamReader = new InputStreamReader(conn.getInputStream());
				String reponse = new BufferedReader(inputStreamReader).lines().collect(Collectors.joining("\n"));

				responseCode = ((HttpURLConnection) conn).getResponseCode();
				if (responseCode == 200) {
					ObjectMapper mapper = new ObjectMapper();
					JsonNode jsonNode = mapper.readTree(reponse);
					JsonNode tableNameTextNode = jsonNode.get("tableName");

					if (Objects.nonNull(tableNameTextNode)) {
						return tableNameTextNode.asText().toLowerCase();
					}
				}
			} catch (MalformedURLException e) {
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.URL_IS_NOT_VALIED);
			} catch (IOException e) {

				throw new ContractCenterApplicationException(ContractCenterAlertMessages.INPUT_SHOULD_NOT_BE_NULL);
			} finally {
				conn.disconnect();
			}
		}
		return null;
	}

	private static String getSamlArtifact(String userDn) {
		return SAMLArtifactProvider.getSAMLArtifact(UserIdentityFactory.getCordysIdentity(userDn));
	}

	public static int[] getNodeList(final String expression, final int node) {
		return XPath.getMatchingNodes(expression, null, node);
	}

	public static int getNode(final String expression, final int node) {
		return XPath.getFirstMatch(expression, null, node);
	}

	public static int parseXML(final String xml) {
		int node = 0;
		try {
			node = BSF.getXMLDocument().load(xml.getBytes("UTF-8"));
		} catch (XMLException | UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
		return node;
	}

	public String getOrgParam(String orgID) {
		return "(OrgRoute LIKE '" + orgID + ";%' OR OrgRoute LIKE '%;" + orgID + ";%')";
	}

}
