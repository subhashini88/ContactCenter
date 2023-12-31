/*
  This class has been generated by the Code Generator
 */

package com.opentext.apps.cc.custom;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.cordys.cpc.bsf.busobject.BusObjectConfig;
import com.cordys.cpc.bsf.busobject.BusObjectIterator;
import com.cordys.cpc.bsf.soap.SOAPRequestObject;
import com.eibus.xml.nom.Node;
import com.eibus.xml.xpath.XPath;
import com.eibus.xml.xpath.XPathMetaInfo;


public class CaseTargetStatePath extends CaseTargetStatePathBase
{
	private static String getcaseModelXML = "<GetCaseModel><model name=\"\" space=\"\" /><status><status></status></status></GetCaseModel>";
	private static XPathMetaInfo metaInfo = new XPathMetaInfo();
	//private static final CordysLogger logger = CordysLogger.getCordysLogger(CaseTargetStatePath.class);


	public CaseTargetStatePath()
	{
		this((BusObjectConfig)null);
	}

	public CaseTargetStatePath(BusObjectConfig config)
	{
		super(config);
	}

	public static BusObjectIterator<com.opentext.apps.cc.custom.CaseTargetStatePath> getCaseTargetStatePathObjects(com.cordys.cpc.bsf.query.Cursor cursor)
	{
		// TODO implement body
		return null;
	}

    public static int getTargetStatePath(String currentState, String targetState, String modelFQN)
	{
		//Read case model based on the case model FQN:
		int caseModelNode = 0, caseSCXML = 0, transitionPath = 0;
		try
		{
			SOAPRequestObject getCaseModelReq = getcaseModelRequest();
			caseModelNode = Utilities.parseXML(getcaseModelXML);
			int modelNode = Utilities.getNode(".//model", caseModelNode);
			Utilities.setAttribute(modelNode, "name",modelFQN);
			int childNode = Node.getFirstChild(caseModelNode);
			for (int j = 0; j < Node.getNumChildren(caseModelNode); j++) 
			{
				getCaseModelReq.addParameterAsXml(childNode);
				final int tempnode = Node.getNextElement(childNode);
				childNode = tempnode;
			}
			caseSCXML = getCaseModelReq.execute();
			transitionPath = prepareCaseTransitions(caseSCXML,currentState,targetState);
			if(transitionPath==0) {
				caseSCXML = NomUtil.parseXML(ContractConstants.CONTRACT_CASE_MODEL_XML.toString());
				transitionPath = prepareCaseTransitions(caseSCXML,currentState,targetState);
			}
		}
		finally
		{
			Utilities.cleanAll(caseModelNode,caseSCXML);
		}
		return transitionPath;
	}
    private static int prepareCaseTransitions(int caseModel, String currentState, String targetState)
    {
    	Map<String,String> caseStates = new HashMap<>();
    	int transitionsListNode = 0;
    	Map<String,HashMap<String,String>> stateTargetDetails = new HashMap<>();
    	String targetStateId = null, finalPath = null;
    	LinkedList<String> sourceStatesQueue = new LinkedList<>();
    	metaInfo.addNamespaceBinding("sm","http://www.w3.org/2005/07/scxml");
    	int[] caseStatesNodes = XPath.getMatchingNodes(".//sm:state", metaInfo, caseModel);
    	for(int caseState : caseStatesNodes)
    	{
    		String stateId = Node.getAttribute(caseState, "id");
    		String stateName = Node.getAttribute(caseState, "name");
    		caseStates.put(stateId, stateName);
    		if(currentState.equals(stateName))
    		{
    			sourceStatesQueue.add(stateId);
    		}
    		if(targetState.equals(stateName))
    		{
    			targetStateId = stateId;
    		}
    	}
    	if(currentState.equals(targetState))
    	{
    		//logger.warn("Source and Target state is same");
    	}
    	else if(!sourceStatesQueue.isEmpty() && !Utilities.isStringEmpty(targetStateId))
    	{
    		//Fetching the available path from source to target state using BFS on state machine tree.
    		do
    		{
    			String currentSoruceState = sourceStatesQueue.remove();
    			HashMap<String,String> activityTargetDetails = fetchStateTransitions(caseModel, currentSoruceState, stateTargetDetails);
    			if(activityTargetDetails.containsKey(targetStateId))
        		{
    				finalPath = currentSoruceState+"_"+targetStateId;
    				break;
        		}
    			else
    			{
    				finalPath = getNextSourceStates(currentSoruceState, sourceStatesQueue, caseModel, targetStateId, stateTargetDetails);
    				if(finalPath != null)
    				{
    					break;
    				}
    			}
    		}while(!sourceStatesQueue.isEmpty());
    		sourceStatesQueue.clear();
    		transitionsListNode = prepareEventTransitions(finalPath, stateTargetDetails, caseStates);
    	}
    	else
    	{
    		//logger.error("State not found");
    	}
    	return transitionsListNode;
    }
    
