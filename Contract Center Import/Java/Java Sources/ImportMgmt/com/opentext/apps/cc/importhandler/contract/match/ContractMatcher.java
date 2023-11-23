package com.opentext.apps.cc.importhandler.contract.match;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cordys.cpc.bsf.soap.SOAPRequestObject;

import com.eibus.xml.nom.Node;
import com.opentext.apps.cc.custom.Utilities;
import com.opentext.apps.cc.importcontent.ImportUtils;
import com.opentext.apps.cc.importcontent.NomUtil;
import com.opentext.apps.cc.importhandler.contract.ImportConstants;

public class ContractMatcher {

	private static Map<String, String> contratMap = new HashMap<String, String>();//(Key, Value) = (ContractId, ContractItemId)
	
	public String matchContract(Map<String, String> contractData, List<Map<String, String>> contractLines, List<Map<String, String>> billerPeriods) throws ContractMatchException {
		String contractId = null;
		Set<String> contracts = null;
		if (!ImportUtils.isEmpty(contractData.get(ImportConstants.OVERRIDE_MATCH_DOCUMENT_ID))) {// Match with override document id: Check if contract exists with ContractNumber(For migrated date) TODO - or Id of the contract(For newly created data)
			contractId = this.getContractDetailsByCNumber(contractData.get(ImportConstants.OVERRIDE_MATCH_DOCUMENT_ID));// TODO - Need to check for BillingStatus='Active'
			if (ImportUtils.isEmpty(contractId)) {
				throw new ContractMatchException("No active contracts found with the given override match document ID: "+ contractData.get(ImportConstants.OVERRIDE_MATCH_DOCUMENT_ID));
			}
		} else if (!ImportUtils.isEmpty(contractData.get(ImportConstants.COLLECTION_ACCOUNT))) { // Match by collection account#/process fees
			// BillingStatus as 'Active'/Null and matching with any one of the contract line (contract -> contract_service ->pg_service)
			contracts = getContractByCollectionAccountAndContractLines(contractData.get(ImportConstants.COLLECTION_ACCOUNT), this.getServices(contractLines));// Should match with collection #, BillingStatus as 'Active'/Null, anyone contract line order by effectivedate
			if (contracts.size() == 1) {
				String currency = getContractCurrency(contracts.iterator().next());
				if (contractData.get(ImportConstants.CURRENCY_ID).equalsIgnoreCase(currency)) {
					contractId = contracts.iterator().next();
				} else {
					throw new ContractMatchException("FAILURE: Acct#/Svc match failed due to currency");
				}
			} else if (contracts.size() > 1) {
				if (!ImportUtils.isEmpty(billerPeriods) && this.getFirstLevelProcessingFee(billerPeriods) > 0) {
					contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, true);
					if (null == contractId) {
						// If you r here means no matching contract found with 1st level process fee Match with any level processing fee
						contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, false);
					}
					if (null == contractId) {
						// If you r here means no matching contract found with 1st level process fee and any level processing fee Iterate over contracts found by collection account #, match currency, return
						// first matched contract
						Iterator<String> contractIterator = contracts.iterator();
						while (contractIterator.hasNext()) {
							String tempId = contractIterator.next();
							String currency = getContractCurrency(tempId);
							if (contractData.get(ImportConstants.CURRENCY_ID).equals(currency)) {
								contractId = tempId;
							}
						}
					}
					if (null == contractId) {
						throw new ContractMatchException("FAILURE: Acct#/Svc/Eff Date failed due to currency");
					}
				} else {// Level 1 process fee is null Match with any level processing fee
					contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, false);
					if (null == contractId) {
						// If you r here means no matching contract found any level processing fee
						// Iterate over contracts found by collection account #, match currency, return
						// first matched contract
						Iterator<String> contractIterator = contracts.iterator();
						while (contractIterator.hasNext()) {
							String tempId = contractIterator.next();
							String currency = getContractCurrency(tempId);
							if (contractData.get(ImportConstants.CURRENCY_ID).equals(currency)) {
								contractId = tempId;
							}
						}
					}
					if (null == contractId) {
						throw new ContractMatchException("FAILURE: Acct#/Svc/Eff Date (Any) failed due to currency");
					}
				}
			}
		}
		if(null == contractId) {//Name match 
			//Check if contracts exists with given account name but different collection account, report error 
			Set<String> contractIds = this.contractsByClientAndDiffAccount(contractData.get(ImportConstants.SECOND_PARTY_ID), contractData.get(ImportConstants.COLLECTION_ACCOUNT));//Get contracts by 
			if(!ImportUtils.isEmpty(contractIds)) {
				throw new ContractMatchException("FAILURE: Account Name/Number mismatch");
			}
			contracts = this.getContractByPartyAndContractLines(contractData.get(ImportConstants.SECOND_PARTY_ID), this.getServices(contractLines));// order by effectivedate
			if (contracts.size() == 1) {
				contractId = contracts.iterator().next();
			} else if (contracts.size() > 1) {
				if (!ImportUtils.isEmpty(billerPeriods) && this.getFirstLevelProcessingFee(billerPeriods) > 0) {
					contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, true);//First level
					if (null == contractId) {
						// If you r here means no matching contract found with 1st level process fee Match with any level processing fee
						contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, false);//Any level
					}
					if (null == contractId) {
						// If you r here means no matching contract found with 1st level process fee and any level processing fee Iterate over contracts found by collection account #, match currency, return
						// first matched contract
						Iterator<String> contractIterator = contracts.iterator();
						while (contractIterator.hasNext()) {
							String tempId = contractIterator.next();
							String currency = getContractCurrency(tempId);
							if (contractData.get(ImportConstants.CURRENCY_ID).equals(currency)) {
								contractId = tempId;
							}
						}
					}
					if (null == contractId) {
						throw new ContractMatchException("FAILURE: Acct Nm/Svc/Eff Date failed due to currency");
					}
				} else {// Level 1 process fee is null Match with any level processing fee
					contractId = getContractsWithMachingPeriodsByLevel(contracts, contractData, billerPeriods, false);
					if (null == contractId) {
						// If you r here means no matching contract found any level processing fee
						// Iterate over contracts found by collection account #, match currency, return
						// first matched contract
						Iterator<String> contractIterator = contracts.iterator();
						while (contractIterator.hasNext()) {
							String tempId = contractIterator.next();
							String currency = getContractCurrency(tempId);
							if (contractData.get(ImportConstants.CURRENCY_ID).equals(currency)) {
								contractId = tempId;
							}
						}
					}
					if (null == contractId) {
						throw new ContractMatchException("FAILURE: Acct#/Svc/Eff Date (Any) failed due to currency");
					}
				}
			}
		}
		
		
		if (null != contractId ) {
			contractId = readContract(contractId);//Always return ItemId
		}
		return contractId;
	}

	private double getFirstLevelProcessingFee(List<Map<String, String>> billerPeriods) {
		double processFee = 0;
		for (Map<String, String> billerPeriod : billerPeriods) {
			if(Integer.parseInt(billerPeriod.get(com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.PERIOD_LEVEL)) == 1) {
				processFee = Double.parseDouble(billerPeriod.get(com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.FEES));
				break;
			}
		}
		return processFee;
	}
	
	private String getContractsWithMachingPeriodsByLevel(Set<String> contracts, Map<String, String> contractData, List<Map<String, String>> billerPeriods, boolean firstLevel) {
		String contractItemId = null;
		Set<String> periodscontracts = getContractsWithMatchingPeriods(contracts, contractData, billerPeriods, firstLevel);
		if (periodscontracts.size() == 1) {
			if (contractData.get(ImportConstants.CURRENCY_ID).equals(getContractCurrency(periodscontracts.iterator().next()))) {
				contractItemId = periodscontracts.iterator().next();
			}
		} else if (periodscontracts.size() > 1) {
			// Iterate over contracts, match currency, return first matched contract
			Iterator<String> contractIterator = contracts.iterator();
			while (contractIterator.hasNext()) {
				String tempId = contractIterator.next();
				String currency = getContractCurrency(tempId);
				if (contractData.get(ImportConstants.CURRENCY_ID).equals(currency)) {
					contractItemId = tempId;
					break;
				}
			}
		}
		return contractItemId;
	}

	private Set<String> getContractsWithMatchingPeriods(Set<String> contracts, Map<String, String> contractDate, List<Map<String, String>> billerperiods, boolean firstLevel) {// order by effectivedate
		Set<String> contractsList = new HashSet<String>();
		if(billerperiods != null) {
			int response = 0, periodNode = 0, periodsNode = 0, contractsNode = 0, contractNode = 0;
			String contractIds = null;
			try {
				SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE, "getContractsWithMatchingRS", null, null);
				contractsNode = NomUtil.parseXML("<contracts></contracts>");
				periodsNode = NomUtil.parseXML("<periods></periods>");
				if(firstLevel) {
					periodNode = NomUtil.parseXML("<period><processFee>"+ billerperiods.iterator().next().get(com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.FEES) + "</processFee></period>");
					Node.appendToChildren(periodNode, periodsNode);
				} else {
					for (Map<String, String> period : billerperiods) {
						contractNode = NomUtil.parseXML("<period><processFee>"+ period.get(com.opentext.apps.cc.importhandler.contractbillerperiod.ImportConstants.FEES) + "</processFee></period>");
						Node.appendToChildren(periodNode, periodsNode);
					}
				}
				for (String contract : contracts) {
					contractNode = NomUtil.parseXML("<contract><id>" + contract + "</id></contract>");
					Node.appendToChildren(contractNode, contractsNode);
				}
	
				request.addParameterAsXml(contractsNode);
				request.addParameterAsXml(periodsNode);
				response = request.sendAndWait();
				contractIds = Node.getDataWithDefault(NomUtil.getNode(".//contracts", response), null);
				if(!ImportUtils.isEmpty(contractIds)) {
					String ids[] = contractIds.split(",");
					if (!ImportUtils.isEmpty(ids)) {
						for (String id : ids) {
							contractsList.add(id);
						}
					}
				}
			} finally {
				Utilities.cleanAll(contractsNode, periodsNode, response);
			}
		}
		return contractsList;
	}

	private String getContractCurrencyItemId(String contractId) {
		int response = 0;
		String currencyItemId = null;
		String contractItemId = null;
		try {
			SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_CENTER_NAMESPACE,"ReadContract", null, null);
			int accountNameNode = NomUtil.parseXML("<Contract-id><Id>" + contractId + "</Id></Contract-id>");
			request.addParameterAsXml(accountNameNode);
			response = request.sendAndWait();
			currencyItemId = Node.getDataWithDefault(NomUtil.getNode(".//Currency/Currency-id/ItemId", response), null);
			contractItemId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/ItemId", response), null);
			if(!ImportUtils.isEmpty(contractItemId)) {
				contratMap.put(contractId, contractItemId);
			}
		} finally {
			Utilities.cleanAll(response);
		}
		return currencyItemId;
	}

	private String readContract(String contractId) {
		int response = 0;
		String contractItemId = null;
		contractItemId = contratMap.get(contractId);
		if(ImportUtils.isEmpty(contractItemId)) {
			try {
				SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_CENTER_NAMESPACE,"ReadContract", null, null);
				int accountNameNode = NomUtil.parseXML("<Contract-id><Id>" + contractId + "</Id></Contract-id>");
				request.addParameterAsXml(accountNameNode);
				response = request.sendAndWait();
				contractItemId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/ItemId", response), null);
				if(!ImportUtils.isEmpty(contractItemId)) {
					contratMap.put(contractId, contractItemId);
				}
			} finally {
				Utilities.cleanAll(response);
			}
		}
		return contractItemId;
	}
	
	private String getContractCurrency(String contractId) {
		String currencyName = "", currencyItemId = getContractCurrencyItemId(contractId);
		if (currencyItemId != null) {
			int response = 0;
			try {
				SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CURRENCY_NAMESPACE, "ReadCurrency", null, null);
				int currencyNode = NomUtil.parseXML("<Currency-id><ItemId>" + currencyItemId + "</ItemId></Currency-id>");
				request.addParameterAsXml(currencyNode);
				response = request.sendAndWait();
				currencyName = Node.getDataWithDefault(NomUtil.getNode(".//Name", response), null);
			} finally {
				Utilities.cleanAll(response);
			}
		}
		return currencyName;
	}

	private Set<String> getContractByCollectionAccountAndContractLines(String collectionAccount, String services) {
		int response = 0;
		Set<String> contractsList = new HashSet<String>();
		try {
			SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE,
					"getContractsByCollectionAccount", null, null);
			int accountNameNode = NomUtil.parseXML("<accountnumber>" + collectionAccount + "</accountnumber>");
			int serviceList = NomUtil.parseXML("<serviceList>" + services + "</serviceList>");
			request.addParameterAsXml(accountNameNode);
			request.addParameterAsXml(serviceList);
			response = request.sendAndWait();
			String contractList = Node.getDataWithDefault(NomUtil.getNode(".//contrctsList", response), null);//We are getting duplicate contract ids here
			if (!ImportUtils.isEmpty(contractList)) {
				contractsList.addAll(Arrays.asList(contractList.split(",")));
			}
		} finally {
			Utilities.cleanAll(response);
		}
		return contractsList;
	}

	private Set<String> getContractByPartyAndContractLines(String secondParty, String services) {
		int response = 0;
		Set<String> contractsList = new HashSet<String>();
		try {
			SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE, "getContractsByParty", null, null);
			int accountNameNode = NomUtil.parseXML("<party>" + secondParty + "</party>");
			int serviceList = NomUtil.parseXML("<serviceList>" + services + "</serviceList>");
			request.addParameterAsXml(accountNameNode);
			request.addParameterAsXml(serviceList);
			response = request.sendAndWait();
			String contractList = Node.getDataWithDefault(NomUtil.getNode(".//contrctsList", response), null);//We are getting duplicate contract ids here
			if (!ImportUtils.isEmpty(contractList)) {
				contractsList.addAll(Arrays.asList(contractList.split(",")));
			}
		} finally {
			Utilities.cleanAll(response);
		}
		return contractsList;
	}
	
	private Set<String> contractsByClientAndDiffAccount(String secondParty, String collectionAccount) {
		Set<String> contractsList = new HashSet<String>();
		if (!ImportUtils.isEmpty(collectionAccount)) {
			Set<String> partyContracts = this.getContractsByParty(secondParty);
			Iterator<String> contractIterator = partyContracts.iterator();
			while (contractIterator.hasNext()) {
				String contractId = contractIterator.next();
				if (!isCollectionAccountMatched(contractId, collectionAccount)) {
					contractsList.add(contractId);
				}
			}
		}
		return contractsList;
	}
	
	private Set<String> getContractsByParty(String secondParty) {
		int response = 0;
		Set<String> contractsList = new HashSet<String>();
		try {
			SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE, "getContractsByClient", null, null);
			int accountNameNode = NomUtil.parseXML("<party>" + secondParty + "</party>");
			request.addParameterAsXml(accountNameNode);
			response = request.sendAndWait();
			String contractList = Node.getDataWithDefault(NomUtil.getNode(".//contrctsList", response), null);//We are getting duplicate contract ids here
			if (!ImportUtils.isEmpty(contractList)) {
				contractsList.addAll(Arrays.asList(contractList.split(",")));
			}
		} finally {
			Utilities.cleanAll(response);
		}
		return contractsList;
	}
	
	private boolean isCollectionAccountMatched(String contractId, String account) {
		int response = 0, accountNode = 0, contractsNode = 0, contractNode = 0;
		boolean result = false;
		try {
			SOAPRequestObject request = new SOAPRequestObject(ImportConstants.CONTRACTS_IMPORT_NAMESPACE, "isCollectionAccountMatched", null, null);
			contractsNode = NomUtil.parseXML("<contractId>" + contractId + "</contractId>");
			accountNode = NomUtil.parseXML("<collectionAccountNumber>" + account + "</collectionAccountNumber>");
			request.addParameterAsXml(contractsNode);
			request.addParameterAsXml(accountNode);
			response = request.sendAndWait();
			String temp = Node.getDataWithDefault(NomUtil.getNode(".//result", response), null);
			if (temp != null && "true".equals(temp)) {
				result = true;
			}
		} finally {
			Utilities.cleanAll(contractsNode, accountNode, response);
		}

		return result;
	}

	
	private String getContractDetailsByCNumber(String contractNumber) throws ContractMatchException {
		int response = 0;
		int[] contracts = null;
		String contractId = null;
		try {
			SOAPRequestObject request = new SOAPRequestObject("", ImportConstants.CONTRACTS_IMPORT_NAMESPACE,
					"GetContractDetailsByCNumber", null, null);
			int cNumberNode = NomUtil.parseXML("<contractNumber>" + contractNumber + "</contractNumber>");
			request.addParameterAsXml(cNumberNode);
			response = request.sendAndWait();
			contracts = NomUtil.getNodeList(".//Contract", response);
			for (int contract : contracts) {
				String tempId = Node.getDataWithDefault(NomUtil.getNode(".//Contract-id/Id", contract), null);
				String billingStatus = Node.getDataWithDefault(NomUtil.getNode(".//BillingStatus", contract), null);
				if (ImportConstants.CONTRACT_BILLING_STATUS_ACTIVE.equalsIgnoreCase(billingStatus)) {
					if (contractId != null) {
						contractId = tempId;
					} else {
						throw new ContractMatchException(
								"Multiple active contracts found with the given override match document ID: "
										+ contractNumber);
					}

				}
			}
		} finally {
			Utilities.cleanAll(response);
		}
		return contractId;
	}

	private String getServices(List<Map<String, String>> contractLines) {
		final StringBuilder services = new StringBuilder();
		if(contractLines != null) { 
			for (Map<String, String> contractLine : contractLines) {
				if (contractLine != null && !ImportUtils.isEmpty(
						contractLine.get(com.opentext.apps.cc.importhandler.contractlines.ImportConstants.SERVICE))) {
					services.append(services.length() > 0
							? "," + contractLine
									.get(com.opentext.apps.cc.importhandler.contractlines.ImportConstants.SERVICE)
							: contractLine.get(com.opentext.apps.cc.importhandler.contractlines.ImportConstants.SERVICE));
				}
			}
		}
		return services.toString();
	}
}