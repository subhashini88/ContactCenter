package com.opentext.apps.cc.importhandler.contract.relateddepartment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.util.logger.CordysLogger;
import com.eibus.util.logger.Severity;
import com.eibus.xml.nom.Document;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.AbstractImportHandler;
import com.opentext.apps.cc.importcontent.ContentManagementRuntimeException;
import com.opentext.apps.cc.importcontent.FileUtil;
import com.opentext.apps.cc.importcontent.ImportConfiguration;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importcontent.ReportListener;
import com.opentext.apps.cc.importcontent.ImportConstants.ImportHandler;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.opentext.apps.cc.importhandler.organizations.MetadataInitializer;

public class RelatedDepartmentsImportHandler extends AbstractImportHandler {

	private final MetadataInitializer orgMemMetadata;
	private final RelatedDepartmentMetadataInitializer departmentMetadataInitializer;
	private final RelatedDeptImportValidator validator;
	private static final CordysLogger logger = CordysLogger.getCordysLogger(RelatedDepartmentsImportHandler.class);

	public RelatedDepartmentsImportHandler() {
		orgMemMetadata = new MetadataInitializer();
		departmentMetadataInitializer = new RelatedDepartmentMetadataInitializer();
		validator = new RelatedDeptImportValidator();
	}

	@Override
	protected String getSheetName() {
		return RelatedDeptImportConstants.SHEET_NAME;
	}

	@Override
	protected ImportListener processRow(ImportConfiguration configuration, Map<String, String> row) {
		RelateddepartmentRecord relatedDepartmentrecord = null;
		if (row != null && !Utilities.isStringEmpty(row.get(RelatedDeptImportConstants.LEGACY_ID))) {
			ReportItem reportItem = validator.validate(row, orgMemMetadata,departmentMetadataInitializer, importConfig.getJobId());
			if (reportItem.getErrors().isEmpty()) {
				relatedDepartmentrecord = new RelateddepartmentRecord(orgMemMetadata, departmentMetadataInitializer,reportItem);
				relatedDepartmentrecord.doWork(new ImportEvent(relatedDepartmentrecord, row));
			} else {
				updateImportLogs(row.get(RelatedDeptImportConstants.LEGACY_ID), importConfig.getJobId(),
						reportItem.getErrors().toString());
			}
		}
		return relatedDepartmentrecord;
	}

