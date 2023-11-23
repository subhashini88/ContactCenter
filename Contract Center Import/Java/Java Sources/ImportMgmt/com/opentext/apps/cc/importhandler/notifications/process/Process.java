package com.opentext.apps.cc.importhandler.notifications.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Process implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, String> actions;
	private Map<String, String> states;
	private Map<String, String> emailTemplates;
	private Map<String, String> roles=new HashMap<>();
	private String name;
	

	private String itemId;

	public Process(String itemId, String name) {
		super();
		this.name = name;
		this.itemId = itemId;
	}

	public Map<String, String> getStates() {
		if (Objects.isNull(states)) {
			states = new HashMap<>();
		}
		return states;
	}

	public Map<String, String> getActions() {
		if (Objects.isNull(actions)) {
			actions = new HashMap<>();
		}
		return actions;
	}

	public Map<String, String> getEmailTemplates() {
		if (Objects.isNull(emailTemplates)) {
			emailTemplates = new HashMap<>();
		}
		return emailTemplates;
	}

	public Map<String, String> getRoles() {
		return roles;
	}

	
	public String getName() {
		return name;
	}

	public String getItemId() {
		return itemId;
	}

}
