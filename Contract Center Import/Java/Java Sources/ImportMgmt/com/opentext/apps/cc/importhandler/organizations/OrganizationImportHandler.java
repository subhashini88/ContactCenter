package com.opentext.apps.cc.importhandler.organizations;


import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.BatchIterator;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;

public class OrganizationImportHandler extends AbstractImportHandler {

	private final MetadataInitializer metadata;
	private final ImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(OrganizationImportHandler.class);

	public OrganizationImportHandler() {
		metadata = new MetadataInitializer();
		validator = new ImportValidator();
	}

	@Override
	protected String getSheetName() {
		return ImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		OrganizationRecord organizationRecord = null;
		if (row != null) {
			ReportItem reportItem = validator.validate(row, metadata, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				organizationRecord = new OrganizationRecord(metadata, reportItem);
				organizationRecord.doWork(new ImportEvent(organizationRecord, row));
			} else {
				updateImportLogs(row.get(ImportConstants.LEGACY_ID), importConfig.getJobId(),
						reportItem.toString());
			}
		}
		return organizationRecord;
	}

	@Override
	protected ReportListener createReportListener() {
		// TODO Auto-generated method stub
		return null;
	}

	private void SkippedStatus(Map<String, List<OrganizationRecord>> skippedrecords) {
		if (skippedrecords.size() > 0) {
			for (String org : skippedrecords.keySet()) {
				for (OrganizationRecord record : skippedrecords.get(org)) {
					updateImportLogs((record.getlegacyId()), importConfig.getJobId(),
							"Errors : {The parent organization code '"+org+"' is incorrect.}");
				}
			}
		}
	}