	@Override
	protected void commit(Collection<ImportListener> records) {
		int relatedDepartmentsRecordNode = 0, jobIdNode = 0, relatedDepartmentsResponse = 0;
		if (records.size() > 0) {
			relatedDepartmentsRecordNode = NomUtil.parseXML("<RelatedDepartments></RelatedDepartments>");

			for (ImportListener record : records) {
				Node.appendToChildren(record.getnode(), relatedDepartmentsRecordNode);
			}

			jobIdNode = NomUtil.parseXML("<JobID>" + super.importConfig.getJobId() + "</JobID>");
			SOAPRequestObject clausesImportRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/contractcenterimport/16.3", "ContractRelatedDepartmentImport", null,
					null);
			clausesImportRequest.addParameterAsXml(jobIdNode);
			clausesImportRequest.addParameterAsXml(relatedDepartmentsRecordNode);

			try {
				relatedDepartmentsResponse = clausesImportRequest.execute();
			} catch (Exception e) {
				logger._log("com.opentext.apps.cc.importhandler.relatedDepartments", Severity.ERROR, e,
						"Error while executing RelatedDepartmentsImport");
				throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_EXECUTING,
						"RelatedDepartmentsImport");
			} finally {
				Utilities.cleanAll(relatedDepartmentsRecordNode, relatedDepartmentsResponse, jobIdNode);
			}
		}
	}

	private void updateImportLogs(String legacyID, String jobID, String errors) {
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int legacyIDNode = 0, jobIDNode = 0, getRelatedDepartmentsImportStatusbyLegacyIDResponse = 0,
				relatedDepartmentsImportStatusIDNode = 0, relatedDepartmentsImportStatusUpdateNode = 0,
				updateRelatedDepartmentsImportStatusResponse = 0;
		try {
			if (!Utilities.isStringEmpty(legacyID)) {
				SOAPRequestObject getClauseImportStatusbyLegacyIDRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/ContractRelatedDeptImportStatus/operations",
						"GetContractRelatedDeptStatusByLegacyId", null, null);
				legacyIDNode = NomUtil.parseXML("<LegacyID>" + legacyID + "</LegacyID>");
				jobIDNode = NomUtil.parseXML("<JobID>" + jobID + "</JobID>");
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(legacyIDNode);
				getClauseImportStatusbyLegacyIDRequest.addParameterAsXml(jobIDNode);
				getRelatedDepartmentsImportStatusbyLegacyIDResponse = getClauseImportStatusbyLegacyIDRequest
						.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(".//ContractRelatedDeptImportStatus-id/ItemId",
						getRelatedDepartmentsImportStatusbyLegacyIDResponse), null);

				SOAPRequestObject updateClauseImportStatusRequest = new SOAPRequestObject(
						"http://schemas/OpenTextContractCenterImport/ContractRelatedDeptImportStatus/operations",
						"UpdateContractRelatedDeptImportStatus", null, null);
				relatedDepartmentsImportStatusIDNode = document.createElement("ContractRelatedDeptImportStatus-id");
				Node.createTextElement("ItemId", itemId, relatedDepartmentsImportStatusIDNode);
				relatedDepartmentsImportStatusUpdateNode = document
						.createElement("ContractRelatedDeptImportStatus-update");
				Node.createTextElement("ImportStatus", "InvalidData", relatedDepartmentsImportStatusUpdateNode);
				Node.createTextElement("LogDetails", errors, relatedDepartmentsImportStatusUpdateNode);
				updateClauseImportStatusRequest.addParameterAsXml(relatedDepartmentsImportStatusIDNode);
				updateClauseImportStatusRequest.addParameterAsXml(relatedDepartmentsImportStatusUpdateNode);
				updateRelatedDepartmentsImportStatusResponse = updateClauseImportStatusRequest.sendAndWait();
			}
		} finally {
			Utilities.cleanAll(legacyIDNode, jobIDNode, getRelatedDepartmentsImportStatusbyLegacyIDResponse,
					relatedDepartmentsImportStatusIDNode, relatedDepartmentsImportStatusUpdateNode,
					updateRelatedDepartmentsImportStatusResponse);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
	}

	@Override
	protected boolean statusDump(List<Map<String, String>> sheetData, String jobId) {

		boolean status = true;
		String jobStatus = "Reading ZIPs completed";
		Document document = NOMDocumentPool.getInstance().lendDocument();
		int count = 0, createRelatedDepartmentsImportStatusResponse = 0, relatedDepartmentsImportStatusCreateNode = 0,
				dataImportIDNode = 0, legacyIDNode = 0;
		Map<String, String> row;
		count = getStatusDumpCount(jobId);
		try {
			for (; count < sheetData.size(); count++) {
				row = sheetData.get(count);
				if (row.get(getKey(RelatedDeptImportConstants.LEGACY_ID)) != null) {
					SOAPRequestObject createClauseImportStatusRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractRelatedDeptImportStatus/operations","CreateContractRelatedDeptImportStatus", null, null);
					relatedDepartmentsImportStatusCreateNode = NomUtil.parseXML("<ContractRelatedDeptImportStatus-create></ContractRelatedDeptImportStatus-create>");
					int relatedJobIDNode = Node.createElement("RelatedJobID", relatedDepartmentsImportStatusCreateNode);
					dataImportIDNode = Node.createElement("DataImport-id", relatedJobIDNode);
					Node.createTextElement("Id", jobId, dataImportIDNode);
					legacyIDNode = document.createElement("LegacyID");
					Node.createTextElement("LegacyID", row.get(getKey(RelatedDeptImportConstants.LEGACY_ID)),
							relatedDepartmentsImportStatusCreateNode);
					createClauseImportStatusRequest.addParameterAsXml(relatedDepartmentsImportStatusCreateNode);
					createRelatedDepartmentsImportStatusResponse = createClauseImportStatusRequest.sendAndWait();
				}
			}
		} catch (Exception e) {
			status = false;
			jobStatus = "Reading ZIPs failed";
			updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump failed"
			logger._log("com.opentext.apps.cc.importhandler.relatedDepartments", Severity.ERROR, e,
					"Error while creating dump.");
			throw new ContractCenterApplicationException(ContractCenterAlertMessages.ERROR_WHILE_CREATING_DUMP);
		} finally {
			Utilities.cleanAll(createRelatedDepartmentsImportStatusResponse, relatedDepartmentsImportStatusCreateNode,
					dataImportIDNode, legacyIDNode);
			NOMDocumentPool.getInstance().returnDocument(document);
		}
		updateImportJob(jobId, jobStatus, count);// update job with count and status "Dump success"
		return status;
	}

	@Override
	protected ReportListener createReportListener() {
		return null;
	}
	
	@SuppressWarnings("deprecation")
	protected void generateErrorReport(String jodId, Path path,ImportConfiguration importparm) 
	{
		int response = 0, jobIDNode=0, nodes[] = null;

		try
		{
			Map<String, Map<String, String>> rowData = super.getSheetData(path, this.getSheetName(), RelatedDeptImportConstants.LEGACY_ID);
			if(null == rowData || rowData.size() == 0) return;
			SOAPRequestObject importRequest = new SOAPRequestObject("http://schemas/OpenTextContractCenterImport/ContractRelatedDeptImportStatus/operations", "GetFailedContractRelDepStatusByJobID", null, null);
			jobIDNode = NomUtil.parseXML("<JobID>"+jodId+"</JobID>");
			importRequest.addParameterAsXml(jobIDNode);
			response = importRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//ContractRelatedDeptImportStatus", response);
			List<Map<String, String>> errorRecords = new LinkedList<Map<String, String>>();
			for (int i : nodes)
			{
				String contactID=null, importStatus = null, logDetails=null;
				contactID = Node.getDataWithDefault(NomUtil.getNode(".//LegacyID", i),null);
				importStatus = Node.getDataWithDefault(NomUtil.getNode(".//ImportStatus", i),null);
				logDetails =Node.getDataWithDefault(NomUtil.getNode(".//LogDetails", i),null);
				if(rowData.get(contactID) != null) {
					Map<String, String> row = rowData.get(contactID);
					row.put("ImportStatus", importStatus);
					row.put("Error", logDetails);
					errorRecords.add(row);
				}
			}
			super.createErrorFile(errorRecords,FileUtil.getDownloadReadPath()+jodId+File.separatorChar+ImportHandler.ContactMapping.name()+"_Error_Report.xlsx", this.getSheetName(),importparm);
		} 
		catch (IOException e) 
		{
			logger.error(e.getMessage(), e);
			throw new ContentManagementRuntimeException(e);
		} 
		finally 
		{ 
			if(nodes != null) Utilities.cleanAll(nodes);
			Utilities.cleanAll(response);
		}
	}
}
