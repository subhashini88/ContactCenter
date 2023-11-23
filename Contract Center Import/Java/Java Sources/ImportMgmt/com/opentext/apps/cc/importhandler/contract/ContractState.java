package com.opentext.apps.cc.importhandler.contract;

import java.util.Map;
import java.util.TreeMap;

public enum ContractState {
	DRAFT("Draft"),
	NEGOTIATION("Negotiation"),
	PREEXECUTION("Pre-Execution"),
	EXECUTION("Execution"),
	PENDINGACTIVATION("Pending Activation"),
	ACTIVE("Active"),
	TERMINATED("Terminated"),
	ARCHIVE("Archive"),
	WITHDRAWN("Withdrawn"),
	EXPIRED("Expired");

	private final String stateName;

	private ContractState(final  String state) {
		this.stateName = state;
	}

	@Override
	public String toString() {
		return this.stateName;
	}
	
	private static Map<String, ContractState> contractStates = initializeStates();
	private static Map<String, ContractState> initializeStates()
	{
		Map<String, ContractState> contractStates = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		for (ContractState state : values()) {
				contractStates.put(state.toString(), state);
		}
		return contractStates;
	}
	
	public static ContractState searchValidState(String value) {
		if (value == null || value == "") return null;
		return contractStates.get(value);
	}

}