	private void putOrg(List<ImportListener> properrecords, Map<String, List<OrganizationRecord>> skippedrecords,
			String PorgCode) {
		if (skippedrecords.containsKey(PorgCode)) {
			List<OrganizationRecord> sub = skippedrecords.get(PorgCode);
			for (OrganizationRecord org : sub) {
				properrecords.add(org);
				org.metadata.orgMap.put(org.getOrgCode(), PorgCode);
				skippedrecords.remove(PorgCode);
				putOrg(properrecords, skippedrecords, org.getOrgCode());
			}
		}
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int organizationRecordNode = 0, jobIdNode = 0, organizationImportResponse = 0;
		if (records.size() > 0) {
			organizationRecordNode = NomUtil.parseXML("<Organizations></Organizations>");
			for (ImportListener record : records) {
				Node.appendToChildren(record.getnode(), organizationRecordNode);
			}
			jobIdNode = NomUtil.parseXML("<JobID>" + super.importConfig.getJobId() + "</JobID>");
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "OrganizationImport", null, null);
			importRequest.addParameterAsXml(jobIdNode);
			importRequest.addParameterAsXml(organizationRecordNode);
			try {
				organizationImportResponse = importRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.organizations.OrganizationImportHandler",
						Severity.ERROR, e, "Error while executing OrganizationImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
						"OrganizationImport");
			} finally {
				Utilities.cleanAll(organizationRecordNode, organizationImportResponse, jobIdNode);
			}
		}
	}

	private void updateImportLogs(String organizationCode, String jobID, String errors) {
		int organizationCodeNode = 0, statusItemIdResponse = 0, updateImportStatusNode = 0, updateDataStatusNode = 0,
				updateImportStatusResponse = 0, jobIDNode = 0;
		try {
			if (!Utilities.isStringEmpty(organizationCode)) {
				SOAPRequestObject statusItemIdRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/OrganizationsImportStatus/operations",
						"OrganizationImportStatusCodeFilter", null, null);
				organizationCodeNode = NomUtil.parseXML("<LegacyID>" + organizationCode + "</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
				statusItemIdRequest.addParameterAsXml(organizationCodeNode);
				statusItemIdRequest.addParameterAsXml(jobIDNode);
				statusItemIdResponse = statusItemIdRequest.sendAndWait();
				String itemId = Node.getDataWithDefault(
						NomUtil.getNode(".//OrganizationsImportStatus-id/ItemId", statusItemIdResponse), null);

				SOAPRequestObject updateImportJobRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/OrganizationsImportStatus/operations",
						"UpdateOrganizationsImportStatus", null, null);
				updateImportStatusNode = NomUtil
						.parseXML("<OrganizationsImportStatus-id></OrganizationsImportStatus-id>");
				Node.setDataElement(updateImportStatusNode, "ItemId", itemId);
				updateDataStatusNode = NomUtil
						.parseXML("<OrganizationsImportStatus-update></OrganizationsImportStatus-update>");
				Node.setDataElement(updateDataStatusNode, "LogDetails", errors);
				Node.setDataElement(updateDataStatusNode, "ImportStatus", "InvalidData");
				updateImportJobRequest.addParameterAsXml(updateDataStatusNode);
				updateImportJobRequest.addParameterAsXml(updateImportStatusNode);
				updateImportStatusResponse = updateImportJobRequest.sendAndWait();
			}
		} finally {
			Utilities.cleanAll(organizationCodeNode, updateImportStatusNode, updateDataStatusNode, statusItemIdResponse,
					updateImportStatusResponse, jobIDNode);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true;
		String jobStatus = "Reading ZIPs completed";
		int count = 0, createNode = 0, importNode = 0, child = 0, importStatusNode = 0, OrganizationNode = 0, organizationCode=0,
				createImportStatusResponse = 0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);// entity web service to read job details and update count value
		try {
			for (; count < sheetData.size(); count++) {
				row = sheetData.get(count);
				if (row.get(getKey(ImportConstants.LEGACY_ID)) != null) {
					SOAPRequestObject createRequest = new SOAPRequestObject(
							"http://schemas/OpenTextContractCenterImport/OrganizationsImportStatus/operations",
							"CreateOrganizationsImportStatus", null, null);
					createNode = NomUtil.parseXML(
							"<OrganizationsImportStatus-create xmlns=\"http://schemas/OpenTextContractCenterImport/OrganizationsImportStatus\"></OrganizationsImportStatus-create>");
					importNode = NomUtil.parseXML("<RelatedJobID></RelatedJobID>");
					child = Node.createElementNS("DataImport-id", "", "",
							"http://schemas/OpenTextContractCenterImport/DataImport", importNode);
					Node.appendToChildren(NomUtil.parseXML("<Id>" + jobId + "</Id>"), child);
					Node.appendToChildren(importNode, createNode);
					OrganizationNode = NomUtil
							.parseXML("<LegacyID>" + row.get(getKey(ImportConstants.LEGACY_ID)) + "</LegacyID>");
					Node.appendToChildren(OrganizationNode, createNode);
					importStatusNode = NomUtil.parseXML("<ImportStatus>NotStarted</ImportStatus>");
					Node.appendToChildren(importStatusNode, createNode);
					createRequest.addParameterAsXml(createNode);
					createImportStatusResponse = createRequest.sendAndWait();
				}
			}
		} catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.organizations.OrganizationImportHandler", Severity.ERROR, e,
					"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		} finally {
			Utilities.cleanAll(createNode, importNode, child, importStatusNode, OrganizationNode,
					createImportStatusResponse);
		}
		updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump success"
		return status;
	}

	protected void generateErrorReport(String jobId, Path path, ImportConfiguration importparm) {
		int response = 0, nodes[] = null;
		try {
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(),
					ImportConstants.LEGACY_ID);
			if (null == rowData || rowData.size() == 0)
				return;
			SOAPRequestObject importRequest = new SOAPRequestObject(
					"http://schemas/OpenTextContractCenterImport/OrganizationsImportStatus/operations",
					"ReadOrganizationImportStatusbyJobID", null, null);
			int input = NomUtil.parseXML("<JobID>"+jobId+"</JobID>");
			// String input = "<Input></Input>";
			// String jobId = "<JobID>"+jodId+"</JobID>";
			//Node.appendToChildren(NomUtil.parseXML("<JobID>" + jodId + "</JobID>"), input);
			importRequest.addParameterAsXml(input);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//OrganizationsImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes) {
				String LegacyID = null, importStatus = null, logDetails = null;
				LegacyID = Node.getDataWithDefault(NomUtil.getNode(".//LegacyID", i), null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i), null);
				logDetails = Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i), null);
				if (rowData.get(LegacyID) != null) {
					Map<String, String> row = rowData.get(LegacyID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jobId+File.separatorChar+this.getSheetName()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} finally {
			if (nodes != null)
				Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}

	@Override
	protected void importContent(ImportConfiguration configuration) {
		this.importConfig = configuration;
		Properties properties = getMappingProperties();
		configuration.setMappedInstances(new LinkedHashMap<String, String>());
		List<Map<String, String>> sheetData = super.readContent(getSheetName());
		String jobId = configuration.getJobId();
		if (!configuration.isRetrigger()) {
			if (!statusDump(sheetData, jobId)) {
				logger._log("com.opentext.apps.cc.custom.AbstractImportHandler", Severity.ERROR, null,
						"Status dump is failed.");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.STATUS_DUMP_FAILED);
			}
		}

		Iterator<List<Map<String, String>>> batchIterator = new BatchIterator(sheetData, properties, sheetData.size());
		ArrayList<ImportListener> records = new ArrayList<>();
		while (batchIterator.hasNext()) {
			try {
				List<Map<String, String>> rows = batchIterator.next();
				for (Map<String, String> row : rows) {
					ImportListener record = processRow(configuration, row);
					if (null != record) {
						records.add(record);
					}
				}
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}

		List<ImportListener> properrecords = new ArrayList<ImportListener>();
		Map<String, List<OrganizationRecord>> skippedrecords = new HashMap<String, List<OrganizationRecord>>();
		for (ImportListener record : records) {
			OrganizationRecord orgrecord = (OrganizationRecord) record;
			if (Objects.nonNull(orgrecord)) {
				if (Objects.nonNull(orgrecord.getParentOrgCode())) {
					if (orgrecord.metadata.orgMap.containsKey(orgrecord.getParentOrgCode())) {
						properrecords.add(orgrecord);
						orgrecord.metadata.orgMap.put(orgrecord.getOrgCode(), orgrecord.getParentOrgCode());
						putOrg(properrecords, skippedrecords, orgrecord.getOrgCode());

					} else {
						if (skippedrecords.containsKey(orgrecord.getParentOrgCode())) {
							skippedrecords.get(orgrecord.getParentOrgCode()).add(orgrecord);
						} else {
							skippedrecords.put(orgrecord.getParentOrgCode(), new ArrayList<>(Arrays.asList(orgrecord)));
						}

					}
				} else {
					properrecords.add(orgrecord);
					orgrecord.metadata.orgMap.put(orgrecord.getOrgCode(), orgrecord.getParentOrgCode());
					putOrg(properrecords, skippedrecords, orgrecord.getOrgCode());
				}
			}
		}
		
//		if (Objects.nonNull(orgrecord)) {
//			if (Objects.nonNull(orgrecord.getParentOrgCode())) {
//				if (orgrecord.metadata.orgMap.containsKey(orgrecord.getParentOrgCode())) {
//					properrecords.add(orgrecord);
//					orgrecord.metadata.orgMap.put(orgrecord.getOrgCode(), orgrecord.getParentOrgCode());
//					putOrg(properrecords, skippedrecords, orgrecord.getOrgCode());
//
//				} else {
//					if (skippedrecords.containsKey(orgrecord.getParentOrgCode())) {
//						skippedrecords.get(orgrecord.getParentOrgCode()).add(orgrecord);
//					} else {
//						skippedrecords.put(orgrecord.getParentOrgCode(), new ArrayList<>(Arrays.asList(orgrecord)));
//					}
//
//				}
//			} else {
//				properrecords.add(orgrecord);
//				orgrecord.metadata.orgMap.put(orgrecord.getOrgCode(), orgrecord.getParentOrgCode());
//				putOrg(properrecords, skippedrecords, orgrecord.getOrgCode());
//			}
//		}
//	}

		int batchCount = 0;
		while (batchCount < properrecords.size()) {
			int endSize = (batchCount + BatchSize) > properrecords.size() ? properrecords.size()
					: batchCount + BatchSize;
			commit(properrecords.subList(batchCount, endSize));
			batchCount = endSize;
		}
		SkippedStatus(skippedrecords);
		batchuploadstatus="Completed";	
	}

}
