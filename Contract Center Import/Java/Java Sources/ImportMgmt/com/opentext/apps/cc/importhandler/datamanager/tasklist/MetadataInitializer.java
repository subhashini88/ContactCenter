package com.opentext.apps.cc.importhandler.datamanager.tasklist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.eibus.xml.nom.internal.NOMDocumentPool;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.datamanager.tasklist.ImportConstants;

public class MetadataInitializer {
	
	public MetadataInitializer() {
		activityListsMap = new HashMap<String, Map<String, String>>();
		activityListsIDMap = new HashMap<String, String>();
		activityListSet = new HashSet<String>();
		activitySet = new HashSet<String>();
		loadActivityListsData();
		loadActivityListsProcesses();
	}
	
	public Map<String, String> personsMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> rolesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> processMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	public Map<String, String> statesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private Map<String, Map<String, String>> activityListsMap;
	private Map<String, String> activityListsIDMap;
	private Map<String, String> activityListProcessMap;
	private Set<String> activityListSet;
	private Set<String> activitySet;
	public String currentTaskListCode = "";
	
	public Map<String, Map<String, String>> getActivityLists() {
		if (Objects.isNull(activityListsMap)) {
			activityListsMap = new HashMap<>();
		}
		return activityListsMap;
	}

	public Map<String, String> getActivityListsID() {
		if (Objects.isNull(activityListsIDMap)) {
			activityListsIDMap = new HashMap<>();
		}
		return activityListsIDMap;
	}
	
	public Map<String, String> activityListProcesses() {
		if (Objects.isNull(activityListProcessMap)) {
			activityListProcessMap = new HashMap<>();
		}
		return activityListProcessMap;
	}
	
	public Set<String> getActivityListSet() {
		if (Objects.isNull(activityListSet)) {
			activityListSet = new HashSet<>();
		}
		return activityListSet;
	}
	
	public Set<String> getActivitySet() {
		if (Objects.isNull(activitySet)) {
			activitySet = new HashSet<>();
		}
		return activitySet;
	}
	
	protected void loadActivityListsData() {
		int creationTypeNode =0;
		int response = 0;
		int nodes[] = null;
		try {
			SOAPRequestObject GetActivitiesRequest = new SOAPRequestObject(
					"http://schemas.opentext.com/apps/cc/configworkflow/20.2",
					"GetActivitieswithFilter", null, null);
				creationTypeNode = NOMDocumentPool.getInstance().createElement(ImportConstants.CREATIONTYPE);
			NomUtil.setData(creationTypeNode, (ImportConstants.DEFAULT+","+ImportConstants.DEFAULTIMPORTED));
			GetActivitiesRequest.addParameterAsXml(creationTypeNode);
			response = GetActivitiesRequest.sendAndWait();
			nodes = NomUtil.getNodeList(".//FindZ_INT_ActivitiesListResponse/ContainingActivities", response);
			for (int i : nodes) {
				String activityListCode = Node.getDataWithDefault(NomUtil.getNode(".//Owner/Code", i), null);
				String activityListId = Node.getDataWithDefault(NomUtil.getNode(".//Owner/ActivityList-id/Id", i), null);
				String activityCode = Node.getDataWithDefault(NomUtil.getNode(".//ContaningActivity/Code", i), null);
				String activityId = Node.getDataWithDefault(NomUtil.getNode(".//ContaningActivity/Activity-id/Id", i), null);
				String containingActivityId = Node.getDataWithDefault(NomUtil.getNode(".//ContainingActivities-id/Id", i), null);
				if (null != activityListCode) {
					if(!getActivityLists().containsKey(activityListCode)) {
						 Map<String, String> activityMap = new HashMap<String, String>();
						 activityMap.put(activityCode, activityId+";"+containingActivityId);
						 getActivityLists().put(activityListCode, activityMap);
						 getActivityListsID().put(activityListCode, activityListId);
					}else {
						getActivityLists().get(activityListCode).put(activityCode, activityId+";"+containingActivityId);
					}
				}
			}
		} finally {
			if (null != nodes) {
				Utilities.cleanAll(response);
			}
		}
	}
	
	protected void loadActivityListsProcesses() {
		activityListProcesses().put("Contract", "CTRACTLIST");
		activityListProcesses().put("Clause", "CLAACTLIST");
		activityListProcesses().put("Template", "TEMACTLIST");
	}
	
}
