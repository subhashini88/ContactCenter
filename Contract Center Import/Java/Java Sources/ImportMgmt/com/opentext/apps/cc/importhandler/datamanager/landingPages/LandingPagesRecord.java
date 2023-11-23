package com.opentext.apps.cc.importhandler.datamanager.landingPages;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportEvent;
import com.opentext.apps.cc.importcontent.ImportListener;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importcontent.ReportItem;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterAlertMessages;
import com.opentext.apps.cc.importhandler.exceptions.ContractCenterApplicationException;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LandingPagesRecord implements ImportListener {

	public int processNode;
	private final MetadataInitializer metadata;
	private final ReportItem reportItem;
	private LandingPageViewType ldgPageViewType;
	


	public LandingPagesRecord(MetadataInitializer metadata, ReportItem reportItem,
			LandingPageViewType ldgPageViewType) {
		this.metadata = metadata;
		this.reportItem = reportItem;
		this.ldgPageViewType = ldgPageViewType;
	}

	@Override
	public void doWork(ImportEvent event) {
		switch (ldgPageViewType) {
		case GCLIST:
			createGCListIfnotExists(event);
			break;
		default:
			break;
		}
	}

	@Override
	public void commit() {

	}

	@Override
	public void postCommit() {
	}

	@Override
	public Object getSourceId() {
		return null;
	}

	@Override
	public int getnode() {
		return this.processNode;
	}

	public ReportItem getReportItem() {
		return this.reportItem;
	}

	public int getProcessNode() {
		return processNode;
	}

	private void createGCListIfnotExists(ImportEvent event) {

		int createNode = 0, updateNode = 0, listResponse = 0, roleViewResponse = 0;
		int relatedRole = 0, relatedRoleIdNode = 0, relatedRoleItemIdNode = 0,relatedList = 0, relatedListIdNode = 0, relatedListItemIdNode = 0;
		Map<String, String> row = event.getRow();
		Map<String, String> ListDetailsMap = null;
		try {
			if (Objects.nonNull(row)
					&& !metadata.getLists().containsKey(row.get(ImportConstants.CODE))) {
				ListDetailsMap = null;
				SOAPRequestObject createList = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCList/operations",
						"CreateGCList", null, null);
				createNode = NomUtil.parseXML("<GCList-create></GCList-create>");
				
				Node.setDataElement(createNode, "Code", row.get(ImportConstants.CODE));
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.LISTNAME));
				Node.setDataElement(createNode, "DisplayName", row.get(ImportConstants.LISTDISPLAYNAME));
				Node.setDataElement(createNode, "ListType", row.get(ImportConstants.TYPE));
				Node.setDataElement(createNode, "Description", row.get(ImportConstants.DESCRIPTION));
				Node.setDataElement(createNode, "DefaultColumns", row.get(ImportConstants.DEFAULTCOLUMNS));
				Node.setDataElement(createNode, "ListMetadata", row.get(ImportConstants.LISTDETAILSJSON));
				createList.addParameterAsXml(createNode);
				listResponse = createList.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//GCList/GCList-id/ItemId", listResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", listResponse), null));
				} else {
					metadata.addListToMap(row.get(ImportConstants.CODE), itemId);
					ObjectMapper mapper = new ObjectMapper();
			        String json = row.get(ImportConstants.ROLEVIEWJSON);
			        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			        RoleView[] roleViews = mapper.readValue(json, RoleView[].class);
			        for (RoleView roleView : roleViews) { 	
			        	if(!Objects.isNull(metadata.getRoleNames().get(roleView.getRole())) && !metadata.getRoleViews().containsKey(metadata.getRoleNames().get(roleView.getRole()))) {
				        	SOAPRequestObject createRoleView = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
									"CreateGCViewRoleMapping", null, null);
				        	createNode = 0;relatedRole = 0;relatedRoleIdNode = 0;relatedRoleItemIdNode = 0;relatedList = 0;relatedListIdNode = 0;relatedListItemIdNode = 0;
							createNode = NomUtil.parseXML("<GCViewRoleMapping-create></GCViewRoleMapping-create>");
							
							Node.setDataElement(createNode, "Order", Integer.toString(roleView.getOrder()));
							Node.setDataElement(createNode, "IsDefault", roleView.getIsDefault());
							Node.setDataElement(createNode, "ViewType", ImportConstants.LIST);
							relatedRole = NomUtil.parseXML("<RelatedRole></RelatedRole>");
							relatedRoleIdNode = NomUtil.parseXML("<Identity-id></Identity-id>");
							relatedRoleItemIdNode = NomUtil.parseXML("<ItemId></ItemId>");
							if(!Objects.isNull(metadata.getRoleNames().get(roleView.getRole()))) {
								NomUtil.setData(relatedRoleItemIdNode, metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())));
							}else{
								throw new ContractCenterApplicationException(ContractCenterAlertMessages.ROLE_NOT_FOUND+" : "+roleView.getRole());
							}
							NomUtil.appendChild(relatedRoleItemIdNode,relatedRoleIdNode);
							NomUtil.appendChild(relatedRoleIdNode,relatedRole);
							NomUtil.appendChild(relatedRole,createNode);
							relatedList = NomUtil.parseXML("<RelatedDefaultList></RelatedDefaultList>");
							relatedListIdNode = NomUtil.parseXML("<GCList-id></GCList-id>");
							relatedListItemIdNode = NomUtil.parseXML("<ItemId></ItemId>");
							NomUtil.setData(relatedListItemIdNode, itemId);
							NomUtil.appendChild(relatedListItemIdNode,relatedListIdNode);
							NomUtil.appendChild(relatedListIdNode,relatedList);
							NomUtil.appendChild(relatedList,createNode);
							
							createRoleView.addParameterAsXml(createNode);
							roleViewResponse = createRoleView.sendAndWait();
							String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
									".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", roleViewResponse), null);
							if (Objects.isNull(roleViewItemId)) {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
								row.put(ImportConstants.STATUS_LOG, Node
										.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", roleViewResponse), null));
							}
							else {
								metadata.addRoleViewToMap(metadata.getRoleNames().get(roleView.getRole()), roleViewItemId);
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
								row.put(ImportConstants.STATUS_LOG, "RoleView creation is success.");
							}
			        	}else{
							throw new ContractCenterApplicationException(ContractCenterAlertMessages.ROLE_NOT_FOUND+" : "+roleView.getRole());
						}
		            }
			        metadata.clearRoleViewMap();
			        row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "List creation is success.");
					
				}
			} else {
				// Update existing as corresponding code already exists
				SOAPRequestObject updateList = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCList/operations",
						"UpdateGCList", null, null);
				int listIDNode = NomUtil.parseXML("<GCList-id></GCList-id>");
				Node.setDataElement(listIDNode, "ItemId", metadata.getLists().get(row.get(ImportConstants.CODE)));
				updateNode = NomUtil.parseXML("<GCList-update></GCList-update>");
				Node.setDataElement(updateNode, "Name", row.get(ImportConstants.LISTNAME));
				Node.setDataElement(updateNode, "DisplayName", row.get(ImportConstants.LISTDISPLAYNAME));
				Node.setDataElement(updateNode, "ListType", row.get(ImportConstants.TYPE));
				Node.setDataElement(updateNode, "Description", row.get(ImportConstants.DESCRIPTION));
				Node.setDataElement(updateNode, "DefaultColumns", row.get(ImportConstants.DEFAULTCOLUMNS));
				Node.setDataElement(updateNode, "ListMetadata", row.get(ImportConstants.LISTDETAILSJSON));
				updateList.addParameterAsXml(listIDNode);
				updateList.addParameterAsXml(updateNode);
				listResponse = updateList.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//GCList/GCList-id/ItemId", listResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", listResponse), null));
				} else {
					metadata.loadViewRoleMappings(itemId, ImportConstants.GCLIST_ENTITY_NAME);
					HashMap<String, String> RoleViewsCopy = new HashMap<>(metadata.getRoleViews());
					ObjectMapper mapper = new ObjectMapper();
			        String json = row.get(ImportConstants.ROLEVIEWJSON);
			        RoleView[] roleViews = mapper.readValue(json, RoleView[].class);
			        for (RoleView roleView : roleViews) {
			        	if(!Objects.isNull(metadata.getRoleNames().get(roleView.getRole())) && metadata.getRoleViews().containsKey(metadata.getRoleNames().get(roleView.getRole()))) {
			        		SOAPRequestObject updateRoleView = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
									"UpdateGCViewRoleMapping", null, null);
			        		int roleViewIDNode = NomUtil.parseXML("<GCViewRoleMapping-id></GCViewRoleMapping-id>");
							Node.setDataElement(roleViewIDNode, "ItemId", metadata.getRoleViews().get(metadata.getRoleNames().get(roleView.getRole())));
							updateNode = NomUtil.parseXML("<GCViewRoleMapping-update></GCViewRoleMapping-update>");
							Node.setDataElement(updateNode, "Order", Integer.toString(roleView.getOrder()));
							Node.setDataElement(updateNode, "IsDefault", roleView.getIsDefault());
							Node.setDataElement(updateNode, "ViewType", "LIST");
							updateRoleView.addParameterAsXml(roleViewIDNode);
							updateRoleView.addParameterAsXml(updateNode);
							roleViewResponse = updateRoleView.sendAndWait();
							String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
									".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", roleViewResponse), null);
							if (Objects.isNull(roleViewItemId)) {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
								row.put(ImportConstants.STATUS_LOG, Node
										.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", roleViewResponse), null));
							}
							else {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
								row.put(ImportConstants.STATUS_LOG, "RoleView update is success.");
							}
							if(RoleViewsCopy.containsKey(metadata.getRoleNames().get(roleView.getRole())))
								RoleViewsCopy.remove(metadata.getRoleNames().get(roleView.getRole()));
							
			        	}
			        	else {
			        		SOAPRequestObject createRoleView = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
									"CreateGCViewRoleMapping", null, null);
				        	createNode = 0;relatedRole = 0;relatedRoleIdNode = 0;relatedRoleItemIdNode = 0;relatedList = 0;relatedListIdNode = 0;relatedListItemIdNode = 0;
							createNode = NomUtil.parseXML("<GCViewRoleMapping-create></GCViewRoleMapping-create>");
							
							Node.setDataElement(createNode, "Order", Integer.toString(roleView.getOrder()));
							Node.setDataElement(createNode, "IsDefault", roleView.getIsDefault());
							Node.setDataElement(createNode, "ViewType", "LIST");
							relatedRole = NomUtil.parseXML("<RelatedRole></RelatedRole>");
							relatedRoleIdNode = NomUtil.parseXML("<Identity-id></Identity-id>");
							relatedRoleItemIdNode = NomUtil.parseXML("<ItemId></ItemId>");
							if(!Objects.isNull(metadata.getRoleNames().get(roleView.getRole()))) {
								NomUtil.setData(relatedRoleItemIdNode, metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())));
							}else{
								throw new ContractCenterApplicationException(ContractCenterAlertMessages.ROLE_NOT_FOUND);
							}
							NomUtil.appendChild(relatedRoleItemIdNode,relatedRoleIdNode);
							NomUtil.appendChild(relatedRoleIdNode,relatedRole);
							NomUtil.appendChild(relatedRole,createNode);
							relatedList = NomUtil.parseXML("<RelatedDefaultList></RelatedDefaultList>");
							relatedListIdNode = NomUtil.parseXML("<GCList-id></GCList-id>");
							relatedListItemIdNode = NomUtil.parseXML("<ItemId></ItemId>");
							NomUtil.setData(relatedListItemIdNode, itemId);
							NomUtil.appendChild(relatedListItemIdNode,relatedListIdNode);
							NomUtil.appendChild(relatedListIdNode,relatedList);
							NomUtil.appendChild(relatedList,createNode);
							
							createRoleView.addParameterAsXml(createNode);
							roleViewResponse = createRoleView.sendAndWait();
							String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
									".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", roleViewResponse), null);
							if (Objects.isNull(roleViewItemId)) {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
								row.put(ImportConstants.STATUS_LOG, Node
										.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", roleViewResponse), null));
							}
							else {
								metadata.addRoleViewToMap(metadata.getRoleNames().get(roleView.getRole()), roleViewItemId);
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
								row.put(ImportConstants.STATUS_LOG, "RoleView creation is success.");
							}
			        	}
				}
			        for (Map.Entry<String, String> roleView : RoleViewsCopy.entrySet()) {
			        	SOAPRequestObject updateRoleView = new SOAPRequestObject(
								"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
								"DeleteGCViewRoleMapping", null, null);
		        		int roleViewIDNode = NomUtil.parseXML("<GCViewRoleMapping-id></GCViewRoleMapping-id>");
						Node.setDataElement(roleViewIDNode, "ItemId", roleView.getValue());
						updateRoleView.addParameterAsXml(roleViewIDNode);
						roleViewResponse = updateRoleView.sendAndWait();
			        }
			        metadata.clearRoleViewMap();
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Record already exists");
			}
		}
		}catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}finally {
			Utilities.cleanAll(createNode, updateNode, listResponse, roleViewResponse);
		}
	}

	private void createGCChartIfnotExists(ImportEvent event) {

		int createNode = 0, updateNode = 0, listResponse = 0, roleViewResponse = 0;
		Map<String, String> row = event.getRow();
		Map<String, String> ListDetailsMap = null;
		try {
			if (Objects.nonNull(row)
					&& !metadata.getLists().containsKey(row.get(ImportConstants.CODE))) {
				SOAPRequestObject createList = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCList/operations",
						"CreateGCList", null, null);
				createNode = NomUtil.parseXML("<GCList-create></GCList-create>");
				
				Node.setDataElement(createNode, "Code", row.get(ImportConstants.CODE));
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.LISTNAME));
				Node.setDataElement(createNode, "DisplayName", row.get(ImportConstants.LISTDISPLAYNAME));
				Node.setDataElement(createNode, "ListType", row.get(ImportConstants.TYPE));
				Node.setDataElement(createNode, "Description", row.get(ImportConstants.DESCRIPTION));
				Node.setDataElement(createNode, "DefaultColumns", row.get(ImportConstants.DEFAULTCOLUMNS));
				Node.setDataElement(createNode, "ListMetadata", row.get(ImportConstants.LISTDETAILSJSON));
				createList.addParameterAsXml(createNode);
				listResponse = createList.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//GCList/GCList-id/ItemId", listResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", listResponse), null));
				} else {
					metadata.addListToMap(row.get(ImportConstants.CODE), itemId);
					ObjectMapper mapper = new ObjectMapper();
			        String json = row.get(ImportConstants.ROLEVIEWJSON);
			        RoleView[] roleViews = mapper.readValue(json, RoleView[].class);
			        for (RoleView roleView : roleViews) {
			        	SOAPRequestObject createRoleView = new SOAPRequestObject(
								"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
								"CreateGCViewRoleMapping", null, null);
						createNode = NomUtil.parseXML("<GCViewRoleMapping-create></GCViewRoleMapping-create>");
						
						Node.setDataElement(createNode, "Order", row.get(ImportConstants.ORDER));
						Node.setDataElement(createNode, "IsDefault", row.get(ImportConstants.ISDEFAULT));
						Node.setDataElement(createNode, "ViewType", "LIST");
						int relatedRole = NOMDocumentPool.getInstance().createElement("RelatedRole");
						int relatedRoleIdNode = NOMDocumentPool.getInstance().createElement("Identity-id");
						Node.setDataElement(relatedRoleIdNode, "ItemId", metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())));
						NomUtil.appendChild(relatedRole,relatedRoleIdNode);
						NomUtil.appendChild(createNode,relatedRole);
						int relatedList = NOMDocumentPool.getInstance().createElement("RelatedDefaultList");
						int relatedListIdNode = NOMDocumentPool.getInstance().createElement("GCList-id");
						Node.setDataElement(relatedListIdNode, "ItemId", itemId);
						NomUtil.appendChild(relatedList,relatedListIdNode);
						NomUtil.appendChild(createNode,relatedList);
						
						createRoleView.addParameterAsXml(createNode);
						roleViewResponse = createRoleView.sendAndWait();
						String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
								".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", roleViewResponse), null);
						if (Objects.isNull(roleViewItemId)) {
							row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
							row.put(ImportConstants.STATUS_LOG, Node
									.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", roleViewResponse), null));
						}
						else {
							metadata.addRoleViewToMap(metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())), roleViewItemId);
							row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
							row.put(ImportConstants.STATUS_LOG, "RoleView creation is success.");
						}
						
		            }
			        metadata.clearRoleViewMap();
			        row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
					row.put(ImportConstants.STATUS_LOG, "List creation is success.");
					
				}
			} else {
				// Update existing as corresponding code already exists
				SOAPRequestObject updateList = new SOAPRequestObject(
						"http://schemas/OpenTextBasicComponents/GCList/operations",
						"UpdateGCList", null, null);
				int listIDNode = NomUtil.parseXML("<GCList-id></GCList-id>");
				Node.setDataElement(listIDNode, "ItemId", metadata.getLists().get(row.get(ImportConstants.CODE)));
				updateNode = NomUtil.parseXML("<GCList-update></GCList-update>");
				Node.setDataElement(createNode, "Name", row.get(ImportConstants.LISTNAME));
				Node.setDataElement(createNode, "DisplayName", row.get(ImportConstants.LISTDISPLAYNAME));
				Node.setDataElement(createNode, "ListType", row.get(ImportConstants.TYPE));
				Node.setDataElement(createNode, "Description", row.get(ImportConstants.DESCRIPTION));
				Node.setDataElement(createNode, "DefaultColumns", row.get(ImportConstants.DEFAULTCOLUMNS));
				Node.setDataElement(createNode, "ListMetadata", row.get(ImportConstants.LISTDETAILSJSON));
				updateList.addParameterAsXml(listIDNode);
				updateList.addParameterAsXml(updateNode);
				listResponse = updateList.sendAndWait();
				String itemId = Node.getDataWithDefault(NomUtil.getNode(
						".//GCList/GCList-id/ItemId", listResponse), null);
				if (Objects.isNull(itemId)) {
					row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
					row.put(ImportConstants.STATUS_LOG, Node
							.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", listResponse), null));
				} else {
					metadata.loadViewRoleMappings(itemId, ImportConstants.GCLIST_ENTITY_NAME);
					HashMap<String, String> RoleViewsCopy = new HashMap<>(metadata.getRoleViews());
					ObjectMapper mapper = new ObjectMapper();
			        String json = row.get(ImportConstants.ROLEVIEWJSON);
			        RoleView[] roleViews = mapper.readValue(json, RoleView[].class);
			        for (RoleView roleView : roleViews) {
			        	if(metadata.getRoleViews().containsKey(metadata.getRoleNames().get(roleView.getRole()))) {
			        		SOAPRequestObject updateRoleView = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
									"UpdateGCViewRoleMapping", null, null);
			        		int roleViewIDNode = NomUtil.parseXML("<GCViewRoleMapping-id></GCViewRoleMapping-id>");
							Node.setDataElement(roleViewIDNode, "ItemId", metadata.getRoleViews().get(metadata.getRoleNames().get(roleView.getRole())));
							updateNode = NomUtil.parseXML("<GCViewRoleMapping-update></GCViewRoleMapping-update>");
							Node.setDataElement(updateNode, "Order", row.get(ImportConstants.ORDER));
							Node.setDataElement(updateNode, "IsDefault", row.get(ImportConstants.ISDEFAULT));
							Node.setDataElement(updateNode, "ViewType", "LIST");
							updateRoleView.addParameterAsXml(updateNode);
							roleViewResponse = updateRoleView.sendAndWait();
							String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
									".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", roleViewResponse), null);
							if (Objects.isNull(roleViewItemId)) {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
								row.put(ImportConstants.STATUS_LOG, Node
										.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", roleViewResponse), null));
							}
							else {
								metadata.addRoleViewToMap(metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())), roleViewItemId);
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
								row.put(ImportConstants.STATUS_LOG, "RoleView update is success.");
							}
							RoleViewsCopy.remove(metadata.getRoleNames().get(roleView.getRole()));
							
			        	}
			        	else {
				        	SOAPRequestObject createRoleView = new SOAPRequestObject(
									"http://schemas/OpenTextBasicComponents/GCViewRoleMapping/operations",
									"CreateGCViewRoleMapping", null, null);
							createNode = NomUtil.parseXML("<GCViewRoleMapping-create></GCViewRoleMapping-create>");
							
							Node.setDataElement(createNode, "Order", row.get(ImportConstants.ORDER));
							Node.setDataElement(createNode, "IsDefault", row.get(ImportConstants.ISDEFAULT));
							Node.setDataElement(createNode, "ViewType", "LIST");
							int relatedRole = NOMDocumentPool.getInstance().createElement("RelatedRole");
							int relatedRoleIdNode = NOMDocumentPool.getInstance().createElement("Identity-id");
							Node.setDataElement(relatedRoleIdNode, "ItemId", metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())));
							NomUtil.appendChild(relatedRole,relatedRoleIdNode);
							NomUtil.appendChild(createNode,relatedRole);
							int relatedList = NOMDocumentPool.getInstance().createElement("RelatedDefaultList");
							int relatedListIdNode = NOMDocumentPool.getInstance().createElement("GCList-id");
							Node.setDataElement(relatedListIdNode, "ItemId", itemId);
							NomUtil.appendChild(relatedList,relatedListIdNode);
							NomUtil.appendChild(createNode,relatedList);
							
							createRoleView.addParameterAsXml(createNode);
							roleViewResponse = createRoleView.sendAndWait();
							String roleViewItemId = Node.getDataWithDefault(NomUtil.getNode(
									".//GCViewRoleMapping/GCViewRoleMapping-id/ItemId", listResponse), null);
							if (Objects.isNull(roleViewItemId)) {
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
								row.put(ImportConstants.STATUS_LOG, Node
										.getDataWithDefault(NomUtil.getNode(".//Fault/faultstring", listResponse), null));
							}
							else {
								metadata.addRoleViewToMap(metadata.getRoles().get(metadata.getRoleNames().get(roleView.getRole())), itemId);
								row.put(ImportConstants.STATUS, ImportConstants.STATUS_SUCESS);
								row.put(ImportConstants.STATUS_LOG, "RoleView creation is success.");
							}
			        	}
				}
			        metadata.clearRoleViewMap();
					row.put(ImportConstants.STATUS, ImportConstants.RECORD_EXISTS);
					row.put(ImportConstants.STATUS_LOG, "Record already exists");
			}
		}
		}catch (Exception e) {
			row.put(ImportConstants.STATUS, ImportConstants.STATUS_ERROR);
			row.put(ImportConstants.STATUS_LOG, e.getMessage());
		}finally {
			Utilities.cleanAll(createNode, updateNode, listResponse, roleViewResponse);
		}
	}

}

class RoleView {
	private String role;
	private String isDefault;
	private Integer order;
    
    public String getRole()
	{
		return role;
	}
    
    public String getIsDefault()
	{
		return isDefault;
	}
    
    public Integer getOrder()
	{
		return order;
	}
}