    private static int prepareEventTransitions(String finalTransitionPath, Map<String,HashMap<String,String>> stateTargetDetails, Map<String,String> caseStates)
    {
    	String transition = "<transition><state></state><event></event></transition>";
    	String transitions = "<Transitions></Transitions>";
    	int transitionsListNode = Utilities.parseXML(transitions);
    	String[] transitionStates = finalTransitionPath.split("_");
    	for(int i=0;i<transitionStates.length-1;i++)
    	{
    		int transitionNode = Utilities.parseXML(transition);
    		String sourceState = transitionStates[i];
    		String targetState = transitionStates[i+1];
    		HashMap<String,String> sourceStateTargets = stateTargetDetails.get(sourceState);
    		String eventToBeRaised = sourceStateTargets.get(targetState);
    		Utilities.setData(transitionNode, ".//state",caseStates.get(sourceState));
    		Utilities.setData(transitionNode, ".//event",eventToBeRaised);
    		Utilities.appendChild(transitionNode, transitionsListNode);
    	}
    	return transitionsListNode;
    }
    
    private static String getNextSourceStates(String currentSoruceState, LinkedList<String> sourceStatesQueue ,int caseModel, String targetStateId, Map<String,HashMap<String,String>> stateTargetDetails)
    {
    	String finalPath = null;
    	HashMap<String,String> activityTargetDetails = fetchStateTransitions(caseModel, currentSoruceState, stateTargetDetails);
    	if(activityTargetDetails.containsKey(targetStateId))
		{
    		finalPath = currentSoruceState+"_"+targetStateId;
		}
    	else
    	{
    		for(String target : activityTargetDetails.keySet())
        	{
        		sourceStatesQueue.addLast(currentSoruceState+"_"+target);
        	}
    	}
    	return finalPath;
    }
    
    private static HashMap<String,String> fetchStateTransitions(int caseModel, String currentStateId, Map<String,HashMap<String,String>> stateTargetDetails)
    {
    	String tempCurrentState = currentStateId.substring(currentStateId.lastIndexOf('_')+1);
    	int currentStateDetails = XPath.getFirstMatch(".//sm:state[@id='"+tempCurrentState+"']", metaInfo, caseModel);
    	HashMap<String,String> ActivityTargetDetails = new HashMap<>();
    	if(currentStateDetails > 0)
    	{
    		int[] stateTransitions = XPath.getMatchingNodes(".//sm:transition[@target]", metaInfo, currentStateDetails);
    		for(int stateTransition : stateTransitions)
    		{
    			String eventDes = Node.getAttribute(stateTransition, "event");
    			if(!Utilities.isStringEmpty(eventDes))
    			{
    				String targetId = Node.getAttribute(stateTransition, "target");
            		ActivityTargetDetails.put(targetId, eventDes);
    			}
    		}
    		stateTargetDetails.put(tempCurrentState, ActivityTargetDetails);
    	}
    	
		return ActivityTargetDetails;
    }
    
	private static SOAPRequestObject getcaseModelRequest()
	{
		return new SOAPRequestObject("http://schemas.cordys.com/casemanagement/modeladministration/1.0", "GetCaseModel", null, null);
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
}
